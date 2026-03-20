package org.example.service;


import org.example.domain.Cashier;
import org.example.domain.Customer;
import org.example.domain.Match;
import org.example.domain.Ticket;
import org.example.repository.interfaces.CashierRepository;
import org.example.repository.interfaces.CustomerRepository;
import org.example.repository.interfaces.MatchRepository;
import org.example.repository.interfaces.TicketRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Implements all business logic for the Basketball Ticket System.
 *
 * Controllers receive a reference to this class only — they never
 * talk directly to repositories. This makes it easy to swap the
 * persistence layer or add networking later.
 */
public class ServiceImpl implements Service {

    private static final Logger logger = LogManager.getLogger(ServiceImpl.class);

    private final CashierRepository cashierRepo;
    private final MatchRepository matchRepo;
    private final CustomerRepository customerRepo;
    private final TicketRepository ticketRepo;

    // List of observers notified when match data changes (for multi-client real-time updates)
    private final List<MatchUpdateObserver> observers = new java.util.concurrent.CopyOnWriteArrayList<>();

    public ServiceImpl(CashierRepository cashierRepo,
                       MatchRepository matchRepo,
                       CustomerRepository customerRepo,
                       TicketRepository ticketRepo) {
        this.cashierRepo = cashierRepo;
        this.matchRepo   = matchRepo;
        this.customerRepo = customerRepo;
        this.ticketRepo  = ticketRepo;
        logger.info("ServiceImpl created");
    }

    // ---- Observer support (for future network/multi-client scale-up) ----

    public void addObserver(MatchUpdateObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(MatchUpdateObserver observer) {
        observers.remove(observer);
    }

    private void notifyMatchUpdated(Match match) {
        observers.forEach(o -> o.onMatchUpdated(match));
    }

    // ---- Auth ----

    @Override
    public Optional<Cashier> login(String username, String password) {
        logger.debug("login attempt: username={}", username);
        return cashierRepo.findByUsernameAndPassword(username, password);
    }

    // ---- Matches ----

    @Override
    public List<Match> getAllMatches() {
        logger.debug("getAllMatches()");
        return matchRepo.findAll();
    }

    // ---- Ticket sales ----

    @Override
    public Ticket sellTicket(String customerName, String customerAddress,
                             Long matchId, int numberOfSeats) {
        logger.debug("sellTicket: customer='{}', matchId={}, seats={}", customerName, matchId, numberOfSeats);

        // 1. Validate match exists and has enough seats
        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getAvailableSeats() < numberOfSeats) {
            logger.warn("Not enough seats for matchId={}: requested={}, available={}",
                    matchId, numberOfSeats, match.getAvailableSeats());
            throw new IllegalArgumentException(
                    "Not enough seats available. Requested: " + numberOfSeats
                            + ", Available: " + match.getAvailableSeats());
        }

        // 2. Find or create customer
        List<Customer> existing = customerRepo.findByNameOrAddress(customerName, customerAddress);
        Customer customer;
        if (existing.isEmpty()) {
            customer = customerRepo.save(new Customer(null, customerName, customerAddress));
            logger.info("New customer created: {}", customer);
        } else {
            customer = existing.get(0);
            logger.debug("Existing customer found: {}", customer);
        }

        // 3. Save ticket
        Ticket ticket = ticketRepo.save(new Ticket(null, customer, match, numberOfSeats));

        // 4. Decrement available seats
        Match updated = matchRepo.updateAvailableSeats(matchId, -numberOfSeats);

        // 5. Notify all connected clients (Observer pattern — ready for networking)
        notifyMatchUpdated(updated);

        logger.info("Ticket sold: {}", ticket);
        return ticket;
    }

    // ---- Search ----

    @Override
    public List<Ticket> searchTickets(String customerName, String customerAddress) {
        logger.debug("searchTickets(name='{}', address='{}')", customerName, customerAddress);
        return ticketRepo.findByCustomerNameOrAddress(customerName, customerAddress);
    }

    // ---- Modify ----

    @Override
    public Ticket modifyTicketSeats(Long ticketId, int newNumberOfSeats) {
        logger.debug("modifyTicketSeats(ticketId={}, newSeats={})", ticketId, newNumberOfSeats);

        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        int oldSeats = ticket.getNumberOfSeats();
        int delta    = oldSeats - newNumberOfSeats; // positive = freeing seats, negative = needing more

        // If we need more seats, validate availability
        if (delta < 0) {
            Match match = matchRepo.findById(ticket.getMatch().getId()).orElseThrow();
            if (match.getAvailableSeats() < -delta) {
                logger.warn("Cannot increase seats for ticketId={}: need {} more, only {} available",
                        ticketId, -delta, match.getAvailableSeats());
                throw new IllegalArgumentException(
                        "Not enough seats available. Need " + (-delta)
                                + " more, but only " + match.getAvailableSeats() + " available.");
            }
        }

        ticket.setNumberOfSeats(newNumberOfSeats);
        ticketRepo.update(ticket);

        Match updated = matchRepo.updateAvailableSeats(ticket.getMatch().getId(), delta);
        notifyMatchUpdated(updated);

        logger.info("Ticket {} modified: {} → {} seats", ticketId, oldSeats, newNumberOfSeats);
        return ticket;
    }
}
