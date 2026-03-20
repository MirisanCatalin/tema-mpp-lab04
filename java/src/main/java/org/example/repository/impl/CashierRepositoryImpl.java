package org.example.repository.impl;

import org.example.domain.Cashier;
import org.example.repository.interfaces.CashierRepository;
import org.example.repository.abstracts.AbstractDBRepository;
import org.example.utils.DatabaseConfig;

import java.sql.*;
import java.util.Optional;
public class CashierRepositoryImpl
        extends AbstractDBRepository<Long, Cashier>
        implements CashierRepository {

    public CashierRepositoryImpl(DatabaseConfig dbConfig) {
        super(dbConfig);
    }

    @Override
    protected String getTableName() { return "cashiers"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO cashiers (username, password, fullName) VALUES (?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE cashiers SET username=?, password=?, fullName=? WHERE id=?";
    }

    @Override
    protected Cashier extractEntity(ResultSet rs) throws SQLException {
        return new Cashier(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("fullName")
        );
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Cashier c) throws SQLException {
        ps.setString(1, c.getUsername());
        ps.setString(2, c.getPassword());
        ps.setString(3, c.getFullName());
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Cashier c) throws SQLException {
        ps.setString(1, c.getUsername());
        ps.setString(2, c.getPassword());
        ps.setString(3, c.getFullName());
        ps.setLong(4, c.getId());
    }

    @Override
    protected void setGeneratedId(Cashier entity, long id) { entity.setId(id); }

    // ---- Domain-specific queries ----

    @Override
    public Optional<Cashier> findByUsernameAndPassword(String username, String password) {
        logger.debug("findByUsernameAndPassword({})", username);
        String sql = "SELECT * FROM cashiers WHERE username=? AND password=?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                logger.info("Login OK for username={}", username);
                return Optional.of(extractEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("findByUsernameAndPassword failed: {}", e.getMessage(), e);
        }
        logger.warn("Login FAILED for username={}", username);
        return Optional.empty();
    }

    @Override
    public Optional<Cashier> findByUsername(String username) {
        logger.debug("findByUsername({})", username);
        String sql = "SELECT * FROM cashiers WHERE username=?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(extractEntity(rs));
        } catch (SQLException e) {
            logger.error("findByUsername failed: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
}