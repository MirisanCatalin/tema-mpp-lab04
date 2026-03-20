namespace cs.Repository.Interfaces;

using cs.Domain;
using System.Collections.Generic;

public interface ITicketRepository : IRepository<long, Ticket>
{

    IEnumerable<Ticket> FindByClient(int clientId);

    IEnumerable<Ticket> FindAll();

}