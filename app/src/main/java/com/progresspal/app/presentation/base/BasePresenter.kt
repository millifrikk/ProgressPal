package com.progresspal.app.presentation.base

import com.progresspal.app.domain.contracts.BaseContract
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

abstract class BasePresenter<V : BaseContract.View> : BaseContract.Presenter<V> {
    
    private var viewRef: WeakReference<V>? = null
    protected val presenterJob = SupervisorJob()
    protected val presenterScope = CoroutineScope(Dispatchers.Main + presenterJob)
    
    protected val view: V?
        get() = viewRef?.get()
    
    override fun attachView(view: V) {
        viewRef = WeakReference(view)
    }
    
    override fun detachView() {
        viewRef?.clear()
        viewRef = null
        presenterJob.cancel()
    }
    
    override fun isViewAttached(): Boolean {
        return viewRef?.get() != null
    }
    
    protected fun ifViewAttached(action: (view: V) -> Unit) {
        view?.let { action(it) }
    }
}