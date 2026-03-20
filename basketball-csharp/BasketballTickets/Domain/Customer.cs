namespace BasketballTickets.Domain;

public class Customer : Entity<long>
{
    public string Name { get; set; } = string.Empty;
    public string Address { get; set; } = string.Empty;

    public Customer() { }

    public Customer(long id, string name, string address)
        : base(id)
    {
        Name = name;
        Address = address;
    }

    public override string ToString() =>
        $"Customer{{Id={Id}, Name='{Name}', Address='{Address}'}}";
}
