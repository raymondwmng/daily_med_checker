package com.raymondwmng.dailymedchecker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.raymondwmng.dailymedchecker.data.MedRepository
import com.raymondwmng.dailymedchecker.model.CapsuleSlot
import com.raymondwmng.dailymedchecker.model.MedTrackerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MedRepository(application.applicationContext)

    private val _state = MutableStateFlow(repository.loadState())
    val state: StateFlow<MedTrackerState> = _state.asStateFlow()

    private val _pendingRestoreSlot = MutableStateFlow<CapsuleSlot?>(null)
    val pendingRestoreSlot: StateFlow<CapsuleSlot?> = _pendingRestoreSlot.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _pendingPackSizeChange = MutableStateFlow<Int?>(null)
    val pendingPackSizeChange: StateFlow<Int?> = _pendingPackSizeChange.asStateFlow()

    fun onCapsuleSlotClicked(slot: CapsuleSlot) {
        if (!slot.isTaken) {
            updateAndPersist(_state.value.takeCapsule(slot.slotNumber))
        } else {
            _pendingRestoreSlot.value = slot
        }
    }

    fun confirmRestoreSlot() {
        val slot = _pendingRestoreSlot.value ?: return
        updateAndPersist(_state.value.restoreCapsule(slot.slotNumber))
        _pendingRestoreSlot.value = null
    }

    fun dismissRestoreDialog() {
        _pendingRestoreSlot.value = null
    }

    fun startNewPack() {
        updateAndPersist(_state.value.startNewPack())
    }

    fun openSettings() {
        _showSettingsDialog.value = true
    }

    fun closeSettings() {
        _showSettingsDialog.value = false
    }

    fun requestPackSizeChange(newSize: Int) {
        val validSize = newSize.coerceIn(1, 60)
        _showSettingsDialog.value = false

        val current = _state.value
        if (current.takenCount == 0) {
            // Apply to the current pack immediately when no pills are taken yet
            updateAndPersist(
                current.applyPackSizeImmediately(
                    newSize = validSize,
                    archiveCurrentIfAnyTaken = false
                )
            )
        } else if (validSize != current.currentPackSize || validSize != current.defaultPackSize) {
            // Ask user whether to reset current pack now or apply to next pack
            _pendingPackSizeChange.value = validSize
        }
    }

    fun confirmPackSizeForNextPackOnly() {
        val newSize = _pendingPackSizeChange.value ?: return
        updateAndPersist(_state.value.applyPackSizeToNextPackOnly(newSize))
        _pendingPackSizeChange.value = null
    }

    fun confirmPackSizeResetCurrentPack() {
        val newSize = _pendingPackSizeChange.value ?: return
        updateAndPersist(
            _state.value.applyPackSizeImmediately(
                newSize = newSize,
                archiveCurrentIfAnyTaken = true
            )
        )
        _pendingPackSizeChange.value = null
    }

    fun dismissPackSizeConflictDialog() {
        _pendingPackSizeChange.value = null
    }

    private fun updateAndPersist(newState: MedTrackerState) {
        _state.value = newState
        repository.saveState(newState)
    }
}

