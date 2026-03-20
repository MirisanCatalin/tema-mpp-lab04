using BasketballTickets.Domain;

namespace BasketballTickets.Repository.Interfaces;

public interface ICustomerRepository : IRepository<long, Customer>
{
    IList<Customer> FindByName(string name);
    IList<Customer> FindByAddress(string address);
    IList<Customer> FindByNameOrAddress(string? name, string? address);
}
