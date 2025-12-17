package com.kjw.fridgerecipe.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ticket_prefs")

@Singleton
class TicketRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_DATE = stringPreferencesKey("last_open_date")
    private val KEY_TICKET_COUNT = intPreferencesKey("ticket_count")

    private val MAX_FREE_TICKETS = 3

    val ticketCount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            val saveDate = preferences[KEY_DATE] ?: ""
            val currentDate = LocalDate.now().toString()

            if (saveDate != currentDate) {
                MAX_FREE_TICKETS
            } else {
                preferences[KEY_TICKET_COUNT] ?: MAX_FREE_TICKETS
            }
        }

    suspend fun checkAndResetTicket() {
        val currentDate = LocalDate.now().toString()
        context.dataStore.edit { preferences ->
            val saveDate = preferences[KEY_DATE]

            if (saveDate != currentDate) {
                preferences[KEY_DATE] = currentDate
                preferences[KEY_TICKET_COUNT] = MAX_FREE_TICKETS
            }
        }
    }

    suspend fun useTicket() {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_TICKET_COUNT] ?: MAX_FREE_TICKETS
            if (current > 0) {
                preferences[KEY_TICKET_COUNT] = current - 1
            }
        }
    }

    suspend fun addTicket(amount: Int = 1) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_TICKET_COUNT] ?: 0
            preferences[KEY_TICKET_COUNT] = current + amount
        }
    }
}