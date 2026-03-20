using BasketballTickets.Domain;
using BasketballTickets.Repository.Abstracts;
using BasketballTickets.Repository.Interfaces;
using BasketballTickets.Utils;
using Microsoft.Data.Sqlite;

namespace BasketballTickets.Repository.Impl;

public class CustomerRepositoryImpl : AbstractDBRepository<long, Customer>, ICustomerRepository
{
    public CustomerRepositoryImpl(DatabaseConfig dbConfig) : base(dbConfig) { }

    protected override string GetTableName() => "customers";

    protected override string GetInsertSql() =>
        "INSERT INTO customers (name, address) VALUES (@name, @address)";

    protected override string GetUpdateSql() =>
        "UPDATE customers SET name=@name, address=@address WHERE id=@id";

    protected override Customer ExtractEntity(SqliteDataReader r) =>
        new(r.GetInt64(r.GetOrdinal("id")),
            r.GetString(r.GetOrdinal("name")),
            r.GetString(r.GetOrdinal("address")));

    protected override void BindInsert(SqliteCommand cmd, Customer c)
    {
        cmd.Parameters.AddWithValue("@name", c.Name);
        cmd.Parameters.AddWithValue("@address", c.Address);
    }

    protected override void BindUpdate(SqliteCommand cmd, Customer c)
    {
        cmd.Parameters.AddWithValue("@name", c.Name);
        cmd.Parameters.AddWithValue("@address", c.Address);
        cmd.Parameters.AddWithValue("@id", c.Id);
    }

    protected override void SetGeneratedId(Customer entity, long id) => entity.Id = id;

    // ---- Metode specifice domeniului ----

    public IList<Customer> FindByName(string name)
    {
        Logger.Debug($"FindByName('{name}')");
        const string sql = "SELECT * FROM customers WHERE LOWER(name) = LOWER(@name)";
        return QueryWithParam(sql, ("@name", name));
    }

    public IList<Customer> FindByAddress(string address)
    {
        Logger.Debug($"FindByAddress('{address}')");
        const string sql = "SELECT * FROM customers WHERE LOWER(address) LIKE LOWER(@address)";
        return QueryWithParam(sql, ("@address", $"%{address}%"));
    }

    public IList<Customer> FindByNameOrAddress(string? name, string? address)
    {
        Logger.Debug($"FindByNameOrAddress(name='{name}', address='{address}')");

        var sql = "SELECT * FROM customers WHERE 1=1";
        var parameters = new List<(string, object)>();

        if (!string.IsNullOrWhiteSpace(name))
        {
            sql += " AND LOWER(name) = LOWER(@name)";
            parameters.Add(("@name", name));
        }
        if (!string.IsNullOrWhiteSpace(address))
        {
            sql += " AND LOWER(address) LIKE LOWER(@address)";
            parameters.Add(("@address", $"%{address}%"));
        }

        var results = new List<Customer>();
        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        foreach (var (paramName, value) in parameters)
            cmd.Parameters.AddWithValue(paramName, value);

        using var reader = cmd.ExecuteReader();
        while (reader.Read()) results.Add(ExtractEntity(reader));

        Logger.Debug($"FindByNameOrAddress → {results.Count} results");
        return results;
    }

    // ---- Helper ----
    private List<Customer> QueryWithParam(string sql, (string name, object value) param)
    {
        var results = new List<Customer>();
        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        cmd.Parameters.AddWithValue(param.name, param.value);
        using var reader = cmd.ExecuteReader();
        while (reader.Read()) results.Add(ExtractEntity(reader));
        return results;
    }
}
