package org.example.repository.impl;

import org.example.domain.Match;
import org.example.repository.abstracts.AbstractDBRepository;
import org.example.repository.interfaces.MatchRepository;
import org.example.utils.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MatchRepositoryImpl
        extends AbstractDBRepository<Long, Match>
        implements MatchRepository {

    public MatchRepositoryImpl(DatabaseConfig dbConfig) {
        super(dbConfig);
    }

    @Override
    protected String getTableName() { return "matches"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO matches (name, ticketPrice, totalSeats, availableSeats) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE matches SET name=?, ticketPrice=?, totalSeats=?, availableSeats=? WHERE id=?";
    }

    @Override
    protected Match extractEntity(ResultSet rs) throws SQLException {
        return new Match(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("ticketPrice"),
                rs.getInt("totalSeats"),
                rs.getInt("availableSeats")
        );
    }

    @Override
    protected void bindInsert(PreparedStatement ps, Match m) throws SQLException {
        ps.setString(1, m.getName());
        ps.setDouble(2, m.getTicketPrice());
        ps.setInt(3, m.getTotalSeats());
        ps.setInt(4, m.getAvailableSeats());
    }

    @Override
    protected void bindUpdate(PreparedStatement ps, Match m) throws SQLException {
        ps.setString(1, m.getName());
        ps.setDouble(2, m.getTicketPrice());
        ps.setInt(3, m.getTotalSeats());
        ps.setInt(4, m.getAvailableSeats());
        ps.setLong(5, m.getId());
    }

    @Override
    protected void setGeneratedId(Match entity, long id) { entity.setId(id); }

    // ---- Domain-specific queries ----

    @Override
    public List<Match> findAvailableMatches() {
        logger.debug("findAvailableMatches()");
        return queryList("SELECT * FROM matches WHERE availableSeats > 0 ORDER BY id");
    }

    @Override
    public List<Match> findSoldOutMatches() {
        logger.debug("findSoldOutMatches()");
        return queryList("SELECT * FROM matches WHERE availableSeats <= 0 ORDER BY id");
    }

    @Override
    public Match updateAvailableSeats(Long matchId, int seatsDelta) {
        logger.debug("updateAvailableSeats(matchId={}, delta={})", matchId, seatsDelta);
        String sql = "UPDATE matches SET availableSeats = availableSeats + ? WHERE id=?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatsDelta);
            ps.setLong(2, matchId);
            ps.executeUpdate();
            logger.info("availableSeats updated for matchId={} by {}", matchId, seatsDelta);
        } catch (SQLException e) {
            logger.error("updateAvailableSeats failed: {}", e.getMessage(), e);
            throw new RuntimeException("updateAvailableSeats failed", e);
        }
        return findById(matchId).orElseThrow();
    }

    // ---- Helper ----
    private List<Match> queryList(String sql) {
        List<Match> result = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(extractEntity(rs));
        } catch (SQLException e) {
            logger.error("queryList failed: {}", e.getMessage(), e);
        }
        return result;
    }
}