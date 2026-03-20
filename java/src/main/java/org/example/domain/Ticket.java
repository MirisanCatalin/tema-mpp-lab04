package org.example.domain;

public class Ticket extends Entity<Long>{
    private Customer customer;
    private Match match;
    private int numberOfSeats;

    public Ticket() {
    }

    public Ticket(Long id, Customer customer, Match match, int numberOfSeats) {
        super(id);
        this.customer = customer;
        this.match = match;
        this.numberOfSeats = numberOfSeats;
    }

    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public Match getMatch() {
        return match;
    }
    public void setMatch(Match match) {
        this.match = match;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }
    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    @Override
    public String toString() {
        return "Ticket{id=" + getId() + ", customer=" + customer + ", match=" + match + ", numberOfSeats=" + numberOfSeats + "}";
    }
}
