package com.kjw.fridgerecipe.domain.repository

import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    val ticketCount: Flow<Int>

    /**
     * 이용권 개수를 확인하고 날짜가 변경되면 초기화합니다.
     */
    suspend fun checkAndResetTicket()

    /**
     * 이용권을 사용합니다.
     */
    suspend fun useTicket()

    /**
     * 이용권을 추가합니다.
     */
    suspend fun addTicket(amount: Int = 1)
}
