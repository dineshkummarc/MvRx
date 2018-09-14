package com.airbnb.mvrx

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface MvRxStateStore<S : Any> : Disposable {
    val state: S
<<<<<<< HEAD
        // value must be present here, since the subject is created with initialState
        get() = subject.value!!

    init {

        flushQueueSubject.observeOn(Schedulers.newThread())
            // We don't want race conditions with setting the state on multiple background threads
            // simultaneously in which two state reducers get the same initial state to reduce.
            .subscribe({ _ -> flushQueues() }, ::handleError)
            // Ensure that state updates don't get processes after dispose.
            .registerDisposable()
    }

    /**
     * Get the current state. The block of code is posted to a queue and all pending setState blocks
     * are guaranteed to run before the get block is run.
     */
    fun get(block: (S) -> Unit) {
        jobs.enqueueGetStateBlock(block)
        flushQueueSubject.onNext(Unit)
    }

    /**
     * Call this to update the state. The state reducer will get added to a queue that is processes
     * on a background thread. The state reducer's receiver type is the current state when the
     * reducer is called.
     *
     * An example of a reducer would be `{ copy(myProperty = 5) }`. The copy comes from the copy
     * function on a Kotlin data class and can be called directly because state is the receiver type
     * of the reducer. In this case, it will also implicitly return the only expression so that is
     * all of the code required.
     */
    fun set(stateReducer: S.() -> S) {
        jobs.enqueueSetStateBlock(stateReducer)
        flushQueueSubject.onNext(Unit)
    }

    private class Jobs<S> {

        private val getStateQueue = LinkedList<(state: S) -> Unit>()
        private var setStateQueue = LinkedList<S.() -> S>()

        @Synchronized
        fun enqueueGetStateBlock(block: (state: S) -> Unit) {
            getStateQueue.add(block)
        }

        @Synchronized
        fun enqueueSetStateBlock(block: S.() -> S) {
            setStateQueue.add(block)
        }

        @Synchronized
        fun dequeueGetStateBlock(): ((state: S) -> Unit)? {
            if (getStateQueue.isEmpty()) return null

            return getStateQueue.removeFirst()
        }

        @Synchronized
        fun dequeueAllSetStateBlocks(): List<(S.() -> S)>? {
            // do not allocate empty queue for no-op flushes
            if (setStateQueue.isEmpty()) return null

            val queue = setStateQueue
            setStateQueue = LinkedList()
            return queue
        }
    }

    /**
     * Flushes the setState and getState queues.
     *
     * This will flush the setState queue then call the first element on the getState queue.
     *
     * In case the setState queue calls setState, we call flushQueues recursively to flush the setState queue
     * in between every getState block gets processed.
     */
    private fun flushQueues() {
        flushSetStateQueue()
        val block = jobs.dequeueGetStateBlock() ?: return
        block(state)
        flushQueues()
    }

    /**
     * Coalesce all updates on the setState queue and clear the queue.
     */
    private fun flushSetStateQueue() {
        val blocks = jobs.dequeueAllSetStateBlocks() ?: return

        blocks
                .fold(state) { state, reducer -> state.reducer() }
                .run { subject.onNext(this) }
    }

    private fun handleError(throwable: Throwable) {
        // Throw the root cause to remove all of the rx stacks.
        // TODO: better error handling
        var e: Throwable? = throwable
        while (e?.cause != null) e = e.cause
        e?.let { throw it }
    }

    override fun isDisposed() = disposables.isDisposed

    override fun dispose() {
        disposables.dispose()
    }
=======
    fun get(block: (S) -> Unit)
    fun set(stateReducer: S.() -> S)
    val observable: Observable<S>
>>>>>>> ScriptableStore

}