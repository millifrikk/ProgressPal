# ProgressPal Enhanced Features & AI Implementation Plan

**Version**: 1.0  
**Date**: January 21, 2025  
**Status**: Implementation Ready  
**Estimated Timeline**: 11-13 days

---

## 📋 Executive Summary

This document outlines the implementation plan for transforming ProgressPal from a weight tracking app into a comprehensive health monitoring platform with AI-powered personal health advisor capabilities. The enhancements include blood pressure monitoring, advanced health scoring, and intelligent AI coaching integrated with a premium subscription model.

**Key Enhancements**:
1. **Blood Pressure & Pulse Tracking** - Vital signs monitoring with color-coded health zones
2. **Health Score System** - Replace misleading BMI with comprehensive body composition scoring
3. **AI Personal Health Advisor** - Claude/OpenAI powered health insights and recommendations
4. **Premium Tier Restructure** - Freemium model with AI features driving premium conversions

---

## 🩺 1. Blood Pressure & Pulse Tracking Implementation

### 1.1 Database Schema

Following Android Room best practices with proper annotations:

```kotlin
// New Entity for Blood Pressure
@Entity(tableName = "blood_pressure",
    indices = [Index(value = ["userId", "timestamp"])],
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class BloodPressureEntity(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "userId")
    val userId: String,
    
    @ColumnInfo(name = "systolic")
    val systolic: Int,  // Top number (90-200 mmHg)
    
    @ColumnInfo(name = "diastolic")
    val diastolic: Int, // Bottom number (60-130 mmHg)
    
    @ColumnInfo(name = "pulse")
    val pulse: Int,     // Heart rate (40-200 bpm)
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "timeOfDay")
    val timeOfDay: String, // "morning", "afternoon", "evening"
    
    @ColumnInfo(name = "tags")
    val tags: String? = null, // JSON array: ["before_meal", "after_exercise"]
    
    @ColumnInfo(name = "notes")
    val notes: String? = null
)
```

### 1.2 DAO Implementation

```kotlin
@Dao
interface BloodPressureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bloodPressure: BloodPressureEntity): Long
    
    @Update
    suspend fun update(bloodPressure: BloodPressureEntity)
    
    @Delete
    suspend fun delete(bloodPressure: BloodPressureEntity)
    
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllForUser(userId: String): LiveData<List<BloodPressureEntity>>
    
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getRecentReadings(userId: String, startTime: Long): List<BloodPressureEntity>
    
    @Query("SELECT AVG(systolic) as avgSystolic, AVG(diastolic) as avgDiastolic, AVG(pulse) as avgPulse FROM blood_pressure WHERE userId = :userId AND timestamp >= :startTime")
    suspend fun getAverages(userId: String, startTime: Long): BloodPressureAverages
}
```

### 1.3 Database Migration

```kotlin
// In ProgressPalDatabase.kt
@Database(
    entities = [
        UserEntity::class,
        WeightEntity::class,
        MeasurementEntity::class,
        GoalEntity::class,
        PhotoEntity::class,
        BloodPressureEntity::class  // New entity
    ],
    version = 7,  // Increment version
    autoMigrations = [
        AutoMigration(from = 6, to = 7)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ProgressPalDatabase : RoomDatabase() {
    // ... existing DAOs
    abstract fun bloodPressureDao(): BloodPressureDao
    
    companion object {
        // Migration for adding blood pressure table
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS blood_pressure (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        systolic INTEGER NOT NULL,
                        diastolic INTEGER NOT NULL,
                        pulse INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        timeOfDay TEXT NOT NULL,
                        tags TEXT,
                        notes TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL(
                    "CREATE INDEX index_blood_pressure_userId_timestamp ON blood_pressure(userId, timestamp)"
                )
            }
        }
    }
}
```

### 1.4 UI Implementation - Material Design 3

#### Dashboard Card Component

```kotlin
// BloodPressureCard.kt - Material3 Card with proper elevation
@Composable
fun BloodPressureCard(
    latestReading: BloodPressureEntity?,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blood Pressure",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (latestReading == null) {
                    TextButton(onClick = onAddClick) {
                        Text("ADD")
                    }
                }
            }
            
            if (latestReading != null) {
                Spacer(modifier = Modifier.height(8.dp))
                BloodPressureDisplay(reading = latestReading)
            } else {
                Text(
                    text = "No readings yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BloodPressureDisplay(reading: BloodPressureEntity) {
    val category = getBloodPressureCategory(reading.systolic, reading.diastolic)
    val color = getBloodPressureColor(category)
    
    Column {
        // Main reading display
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${reading.systolic}",
                style = MaterialTheme.typography.headlineLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${reading.diastolic}",
                style = MaterialTheme.typography.headlineLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "mmHg",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Pulse display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Pulse",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${reading.pulse} bpm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Category chip
        Chip(
            onClick = { },
            colors = ChipDefaults.chipColors(
                containerColor = color.copy(alpha = 0.1f),
                labelColor = color
            ),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
```

### 1.5 Blood Pressure Categories & Color Coding

```kotlin
enum class BloodPressureCategory(
    val displayName: String,
    val colorLight: Color,
    val colorDark: Color
) {
    OPTIMAL("Optimal", Color(0xFF4CAF50), Color(0xFF81C784)),
    NORMAL("Normal", Color(0xFF8BC34A), Color(0xFFA5D6A7)),
    ELEVATED("Elevated", Color(0xFFFFEB3B), Color(0xFFFFF176)),
    STAGE_1("Stage 1 HTN", Color(0xFFFF9800), Color(0xFFFFB74D)),
    STAGE_2("Stage 2 HTN", Color(0xFFFF5722), Color(0xFFFF8A65)),
    CRISIS("Hypertensive Crisis", Color(0xFFF44336), Color(0xFFE57373))
}

fun getBloodPressureCategory(systolic: Int, diastolic: Int): BloodPressureCategory {
    return when {
        systolic >= 180 || diastolic >= 120 -> BloodPressureCategory.CRISIS
        systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.STAGE_2
        systolic >= 130 || diastolic >= 80 -> BloodPressureCategory.STAGE_1
        systolic >= 120 && diastolic < 80 -> BloodPressureCategory.ELEVATED
        systolic < 120 && diastolic < 80 -> BloodPressureCategory.OPTIMAL
        else -> BloodPressureCategory.NORMAL
    }
}
```

---

## 🎯 2. Health Score System (BMI Replacement)

### 2.1 Health Score Calculator

```kotlin
// HealthScoreCalculator.kt
object HealthScoreCalculator {
    
    data class HealthScore(
        val totalScore: Int,        // 0-100
        val bodyFatScore: Int,       // 0-40 points
        val waistHipScore: Int,      // 0-30 points
        val trendScore: Int,         // 0-20 points
        val activityScore: Int,      // 0-10 points
        val category: HealthCategory,
        val improvements: List<String>
    )
    
    enum class HealthCategory {
        EXCELLENT,    // 85-100
        GOOD,        // 70-84
        FAIR,        // 50-69
        NEEDS_WORK   // 0-49
    }
    
    fun calculateHealthScore(
        user: User,
        recentWeights: List<Weight>,
        measurements: Map<String, Float>,
        activityLevel: Int = 3  // 1-5 scale
    ): HealthScore {
        // 1. Body Fat Score (40% weight)
        val bodyFatPercentage = if (measurements.containsKey("neck") && measurements.containsKey("waist")) {
            BodyFatCalculator.calculateNavyMethod(
                gender = user.gender,
                waistCm = measurements["waist"] ?: 0f,
                neckCm = measurements["neck"] ?: 0f,
                heightCm = user.height,
                hipsCm = measurements["hips"]
            )
        } else null
        
        val bodyFatScore = calculateBodyFatScore(bodyFatPercentage, user.gender)
        
        // 2. Waist-to-Hip Ratio Score (30% weight)
        val waistHipRatio = if (measurements.containsKey("waist") && measurements.containsKey("hips")) {
            measurements["waist"]!! / measurements["hips"]!!
        } else null
        
        val waistHipScore = calculateWaistHipScore(waistHipRatio, user.gender)
        
        // 3. Weight Trend Score (20% weight)
        val trendScore = calculateTrendScore(recentWeights, user.goalWeight)
        
        // 4. Activity Score (10% weight)
        val activityScore = (activityLevel * 2).coerceIn(0, 10)
        
        // Calculate total
        val totalScore = bodyFatScore + waistHipScore + trendScore + activityScore
        
        // Determine category
        val category = when (totalScore) {
            in 85..100 -> HealthCategory.EXCELLENT
            in 70..84 -> HealthCategory.GOOD
            in 50..69 -> HealthCategory.FAIR
            else -> HealthCategory.NEEDS_WORK
        }
        
        // Generate improvement suggestions
        val improvements = generateImprovements(
            bodyFatScore, waistHipScore, trendScore, activityScore
        )
        
        return HealthScore(
            totalScore = totalScore,
            bodyFatScore = bodyFatScore,
            waistHipScore = waistHipScore,
            trendScore = trendScore,
            activityScore = activityScore,
            category = category,
            improvements = improvements
        )
    }
    
    private fun calculateBodyFatScore(bodyFat: Float?, gender: User.Gender): Int {
        if (bodyFat == null) return 20 // Default middle score
        
        val optimalRange = when (gender) {
            User.Gender.MALE -> 10f..18f
            User.Gender.FEMALE -> 18f..25f
            else -> 14f..22f
        }
        
        return when {
            bodyFat in optimalRange -> 40
            bodyFat < optimalRange.start -> {
                // Too low body fat
                val diff = optimalRange.start - bodyFat
                (40 - diff * 2).toInt().coerceIn(20, 40)
            }
            else -> {
                // Too high body fat
                val diff = bodyFat - optimalRange.endInclusive
                (40 - diff).toInt().coerceIn(0, 40)
            }
        }
    }
}
```

### 2.2 Body Fat Calculator (Navy Method)

```kotlin
// BodyFatCalculator.kt
object BodyFatCalculator {
    
    /**
     * Calculate body fat percentage using US Navy Method
     * More accurate than BMI as it accounts for muscle mass
     */
    fun calculateNavyMethod(
        gender: User.Gender,
        waistCm: Float,
        neckCm: Float,
        heightCm: Float,
        hipsCm: Float? = null  // Required for females
    ): Float {
        return when (gender) {
            User.Gender.MALE -> {
                // Male formula: 86.010 × log10(waist - neck) - 70.041 × log10(height) + 36.76
                val result = 86.010 * log10(waistCm - neckCm) - 
                           70.041 * log10(heightCm) + 36.76
                result.toFloat().coerceIn(2f, 50f)
            }
            User.Gender.FEMALE -> {
                // Female formula: 163.205 × log10(waist + hips - neck) - 97.684 × log10(height) - 78.387
                if (hipsCm == null) return 25f // Default if hips not provided
                val result = 163.205 * log10(waistCm + hipsCm - neckCm) - 
                           97.684 * log10(heightCm) - 78.387
                result.toFloat().coerceIn(10f, 50f)
            }
            else -> {
                // Use male formula as default
                val result = 86.010 * log10(waistCm - neckCm) - 
                           70.041 * log10(heightCm) + 36.76
                result.toFloat().coerceIn(5f, 50f)
            }
        }
    }
    
    fun getCategory(bodyFatPercentage: Float, gender: User.Gender): String {
        val ranges = when (gender) {
            User.Gender.MALE -> mapOf(
                "Essential" to 2f..5f,
                "Athletes" to 6f..13f,
                "Fitness" to 14f..17f,
                "Average" to 18f..24f,
                "Obese" to 25f..Float.MAX_VALUE
            )
            User.Gender.FEMALE -> mapOf(
                "Essential" to 10f..13f,
                "Athletes" to 14f..20f,
                "Fitness" to 21f..24f,
                "Average" to 25f..31f,
                "Obese" to 32f..Float.MAX_VALUE
            )
            else -> mapOf(
                "Lean" to 0f..15f,
                "Ideal" to 15f..20f,
                "Average" to 20f..25f,
                "Above Average" to 25f..30f,
                "High" to 30f..Float.MAX_VALUE
            )
        }
        
        return ranges.entries.find { bodyFatPercentage in it.value }?.key ?: "Unknown"
    }
}
```

---

## 🤖 3. AI Personal Health Advisor Implementation

### 3.1 AI Service Architecture

```kotlin
// AIHealthAdvisor.kt
class AIHealthAdvisor(
    private val apiKey: String,
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val measurementRepository: MeasurementRepository,
    private val insightsCalculator: InsightsCalculator
) {
    
    enum class AIProvider {
        CLAUDE_HAIKU,    // Most cost-effective: $0.80/$4 per million
        OPENAI_MINI,     // Good alternative: $0.15/$0.60 per million
        CLAUDE_SONNET    // Premium option: $3/$15 per million
    }
    
    private val selectedProvider = AIProvider.CLAUDE_HAIKU
    
    data class HealthContext(
        val user: User,
        val recentWeights: List<Weight>,
        val recentBloodPressure: List<BloodPressureEntity>,
        val currentMeasurements: Map<String, Float>,
        val healthScore: HealthScoreCalculator.HealthScore,
        val insights: List<InsightCard>,
        val goals: List<Goal>
    )
    
    data class AIResponse(
        val message: String,
        val suggestions: List<String>,
        val focusAreas: List<String>,
        val tokensUsed: Int
    )
    
    suspend fun getPersonalizedAdvice(
        userId: String,
        userQuery: String
    ): AIResponse = withContext(Dispatchers.IO) {
        // 1. Gather comprehensive health context
        val context = gatherHealthContext(userId)
        
        // 2. Build structured prompt
        val prompt = buildHealthPrompt(context, userQuery)
        
        // 3. Call AI API based on provider
        val response = when (selectedProvider) {
            AIProvider.CLAUDE_HAIKU -> callClaudeAPI(prompt)
            AIProvider.OPENAI_MINI -> callOpenAIAPI(prompt)
            AIProvider.CLAUDE_SONNET -> callClaudeAPI(prompt, model = "claude-3-5-sonnet")
        }
        
        // 4. Parse and return structured response
        parseAIResponse(response)
    }
    
    private suspend fun gatherHealthContext(userId: String): HealthContext {
        val user = userRepository.getUser(userId)
        val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        
        return HealthContext(
            user = user,
            recentWeights = weightRepository.getRecentWeights(userId, thirtyDaysAgo),
            recentBloodPressure = bloodPressureRepository.getRecentReadings(userId, thirtyDaysAgo),
            currentMeasurements = measurementRepository.getLatestMeasurements(userId),
            healthScore = HealthScoreCalculator.calculateHealthScore(
                user, recentWeights, currentMeasurements
            ),
            insights = insightsCalculator.generateInsights(recentWeights),
            goals = userRepository.getUserGoals(userId)
        )
    }
    
    private fun buildHealthPrompt(context: HealthContext, userQuery: String): String {
        return """
        You are a professional health advisor analyzing data for a user tracking their fitness journey.
        
        USER PROFILE:
        - Age: ${context.user.age}, Gender: ${context.user.gender}
        - Current Weight: ${context.recentWeights.firstOrNull()?.value ?: "Unknown"} kg
        - Goal Weight: ${context.user.goalWeight ?: "Not set"} kg
        - Health Score: ${context.healthScore.totalScore}/100 (${context.healthScore.category})
        
        RECENT TRENDS (30 days):
        - Weight change: ${calculateWeightChange(context.recentWeights)} kg
        - Average BP: ${calculateAverageBP(context.recentBloodPressure)}
        - Body Fat: ${context.currentMeasurements["body_fat"] ?: "Not measured"}%
        
        KEY INSIGHTS:
        ${context.insights.take(3).joinToString("\n") { "- ${it.title}: ${it.description}" }}
        
        USER QUESTION: $userQuery
        
        Please provide:
        1. Direct answer to the user's question
        2. 3 specific, actionable suggestions based on their data
        3. 2-3 focus areas for the next week
        
        Keep the response concise, encouraging, and evidence-based.
        """.trimIndent()
    }
    
    private suspend fun callClaudeAPI(
        prompt: String,
        model: String = "claude-3-haiku-20240307"
    ): String {
        val client = OkHttpClient()
        val requestBody = JSONObject().apply {
            put("model", model)
            put("max_tokens", 500)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }
        
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()
        
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            response.body?.string() ?: ""
        }
    }
}
```

### 3.2 AI Chat UI Implementation

```kotlin
// AIChatActivity.kt
class AIChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAiChatBinding
    private lateinit var aiAdvisor: AIHealthAdvisor
    private lateinit var chatAdapter: ChatMessageAdapter
    private val messages = mutableListOf<ChatMessage>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupInputField()
        loadConversationStarters()
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter(messages)
        binding.recyclerViewChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@AIChatActivity)
        }
    }
    
    private fun loadConversationStarters() {
        if (messages.isEmpty()) {
            // Show suggested questions as chips
            val suggestions = listOf(
                "What should I focus on this week?",
                "Analyze my blood pressure trends",
                "Why is my weight plateauing?",
                "Create a plan to reach my goal",
                "What's my body fat percentage?"
            )
            
            binding.chipGroupSuggestions.removeAllViews()
            suggestions.forEach { suggestion ->
                val chip = Chip(this).apply {
                    text = suggestion
                    isClickable = true
                    setOnClickListener {
                        sendMessage(suggestion)
                        binding.chipGroupSuggestions.visibility = View.GONE
                    }
                }
                binding.chipGroupSuggestions.addView(chip)
            }
        }
    }
    
    private fun sendMessage(text: String) {
        // Add user message to chat
        messages.add(ChatMessage(text, ChatMessage.Type.USER))
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerViewChat.scrollToPosition(messages.size - 1)
        
        // Show typing indicator
        messages.add(ChatMessage("...", ChatMessage.Type.AI_TYPING))
        chatAdapter.notifyItemInserted(messages.size - 1)
        
        // Get AI response
        lifecycleScope.launch {
            try {
                val response = aiAdvisor.getPersonalizedAdvice(
                    userId = getCurrentUserId(),
                    userQuery = text
                )
                
                // Remove typing indicator
                messages.removeAt(messages.size - 1)
                
                // Add AI response
                messages.add(ChatMessage(response.message, ChatMessage.Type.AI))
                chatAdapter.notifyDataSetChanged()
                
                // Update token usage for premium tracking
                updateTokenUsage(response.tokensUsed)
                
            } catch (e: Exception) {
                // Handle error
                messages.removeAt(messages.size - 1)
                messages.add(ChatMessage(
                    "I'm having trouble connecting right now. Please try again.",
                    ChatMessage.Type.AI
                ))
                chatAdapter.notifyDataSetChanged()
            }
        }
    }
}
```

### 3.3 AI Conversation Storage

```kotlin
@Entity(tableName = "ai_conversations")
data class AIConversationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "userId")
    val userId: String,
    
    @ColumnInfo(name = "messages")
    val messages: String,  // JSON array of messages
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "tokensUsed")
    val tokensUsed: Int,
    
    @ColumnInfo(name = "summary")
    val summary: String? = null  // AI-generated summary for quick reference
)
```

---

## 💎 4. Premium Feature Integration

### 4.1 Updated Premium Tiers

```kotlin
// PremiumManager.kt updates
enum class PremiumTier(
    val displayName: String,
    val monthlyPrice: Float,
    val features: List<PremiumFeature>
) {
    FREE(
        displayName = "Free",
        monthlyPrice = 0f,
        features = listOf(
            PremiumFeature.BASIC_TRACKING,
            PremiumFeature.BASIC_CHARTS,
            PremiumFeature.SEVEN_DAY_HISTORY
        )
    ),
    
    PREMIUM(
        displayName = "Premium",
        monthlyPrice = 4.99f,
        features = listOf(
            PremiumFeature.BASIC_TRACKING,
            PremiumFeature.BASIC_CHARTS,
            PremiumFeature.UNLIMITED_HISTORY,
            PremiumFeature.BLOOD_PRESSURE_TRACKING,
            PremiumFeature.ALL_MEASUREMENTS,
            PremiumFeature.AI_ADVISOR_LIMITED,  // 5 chats/month
            PremiumFeature.ADVANCED_INSIGHTS,
            PremiumFeature.PHOTO_COMPARISONS,
            PremiumFeature.CSV_EXPORT,
            PremiumFeature.HEALTH_SCORE
        )
    ),
    
    PREMIUM_PLUS(
        displayName = "Premium+",
        monthlyPrice = 9.99f,
        features = listOf(
            // Everything in Premium
            *PREMIUM.features.toTypedArray(),
            PremiumFeature.AI_ADVISOR_UNLIMITED,
            PremiumFeature.PERSONALIZED_MEAL_PLANS,
            PremiumFeature.WEEKLY_AI_REPORTS,
            PremiumFeature.PRIORITY_SUPPORT,
            PremiumFeature.FAMILY_SHARING,  // 3 profiles
            PremiumFeature.API_ACCESS,
            PremiumFeature.CUSTOM_REMINDERS
        )
    )
}

enum class PremiumFeature {
    // Free features
    BASIC_TRACKING,
    BASIC_CHARTS,
    SEVEN_DAY_HISTORY,
    
    // Premium features
    BLOOD_PRESSURE_TRACKING,
    ALL_MEASUREMENTS,
    AI_ADVISOR_LIMITED,
    ADVANCED_INSIGHTS,
    PHOTO_COMPARISONS,
    CSV_EXPORT,
    UNLIMITED_HISTORY,
    HEALTH_SCORE,
    
    // Premium+ features
    AI_ADVISOR_UNLIMITED,
    PERSONALIZED_MEAL_PLANS,
    WEEKLY_AI_REPORTS,
    PRIORITY_SUPPORT,
    FAMILY_SHARING,
    API_ACCESS,
    CUSTOM_REMINDERS
}
```

### 4.2 AI Usage Tracking

```kotlin
// AIUsageTracker.kt
class AIUsageTracker(
    private val sharedPreferences: SharedPreferences,
    private val premiumManager: PremiumManager
) {
    
    companion object {
        const val KEY_AI_CHATS_THIS_MONTH = "ai_chats_this_month"
        const val KEY_AI_TOKENS_USED = "ai_tokens_used"
        const val KEY_LAST_RESET_DATE = "ai_last_reset_date"
        const val FREE_TIER_LIMIT = 0
        const val PREMIUM_TIER_LIMIT = 5
        const val PREMIUM_PLUS_LIMIT = Int.MAX_VALUE
    }
    
    fun canUseAIAdvisor(): Boolean {
        checkMonthlyReset()
        
        val currentUsage = getCurrentMonthlyUsage()
        val limit = when (premiumManager.getCurrentTier()) {
            PremiumTier.FREE -> FREE_TIER_LIMIT
            PremiumTier.PREMIUM -> PREMIUM_TIER_LIMIT
            PremiumTier.PREMIUM_PLUS -> PREMIUM_PLUS_LIMIT
        }
        
        return currentUsage < limit
    }
    
    fun recordAIUsage(tokensUsed: Int) {
        val currentChats = sharedPreferences.getInt(KEY_AI_CHATS_THIS_MONTH, 0)
        val currentTokens = sharedPreferences.getInt(KEY_AI_TOKENS_USED, 0)
        
        sharedPreferences.edit().apply {
            putInt(KEY_AI_CHATS_THIS_MONTH, currentChats + 1)
            putInt(KEY_AI_TOKENS_USED, currentTokens + tokensUsed)
            apply()
        }
    }
    
    fun getRemainingChats(): Int {
        checkMonthlyReset()
        
        val currentUsage = getCurrentMonthlyUsage()
        val limit = when (premiumManager.getCurrentTier()) {
            PremiumTier.FREE -> FREE_TIER_LIMIT
            PremiumTier.PREMIUM -> PREMIUM_TIER_LIMIT
            PremiumTier.PREMIUM_PLUS -> PREMIUM_PLUS_LIMIT
        }
        
        return (limit - currentUsage).coerceAtLeast(0)
    }
    
    private fun checkMonthlyReset() {
        val lastReset = sharedPreferences.getLong(KEY_LAST_RESET_DATE, 0)
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        
        calendar.timeInMillis = lastReset
        val lastResetMonth = calendar.get(Calendar.MONTH)
        
        if (currentMonth != lastResetMonth) {
            // Reset monthly counters
            sharedPreferences.edit().apply {
                putInt(KEY_AI_CHATS_THIS_MONTH, 0)
                putLong(KEY_LAST_RESET_DATE, System.currentTimeMillis())
                apply()
            }
        }
    }
}
```

---

## 📱 5. UI/UX Enhancements

### 5.1 Updated Dashboard Layout

```xml
<!-- fragment_dashboard.xml updates -->
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- Health Score Card (Replaces BMI) -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/cardHealthScore"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardElevation="4dp"
            app:cardCornerRadius="12dp">
            
            <include layout="@layout/view_health_score_card" />
            
        </com.google.android.material.card.MaterialCardView>

        <!-- Blood Pressure Card -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/cardBloodPressure"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardElevation="2dp"
            app:cardCornerRadius="8dp">
            
            <include layout="@layout/view_blood_pressure_card" />
            
        </com.google.android.material.card.MaterialCardView>

        <!-- Weight Progress Card -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/cardWeightProgress"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardElevation="2dp"
            app:cardCornerRadius="8dp">
            
            <include layout="@layout/view_weight_progress_card" />
            
        </com.google.android.material.card.MaterialCardView>

        <!-- AI Advisor Quick Access -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/cardAIAdvisor"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardElevation="2dp"
            app:cardCornerRadius="8dp"
            app:strokeColor="?attr/colorPrimary"
            app:strokeWidth="1dp">
            
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="16dp"
                android:gravity="center_vertical">
                
                <ImageView
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:src="@drawable/ic_ai_advisor"
                    app:tint="?attr/colorPrimary" />
                
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginStart="16dp"
                    android:orientation="vertical">
                    
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="AI Health Advisor"
                        android:textAppearance="?attr/textAppearanceTitleMedium" />
                    
                    <TextView
                        android:id="@+id/tvAIAdvisorStatus"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="5 chats remaining this month"
                        android:textAppearance="?attr/textAppearanceBodySmall"
                        android:textColor="?attr/colorOnSurfaceVariant" />
                    
                </LinearLayout>
                
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnChatWithAI"
                    style="?attr/materialButtonOutlinedStyle"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Chat" />
                
            </LinearLayout>
            
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>

</ScrollView>
```

---

## 🚀 6. Implementation Timeline

### Phase 1: Blood Pressure Tracking (Days 1-4)
**Day 1: Database Setup**
- [ ] Create BloodPressureEntity
- [ ] Implement BloodPressureDao
- [ ] Add database migration
- [ ] Create Repository layer

**Day 2: UI Components**
- [ ] Design input screen layout
- [ ] Create BloodPressureCard component
- [ ] Implement color-coded categories
- [ ] Add Material3 styling

**Day 3: Business Logic**
- [ ] Implement AddBloodPressureActivity
- [ ] Create BloodPressurePresenter
- [ ] Add validation logic
- [ ] Integrate with dashboard

**Day 4: Charts & Statistics**
- [ ] Add BP trends chart
- [ ] Create statistics calculations
- [ ] Implement time-based filtering
- [ ] Add to Statistics tab

### Phase 2: Health Score System (Days 5-6)
**Day 5: Calculator Implementation**
- [ ] Create HealthScoreCalculator
- [ ] Implement BodyFatCalculator
- [ ] Add waist-to-hip ratio logic
- [ ] Create scoring algorithms

**Day 6: UI Integration**
- [ ] Replace BMI displays
- [ ] Create HealthScoreCard
- [ ] Update dashboard layout
- [ ] Add detailed breakdown view

### Phase 3: AI Health Advisor (Days 7-11)
**Day 7: API Integration**
- [ ] Set up Claude/OpenAI SDK
- [ ] Create API service class
- [ ] Implement token management
- [ ] Add error handling

**Day 8: Context Gathering**
- [ ] Create HealthContext builder
- [ ] Implement data aggregation
- [ ] Build structured prompts
- [ ] Add response parsing

**Day 9: Chat UI**
- [ ] Design chat interface
- [ ] Create message adapter
- [ ] Implement typing indicators
- [ ] Add conversation starters

**Day 10: Conversation Management**
- [ ] Create conversation storage
- [ ] Implement history loading
- [ ] Add summary generation
- [ ] Create search functionality

**Day 11: Premium Integration**
- [ ] Update PremiumManager
- [ ] Implement usage tracking
- [ ] Add upgrade prompts
- [ ] Create limitation UI

### Phase 4: Premium & Polish (Days 12-13)
**Day 12: Premium Features**
- [ ] Update subscription tiers
- [ ] Modify paywall screens
- [ ] Add feature flags
- [ ] Test premium flows

**Day 13: Testing & Polish**
- [ ] End-to-end testing
- [ ] Performance optimization
- [ ] UI polish
- [ ] Documentation update

---

## 💰 7. Cost Analysis & Revenue Projections

### AI API Costs (Per 1000 Users/Month)
```
Assumptions:
- Average 10 conversations per user per month
- Average 500 tokens per conversation
- Total: 5,000 tokens per user per month

Claude Haiku ($0.80/$4 per million):
- Input: 5M tokens × $0.0008 = $4
- Output: 5M tokens × $0.004 = $20
- Total: $24/month for 1000 users

Revenue (at $4.99/month, 30% conversion):
- 300 premium users × $4.99 = $1,497
- Profit margin: 98.4%
```

### Break-Even Analysis
- **Claude Haiku**: 5 premium subscribers cover 1000 users' API costs
- **OpenAI Mini**: 20 premium subscribers cover 1000 users' API costs
- **Target**: 5% conversion rate easily profitable

---

## 🔒 8. Security & Privacy Considerations

### Health Data Protection
1. **Encryption**: All health data encrypted at rest using SQLCipher
2. **API Keys**: Store in Android Keystore, never in code
3. **PII Handling**: Never send identifiable information to AI APIs
4. **Data Retention**: Implement automatic conversation cleanup after 90 days
5. **Consent**: Explicit user consent for AI analysis of health data

### HIPAA Considerations
While ProgressPal is not a medical device:
- Implement audit logging for health data access
- Provide data export capabilities
- Allow users to delete all data
- Clear privacy policy about AI usage

---

## 📊 9. Success Metrics

### Technical Metrics
- **API Response Time**: < 2 seconds for AI responses
- **Token Efficiency**: < 1000 tokens per conversation
- **Crash Rate**: < 0.1% for new features
- **Database Migration**: Zero data loss

### Business Metrics
- **Premium Conversion**: Target 10% (up from 5%)
- **User Engagement**: 3x daily active usage with AI
- **Retention**: 60% 30-day retention (up from 40%)
- **Revenue per User**: $0.50/month average

### User Experience Metrics
- **AI Satisfaction**: > 4.5/5 rating for AI advice
- **Feature Adoption**: 50% use blood pressure tracking
- **Health Score Understanding**: 80% prefer over BMI

---

## 📝 10. Testing Strategy

### Unit Tests Required
```kotlin
class HealthScoreCalculatorTest {
    @Test
    fun testBodyFatScoring()
    @Test
    fun testWaistHipRatioCalculation()
    @Test
    fun testHealthScoreCategorization()
}

class BloodPressureCategoryTest {
    @Test
    fun testOptimalRange()
    @Test
    fun testHypertensionDetection()
    @Test
    fun testCrisisIdentification()
}

class AIPromptBuilderTest {
    @Test
    fun testContextGathering()
    @Test
    fun testPromptStructure()
    @Test
    fun testTokenCounting()
}
```

### Integration Tests
- Database migration from v6 to v7
- AI API error handling
- Premium feature access control
- Blood pressure data flow

### UI Tests
- Blood pressure input validation
- Health score display accuracy
- AI chat interaction flow
- Premium upgrade journey

---

## 🎯 11. Key Risk Mitigations

### Technical Risks
1. **AI API Downtime**: Implement fallback to cached responses
2. **Token Overuse**: Hard limits and warning system
3. **Database Migration Failure**: Comprehensive backup before migration
4. **Memory Issues**: Pagination for conversation history

### Business Risks
1. **Low Conversion**: A/B test pricing and feature sets
2. **High API Costs**: Monitor usage, implement circuit breakers
3. **Privacy Concerns**: Clear opt-in, transparent data usage
4. **Competition**: Rapid iteration based on user feedback

---

## 📚 12. Documentation Updates Required

### User Documentation
- Blood pressure tracking guide
- Understanding your Health Score
- AI Advisor best practices
- Premium features overview

### Developer Documentation
- Update CLAUDE.md with new entities
- API integration guide
- Database schema v7 documentation
- Testing procedures

---

## ✅ 13. Definition of Done

### Each Feature Must Have:
1. ✅ Database schema implemented and tested
2. ✅ UI components following Material Design 3
3. ✅ Business logic with proper error handling
4. ✅ Integration with existing premium system
5. ✅ Unit tests with >80% coverage
6. ✅ User documentation
7. ✅ Accessibility support (TalkBack tested)
8. ✅ Dark mode compatibility
9. ✅ Performance metrics within targets
10. ✅ Analytics events implemented

---

## 🚦 14. Go/No-Go Criteria

### Launch Requirements
- [ ] All Phase 1-3 features complete
- [ ] < 0.1% crash rate in testing
- [ ] AI response time < 3 seconds
- [ ] Premium upgrade flow tested
- [ ] Privacy policy updated
- [ ] 10 beta testers feedback incorporated

### Success Criteria (30 days post-launch)
- 10% premium conversion rate
- 4.0+ rating maintained
- < 5% uninstall rate
- Positive AI advisor feedback
- No critical bugs reported

---

*This implementation plan provides a comprehensive roadmap for transforming ProgressPal into a premium health monitoring platform with AI capabilities, positioning it for significant revenue growth while maintaining high user value.*