package org.example.service;

import org.example.domain.Cashier;
import org.example.domain.Match;
import org.example.domain.Ticket;

import java.util.List;
import java.util.Optional;

/**
 * Business logic interface for the Basketball Ticket System.
 * Controllers talk ONLY to this — never directly to repositories.
 */
public interface Service {

    // ---- Auth ----

    /**
     * Authenticate a cashier by username + password.
     * @return the logged-in Cashier, or empty if credentials are wrong
     */
    Optional<Cashier> login(String username, String password);

    // ---- Matches ----

    /**
     * Get all matches with their current available seats.
     */
    List<Match> getAllMatches();

    // ---- Ticket sales ----

    /**
     * Sell tickets to a customer for a given match.
     * Creates the customer if they don't exist yet.
     * Decrements availableSeats on the match atomically.
     *
     * @throws IllegalArgumentException if not enough seats available
     */
    Ticket sellTicket(String customerName, String customerAddress,
                      Long matchId, int numberOfSeats);

    // ---- Search ----

    /**
     * Find all tickets for customers matching the given name and/or address.
     */
    List<Ticket> searchTickets(String customerName, String customerAddress);

    // ---- Modify ----

    /**
     * Change the number of seats on an existing ticket.
     * Adjusts availableSeats on the match accordingly.
     *
     * @throws IllegalArgumentException if not enough seats available for the increase
     */
    Ticket modifyTicketSeats(Long ticketId, int newNumberOfSeats);
}