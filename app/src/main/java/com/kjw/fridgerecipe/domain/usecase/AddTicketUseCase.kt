package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.TicketRepository
import javax.inject.Inject

class AddTicketUseCase
    @Inject
    constructor(
        private val ticketRepository: TicketRepository,
    ) {
        suspend operator fun invoke(amount: Int = 1) = ticketRepository.addTicket(amount)
    }
