namespace cs.Domain;
public class Tickets : Entity<long>
{
    public int ClientId { get; set; }
    public int MatchId { get; set; }
    public int NumberOfSeats { get; set; }

    public Tickets(long id, int clientId, int matchId, int numberOfSeats) : base(id)
    {
        ClientId = clientId;
        MatchId = matchId;
        NumberOfSeats = numberOfSeats;
    }

    public Tickets()
    {
    }

    public override string ToString()
    {
        return "Tickets{" +
                "id=" + id +
                ", ClientId=" + ClientId +
                ", MatchId=" + MatchId +
                ", NumberOfSeats=" + NumberOfSeats +
                '}';
    }
}