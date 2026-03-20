using BasketballTickets.Domain;
using BasketballTickets.Repository.Abstracts;
using BasketballTickets.Repository.Interfaces;
using BasketballTickets.Utils;
using Microsoft.Data.Sqlite;

namespace BasketballTickets.Repository.Impl;

public class TicketRepositoryImpl : AbstractDBRepository<long, Ticket>, ITicketRepository
{
    private const string JoinSql = """
        SELECT t.id, t.numberOfSeats,
               c.id AS cId, c.name AS cName, c.address AS cAddress,
               m.id AS mId, m.name AS mName, m.ticketPrice, m.totalSeats, m.availableSeats
        FROM tickets t
        JOIN customers c ON t.customerId = c.id
        JOIN matches   m ON t.matchId   = m.id
        """;

    public TicketRepositoryImpl(DatabaseConfig dbConfig) : base(dbConfig) { }

    protected override string GetTableName() => "tickets";

    protected override string GetInsertSql() =>
        "INSERT INTO tickets (customerId, matchId, numberOfSeats) VALUES (@customerId, @matchId, @seats)";

    protected override string GetUpdateSql() =>
        "UPDATE tickets SET customerId=@customerId, matchId=@matchId, numberOfSeats=@seats WHERE id=@id";

    // Tickets au nevoie de JOIN — suprascriem FindById si FindAll
    public override Ticket? FindById(long id)
    {
        Logger.Debug($"FindById({id})");
        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(JoinSql + " WHERE t.id=@id", conn);
        cmd.Parameters.AddWithValue("@id", id);
        using var reader = cmd.ExecuteReader();
        return reader.Read() ? ExtractEntity(reader) : null;
    }

    public override IList<Ticket> FindAll()
    {
        Logger.Debug("FindAll()");
        return RunJoinQuery(JoinSql, []);
    }

    protected override Ticket ExtractEntity(SqliteDataReader r)
    {
        var customer = new Customer(
            r.GetInt64(r.GetOrdinal("cId")),
            r.GetString(r.GetOrdinal("cName")),
            r.GetString(r.GetOrdinal("cAddress")));

        var match = new Match(
            r.GetInt64(r.GetOrdinal("mId")),
            r.GetString(r.GetOrdinal("mName")),
            r.GetDouble(r.GetOrdinal("ticketPrice")),
            r.GetInt32(r.GetOrdinal("totalSeats")),
            r.GetInt32(r.GetOrdinal("availableSeats")));

        return new Ticket(
            r.GetInt64(r.GetOrdinal("id")),
            customer,
            match,
            r.GetInt32(r.GetOrdinal("numberOfSeats")));
    }

    protected override void BindInsert(SqliteCommand cmd, Ticket t)
    {
        cmd.Parameters.AddWithValue("@customerId", t.Customer.Id);
        cmd.Parameters.AddWithValue("@matchId", t.Match.Id);
        cmd.Parameters.AddWithValue("@seats", t.NumberOfSeats);
    }

    protected override void BindUpdate(SqliteCommand cmd, Ticket t)
    {
        cmd.Parameters.AddWithValue("@customerId", t.Customer.Id);
        cmd.Parameters.AddWithValue("@matchId", t.Match.Id);
        cmd.Parameters.AddWithValue("@seats", t.NumberOfSeats);
        cmd.Parameters.AddWithValue("@id", t.Id);
    }

    protected override void SetGeneratedId(Ticket entity, long id) => entity.Id = id;

    // ---- Metode specifice domeniului ----

    public IList<Ticket> FindByCustomerNameOrAddress(string? name, string? address)
    {
        Logger.Debug($"FindByCustomerNameOrAddress(name='{name}', address='{address}')");

        var sql = JoinSql + " WHERE 1=1";
        var parameters = new List<(string, object)>();

        if (!string.IsNullOrWhiteSpace(name))
        {
            sql += " AND LOWER(c.name) = LOWER(@name)";
            parameters.Add(("@name", name));
        }
        if (!string.IsNullOrWhiteSpace(address))
        {
            sql += " AND LOWER(c.address) LIKE LOWER(@address)";
            parameters.Add(("@address", $"%{address}%"));
        }

        return RunJoinQuery(sql, parameters);
    }

    public IList<Ticket> FindByMatchId(long matchId)
    {
        Logger.Debug($"FindByMatchId({matchId})");
        return RunJoinQuery(JoinSql + " WHERE t.matchId=@p", [("@p", (object)matchId)]);
    }

    public IList<Ticket> FindByCustomerId(long customerId)
    {
        Logger.Debug($"FindByCustomerId({customerId})");
        return RunJoinQuery(JoinSql + " WHERE t.customerId=@p", [("@p", (object)customerId)]);
    }

    // ---- Helper ----
    private List<Ticket> RunJoinQuery(string sql, IList<(string, object)> parameters)
    {
        var results = new List<Ticket>();
        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        foreach (var (paramName, value) in parameters)
            cmd.Parameters.AddWithValue(paramName, value);
        using var reader = cmd.ExecuteReader();
        while (reader.Read()) results.Add(ExtractEntity(reader));
        Logger.Debug($"RunJoinQuery → {results.Count} tickets");
        return results;
    }
}
