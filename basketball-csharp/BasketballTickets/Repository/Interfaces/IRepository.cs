namespace BasketballTickets.Repository.Interfaces;

/// <summary>
/// Interfata generica CRUD — echivalent cu Repository.java
/// </summary>
public interface IRepository<TId, TEntity>
{
    TEntity? FindById(TId id);
    IList<TEntity> FindAll();
    TEntity Save(TEntity entity);
    TEntity Update(TEntity entity);
    void Delete(TId id);
}
