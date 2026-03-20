using BasketballTickets.Controller;
using BasketballTickets.Domain;
using BasketballTickets.Repository.Impl;
using BasketballTickets.Service;
using BasketballTickets.Utils;
using log4net;
using log4net.Config;

// Initializeaza log4net din fisierul de configurare
var logConfig = new FileInfo(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "configs", "log4net.config"));
XmlConfigurator.Configure(logConfig);

var log = LogManager.GetLogger(typeof(Program));
log.Info("=== Basketball Ticket System starting ===");

// 1. Infrastructura
var dbConfig = new DatabaseConfig();

// 2. Repository-uri
var cashierRepo  = new CashierRepositoryImpl(dbConfig);
var matchRepo    = new MatchRepositoryImpl(dbConfig);
var customerRepo = new CustomerRepositoryImpl(dbConfig);
var ticketRepo   = new TicketRepositoryImpl(dbConfig);

// 3. Service (o singura instanta, partajata de toti controllerii)
var service = new ServiceImpl(cashierRepo, matchRepo, customerRepo, ticketRepo);

// 4. Date initiale (doar daca BD e goala)
SeedData(cashierRepo, matchRepo);

// ---- Demo flow ----

// Login
var loginCtrl = new LoginController(service);
var cashier = loginCtrl.HandleLogin("ionescu", "pass123");

if (cashier is null)
{
    log.Error("Login failed — stopping demo");
    return;
}

// Deschide dashboard
var dashboard = new CashierDashboardController(service, cashier);

// Afiseaza meciurile
var matches = dashboard.LoadMatches();
log.Info($"Matches loaded: {matches.Count}");
foreach (var m in matches)
    log.Info($"  {m.Name} | {m.AvailableSeats}/{m.TotalSeats} locuri | {m.TicketPrice} RON{(m.IsSoldOut ? " [SOLD OUT]" : "")}");

// Vinde un bilet
var ticket = dashboard.HandleSellTicket("Maria Popescu", "Str. Florilor 5, Cluj", matches[0].Id, 3);
log.Info($"Sold: {ticket}");

// Cauta dupa nume
var found = dashboard.HandleSearch("Maria Popescu", null);
log.Info($"Search results: {found.Count}");
foreach (var t in found)
    log.Info($"  {t.Customer.Name} | {t.Match.Name} | {t.NumberOfSeats} locuri");

// Modifica biletul
var modified = dashboard.HandleModifySeats(ticket.Id, 5);
log.Info($"Modified ticket: {modified}");

// Logout
dashboard.HandleLogout();

log.Info("=== Demo complete ===");

// ---- Seed data ----
static void SeedData(CashierRepositoryImpl cashierRepo, MatchRepositoryImpl matchRepo)
{
    if (cashierRepo.FindAll().Count == 0)
    {
        cashierRepo.Save(new Cashier(0, "ionescu", "pass123", "Ion Ionescu"));
        cashierRepo.Save(new Cashier(0, "popescu", "pass456", "Ana Popescu"));
        LogManager.GetLogger(typeof(Program)).Info("Seeded cashiers");
    }

    if (matchRepo.FindAll().Count == 0)
    {
        matchRepo.Save(new Match(0, "Steaua vs Dinamo",   50.0, 500, 500));
        matchRepo.Save(new Match(0, "Rapid vs CFR Cluj",  40.0, 300, 300));
        matchRepo.Save(new Match(0, "Semifinala 1",       75.0, 200, 200));
        matchRepo.Save(new Match(0, "Semifinala 2",       75.0, 200, 200));
        matchRepo.Save(new Match(0, "Finala",            100.0, 100, 100));
        LogManager.GetLogger(typeof(Program)).Info("Seeded matches");
    }
}
