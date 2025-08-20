package com.progresspal.app.presentation.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.progresspal.app.data.database.entities.UserEntity
import com.progresspal.app.data.database.entities.WeightEntity
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.domain.contracts.HistoryContract
import com.progresspal.app.domain.models.Weight
import kotlinx.coroutines.*

class HistoryPresenter(
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository
) : HistoryContract.Presenter {
    
    private var view: HistoryContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    private var userObserver: Observer<UserEntity?>? = null
    private var weightObserver: Observer<List<WeightEntity>>? = null
    private var userLiveData: LiveData<UserEntity?>? = null
    private var weightLiveData: LiveData<List<WeightEntity>>? = null
    
    override fun attachView(view: HistoryContract.View) {
        this.view = view
    }
    
    override fun detachView() {
        // Clean up observers
        userLiveData?.removeObserver(userObserver ?: return)
        weightLiveData?.removeObserver(weightObserver ?: return)
        
        this.view = null
        job.cancelChildren()
    }
    
    override fun isViewAttached(): Boolean = view != null
    
    override fun loadWeightHistory() {
        if (!isViewAttached()) return
        
        view?.showLoading()
        
        scope.launch {
            try {
                // First check if user exists
                val hasUser = userRepository.hasUser()
                if (!hasUser) {
                    view?.hideLoading()
                    view?.showEmptyState()
                    return@launch
                }
                
                // Set up user data observer to get user ID
                userLiveData = userRepository.getUserLiveData()
                userObserver = Observer { userEntity ->
                    userEntity?.let { entity ->
                        setupWeightObserver(entity.id)
                    }
                }
                userLiveData?.observeForever(userObserver!!)
                
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to load weight history: ${e.message}")
            }
        }
    }
    
    private fun setupWeightObserver(userId: Long) {
        weightLiveData = weightRepository.getAllWeightsLiveData(userId)
        weightObserver = Observer { weightEntities ->
            val weights = weightEntities.map { mapWeightEntityToWeight(it) }
            view?.hideLoading()
            
            if (weights.isEmpty()) {
                view?.showEmptyState()
            } else {
                view?.showWeightHistory(weights)
            }
        }
        weightLiveData?.observeForever(weightObserver!!)
    }
    
    override fun onWeightClicked(weight: Weight) {
        view?.navigateToEditEntry(weight.id)
    }
    
    override fun onDeleteClicked(weight: Weight) {
        view?.showDeleteConfirmation(weight)
    }
    
    override fun onDeleteConfirmed(weight: Weight) {
        if (!isViewAttached()) return
        
        scope.launch {
            try {
                val weightEntity = WeightEntity(
                    id = weight.id,
                    userId = weight.userId,
                    weight = weight.weight,
                    date = weight.date,
                    time = weight.time,
                    notes = weight.notes,
                    photoUri = weight.photoUri,
                    createdAt = weight.createdAt
                )
                
                weightRepository.deleteWeight(weightEntity)
                view?.showMessage("Weight entry deleted")
                
            } catch (e: Exception) {
                view?.showError("Failed to delete entry: ${e.message}")
            }
        }
    }
    
    override fun onRefresh() {
        loadWeightHistory()
    }
    
    private fun mapWeightEntityToWeight(entity: WeightEntity): Weight {
        return Weight(
            id = entity.id,
            userId = entity.userId,
            weight = entity.weight,
            date = entity.date,
            time = entity.time,
            notes = entity.notes,
            photoUri = entity.photoUri,
            createdAt = entity.createdAt
        )
    }
}