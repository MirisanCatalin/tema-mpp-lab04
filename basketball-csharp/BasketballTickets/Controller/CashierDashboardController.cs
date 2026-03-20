using BasketballTickets.Domain;
using BasketballTickets.Service;
using log4net;

namespace BasketballTickets.Controller;

/// <summary>
/// Gestioneaza logica ecranului principal al casierului.
/// Implementeaza IMatchUpdateObserver pentru a primi notificari in timp real
/// cand alt casier vinde/modifica un bilet.
/// Echivalent cu CashierDashboardController.java
/// </summary>
public class CashierDashboardController : IMatchUpdateObserver
{
    private static readonly ILog Logger = LogManager.GetLogger(typeof(CashierDashboardController));

    private readonly IService _service;
    private readonly Cashier _loggedInCashier;

    public CashierDashboardController(IService service, Cashier loggedInCashier)
    {
        _service = service;
        _loggedInCashier = loggedInCashier;

        // Inregistreaza acest controller ca observer
        if (_service is ServiceImpl si)
            si.AddObserver(this);

        Logger.Info($"Dashboard opened for cashier: {_loggedInCashier.Username}");
    }

    // ---- Incarcare date initiale ----

    public IList<Match> LoadMatches()
    {
        Logger.Debug("LoadMatches()");
        return _service.GetAllMatches();
    }

    // ---- Vanzare bilet ----

    public Ticket HandleSellTicket(string customerName, string customerAddress,
                                   long matchId, int numberOfSeats)
    {
        Logger.Debug($"HandleSellTicket: customer='{customerName}', matchId={matchId}, seats={numberOfSeats}");
        return _service.SellTicket(customerName, customerAddress, matchId, numberOfSeats);
    }

    // ---- Cautare ----

    public IList<Ticket> HandleSearch(string? name, string? address)
    {
        Logger.Debug($"HandleSearch: name='{name}', address='{address}'");
        return _service.SearchTickets(name, address);
    }

    // ---- Modificare ----

    public Ticket HandleModifySeats(long ticketId, int newSeats)
    {
        Logger.Debug($"HandleModifySeats: ticketId={ticketId}, newSeats={newSeats}");
        return _service.ModifyTicketSeats(ticketId, newSeats);
    }

    // ---- Logout ----

    public void HandleLogout()
    {
        Logger.Info($"Cashier {_loggedInCashier.Username} logged out");

        if (_service is ServiceImpl si)
            si.RemoveObserver(this);

        // In WinForms/WPF: inchide aceasta fereastra, redeschide Login
    }

    // ---- Observer callback ----

    /// <summary>
    /// Apelat automat de ServiceImpl cand orice meci se schimba
    /// (alt casier a vandut/modificat un bilet).
    /// In WinForms: Invoke(() => RefreshMatchRow(match))
    /// In WPF:      Dispatcher.Invoke(() => RefreshMatchRow(match))
    /// </summary>
    public void OnMatchUpdated(Match match)
    {
        Logger.Info($"Match updated notification received: {match}");
        // Aici se implementeaza: afiseaza SOLD OUT cu rosu daca match.IsSoldOut
        // In WinForms: this.Invoke(() => UpdateMatchInGrid(match));
        // In WPF:      Application.Current.Dispatcher.Invoke(() => UpdateMatchInGrid(match));
    }
}
