using BasketballTickets.Domain;

namespace BasketballTickets.Repository.Interfaces;

public interface ITicketRepository : IRepository<long, Ticket>
{
    IList<Ticket> FindByCustomerNameOrAddress(string? name, string? address);
    IList<Ticket> FindByMatchId(long matchId);
    IList<Ticket> FindByCustomerId(long customerId);
}
