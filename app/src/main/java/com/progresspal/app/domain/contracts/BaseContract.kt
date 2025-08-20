package com.progresspal.app.domain.contracts

interface BaseContract {
    
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showMessage(message: String)
    }
    
    interface Presenter<V : View> {
        fun attachView(view: V)
        fun detachView()
        fun isViewAttached(): Boolean
    }
}