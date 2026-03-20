using BasketballTickets.Domain;
using BasketballTickets.Repository.Abstracts;
using BasketballTickets.Repository.Interfaces;
using BasketballTickets.Utils;
using Microsoft.Data.Sqlite;

namespace BasketballTickets.Repository.Impl;

public class MatchRepositoryImpl : AbstractDBRepository<long, Match>, IMatchRepository
{
    public MatchRepositoryImpl(DatabaseConfig dbConfig) : base(dbConfig) { }

    protected override string GetTableName() => "matches";

    protected override string GetInsertSql() =>
        "INSERT INTO matches (name, ticketPrice, totalSeats, availableSeats) VALUES (@name, @price, @total, @available)";

    protected override string GetUpdateSql() =>
        "UPDATE matches SET name=@name, ticketPrice=@price, totalSeats=@total, availableSeats=@available WHERE id=@id";

    protected override Match ExtractEntity(SqliteDataReader r) =>
        new(r.GetInt64(r.GetOrdinal("id")),
            r.GetString(r.GetOrdinal("name")),
            r.GetDouble(r.GetOrdinal("ticketPrice")),
            r.GetInt32(r.GetOrdinal("totalSeats")),
            r.GetInt32(r.GetOrdinal("availableSeats")));

    protected override void BindInsert(SqliteCommand cmd, Match m)
    {
        cmd.Parameters.AddWithValue("@name", m.Name);
        cmd.Parameters.AddWithValue("@price", m.TicketPrice);
        cmd.Parameters.AddWithValue("@total", m.TotalSeats);
        cmd.Parameters.AddWithValue("@available", m.AvailableSeats);
    }

    protected override void BindUpdate(SqliteCommand cmd, Match m)
    {
        cmd.Parameters.AddWithValue("@name", m.Name);
        cmd.Parameters.AddWithValue("@price", m.TicketPrice);
        cmd.Parameters.AddWithValue("@total", m.TotalSeats);
        cmd.Parameters.AddWithValue("@available", m.AvailableSeats);
        cmd.Parameters.AddWithValue("@id", m.Id);
    }

    protected override void SetGeneratedId(Match entity, long id) => entity.Id = id;

    // ---- Metode specifice domeniului ----

    public IList<Match> FindAvailableMatches()
    {
        Logger.Debug("FindAvailableMatches()");
        return QueryList("SELECT * FROM matches WHERE availableSeats > 0 ORDER BY id");
    }

    public IList<Match> FindSoldOutMatches()
    {
        Logger.Debug("FindSoldOutMatches()");
        return QueryList("SELECT * FROM matches WHERE availableSeats <= 0 ORDER BY id");
    }

    public Match UpdateAvailableSeats(long matchId, int seatsDelta)
    {
        Logger.Debug($"UpdateAvailableSeats(matchId={matchId}, delta={seatsDelta})");
        const string sql = "UPDATE matches SET availableSeats = availableSeats + @delta WHERE id=@id";

        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        cmd.Parameters.AddWithValue("@delta", seatsDelta);
        cmd.Parameters.AddWithValue("@id", matchId);
        cmd.ExecuteNonQuery();

        Logger.Info($"AvailableSeats updated for matchId={matchId} by {seatsDelta}");

        return FindById(matchId)
            ?? throw new InvalidOperationException($"Match not found after update: {matchId}");
    }

    // ---- Helper ----
    private List<Match> QueryList(string sql)
    {
        var results = new List<Match>();
        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        using var reader = cmd.ExecuteReader();
        while (reader.Read()) results.Add(ExtractEntity(reader));
        return results;
    }
}
