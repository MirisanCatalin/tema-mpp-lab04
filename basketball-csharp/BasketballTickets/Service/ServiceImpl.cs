using BasketballTickets.Domain;
using BasketballTickets.Repository.Interfaces;
using log4net;

namespace BasketballTickets.Service;

/// <summary>
/// Implementeaza toata logica de business pentru sistemul de bilete de baschet.
/// Controllerele primesc o referinta la aceasta clasa — nu ating niciodata repository-urile direct.
/// Echivalent cu ServiceImpl.java
/// </summary>
public class ServiceImpl : IService
{
    private static readonly ILog Logger = LogManager.GetLogger(typeof(ServiceImpl));

    private readonly ICashierRepository _cashierRepo;
    private readonly IMatchRepository _matchRepo;
    private readonly ICustomerRepository _customerRepo;
    private readonly ITicketRepository _ticketRepo;

    // Lista de observatori notificati cand datele unui meci se schimba
    // (pentru update-uri multi-client in timp real)
    private readonly List<IMatchUpdateObserver> _observers = [];

    public ServiceImpl(ICashierRepository cashierRepo,
                       IMatchRepository matchRepo,
                       ICustomerRepository customerRepo,
                       ITicketRepository ticketRepo)
    {
        _cashierRepo  = cashierRepo;
        _matchRepo    = matchRepo;
        _customerRepo = customerRepo;
        _ticketRepo   = ticketRepo;
        Logger.Info("ServiceImpl created");
    }

    // ---- Suport Observer (pentru networking/multi-client in viitor) ----

    public void AddObserver(IMatchUpdateObserver observer)    => _observers.Add(observer);
    public void RemoveObserver(IMatchUpdateObserver observer) => _observers.Remove(observer);

    private void NotifyMatchUpdated(Match match) =>
        _observers.ForEach(o => o.OnMatchUpdated(match));

    // ---- Autentificare ----

    public Cashier? Login(string username, string password)
    {
        Logger.Debug($"Login attempt: username={username}");
        return _cashierRepo.FindByUsernameAndPassword(username, password);
    }

    // ---- Meciuri ----

    public IList<Match> GetAllMatches()
    {
        Logger.Debug("GetAllMatches()");
        return _matchRepo.FindAll();
    }

    // ---- Vanzare bilete ----

    public Ticket SellTicket(string customerName, string customerAddress,
                             long matchId, int numberOfSeats)
    {
        Logger.Debug($"SellTicket: customer='{customerName}', matchId={matchId}, seats={numberOfSeats}");

        // 1. Valideaza meciul si locurile disponibile
        var match = _matchRepo.FindById(matchId)
            ?? throw new InvalidOperationException($"Match not found: {matchId}");

        if (match.AvailableSeats < numberOfSeats)
        {
            Logger.Warn($"Not enough seats for matchId={matchId}: " +
                        $"requested={numberOfSeats}, available={match.AvailableSeats}");
            throw new InvalidOperationException(
                $"Not enough seats available. Requested: {numberOfSeats}, " +
                $"Available: {match.AvailableSeats}");
        }

        // 2. Gaseste sau creeaza clientul
        var existing = _customerRepo.FindByNameOrAddress(customerName, customerAddress);
        Customer customer;
        if (existing.Count == 0)
        {
            customer = _customerRepo.Save(new Customer(0, customerName, customerAddress));
            Logger.Info($"New customer created: {customer}");
        }
        else
        {
            customer = existing[0];
            Logger.Debug($"Existing customer found: {customer}");
        }

        // 3. Salveaza biletul
        var ticket = _ticketRepo.Save(new Ticket(0, customer, match, numberOfSeats));

        // 4. Decrementeaza locurile disponibile
        var updated = _matchRepo.UpdateAvailableSeats(matchId, -numberOfSeats);

        // 5. Notifica toti clientii conectati (Observer pattern)
        NotifyMatchUpdated(updated);

        Logger.Info($"Ticket sold: {ticket}");
        return ticket;
    }

    // ---- Cautare ----

    public IList<Ticket> SearchTickets(string? customerName, string? customerAddress)
    {
        Logger.Debug($"SearchTickets(name='{customerName}', address='{customerAddress}')");
        return _ticketRepo.FindByCustomerNameOrAddress(customerName, customerAddress);
    }

    // ---- Modificare ----

    public Ticket ModifyTicketSeats(long ticketId, int newNumberOfSeats)
    {
        Logger.Debug($"ModifyTicketSeats(ticketId={ticketId}, newSeats={newNumberOfSeats})");

        var ticket = _ticketRepo.FindById(ticketId)
            ?? throw new InvalidOperationException($"Ticket not found: {ticketId}");

        var oldSeats = ticket.NumberOfSeats;
        var delta    = oldSeats - newNumberOfSeats; // pozitiv = eliberam locuri, negativ = avem nevoie de mai multe

        // Daca avem nevoie de mai multe locuri, validam disponibilitatea
        if (delta < 0)
        {
            var match = _matchRepo.FindById(ticket.Match.Id)
                ?? throw new InvalidOperationException($"Match not found: {ticket.Match.Id}");

            if (match.AvailableSeats < -delta)
            {
                Logger.Warn($"Cannot increase seats for ticketId={ticketId}: " +
                            $"need {-delta} more, only {match.AvailableSeats} available");
                throw new InvalidOperationException(
                    $"Not enough seats available. Need {-delta} more, " +
                    $"but only {match.AvailableSeats} available.");
            }
        }

        ticket.NumberOfSeats = newNumberOfSeats;
        _ticketRepo.Update(ticket);

        var updated = _matchRepo.UpdateAvailableSeats(ticket.Match.Id, delta);
        NotifyMatchUpdated(updated);

        Logger.Info($"Ticket {ticketId} modified: {oldSeats} → {newNumberOfSeats} seats");
        return ticket;
    }
}
