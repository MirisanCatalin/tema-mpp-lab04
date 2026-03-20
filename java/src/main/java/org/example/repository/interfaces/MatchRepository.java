package org.example.repository.interfaces;

import org.example.domain.Match;

import java.util.List;

public interface MatchRepository extends Repository<Long, Match> {
    List<Match> findAvailableMatches();
    List<Match> findSoldOutMatches();
    Match updateAvailableSeats(Long matchId, int seatsDelta);
}
