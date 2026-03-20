using BasketballTickets.Domain;

namespace BasketballTickets.Repository.Interfaces;

public interface IMatchRepository : IRepository<long, Match>
{
    IList<Match> FindAvailableMatches();
    IList<Match> FindSoldOutMatches();
    Match UpdateAvailableSeats(long matchId, int seatsDelta);
}
