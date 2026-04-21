package com.achub.hram.usecase

import com.achub.hram.models.BiologicalSex
import com.achub.hram.models.HrBucket
import com.achub.hram.models.UserSettings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.time.Clock.System.now

private const val MAX_HR_BASE = 220
private const val ACTIVE_THRESHOLD_PERCENT = 0.6
private const val BUCKET_COUNT_FOR_RATIO = 100.0
private const val SECONDS_IN_MINUTE = 60.0
private const val MINUTES_IN_DAY = 1440.0

// Mifflin-St Jeor Constants
private const val MSJ_WEIGHT_COEFF = 10.0
private const val MSJ_HEIGHT_COEFF = 6.25
private const val MSJ_AGE_COEFF = 5.0
private const val MSJ_MALE_CONST = 5.0
private const val MSJ_FEMALE_CONST = 161.0

// Keytel (2005) Constants
private const val KEYTEL_KJ_TO_KCAL = 4.184
private const val KEYTEL_MALE_HR_COEFF = 0.6309
private const val KEYTEL_MALE_WEIGHT_COEFF = 0.1988
private const val KEYTEL_MALE_AGE_COEFF = 0.2017
private const val KEYTEL_MALE_CONST = 55.0969
private const val KEYTEL_FEMALE_HR_COEFF = 0.4472
private const val KEYTEL_FEMALE_WEIGHT_COEFF = 0.1263
private const val KEYTEL_FEMALE_AGE_COEFF = 0.074
private const val KEYTEL_FEMALE_CONST = 20.4022

/**
 * Use case to calculate the total calories burned during an activity session.
 *
 * This calculation uses two primary metabolic equations:
 * 1. **Mifflin-St Jeor Equation**: Used for resting states (HR <= 60% of Max HR).
 *    It calculates Basal Metabolic Rate (BMR).
 * 2. **Keytel (2005) Equation**: Used for active states (HR > 60% of Max HR).
 *    It estimates energy expenditure based on heart rate, age, weight, and sex.
 */
class CalculateCaloriesUseCase {
    /**
     * Calculates total kilocalories burned.
     *
     * @param settings User profile information (age, weight, height, sex).
     * @param buckets List of heart rate buckets with average HR and duration.
     * @param totalDurationSeconds Total duration of the activity in seconds.
     * @return Total estimated calories burned (kcal).
     */
    operator fun invoke(
        settings: UserSettings,
        buckets: List<HrBucket>,
        totalDurationSeconds: Long
    ): Double {
        if (buckets.isEmpty() || totalDurationSeconds <= 0) return 0.0

        val currentYear = now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        val age = max(1, currentYear - settings.birthYear)
        val maxHr = MAX_HR_BASE - age
        val activeThreshold = maxHr * ACTIVE_THRESHOLD_PERCENT
        val isMale = settings.biologicalSex == BiologicalSex.Male

        val bucketDurationMinutes = (totalDurationSeconds.toDouble() / BUCKET_COUNT_FOR_RATIO) / SECONDS_IN_MINUTE

        return buckets.sumOf { bucket ->
            if (bucket.avgHr <= activeThreshold) {
                calculateRestingKcal(settings, age, isMale, bucketDurationMinutes)
            } else {
                calculateActiveKcal(bucket.avgHr, age, settings.weightKg, isMale, bucketDurationMinutes)
            }
        }.coerceAtLeast(0.0)
    }

    private fun calculateRestingKcal(
        settings: UserSettings,
        age: Int,
        isMale: Boolean,
        durationMinutes: Double
    ): Double {
        val bmr = if (isMale) {
            (MSJ_WEIGHT_COEFF * settings.weightKg) +
                (MSJ_HEIGHT_COEFF * settings.heightCm) -
                (MSJ_AGE_COEFF * age) +
                MSJ_MALE_CONST
        } else {
            (MSJ_WEIGHT_COEFF * settings.weightKg) +
                (MSJ_HEIGHT_COEFF * settings.heightCm) -
                (MSJ_AGE_COEFF * age) -
                MSJ_FEMALE_CONST
        }
        val kcalPerMinute = bmr / MINUTES_IN_DAY
        return kcalPerMinute * durationMinutes
    }

    private fun calculateActiveKcal(
        hr: Float,
        age: Int,
        weight: Float,
        isMale: Boolean,
        durationMinutes: Double
    ): Double {
        val kcal = if (isMale) {
            (
                durationMinutes *
                    (
                        KEYTEL_MALE_HR_COEFF *
                            hr +
                            KEYTEL_MALE_WEIGHT_COEFF *
                            weight +
                            KEYTEL_MALE_AGE_COEFF *
                            age -
                            KEYTEL_MALE_CONST
                    )
            ) /
                KEYTEL_KJ_TO_KCAL
        } else {
            (
                durationMinutes *
                    (
                        KEYTEL_FEMALE_HR_COEFF *
                            hr -
                            KEYTEL_FEMALE_WEIGHT_COEFF *
                            weight +
                            KEYTEL_FEMALE_AGE_COEFF *
                            age -
                            KEYTEL_FEMALE_CONST
                    )
            ) /
                KEYTEL_KJ_TO_KCAL
        }
        return max(0.0, kcal)
    }
}
