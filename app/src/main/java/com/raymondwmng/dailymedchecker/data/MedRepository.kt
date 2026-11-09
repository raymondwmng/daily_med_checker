package com.raymondwmng.dailymedchecker.data

import android.content.Context
import android.content.SharedPreferences
import com.raymondwmng.dailymedchecker.model.CapsuleSlot
import com.raymondwmng.dailymedchecker.model.CompletedPackRecord
import com.raymondwmng.dailymedchecker.model.DEFAULT_PACK_SIZE
import com.raymondwmng.dailymedchecker.model.DoseRecord
import com.raymondwmng.dailymedchecker.model.MedTrackerState
import org.json.JSONArray
import org.json.JSONObject

class MedRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadState(): MedTrackerState {
        val rawJson = prefs.getString(KEY_STATE_JSON, null) ?: return MedTrackerState()
        return try {
            deserializeState(rawJson)
        } catch (e: Exception) {
            MedTrackerState()
        }
    }

    fun saveState(state: MedTrackerState) {
        val jsonString = serializeState(state)
        // Use commit() so state is synchronously flushed to disk immediately upon every tap
        prefs.edit().putString(KEY_STATE_JSON, jsonString).commit()
    }

    companion object {
        private const val PREFS_NAME = "daily_med_checker_prefs"
        private const val KEY_STATE_JSON = "med_tracker_state_v1"

        fun serializeState(state: MedTrackerState): String {
            val root = JSONObject()
            root.put("currentPackNumber", state.currentPackNumber)
            root.put("currentPackSize", state.currentPackSize)
            root.put("defaultPackSize", state.defaultPackSize)
            root.put("currentPackStartedAtMillis", state.currentPackStartedAtMillis)

            val slotsArray = JSONArray()
            for (slot in state.slots) {
                val slotObj = JSONObject()
                slotObj.put("slotNumber", slot.slotNumber)
                if (slot.takenAtMillis != null) {
                    slotObj.put("takenAtMillis", slot.takenAtMillis)
                } else {
                    slotObj.put("takenAtMillis", JSONObject.NULL)
                }
                slotsArray.put(slotObj)
            }
            root.put("slots", slotsArray)

            val pastPacksArray = JSONArray()
            for (pack in state.pastPacks) {
                val packObj = JSONObject()
                packObj.put("packNumber", pack.packNumber)
                packObj.put("packSize", pack.packSize)
                packObj.put("startedAtMillis", pack.startedAtMillis)
                packObj.put("completedAtMillis", pack.completedAtMillis)

                val recordsArray = JSONArray()
                for (record in pack.records) {
                    val recObj = JSONObject()
                    recObj.put("slotNumber", record.slotNumber)
                    recObj.put("takenAtMillis", record.takenAtMillis)
                    recordsArray.put(recObj)
                }
                packObj.put("records", recordsArray)
                pastPacksArray.put(packObj)
            }
            root.put("pastPacks", pastPacksArray)

            return root.toString()
        }

        fun deserializeState(jsonString: String): MedTrackerState {
            val root = JSONObject(jsonString)
            val currentPackNumber = root.optInt("currentPackNumber", 1)
            val currentPackSize = root.optInt("currentPackSize", DEFAULT_PACK_SIZE).coerceIn(1, 60)
            val defaultPackSize = root.optInt("defaultPackSize", currentPackSize).coerceIn(1, 60)
            val currentPackStartedAtMillis =
                root.optLong("currentPackStartedAtMillis", System.currentTimeMillis())

            val slotsArray = root.optJSONArray("slots")
            val slots = mutableListOf<CapsuleSlot>()
            if (slotsArray != null && slotsArray.length() > 0) {
                for (i in 0 until slotsArray.length()) {
                    val obj = slotsArray.getJSONObject(i)
                    val slotNumber = obj.optInt("slotNumber", i + 1)
                    val takenAtMillis = if (obj.isNull("takenAtMillis")) {
                        null
                    } else {
                        obj.optLong("takenAtMillis")
                    }
                    slots.add(CapsuleSlot(slotNumber = slotNumber, takenAtMillis = takenAtMillis))
                }
            } else {
                slots.addAll(MedTrackerState.createFreshSlots(currentPackSize))
            }

            val pastPacksArray = root.optJSONArray("pastPacks")
            val pastPacks = mutableListOf<CompletedPackRecord>()
            if (pastPacksArray != null) {
                for (i in 0 until pastPacksArray.length()) {
                    val packObj = pastPacksArray.getJSONObject(i)
                    val packNumber = packObj.optInt("packNumber", i + 1)
                    val packSize = packObj.optInt("packSize", DEFAULT_PACK_SIZE)
                    val startedAtMillis = packObj.optLong("startedAtMillis", 0L)
                    val completedAtMillis = packObj.optLong("completedAtMillis", 0L)

                    val recordsArray = packObj.optJSONArray("records")
                    val records = mutableListOf<DoseRecord>()
                    if (recordsArray != null) {
                        for (j in 0 until recordsArray.length()) {
                            val recObj = recordsArray.getJSONObject(j)
                            records.add(
                                DoseRecord(
                                    slotNumber = recObj.optInt("slotNumber", j + 1),
                                    takenAtMillis = recObj.optLong("takenAtMillis", 0L)
                                )
                            )
                        }
                    }
                    pastPacks.add(
                        CompletedPackRecord(
                            packNumber = packNumber,
                            packSize = packSize,
                            startedAtMillis = startedAtMillis,
                            completedAtMillis = completedAtMillis,
                            records = records
                        )
                    )
                }
            }

            return MedTrackerState(
                currentPackNumber = currentPackNumber,
                currentPackSize = currentPackSize,
                defaultPackSize = defaultPackSize,
                currentPackStartedAtMillis = currentPackStartedAtMillis,
                slots = slots,
                pastPacks = pastPacks
            )
        }
    }
}

