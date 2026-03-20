namespace cs.Repository.Interfaces;

using cs.Domain;
using System.Collections.Generic;

public interface IUserRepository : IRepository<long, User>
{
    IEnumerable<Client> FindByName(string name);

    IEnumerable<Client> FindAll();
}