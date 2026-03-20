using BasketballTickets.Domain;

namespace BasketballTickets.Service;

/// <summary>
/// Interfata business logic pentru sistemul de bilete de baschet.
/// Controllerele comunica DOAR prin aceasta interfata — niciodata direct cu repository-urile.
/// Echivalent cu Service.java
/// </summary>
public interface IService
{
    // ---- Autentificare ----

    /// <summary>
    /// Autentifica un casier. Returneaza null daca credentialele sunt gresite.
    /// </summary>
    Cashier? Login(string username, string password);

    // ---- Meciuri ----

    /// <summary>
    /// Returneaza toate meciurile cu locurile disponibile curente.
    /// </summary>
    IList<Match> GetAllMatches();

    // ---- Vanzare bilete ----

    /// <summary>
    /// Vinde bilete unui client pentru un meci dat.
    /// Creeaza clientul daca nu exista. Decrementeaza locurile disponibile.
    /// </summary>
    /// <exception cref="InvalidOperationException">Daca nu sunt suficiente locuri.</exception>
    Ticket SellTicket(string customerName, string customerAddress,
                      long matchId, int numberOfSeats);

    // ---- Cautare ----

    /// <summary>
    /// Gaseste toate biletele pentru clientii care corespund numelui si/sau adresei.
    /// </summary>
    IList<Ticket> SearchTickets(string? customerName, string? customerAddress);

    // ---- Modificare ----

    /// <summary>
    /// Modifica numarul de locuri pe un bilet existent.
    /// Ajusteaza locurile disponibile la meci corespunzator.
    /// </summary>
    /// <exception cref="InvalidOperationException">Daca nu sunt suficiente locuri pentru majorare.</exception>
    Ticket ModifyTicketSeats(long ticketId, int newNumberOfSeats);
}
