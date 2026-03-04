package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.TicketRepository
import javax.inject.Inject

class CheckAndResetTicketUseCase
    @Inject
    constructor(
        private val ticketRepository: TicketRepository,
    ) {
        suspend operator fun invoke() = ticketRepository.checkAndResetTicket()
    }
