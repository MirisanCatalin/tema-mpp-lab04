namespace BasketballTickets.Domain;

public class Match : Entity<long>
{
    public string Name { get; set; } = string.Empty;
    public double TicketPrice { get; set; }
    public int TotalSeats { get; set; }
    public int AvailableSeats { get; set; }

    public bool IsSoldOut => AvailableSeats <= 0;

    public Match() { }

    public Match(long id, string name, double ticketPrice, int totalSeats, int availableSeats)
        : base(id)
    {
        Name = name;
        TicketPrice = ticketPrice;
        TotalSeats = totalSeats;
        AvailableSeats = availableSeats;
    }

    public override string ToString() =>
        $"Match{{Id={Id}, Name='{Name}', Price={TicketPrice}, Seats={AvailableSeats}/{TotalSeats}}}";
}
