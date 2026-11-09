package com.raymondwmng.dailymedchecker.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val DEFAULT_PACK_SIZE = 12

/**
 * Represents a single capsule cavity in the current blister pack.
 *
 * @property slotNumber 1-based index of the capsule slot in the pack.
 * @property takenAtMillis Epoch milliseconds when the capsule was taken, or null if still in the pack.
 */
data class CapsuleSlot(
    val slotNumber: Int,
    val takenAtMillis: Long? = null
) {
    val isTaken: Boolean
        get() = takenAtMillis != null
}

/**
 * Represents a single dose record with its capsule slot number and timestamp.
 */
data class DoseRecord(
    val slotNumber: Int,
    val takenAtMillis: Long
)

/**
 * Represents a finished/archived pack and all the timestamps when its capsules were taken.
 */
data class CompletedPackRecord(
    val packNumber: Int,
    val packSize: Int,
    val startedAtMillis: Long,
    val completedAtMillis: Long,
    val records: List<DoseRecord>
)

/**
 * Complete persisted state of the Daily Pill Tracker app.
 */
data class MedTrackerState(
    val currentPackNumber: Int = 1,
    val currentPackSize: Int = DEFAULT_PACK_SIZE,
    val defaultPackSize: Int = DEFAULT_PACK_SIZE,
    val currentPackStartedAtMillis: Long = System.currentTimeMillis(),
    val slots: List<CapsuleSlot> = createFreshSlots(DEFAULT_PACK_SIZE),
    val pastPacks: List<CompletedPackRecord> = emptyList()
) {
    val takenCount: Int
        get() = slots.count { it.isTaken }

    val remainingCount: Int
        get() = slots.count { !it.isTaken }

    val isPackComplete: Boolean
        get() = slots.isNotEmpty() && slots.all { it.isTaken }

    /**
     * Taken records for the current active pack, ordered from most recent to oldest.
     */
    val currentPackRecords: List<DoseRecord>
        get() = slots
            .mapNotNull { slot ->
                slot.takenAtMillis?.let { millis ->
                    DoseRecord(slotNumber = slot.slotNumber, takenAtMillis = millis)
                }
            }
            .sortedByDescending { it.takenAtMillis }

    /**
     * Returns the most recent dose taken today (in local calendar day), if any.
     */
    fun latestDoseToday(nowMillis: Long = System.currentTimeMillis()): DoseRecord? {
        val allRecent = currentPackRecords + pastPacks.flatMap { it.records }
        return allRecent
            .filter { isSameLocalDay(it.takenAtMillis, nowMillis) }
            .maxByOrNull { it.takenAtMillis }
    }

    /**
     * Marks a specific capsule slot as taken at [nowMillis].
     */
    fun takeCapsule(slotNumber: Int, nowMillis: Long = System.currentTimeMillis()): MedTrackerState {
        val updatedSlots = slots.map { slot ->
            if (slot.slotNumber == slotNumber && !slot.isTaken) {
                slot.copy(takenAtMillis = nowMillis)
            } else {
                slot
            }
        }
        return copy(slots = updatedSlots)
    }

    /**
     * Restores an empty capsule slot back to untaken and removes its timestamp record.
     */
    fun restoreCapsule(slotNumber: Int): MedTrackerState {
        val updatedSlots = slots.map { slot ->
            if (slot.slotNumber == slotNumber) {
                slot.copy(takenAtMillis = null)
            } else {
                slot
            }
        }
        return copy(slots = updatedSlots)
    }

    /**
     * Archives the current pack into [pastPacks] (if at least one capsule was taken)
     * and opens a brand new pack with [defaultPackSize] capsules.
     */
    fun startNewPack(nowMillis: Long = System.currentTimeMillis()): MedTrackerState {
        val takenRecords = currentPackRecords
        val updatedPastPacks = if (takenRecords.isNotEmpty()) {
            val completedRecord = CompletedPackRecord(
                packNumber = currentPackNumber,
                packSize = currentPackSize,
                startedAtMillis = currentPackStartedAtMillis,
                completedAtMillis = nowMillis,
                records = takenRecords
            )
            listOf(completedRecord) + pastPacks
        } else {
            pastPacks
        }

        val nextPackNum = if (takenRecords.isNotEmpty()) currentPackNumber + 1 else currentPackNumber
        return copy(
            currentPackNumber = nextPackNum,
            currentPackSize = defaultPackSize,
            currentPackStartedAtMillis = nowMillis,
            slots = createFreshSlots(defaultPackSize),
            pastPacks = updatedPastPacks
        )
    }

    /**
     * Updates the configured pack size and applies it to the current pack immediately.
     * If any pills were already taken in the current pack and [archiveCurrentIfAnyTaken] is true,
     * those taken records are saved to [pastPacks] before resetting the pack.
     */
    fun applyPackSizeImmediately(
        newSize: Int,
        nowMillis: Long = System.currentTimeMillis(),
        archiveCurrentIfAnyTaken: Boolean = true
    ): MedTrackerState {
        val coercedSize = newSize.coerceIn(1, 60)
        val takenRecords = currentPackRecords
        val updatedPastPacks = if (archiveCurrentIfAnyTaken && takenRecords.isNotEmpty()) {
            val archived = CompletedPackRecord(
                packNumber = currentPackNumber,
                packSize = currentPackSize,
                startedAtMillis = currentPackStartedAtMillis,
                completedAtMillis = nowMillis,
                records = takenRecords
            )
            listOf(archived) + pastPacks
        } else {
            pastPacks
        }
        val nextPackNum = if (archiveCurrentIfAnyTaken && takenRecords.isNotEmpty()) {
            currentPackNumber + 1
        } else {
            currentPackNumber
        }

        return copy(
            currentPackNumber = nextPackNum,
            currentPackSize = coercedSize,
            defaultPackSize = coercedSize,
            currentPackStartedAtMillis = nowMillis,
            slots = createFreshSlots(coercedSize),
            pastPacks = updatedPastPacks
        )
    }

    /**
     * Updates [defaultPackSize] so the next pack uses [newSize], while leaving the active pack untouched.
     */
    fun applyPackSizeToNextPackOnly(newSize: Int): MedTrackerState {
        val coercedSize = newSize.coerceIn(1, 60)
        return copy(defaultPackSize = coercedSize)
    }

    companion object {
        fun createFreshSlots(size: Int): List<CapsuleSlot> {
            val coerced = size.coerceIn(1, 60)
            return (1..coerced).map { CapsuleSlot(slotNumber = it, takenAtMillis = null) }
        }

        fun isSameLocalDay(millisA: Long, millisB: Long): Boolean {
            val calA = Calendar.getInstance().apply { timeInMillis = millisA }
            val calB = Calendar.getInstance().apply { timeInMillis = millisB }
            return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
                calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
        }
    }
}

object DateTimeFormatters {
    private val cantoneseLocale = Locale("zh", "HK")

    fun formatFullDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy年M月d日 (EEE) ahh:mm", cantoneseLocale)
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy年M月d日", cantoneseLocale)
        return sdf.format(Date(millis))
    }

    fun formatTimeOnly(millis: Long): String {
        val sdf = SimpleDateFormat("ahh:mm", cantoneseLocale)
        return sdf.format(Date(millis))
    }
}

