package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTicketCountUseCase
    @Inject
    constructor(
        private val ticketRepository: TicketRepository,
    ) {
        operator fun invoke(): Flow<Int> = ticketRepository.ticketCount
    }
