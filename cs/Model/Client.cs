namespace cs.Domain;
public class Client : Entity<long>
{
    public string Name { get; set; }
    public string Address { get; set; }

    public Client(long id, string name, string address) : base(id)
    {
        Name = name;
        Address = address;
    }

    public Client()
    {
    }

    public override string ToString()
    {
        return "Client{" +
                "id=" + id +
                ", Name='" + Name + '\'' +
                ", Address='" + Address + '\'' +
                '}';
    }
}