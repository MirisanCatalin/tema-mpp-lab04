using BasketballTickets.Domain;
using BasketballTickets.Repository.Abstracts;
using BasketballTickets.Repository.Interfaces;
using BasketballTickets.Utils;
using Microsoft.Data.Sqlite;

namespace BasketballTickets.Repository.Impl;

public class CashierRepositoryImpl : AbstractDBRepository<long, Cashier>, ICashierRepository
{
    public CashierRepositoryImpl(DatabaseConfig dbConfig) : base(dbConfig) { }

    protected override string GetTableName() => "cashiers";

    protected override string GetInsertSql() =>
        "INSERT INTO cashiers (username, password, fullName) VALUES (@username, @password, @fullName)";

    protected override string GetUpdateSql() =>
        "UPDATE cashiers SET username=@username, password=@password, fullName=@fullName WHERE id=@id";

    protected override Cashier ExtractEntity(SqliteDataReader r) =>
        new(r.GetInt64(r.GetOrdinal("id")),
            r.GetString(r.GetOrdinal("username")),
            r.GetString(r.GetOrdinal("password")),
            r.GetString(r.GetOrdinal("fullName")));

    protected override void BindInsert(SqliteCommand cmd, Cashier c)
    {
        cmd.Parameters.AddWithValue("@username", c.Username);
        cmd.Parameters.AddWithValue("@password", c.Password);
        cmd.Parameters.AddWithValue("@fullName", c.FullName);
    }

    protected override void BindUpdate(SqliteCommand cmd, Cashier c)
    {
        cmd.Parameters.AddWithValue("@username", c.Username);
        cmd.Parameters.AddWithValue("@password", c.Password);
        cmd.Parameters.AddWithValue("@fullName", c.FullName);
        cmd.Parameters.AddWithValue("@id", c.Id);
    }

    protected override void SetGeneratedId(Cashier entity, long id) => entity.Id = id;

    // ---- Metode specifice domeniului ----

    public Cashier? FindByUsernameAndPassword(string username, string password)
    {
        Logger.Debug($"FindByUsernameAndPassword({username})");
        const string sql = "SELECT * FROM cashiers WHERE username=@u AND password=@p";

        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        cmd.Parameters.AddWithValue("@u", username);
        cmd.Parameters.AddWithValue("@p", password);

        using var reader = cmd.ExecuteReader();
        if (reader.Read())
        {
            Logger.Info($"Login OK for username={username}");
            return ExtractEntity(reader);
        }

        Logger.Warn($"Login FAILED for username={username}");
        return null;
    }

    public Cashier? FindByUsername(string username)
    {
        Logger.Debug($"FindByUsername({username})");
        const string sql = "SELECT * FROM cashiers WHERE username=@u";

        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        cmd.Parameters.AddWithValue("@u", username);

        using var reader = cmd.ExecuteReader();
        return reader.Read() ? ExtractEntity(reader) : null;
    }
}
