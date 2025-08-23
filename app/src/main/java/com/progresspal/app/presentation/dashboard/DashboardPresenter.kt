package com.progresspal.app.presentation.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.progresspal.app.data.database.entities.BloodPressureEntity
import com.progresspal.app.data.database.entities.UserEntity
import com.progresspal.app.data.database.entities.WeightEntity
import com.progresspal.app.data.repository.BloodPressureRepository
import com.progresspal.app.data.repository.UserRepository
import com.progresspal.app.data.repository.WeightRepository
import com.progresspal.app.domain.contracts.DashboardContract
import com.progresspal.app.domain.models.User
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.presentation.dialogs.BodyMeasurementsDialog
import com.progresspal.app.utils.BMICalculator
import kotlinx.coroutines.*

class DashboardPresenter(
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository,
    private val bloodPressureRepository: BloodPressureRepository
) : DashboardContract.Presenter {
    
    private var view: DashboardContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    private var userObserver: Observer<UserEntity?>? = null
    private var weightObserver: Observer<List<WeightEntity>>? = null
    private var bloodPressureObserver: Observer<BloodPressureEntity?>? = null
    private var userLiveData: LiveData<UserEntity?>? = null
    private var weightLiveData: LiveData<List<WeightEntity>>? = null
    private var bloodPressureLiveData: LiveData<BloodPressureEntity?>? = null
    
    override fun attachView(view: DashboardContract.View) {
        this.view = view
    }
    
    override fun detachView() {
        // Clean up observers
        userLiveData?.removeObserver(userObserver ?: return)
        weightLiveData?.removeObserver(weightObserver ?: return)
        bloodPressureLiveData?.removeObserver(bloodPressureObserver ?: return)
        
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
                        
                        // Calculate and show BMI (legacy)
                        val currentWeight = entity.currentWeight ?: entity.initialWeight
                        val bmi = BMICalculator.calculate(currentWeight, entity.height)
                        val category = BMICalculator.getCategory(bmi)
                        view?.showBMI(bmi, category)
                        
                        // Show enhanced body composition assessment
                        scope.launch {
                            val latestWeight = weightRepository.getLatestWeightSync(entity.id)
                            latestWeight?.let { weightEntity ->
                                val weight = mapWeightEntityToWeight(weightEntity)
                                view?.showBodyComposition(user, weight)
                            }
                        }
                        
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
                        
                        // Set up blood pressure observer
                        setupBloodPressureObserver(entity.id)
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
    
    private fun setupBloodPressureObserver(userId: Long) {
        bloodPressureLiveData = bloodPressureRepository.getLatestForUserLive(userId)
        bloodPressureObserver = Observer { bloodPressureEntity ->
            scope.launch {
                try {
                    val trend = if (bloodPressureEntity != null) {
                        bloodPressureRepository.getBloodPressureTrend(userId)
                    } else null
                    
                    view?.showBloodPressureData(bloodPressureEntity, trend)
                } catch (e: Exception) {
                    // If trend calculation fails, just show the reading without trend
                    view?.showBloodPressureData(bloodPressureEntity, null)
                }
            }
        }
        bloodPressureLiveData?.observeForever(bloodPressureObserver!!)
    }
    
    override fun onAddEntryClicked() {
        view?.navigateToAddEntry()
    }
    
    override fun onRefresh() {
        loadDashboardData()
    }
    
    override fun onAddBloodPressureClicked() {
        view?.navigateToAddBloodPressure()
    }
    
    override fun onViewBloodPressureHistoryClicked() {
        view?.navigateToBloodPressureHistory()
    }
    
    override fun onViewBloodPressureTrendsClicked() {
        view?.navigateToBloodPressureTrends()
    }
    
    override fun onWaistMeasurementAdded(waistCm: Float) {
        scope.launch {
            try {
                // Update user's waist circumference
                val currentUser = userRepository.getUser()
                currentUser?.let { user ->
                    val updatedUser = user.copy(waistCircumference = waistCm)
                    userRepository.updateUser(updatedUser)
                    
                    // Refresh dashboard to show updated body composition
                    loadDashboardData()
                }
            } catch (e: Exception) {
                view?.showError("Failed to save waist measurement: ${e.message}")
            }
        }
    }
    
    override fun onBodyMeasurementsAdded(measurements: BodyMeasurementsDialog.BodyMeasurements) {
        scope.launch {
            try {
                // Update all provided measurements
                userRepository.updateMultipleBodyMeasurements(
                    neckCm = measurements.neckCm,
                    waistCm = measurements.waistCm,
                    hipCm = measurements.hipCm
                )
                
                // Update gender if provided and different from current
                measurements.gender?.let { selectedGender ->
                    val currentUser = userRepository.getUser()
                    currentUser?.let { user ->
                        if (user.gender != selectedGender) {
                            val updatedUser = user.copy(gender = selectedGender)
                            userRepository.updateUser(updatedUser)
                        }
                    }
                }
                
                // Refresh dashboard to show updated body composition
                loadDashboardData()
                
                view?.showMessage("Body measurements saved successfully")
            } catch (e: Exception) {
                view?.showError("Failed to save measurements: ${e.message}")
            }
        }
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
            birthDate = entity.birthDate,
            neckCircumference = entity.neckCircumference,
            waistCircumference = entity.waistCircumference,
            hipCircumference = entity.hipCircumference,
            measurementSystem = entity.measurementSystem ?: "METRIC",
            medicalGuidelines = entity.medicalGuidelines ?: "US_AHA",
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