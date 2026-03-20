using BasketballTickets.Repository.Interfaces;
using BasketballTickets.Utils;
using log4net;
using Microsoft.Data.Sqlite;

namespace BasketballTickets.Repository.Abstracts;

/// <summary>
/// Clasa abstracta de baza pentru toate repository-urile SQLite.
/// Echivalent cu AbstractDBRepository.java — logica CRUD comuna e implementata
/// o singura data aici; subclasele implementeaza doar metodele specifice entitatii.
/// </summary>
public abstract class AbstractDBRepository<TId, TEntity> : IRepository<TId, TEntity>
{
    protected readonly ILog Logger;
    protected readonly DatabaseConfig DbConfig;

    protected AbstractDBRepository(DatabaseConfig dbConfig)
    {
        DbConfig = dbConfig;
        Logger = LogManager.GetLogger(GetType());
    }

    // -------------------------------------------------------------------------
    // Contract abstract — subclasele trebuie sa implementeze acestea
    // -------------------------------------------------------------------------

    protected abstract string GetTableName();
    protected abstract string GetInsertSql();
    protected abstract string GetUpdateSql();
    protected abstract TEntity ExtractEntity(SqliteDataReader reader);
    protected abstract void BindInsert(SqliteCommand cmd, TEntity entity);
    protected abstract void BindUpdate(SqliteCommand cmd, TEntity entity);

    /// <summary>
    /// Apelat dupa INSERT pentru a seta ID-ul generat pe entitate.
    /// </summary>
    protected virtual void SetGeneratedId(TEntity entity, long generatedId) { }

    // -------------------------------------------------------------------------
    // CRUD generic — implementat o singura data pentru toate repository-urile
    // -------------------------------------------------------------------------

    public virtual TEntity? FindById(TId id)
    {
        Logger.Debug($"FindById({id}) in {GetTableName()}");
        var sql = $"SELECT * FROM {GetTableName()} WHERE id = @id";

        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        cmd.Parameters.AddWithValue("@id", id);

        using var reader = cmd.ExecuteReader();
        if (reader.Read())
        {
            var entity = ExtractEntity(reader);
            Logger.Debug($"FindById({id}) → {entity}");
            return entity;
        }

        Logger.Debug($"FindById({id}): not found in {GetTableName()}");
        return default;
    }

    public virtual IList<TEntity> FindAll()
    {
        Logger.Debug($"FindAll() in {GetTableName()}");
        var sql = $"SELECT * FROM {GetTableName()}";
        var results = new List<TEntity>();

        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        using var reader = cmd.ExecuteReader();

        while (reader.Read())
            results.Add(ExtractEntity(reader));

        Logger.Debug($"FindAll() → {results.Count} rows from {GetTableName()}");
        return results;
    }

    public virtual TEntity Save(TEntity entity)
    {
        Logger.Debug($"Save() in {GetTableName()}: {entity}");

        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(GetInsertSql(), conn);
        BindInsert(cmd, entity);
        cmd.ExecuteNonQuery();

        // Obtine ID-ul generat
        using var idCmd = new SqliteCommand("SELECT last_insert_rowid()", conn);
        var generatedId = (long)(idCmd.ExecuteScalar() ?? 0L);
        SetGeneratedId(entity, generatedId);

        Logger.Info($"Save() succeeded in {GetTableName()}: {entity}");
        return entity;
    }

    public virtual TEntity Update(TEntity entity)
    {
        Logger.Debug($"Update() in {GetTableName()}: {entity}");

        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(GetUpdateSql(), conn);
        BindUpdate(cmd, entity);
        var rows = cmd.ExecuteNonQuery();

        Logger.Info($"Update() succeeded in {GetTableName()} (rows={rows}): {entity}");
        return entity;
    }

    public virtual void Delete(TId id)
    {
        Logger.Debug($"Delete({id}) in {GetTableName()}");
        var sql = $"DELETE FROM {GetTableName()} WHERE id = @id";

        using var conn = DbConfig.GetConnection();
        using var cmd = new SqliteCommand(sql, conn);
        cmd.Parameters.AddWithValue("@id", id);
        var rows = cmd.ExecuteNonQuery();

        Logger.Info($"Delete({id}) succeeded in {GetTableName()} (rows={rows})");
    }
}
