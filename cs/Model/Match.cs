namespace cs.Domain;
public class Match : Entity<long>
{
    public string TeamA { get; set; }
    public string TeamB { get; set; }
    public double TicketPrice { get; set; }
    public int AvailableSeats { get; set; }

    public Match(long id, string teamA, string teamB, double ticketPrice, int availableSeats) : base(id)
    {
        TeamA = teamA;
        TeamB = teamB;
        TicketPrice = ticketPrice;
        AvailableSeats = availableSeats;
    }

    public Match()
    {
    }

    public override string ToString()
    {
        return "Match{" +
                "id=" + id +
                ", TeamA='" + TeamA + '\'' +
                ", TeamB='" + TeamB + '\'' +
                ", TicketPrice=" + TicketPrice +
                ", AvailableSeats=" + AvailableSeats +
                '}';
    }
}