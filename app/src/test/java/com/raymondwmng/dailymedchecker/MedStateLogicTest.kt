package com.raymondwmng.dailymedchecker

import com.raymondwmng.dailymedchecker.model.DEFAULT_PACK_SIZE
import com.raymondwmng.dailymedchecker.model.MedTrackerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MedStateLogicTest {

    @Test
    fun defaultState_has12UntakenCapsules() {
        val state = MedTrackerState()
        assertEquals(DEFAULT_PACK_SIZE, state.currentPackSize)
        assertEquals(12, state.slots.size)
        assertEquals(12, state.remainingCount)
        assertEquals(0, state.takenCount)
        assertFalse(state.isPackComplete)
        assertTrue(state.pastPacks.isEmpty())
    }

    @Test
    fun takeAndRestoreIndividualCapsule_updatesSlotAndRecords() {
        val initial = MedTrackerState(currentPackStartedAtMillis = 1000L)
        val afterTake = initial.takeCapsule(slotNumber = 5, nowMillis = 2500L)

        assertEquals(11, afterTake.remainingCount)
        assertEquals(1, afterTake.takenCount)
        val slot5 = afterTake.slots.first { it.slotNumber == 5 }
        assertTrue(slot5.isTaken)
        assertEquals(2500L, slot5.takenAtMillis)
        assertEquals(1, afterTake.currentPackRecords.size)
        assertEquals(5, afterTake.currentPackRecords.first().slotNumber)

        val afterRestore = afterTake.restoreCapsule(slotNumber = 5)
        assertEquals(12, afterRestore.remainingCount)
        assertEquals(0, afterRestore.takenCount)
        assertNull(afterRestore.slots.first { it.slotNumber == 5 }.takenAtMillis)
        assertTrue(afterRestore.currentPackRecords.isEmpty())
    }

    @Test
    fun completingPackAndStartingNewPack_archivesRecordsAndResetsPack() {
        var state = MedTrackerState(currentPackStartedAtMillis = 1000L)
        for (i in 1..12) {
            state = state.takeCapsule(slotNumber = i, nowMillis = 1000L + i * 1000L)
        }

        assertTrue(state.isPackComplete)
        assertEquals(0, state.remainingCount)
        assertEquals(12, state.takenCount)

        val nextPackState = state.startNewPack(nowMillis = 20_000L)
        assertEquals(2, nextPackState.currentPackNumber)
        assertEquals(12, nextPackState.remainingCount)
        assertEquals(0, nextPackState.takenCount)
        assertFalse(nextPackState.isPackComplete)

        assertEquals(1, nextPackState.pastPacks.size)
        val archived = nextPackState.pastPacks.first()
        assertEquals(1, archived.packNumber)
        assertEquals(12, archived.records.size)
        assertNotNull(archived.records.find { it.slotNumber == 12 && it.takenAtMillis == 13_000L })
    }

    @Test
    fun configurePackSize_appliesImmediatelyOrNextPack() {
        val freshState = MedTrackerState()
        val resizedImmediate = freshState.applyPackSizeImmediately(newSize = 21, nowMillis = 5000L)
        assertEquals(21, resizedImmediate.currentPackSize)
        assertEquals(21, resizedImmediate.defaultPackSize)
        assertEquals(21, resizedImmediate.remainingCount)

        val partialState = resizedImmediate.takeCapsule(slotNumber = 1, nowMillis = 6000L)
        val nextPackOnly = partialState.applyPackSizeToNextPackOnly(newSize = 14)
        assertEquals(21, nextPackOnly.currentPackSize)
        assertEquals(14, nextPackOnly.defaultPackSize)
        assertEquals(20, nextPackOnly.remainingCount)

        val startedNext = nextPackOnly.startNewPack(nowMillis = 7000L)
        assertEquals(14, startedNext.currentPackSize)
        assertEquals(14, startedNext.remainingCount)
        assertEquals(1, startedNext.pastPacks.size)
    }
}

