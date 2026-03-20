using log4net;
using Microsoft.Data.Sqlite;

namespace BasketballTickets.Utils;

/// <summary>
/// Citeste configuratia din configs/config.properties si ofera conexiuni SQLite.
/// Echivalent cu DatabaseConfig.java
/// </summary>
public class DatabaseConfig
{
    private static readonly ILog Logger = LogManager.GetLogger(typeof(DatabaseConfig));
    private readonly string _connectionString;

    public DatabaseConfig()
    {
        _connectionString = LoadConnectionString();
        Logger.Info($"DatabaseConfig initialised → {_connectionString}");
        InitSchema();
    }

    private string LoadConnectionString()
    {
        var configPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "configs", "config.properties");

        if (!File.Exists(configPath))
        {
            Logger.Warn($"config.properties not found at {configPath}, using default");
            return "Data Source=basketball.db";
        }

        foreach (var line in File.ReadAllLines(configPath))
        {
            var trimmed = line.Trim();
            if (trimmed.StartsWith("db.url="))
            {
                var value = trimmed["db.url=".Length..].Trim();
                Logger.Debug($"Loaded connection string from config.properties: {value}");
                return value;
            }
        }

        Logger.Warn("db.url not found in config.properties, using default");
        return "Data Source=basketball.db";
    }

    /// <summary>
    /// Returneaza o conexiune SQLite deschisa.
    /// Apelantul este responsabil sa o inchida (using).
    /// </summary>
    public SqliteConnection GetConnection()
    {
        var conn = new SqliteConnection(_connectionString);
        conn.Open();
        return conn;
    }

    private void InitSchema()
    {
        Logger.Info("Initialising database schema...");

        var ddl = new[]
        {
            """
            CREATE TABLE IF NOT EXISTS cashiers (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT    NOT NULL UNIQUE,
                password TEXT    NOT NULL,
                fullName TEXT    NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS matches (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                name           TEXT    NOT NULL,
                ticketPrice    REAL    NOT NULL,
                totalSeats     INTEGER NOT NULL,
                availableSeats INTEGER NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS customers (
                id      INTEGER PRIMARY KEY AUTOINCREMENT,
                name    TEXT NOT NULL,
                address TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS tickets (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                customerId    INTEGER NOT NULL REFERENCES customers(id),
                matchId       INTEGER NOT NULL REFERENCES matches(id),
                numberOfSeats INTEGER NOT NULL
            )
            """
        };

        using var conn = GetConnection();
        foreach (var sql in ddl)
        {
            using var cmd = new SqliteCommand(sql, conn);
            cmd.ExecuteNonQuery();
        }

        Logger.Info("Schema ready.");
    }
}
