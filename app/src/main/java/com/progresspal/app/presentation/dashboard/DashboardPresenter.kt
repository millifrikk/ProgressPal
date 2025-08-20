package com.progresspal.app.presentation.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.progresspal.app.data.database.entities.UserEntity
import com.progresspal.app.data.database.entities.WeightEntity
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.domain.contracts.DashboardContract
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.utils.BMICalculator
import kotlinx.coroutines.*

class DashboardPresenter(
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository
) : DashboardContract.Presenter {
    
    private var view: DashboardContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    private var userObserver: Observer<UserEntity?>? = null
    private var weightObserver: Observer<List<WeightEntity>>? = null
    private var userLiveData: LiveData<UserEntity?>? = null
    private var weightLiveData: LiveData<List<WeightEntity>>? = null
    
    override fun attachView(view: DashboardContract.View) {
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
    
    override fun loadDashboardData() {
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
                
                // Set up user data observer
                userLiveData = userRepository.getUserLiveData()
                userObserver = Observer { userEntity ->
                    userEntity?.let { entity ->
                        val user = mapUserEntityToUser(entity)
                        view?.showUser(user)
                        
                        // Calculate and show BMI
                        val currentWeight = entity.currentWeight ?: entity.initialWeight
                        val bmi = BMICalculator.calculate(currentWeight, entity.height)
                        val category = BMICalculator.getCategory(bmi)
                        view?.showBMI(bmi, category)
                        
                        // Show current and goal weights
                        view?.showCurrentWeight(currentWeight)
                        entity.targetWeight?.let { target ->
                            view?.showGoalWeight(target)
                            
                            // Calculate progress
                            val progress = calculateProgress(entity.initialWeight, currentWeight, target)
                            view?.showProgress(progress)
                        }
                        
                        // Set up weight history observer
                        setupWeightObserver(entity.id)
                    }
                }
                userLiveData?.observeForever(userObserver!!)
                
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to load dashboard data: ${e.message}")
            }
        }
    }
    
    private fun setupWeightObserver(userId: Long) {
        weightLiveData = weightRepository.getRecentWeights(userId, 30)
        weightObserver = Observer { weightEntities ->
            val weights = weightEntities.map { mapWeightEntityToWeight(it) }
            view?.showQuickStats(weights)
            view?.showWeightChart(weights)
            view?.hideLoading()
        }
        weightLiveData?.observeForever(weightObserver!!)
    }
    
    override fun onAddEntryClicked() {
        view?.navigateToAddEntry()
    }
    
    override fun onRefresh() {
        loadDashboardData()
    }
    
    private fun calculateProgress(initialWeight: Float, currentWeight: Float, targetWeight: Float): Float {
        val totalChange = kotlin.math.abs(initialWeight - targetWeight)
        val currentChange = kotlin.math.abs(initialWeight - currentWeight)
        
        return if (totalChange > 0) {
            (currentChange / totalChange * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }
    }
    
    private fun mapUserEntityToUser(entity: UserEntity): User {
        return User(
            id = entity.id,
            name = entity.name,
            age = entity.age,
            height = entity.height,
            gender = entity.gender,
            activityLevel = entity.activityLevel,
            initialWeight = entity.initialWeight,
            currentWeight = entity.currentWeight,
            targetWeight = entity.targetWeight,
            initialWaist = entity.initialWaist,
            initialChest = entity.initialChest,
            initialHips = entity.initialHips,
            targetWaist = entity.targetWaist,
            targetChest = entity.targetChest,
            targetHips = entity.targetHips,
            trackMeasurements = entity.trackMeasurements,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
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