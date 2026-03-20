package org.example.repository.interfaces;

import org.example.domain.Ticket;

import java.util.List;

public interface TicketRepository extends Repository<Long, Ticket> {
    List<Ticket> findByCustomerNameOrAddress(String name, String address);
    List<Ticket> findByMatchId(Long matchId);
    List<Ticket> findByCustomerId(Long customerId);
}
