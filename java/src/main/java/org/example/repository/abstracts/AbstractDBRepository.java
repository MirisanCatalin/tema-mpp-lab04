package org.example.repository.abstracts;

import org.example.repository.interfaces.Repository;
import org.example.utils.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base for all SQLite repositories.
 *
 * Subclasses only need to implement:
 *  - extractEntity(ResultSet)  → how to build a domain object from a row
 *  - getTableName()            → the SQL table name
 *  - getInsertSql()            → INSERT statement with ? placeholders
 *  - getUpdateSql()            → UPDATE statement with ? placeholders (last ? is id)
 *  - bindInsert(PreparedStatement, T)
 *  - bindUpdate(PreparedStatement, T)
 *
 * findById / findAll / delete are handled here automatically.
 */
public abstract class AbstractDBRepository<ID, T> implements Repository<ID, T> {

    protected final Logger logger = LogManager.getLogger(getClass());
    protected final DatabaseConfig dbConfig;

    protected AbstractDBRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    // -------------------------------------------------------------------------
    // Abstract contract — subclasses must implement these
    // -------------------------------------------------------------------------

    protected abstract String getTableName();
    protected abstract String getInsertSql();
    protected abstract String getUpdateSql();
    protected abstract T extractEntity(ResultSet rs) throws SQLException;
    protected abstract void bindInsert(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void bindUpdate(PreparedStatement ps, T entity) throws SQLException;

    // -------------------------------------------------------------------------
    // Generic CRUD — implemented once here for all repositories
    // -------------------------------------------------------------------------

    @Override
    public Optional<T> findById(ID id) {
        logger.debug("findById({}) in {}", id, getTableName());
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                T entity = extractEntity(rs);
                logger.debug("findById({}) → {}", id, entity);
                return Optional.of(entity);
            }
        } catch (SQLException e) {
            logger.error("findById({}) failed: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<T> findAll() {
        logger.debug("findAll() in {}", getTableName());
        String sql = "SELECT * FROM " + getTableName();
        List<T> results = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) results.add(extractEntity(rs));
        } catch (SQLException e) {
            logger.error("findAll() failed in {}: {}", getTableName(), e.getMessage(), e);
        }
        logger.debug("findAll() → {} rows from {}", results.size(), getTableName());
        return results;
    }

    @Override
    public T save(T entity) {
        logger.debug("save() in {}: {}", getTableName(), entity);
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            bindInsert(ps, entity);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                setGeneratedId(entity, keys.getLong(1));
            }
            logger.info("save() succeeded in {}: {}", getTableName(), entity);
        } catch (SQLException e) {
            logger.error("save() failed in {}: {}", getTableName(), e.getMessage(), e);
            throw new RuntimeException("Save failed for " + getTableName(), e);
        }
        return entity;
    }

    @Override
    public T update(T entity) {
        logger.debug("update() in {}: {}", getTableName(), entity);
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            bindUpdate(ps, entity);
            int rows = ps.executeUpdate();
            logger.info("update() succeeded in {} (rows={}): {}", getTableName(), rows, entity);
        } catch (SQLException e) {
            logger.error("update() failed in {}: {}", getTableName(), e.getMessage(), e);
            throw new RuntimeException("Update failed for " + getTableName(), e);
        }
        return entity;
    }

    @Override
    public void delete(ID id) {
        logger.debug("delete({}) in {}", id, getTableName());
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            int rows = ps.executeUpdate();
            logger.info("delete({}) succeeded in {} (rows={})", id, getTableName(), rows);
        } catch (SQLException e) {
            logger.error("delete({}) failed in {}: {}", id, getTableName(), e.getMessage(), e);
            throw new RuntimeException("Delete failed for " + getTableName(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Helper — subclasses override this if they need to set generated ID
    // -------------------------------------------------------------------------

    /**
     * Called after INSERT to set the auto-generated ID on the entity.
     * Subclasses must override this to cast and set the ID appropriately.
     */
    protected void setGeneratedId(T entity, long generatedId) {
        // Default no-op; override in each impl
    }
}