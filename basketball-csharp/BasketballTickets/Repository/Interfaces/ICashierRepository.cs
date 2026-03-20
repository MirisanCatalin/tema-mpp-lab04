using BasketballTickets.Domain;

namespace BasketballTickets.Repository.Interfaces;

public interface ICashierRepository : IRepository<long, Cashier>
{
    Cashier? FindByUsernameAndPassword(string username, string password);
    Cashier? FindByUsername(string username);
}
