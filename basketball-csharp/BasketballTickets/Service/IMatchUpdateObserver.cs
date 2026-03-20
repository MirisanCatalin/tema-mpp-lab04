using BasketballTickets.Domain;

namespace BasketballTickets.Service;

/// <summary>
/// Observer pentru update-uri in timp real ale meciurilor.
/// Implementat de controllere pentru a fi notificate cand alt casier
/// vinde sau modifica un bilet.
/// Echivalent cu MatchUpdateObserver.java
/// </summary>
public interface IMatchUpdateObserver
{
    void OnMatchUpdated(Match match);
}
