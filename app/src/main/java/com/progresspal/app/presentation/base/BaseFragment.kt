package com.progresspal.app.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.progresspal.app.domain.contracts.BaseContract

abstract class BaseFragment<VB : ViewBinding, P : BaseContract.Presenter<*>> : 
    Fragment(), BaseContract.View {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    protected abstract val presenter: P
    
    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        attachPresenter()
    }
    
    override fun onDestroyView() {
        detachPresenter()
        _binding = null
        super.onDestroyView()
    }
    
    protected abstract fun setupViews()
    protected abstract fun attachPresenter()
    protected abstract fun detachPresenter()
    
    override fun showLoading() {
        // Override in specific fragments if needed
    }
    
    override fun hideLoading() {
        // Override in specific fragments if needed
    }
    
    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    override fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}