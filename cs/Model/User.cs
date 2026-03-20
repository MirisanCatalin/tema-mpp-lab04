namespace cs.Domain;
public class User : Entity<long>
{
    public string username { get; set; }
    public string password { get; set; }

    public User(long id, string username, string password) : base(id)
    {
        this.username = username;
        this.password = password;
    }

    public User()
    {
    }
    
    public override string ToString()
    {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }


}