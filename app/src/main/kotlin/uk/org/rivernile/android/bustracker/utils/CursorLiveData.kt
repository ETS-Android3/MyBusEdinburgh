/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors or contributors be held liable for
 * any damages arising from the use of this software.
 *
 * The aforementioned copyright holder(s) hereby grant you a
 * non-transferrable right to use this software for any purpose (including
 * commercial applications), and to modify it and redistribute it, subject to
 * the following conditions:
 *
 *  1. This notice may not be removed or altered from any file it appears in.
 *
 *  2. Any modifications made to this software, except those defined in
 *     clause 3 of this agreement, must be released under this license, and
 *     the source code of any modifications must be made available on a
 *     publically accessible (and locateable) website, or sent to the
 *     original author of this software.
 *
 *  3. Software modifications that do not alter the functionality of the
 *     software but are simply adaptations to a specific environment are
 *     exempt from clause 2.
 *
 */

package uk.org.rivernile.android.bustracker.utils

import android.arch.lifecycle.LiveData
import android.database.ContentObserver
import android.database.Cursor
import android.os.AsyncTask
import android.os.CancellationSignal
import android.os.Handler
import android.support.annotation.CallSuper
import android.support.annotation.WorkerThread
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This is an abstract version of [LiveData] which is used to load a [Cursor] and optionally
 * process it in to model objects.
 *
 * @param T The type of data set in the [LiveData].
 * @author Niall Scott
 */
abstract class CursorLiveData<T> : ClearableLiveData<T>() {

    /**
     * The cancellation signal is called when the loading of a [Cursor] should be cancelled. If you
     * are loading the [Cursor] from a [android.content.ContentProvider], then please pass in this
     * field.
     */
    var cancellationSignal = CancellationSignal()
        private set
    val contentObserver: ContentObserver = CursorContentObserver()
    private var data: T? = null
        set (value) {
            field = value
            setValue(value)
        }
    private var currentLoadTask: CursorAsyncTask<T>? = null
    private val invalidated = AtomicBoolean(true)

    @CallSuper
    override fun onActive() {
        if (invalidated.compareAndSet(true, false)) {
            beginLoadCursor()
        }
    }

    @CallSuper
    override fun onInactive() {
        cancelCurrentTask()
    }

    /**
     * Load a [Cursor] from whatever source you choose. This method will be called on a background
     * thread.
     *
     * @return A loaded [Cursor] object, or `null`.
     */
    @WorkerThread
    abstract fun loadCursor(): Cursor?

    /**
     * Process a given [Cursor] and optionally return a model object from this processing.
     *
     * @param cursor The [Cursor] to process.
     * @return An object resulting from the [Cursor] processing, or `null`.
     */
    @WorkerThread
    open fun processCursor(cursor: Cursor?): T? = null

    /**
     * Invalidate the current data.
     */
    fun invalidate() {
        if (hasActiveObservers()) {
            beginLoadCursor()
        } else {
            invalidated.set(true)
        }
    }

    /**
     * Begin loading the [Cursor]. This will be performed on a background thread.
     */
    private fun beginLoadCursor() {
        cancelCurrentTask()
        currentLoadTask = CursorAsyncTask(this::loadCursor, this::processCursor,
                this::replaceCurrentData)
                .also { it.execute() }
    }

    /**
     * Replace the current data with a new [ProcessedResult].
     *
     * @param newData The new data to replace the old data with.
     */
    private fun replaceCurrentData(newData: T?) {
        data = newData
    }

    /**
     * Cancel the current operation, if there is one.
     */
    private fun cancelCurrentTask() {
        cancellationSignal.cancel()
        cancellationSignal = CancellationSignal()
        currentLoadTask?.cancel(false)
        currentLoadTask = null
    }

    /**
     * This background task is used to load a [Cursor] and give the caller the opportunity to
     * process it in to a model object.
     *
     * @param T the type of data to be returned by the [Cursor] processing.
     */
    private class CursorAsyncTask<T>(
            private val onLoadCursor: () -> Cursor?,
            private val onProcessCursor: (Cursor?) -> T?,
            private val onTaskFinished: (T?) -> Unit)
        : AsyncTask<Void, Void, T?>() {

        override fun doInBackground(vararg params: Void?): T? {
            val cursor = onLoadCursor()
            val processed = if (!isCancelled) onProcessCursor(cursor) else null
            cursor?.close()

            return processed
        }

        override fun onPostExecute(result: T?) {
            onTaskFinished(result)
        }
    }

    /**
     * A [ContentObserver] which will trigger a reload of the [Cursor] when the source data has
     * changed.
     */
    private inner class CursorContentObserver : ContentObserver(Handler()) {

        override fun deliverSelfNotifications() = true

        override fun onChange(selfChange: Boolean) {
            invalidate()
        }
    }
}