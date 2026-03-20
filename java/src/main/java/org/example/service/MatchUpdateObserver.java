package org.example.service;

import org.example.domain.Match;

/**
 * Observer interface — implemented by controllers to receive real-time
 * match updates when any cashier sells or modifies a ticket.
 *
 * This is the hook that makes multi-client networking easy to add:
 * a network proxy can implement this interface and push updates to
 * remote clients via sockets/RMI/REST without changing the service layer.
 */
public interface MatchUpdateObserver {
    void onMatchUpdated(Match match);
}
