namespace BasketballTickets.Domain;

public class Cashier : Entity<long>
{
    public string Username { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;

    public Cashier() { }

    public Cashier(long id, string username, string password, string fullName)
        : base(id)
    {
        Username = username;
        Password = password;
        FullName = fullName;
    }

    public override string ToString() =>
        $"Cashier{{Id={Id}, Username='{Username}', FullName='{FullName}'}}";
}
