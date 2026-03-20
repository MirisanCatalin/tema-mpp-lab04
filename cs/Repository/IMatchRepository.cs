namespace cs.Repository.Interfaces;

using cs.Domain;
using System.Collections.Generic;

public interface IUserRepository : IRepository<long, User>
{
    IEnumerable<Match> FindAll();

    Match FindById(int id);
}