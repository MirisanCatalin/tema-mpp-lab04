# Basketball Ticket System – C# / SQLite
### Arhitectura identica cu versiunea Java (lab04 pattern)

---

## Structura proiectului

```
BasketballTickets/
├── BasketballTickets.csproj
├── Program.cs                         ← entry point, wireing
├── configs/
│   ├── config.properties              ← connection string SQLite
│   └── log4net.config                 ← configurare logging
├── logs/
│   └── app.log
├── Domain/
│   ├── Entity.cs                      ← clasa de baza generica
│   ├── Cashier.cs
│   ├── Match.cs
│   ├── Customer.cs
│   └── Ticket.cs
├── Repository/
│   ├── Interfaces/
│   │   ├── IRepository.cs             ← interfata CRUD generica
│   │   ├── ICashierRepository.cs
│   │   ├── IMatchRepository.cs
│   │   ├── ICustomerRepository.cs
│   │   └── ITicketRepository.cs
│   ├── Abstracts/
│   │   └── AbstractDBRepository.cs   ★ inima arhitecturii
│   └── Impl/
│       ├── CashierRepositoryImpl.cs
│       ├── MatchRepositoryImpl.cs
│       ├── CustomerRepositoryImpl.cs
│       └── TicketRepositoryImpl.cs
├── Service/
│   ├── IService.cs
│   ├── ServiceImpl.cs
│   └── IMatchUpdateObserver.cs
├── Controller/
│   ├── LoginController.cs
│   └── CashierDashboardController.cs
└── Utils/
    └── DatabaseConfig.cs
```

---

## Corespondenta Java ↔ C#

| Java                        | C#                              |
|-----------------------------|---------------------------------|
| `Entity<ID>`                | `Entity<TId>`                   |
| `Repository<ID,T>`          | `IRepository<TId, TEntity>`     |
| `AbstractDBRepository`      | `AbstractDBRepository`          |
| `DatabaseConfig`            | `DatabaseConfig`                |
| `ServiceImpl`               | `ServiceImpl`                   |
| `MatchUpdateObserver`       | `IMatchUpdateObserver`          |
| `Optional<T>`               | nullable `T?`                   |
| `ResultSet`                 | `SqliteDataReader`              |
| `PreparedStatement`         | `SqliteCommand` cu `@parametri` |
| `DriverManager.getConnection` | `new SqliteConnection(...)`   |
| Log4j2                      | log4net                         |
| Gradle                      | .csproj / dotnet CLI            |

---

## Rulare

```bash
# Din folderul BasketballTickets/
dotnet run

# Sau din folderul solutiei
dotnet run --project BasketballTickets/BasketballTickets.csproj
```

Baza de date `basketball.db` se creeaza automat la primul run.

## Dependente NuGet

| Pachet                  | Versiune | Scop              |
|-------------------------|----------|-------------------|
| Microsoft.Data.Sqlite   | 8.0.0    | Driver SQLite     |
| log4net                 | 2.0.15   | Jurnalizare       |
