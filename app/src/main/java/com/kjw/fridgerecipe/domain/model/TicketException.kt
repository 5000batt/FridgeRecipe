package com.kjw.fridgerecipe.domain.model

sealed class TicketException(message: String) : Exception(message) {
    data object Exhausted : TicketException("무료 티켓이 모두 소진되었습니다.")
}