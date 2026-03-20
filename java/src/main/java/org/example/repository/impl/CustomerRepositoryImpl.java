package org.example.repository.impl;

import org.example.domain.Customer;
import org.example.repository.abstracts.AbstractDBRepository;
import org.example.repository.interfaces.CustomerRepository;
import org.example.utils.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepositoryImpl
        extends AbstractDBRepository<Long, Customer>
        implements CustomerRepository {

    public CustomerRepositoryImpl(DatabaseConfig dbConfig) {
        super(dbConfig);
    }

    @Override
    protected String getTableName() { return "customers"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO customers (name, address) VALUES (?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE customers SET name=?, address=? WHERE id=?";
    }

    @Override
    protected Customer extractEntity(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("address")
        );
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Customer c) throws SQLException {
        ps.setString(1, c.getName());
        ps.setString(2, c.getAddress());
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Customer c) throws SQLException {
        ps.setString(1, c.getName());
        ps.setString(2, c.getAddress());
        ps.setLong(3, c.getId());
    }

    @Override
    protected void setGeneratedId(Customer entity, long id) { entity.setId(id); }

    // ---- Domain-specific queries ----

    @Override
    public List<Customer> findByName(String name) {
        logger.debug("findByName('{}')", name);
        String sql = "SELECT * FROM customers WHERE LOWER(name) = LOWER(?)";
        return queryWithParam(sql, name);
    }

    @Override
    public List<Customer> findByAddress(String address) {
        logger.debug("findByAddress('{}')", address);
        String sql = "SELECT * FROM customers WHERE LOWER(address) LIKE LOWER(?)";
        return queryWithParam(sql, "%" + address + "%");
    }

    @Override
    public List<Customer> findByNameOrAddress(String name, String address) {
        logger.debug("findByNameOrAddress(name='{}', address='{}')", name, address);
        StringBuilder sql = new StringBuilder("SELECT * FROM customers WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(name) = LOWER(?)");
            params.add(name);
        }
        if (address != null && !address.isBlank()) {
            sql.append(" AND LOWER(address) LIKE LOWER(?)");
            params.add("%" + address + "%");
        }

        List<Customer> result = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(extractEntity(rs));
        } catch (SQLException e) {
            logger.error("findByNameOrAddress failed: {}", e.getMessage(), e);
        }
        logger.debug("findByNameOrAddress → {} results", result.size());
        return result;
    }

    // ---- Helper ----
    private List<Customer> queryWithParam(String sql, String param) {
        List<Customer> result = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(extractEntity(rs));
        } catch (SQLException e) {
            logger.error("queryWithParam failed: {}", e.getMessage(), e);
        }
        return result;
    }
}