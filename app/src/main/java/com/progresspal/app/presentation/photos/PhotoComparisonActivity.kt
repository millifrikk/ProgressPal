package com.progresspal.app.presentation.photos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.progresspal.app.databinding.ActivityPhotoComparisonBinding

/**
 * Activity that hosts the PhotoComparisonFragment
 * Provides a dedicated screen for before/after photo comparison
 */
class PhotoComparisonActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPhotoComparisonBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoComparisonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        
        // Add the PhotoComparisonFragment if not already added
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, PhotoComparisonFragment.newInstance())
                .commit()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Progress Comparison"
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, PhotoComparisonActivity::class.java)
        }
    }
}