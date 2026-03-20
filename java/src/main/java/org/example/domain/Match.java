package org.example.domain;

public class Match extends Entity<Long> {
    private String name;
    private double ticketPrice;
    private int totalSeats;
    private int availableSeats;

    public Match() {}

    public Match(Long id, String name, double ticketPrice, int totalSeats, int availableSeats) {
        super(id);
        this.name = name;
        this.ticketPrice = ticketPrice;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public boolean isSoldOut() { return availableSeats <= 0; }

    @Override
    public String toString() {
        return "Match{id=" + getId() + ", name='" + name + "', price=" + ticketPrice
                + ", seats=" + availableSeats + "/" + totalSeats + "}";
    }
}