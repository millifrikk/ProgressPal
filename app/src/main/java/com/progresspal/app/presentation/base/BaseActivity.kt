package com.progresspal.app.presentation.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.progresspal.app.domain.contracts.BaseContract

abstract class BaseActivity<VB : ViewBinding, P : BaseContract.Presenter<*>> : 
    AppCompatActivity(), BaseContract.View {
    
    protected lateinit var binding: VB
    protected abstract val presenter: P
    
    protected abstract fun getViewBinding(): VB
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        
        setupViews()
        attachPresenter()
    }
    
    override fun onDestroy() {
        detachPresenter()
        super.onDestroy()
    }
    
    protected abstract fun setupViews()
    protected abstract fun attachPresenter()
    protected abstract fun detachPresenter()
    
    override fun showLoading() {
        // Override in specific activities if needed
    }
    
    override fun hideLoading() {
        // Override in specific activities if needed
    }
    
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}