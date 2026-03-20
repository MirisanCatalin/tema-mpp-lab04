package org.example.repository.impl;

import org.example.domain.Customer;
import org.example.domain.Match;
import org.example.domain.Ticket;
import org.example.repository.abstracts.AbstractDBRepository;
import org.example.repository.interfaces.TicketRepository;
import org.example.utils.DatabaseConfig;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketRepositoryImpl
        extends AbstractDBRepository<Long, Ticket>
        implements TicketRepository {

    public TicketRepositoryImpl(DatabaseConfig dbConfig) {
        super(dbConfig);
    }

    @Override
    protected String getTableName() { return "tickets"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO tickets (customerId, matchId, numberOfSeats) VALUES (?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE tickets SET customerId=?, matchId=?, numberOfSeats=? WHERE id=?";
    }

    // Tickets need JOINs — override findById and findAll to use the JOIN query
    private static final String JOIN_SQL = """
            SELECT t.id, t.numberOfSeats,
                   c.id AS cId, c.name AS cName, c.address AS cAddress,
                   m.id AS mId, m.name AS mName, m.ticketPrice, m.totalSeats, m.availableSeats
            FROM tickets t
            JOIN customers c ON t.customerId = c.id
            JOIN matches   m ON t.matchId   = m.id
            """;

    @Override
    public Optional<Ticket> findById(Long id) {
        logger.debug("findById({})", id);
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(JOIN_SQL + " WHERE t.id=?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(extractEntity(rs));
        } catch (SQLException e) {
            logger.error("findById({}) failed: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ticket> findAll() {
        logger.debug("findAll()");
        return runJoinQuery(JOIN_SQL, List.of());
    }

    @Override
    protected Ticket extractEntity(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
                rs.getLong("cId"), rs.getString("cName"), rs.getString("cAddress"));
        Match match = new Match(
                rs.getLong("mId"), rs.getString("mName"),
                rs.getDouble("ticketPrice"), rs.getInt("totalSeats"), rs.getInt("availableSeats"));
        return new Ticket(rs.getLong("id"), customer, match, rs.getInt("numberOfSeats"));
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Ticket t) throws SQLException {
        ps.setLong(1, t.getCustomer().getId());
        ps.setLong(2, t.getMatch().getId());
        ps.setInt(3, t.getNumberOfSeats());
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Ticket t) throws SQLException {
        ps.setLong(1, t.getCustomer().getId());
        ps.setLong(2, t.getMatch().getId());
        ps.setInt(3, t.getNumberOfSeats());
        ps.setLong(4, t.getId());
    }

    @Override
    protected void setGeneratedId(Ticket entity, long id) { entity.setId(id); }

    // ---- Domain-specific queries ----

    @Override
    public List<Ticket> findByCustomerNameOrAddress(String name, String address) {
        logger.debug("findByCustomerNameOrAddress(name='{}', address='{}')", name, address);
        StringBuilder sql = new StringBuilder(JOIN_SQL + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(c.name) = LOWER(?)");
            params.add(name);
        }
        if (address != null && !address.isBlank()) {
            sql.append(" AND LOWER(c.address) LIKE LOWER(?)");
            params.add("%" + address + "%");
        }
        return runJoinQuery(sql.toString(), params);
    }

    @Override
    public List<Ticket> findByMatchId(Long matchId) {
        logger.debug("findByMatchId({})", matchId);
        return runJoinQuery(JOIN_SQL + " WHERE t.matchId=?", List.of(matchId));
    }

    @Override
    public List<Ticket> findByCustomerId(Long customerId) {
        logger.debug("findByCustomerId({})", customerId);
        return runJoinQuery(JOIN_SQL + " WHERE t.customerId=?", List.of(customerId));
    }

    // ---- Helper ----
    private List<Ticket> runJoinQuery(String sql, List<Object> params) {
        List<Ticket> result = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(extractEntity(rs));
        } catch (SQLException e) {
            logger.error("runJoinQuery failed: {}", e.getMessage(), e);
        }
        logger.debug("runJoinQuery → {} tickets", result.size());
        return result;
    }
}

