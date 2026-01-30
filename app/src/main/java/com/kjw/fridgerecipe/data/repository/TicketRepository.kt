package com.kjw.fridgerecipe.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.kjw.fridgerecipe.di.TicketDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepository @Inject constructor(
    @TicketDataStore private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val DATE_EPOCH = longPreferencesKey("last_open_date_epoch")
        val TICKET_COUNT = intPreferencesKey("ticket_count")
    }

    private companion object {
        const val MAX_FREE_TICKETS = 3
        const val TAG = "TicketRepo"
    }

    val ticketCount: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading ticket preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val lastSavedEpochDay = preferences[PreferencesKeys.DATE_EPOCH] ?: 0L
            val currentEpochDay = LocalDate.now().toEpochDay()

            // 저장된 날짜가 오늘과 다르면 매일의 기본 티켓 갯수를 반환
            if (lastSavedEpochDay != currentEpochDay) {
                MAX_FREE_TICKETS
            } else {
                preferences[PreferencesKeys.TICKET_COUNT] ?: MAX_FREE_TICKETS
            }
        }

    suspend fun checkAndResetTicket() {
        val currentEpochDay = LocalDate.now().toEpochDay()
        try {
            dataStore.edit { preferences ->
                val lastSavedEpochDay = preferences[PreferencesKeys.DATE_EPOCH]

                if (lastSavedEpochDay != currentEpochDay) {
                    preferences[PreferencesKeys.DATE_EPOCH] = currentEpochDay
                    preferences[PreferencesKeys.TICKET_COUNT] = MAX_FREE_TICKETS
                    Log.d(TAG, "Tickets reset for new day: $currentEpochDay")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to reset tickets", e)
        }
    }

    suspend fun useTicket() {
        try {
            dataStore.edit { preferences ->
                val current = preferences[PreferencesKeys.TICKET_COUNT] ?: MAX_FREE_TICKETS
                if (current > 0) {
                    preferences[PreferencesKeys.TICKET_COUNT] = current - 1
                }
                preferences[PreferencesKeys.DATE_EPOCH] = LocalDate.now().toEpochDay()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to use ticket", e)
        }
    }

    suspend fun addTicket(amount: Int = 1) {
        try {
            dataStore.edit { preferences ->
                val current = preferences[PreferencesKeys.TICKET_COUNT] ?: 0
                preferences[PreferencesKeys.TICKET_COUNT] = current + amount
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to add ticket", e)
        }
    }
}
