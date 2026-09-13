package com.gym.model.goal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BmrCalculatorTest {

    // ── BMI ──────────────────────────────────────────────────────────

    @Test
    fun calculateBmi_knownValues_matchesStandardFormula() {
        // 69kg / (1.72m)^2 = 23.32... -> rounded to 1dp
        assertEquals(23.3, BmrCalculator.calculateBmi(172.0, 69.0))
    }

    @Test
    fun calculateBmi_zeroHeight_returnsZeroInsteadOfDividingByZero() {
        assertEquals(0.0, BmrCalculator.calculateBmi(0.0, 70.0))
    }

    @Test
    fun calculateBmi_negativeHeight_returnsZeroGuard() {
        assertEquals(0.0, BmrCalculator.calculateBmi(-172.0, 70.0))
    }

    @Test
    fun calculateBmi_zeroWeight_returnsZero() {
        assertEquals(0.0, BmrCalculator.calculateBmi(172.0, 0.0))
    }

    @Test
    fun idealWeightRange_forStandardHeight_matchesKnownRange() {
        assertEquals(55.0, BmrCalculator.calculateIdealWeightMin(172.0))
        assertEquals(74.0, BmrCalculator.calculateIdealWeightMax(172.0))
    }

    // ── BMI category boundaries (WHO bands: <18.5 / 18.5-24.9 / 25-29.9 / 30+) ──

    @Test
    fun bmiCategory_justBelowUnderweightBoundary_isUnderweight() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmrCalculator.bmiCategory(18.49))
    }

    @Test
    fun bmiCategory_atNormalLowerBoundary_isNormal() {
        assertEquals(BmiCategory.NORMAL, BmrCalculator.bmiCategory(18.5))
    }

    @Test
    fun bmiCategory_atNormalUpperEdge_isStillNormal() {
        assertEquals(BmiCategory.NORMAL, BmrCalculator.bmiCategory(24.9))
    }

    @Test
    fun bmiCategory_atOverweightLowerBoundary_isOverweight() {
        assertEquals(BmiCategory.OVERWEIGHT, BmrCalculator.bmiCategory(25.0))
    }

    @Test
    fun bmiCategory_atOverweightUpperEdge_isStillOverweight() {
        assertEquals(BmiCategory.OVERWEIGHT, BmrCalculator.bmiCategory(29.9))
    }

    @Test
    fun bmiCategory_atObeseBoundary_isObese() {
        assertEquals(BmiCategory.OBESE, BmrCalculator.bmiCategory(30.0))
    }

    @Test
    fun bmiCategory_veryHighBmi_isObese() {
        assertEquals(BmiCategory.OBESE, BmrCalculator.bmiCategory(55.0))
    }

    @Test
    fun bmiCategory_zeroOrNegativeBmi_isUnderweight() {
        // A 0.0 BMI (e.g. from the zero-height guard) must not crash the category lookup.
        assertEquals(BmiCategory.UNDERWEIGHT, BmrCalculator.bmiCategory(0.0))
        assertEquals(BmiCategory.UNDERWEIGHT, BmrCalculator.bmiCategory(-5.0))
    }

    // ── Mifflin-St Jeor BMR / TDEE ───────────────────────────────────

    @Test
    fun calculateGoalMetrics_male_matchesManualMifflinStJeorCalculation() {
        // BMR = 10*69 + 6.25*172 - 5*26 + 5 = 1640; TDEE(moderate x1.55) = 2542; GAIN = +300 = 2842
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.MALE, age = 26.0, heightCm = 172.0, weightKg = 69.0,
            goalWeightKg = 75.0, weightGoal = WeightGoal.GAIN, activityLevel = ActivityLevel.MODERATELY
        )
        assertEquals(2842.0, result.calculatedCalories, absoluteTolerance = 0.01)
    }

    @Test
    fun calculateGoalMetrics_female_usesFemaleOffsetInsteadOfMaleOffset() {
        // BMR = 10*69 + 6.25*172 - 5*26 - 161 = 1474; TDEE(moderate x1.55) = 2284.7; MAINTAIN = same
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.FEMALE, age = 26.0, heightCm = 172.0, weightKg = 69.0,
            goalWeightKg = 65.0, weightGoal = WeightGoal.MAINTAIN, activityLevel = ActivityLevel.MODERATELY
        )
        assertEquals(2284.7, result.calculatedCalories, absoluteTolerance = 0.01)
    }

    @Test
    fun calculateGoalMetrics_allActivityLevels_scaleTdeeByMultiplier() {
        val expectedMultipliers = mapOf(
            ActivityLevel.SEDENTARY to 1.2,
            ActivityLevel.LIGHTLY to 1.375,
            ActivityLevel.MODERATELY to 1.55,
            ActivityLevel.VERY to 1.725
        )
        val bmr = 10.0 * 69.0 + 6.25 * 172.0 - 5.0 * 26.0 + 5.0 // male BMR = 1640

        expectedMultipliers.forEach { (level, multiplier) ->
            val result = BmrCalculator.calculateGoalMetrics(
                gender = Gender.MALE, age = 26.0, heightCm = 172.0, weightKg = 69.0,
                goalWeightKg = 69.0, weightGoal = WeightGoal.MAINTAIN, activityLevel = level
            )
            assertEquals(bmr * multiplier, result.calculatedCalories, absoluteTolerance = 0.01)
        }
    }

    @Test
    fun calculateGoalMetrics_loseGoal_subtracts400CaloriesFromTdee() {
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.MALE, age = 26.0, heightCm = 172.0, weightKg = 90.0,
            goalWeightKg = 80.0, weightGoal = WeightGoal.LOSE, activityLevel = ActivityLevel.SEDENTARY
        )
        val bmr = 10.0 * 90.0 + 6.25 * 172.0 - 5.0 * 26.0 + 5.0
        val expectedTdee = bmr * 1.2
        assertEquals(expectedTdee - 400.0, result.calculatedCalories, absoluteTolerance = 0.01)
    }

    @Test
    fun calculateGoalMetrics_loseGoal_neverDropsBelow1200CalorieFloor() {
        // Small, older, low-activity female: TDEE is naturally low enough that tdee-400 < 1200.
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.FEMALE, age = 70.0, heightCm = 150.0, weightKg = 40.0,
            goalWeightKg = 45.0, weightGoal = WeightGoal.LOSE, activityLevel = ActivityLevel.SEDENTARY
        )
        assertEquals(1200.0, result.calculatedCalories, absoluteTolerance = 0.01)
    }

    @Test
    fun calculateGoalMetrics_loseKeto_alsoRespects1200CalorieFloor() {
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.FEMALE, age = 70.0, heightCm = 150.0, weightKg = 40.0,
            goalWeightKg = 45.0, weightGoal = WeightGoal.LOSE_KETO, activityLevel = ActivityLevel.SEDENTARY
        )
        assertEquals(1200.0, result.calculatedCalories, absoluteTolerance = 0.01)
    }

    @Test
    fun calculateGoalMetrics_loseKeto_usesHighFatLowCarbMacroSplit() {
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.MALE, age = 30.0, heightCm = 180.0, weightKg = 85.0,
            goalWeightKg = 80.0, weightGoal = WeightGoal.LOSE_KETO, activityLevel = ActivityLevel.VERY
        )
        val caloriesFromCarbs = result.calculatedCarbsGrams * 4.0
        val caloriesFromFat = result.calculatedFatGrams * 9.0
        val caloriesFromProtein = result.calculatedProteinGrams * 4.0

        // Keto split is 25% protein / 5% carbs / 70% fat of total calories.
        assertEquals(result.calculatedCalories * 0.05, caloriesFromCarbs, absoluteTolerance = 0.5)
        assertEquals(result.calculatedCalories * 0.70, caloriesFromFat, absoluteTolerance = 0.5)
        assertEquals(result.calculatedCalories * 0.25, caloriesFromProtein, absoluteTolerance = 0.5)
    }

    @Test
    fun calculateGoalMetrics_nonKetoGoals_useStandardBalancedMacroSplit() {
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.MALE, age = 30.0, heightCm = 180.0, weightKg = 85.0,
            goalWeightKg = 80.0, weightGoal = WeightGoal.MAINTAIN, activityLevel = ActivityLevel.VERY
        )
        val caloriesFromCarbs = result.calculatedCarbsGrams * 4.0
        val caloriesFromFat = result.calculatedFatGrams * 9.0
        val caloriesFromProtein = result.calculatedProteinGrams * 4.0

        assertEquals(result.calculatedCalories * 0.60, caloriesFromCarbs, absoluteTolerance = 0.5)
        assertEquals(result.calculatedCalories * 0.15, caloriesFromFat, absoluteTolerance = 0.5)
        assertEquals(result.calculatedCalories * 0.25, caloriesFromProtein, absoluteTolerance = 0.5)
    }

    // ── Extreme / invalid input edge cases ───────────────────────────
    // NOTE: these document CURRENT behavior. See QA report "GOAL-1": GAIN/MAINTAIN
    // have no lower-bound floor (unlike LOSE/LOSE_KETO's coerceAtLeast(1200.0)), so
    // pathological inputs can legitimately produce negative "calculated calories".

    @Test
    fun calculateGoalMetrics_extremeAgeWithZeroWeight_currentlyProducesNegativeCalories_documentsMissingFloor() {
        // BMR = 10*0 + 6.25*100 - 5*150 + 5 = -120; TDEE(sedentary x1.2) = -144; MAINTAIN keeps it.
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.MALE, age = 150.0, heightCm = 100.0, weightKg = 0.0,
            goalWeightKg = 70.0, weightGoal = WeightGoal.MAINTAIN, activityLevel = ActivityLevel.SEDENTARY
        )
        // Documents the gap rather than asserting it's "correct": these are pathological
        // biometric inputs, but the calculator has no input validation guarding against them,
        // and only LOSE/LOSE_KETO have a 1200-calorie floor - MAINTAIN and GAIN have none.
        assertTrue(result.calculatedCalories < 0.0, "Expected the unguarded MAINTAIN path to go negative for this pathological input; got ${result.calculatedCalories}")
    }

    @Test
    fun calculateGoalMetrics_zeroAge_doesNotThrow() {
        val result = BmrCalculator.calculateGoalMetrics(
            gender = Gender.FEMALE, age = 0.0, heightCm = 172.0, weightKg = 69.0,
            goalWeightKg = 69.0, weightGoal = WeightGoal.MAINTAIN, activityLevel = ActivityLevel.SEDENTARY
        )
        assertTrue(result.calculatedCalories > 0.0)
    }
}
