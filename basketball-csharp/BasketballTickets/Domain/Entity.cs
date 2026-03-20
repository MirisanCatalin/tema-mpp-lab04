namespace BasketballTickets.Domain;

/// <summary>
/// Clasa de baza pentru toate entitatile domeniului.
/// Echivalent cu Entity.java
/// </summary>
public abstract class Entity<TId>
{
    public TId? Id { get; set; }

    protected Entity() { }

    protected Entity(TId id)
    {
        Id = id;
    }
}
