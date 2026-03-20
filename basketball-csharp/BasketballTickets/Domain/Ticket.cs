namespace BasketballTickets.Domain;

public class Ticket : Entity<long>
{
    public Customer Customer { get; set; } = null!;
    public Match Match { get; set; } = null!;
    public int NumberOfSeats { get; set; }

    public Ticket() { }

    public Ticket(long id, Customer customer, Match match, int numberOfSeats)
        : base(id)
    {
        Customer = customer;
        Match = match;
        NumberOfSeats = numberOfSeats;
    }

    public override string ToString() =>
        $"Ticket{{Id={Id}, Customer='{Customer?.Name}', Match='{Match?.Name}', Seats={NumberOfSeats}}}";
}
