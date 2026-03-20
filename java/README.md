# Basketball Ticket System - Documentation

A Java-based ticket management system for basketball matches. This application demonstrates a layered architecture with Repository, Service, and Controller patterns, using SQLite for data persistence.

## Table of Contents
1. [Project Structure](#project-structure)
2. [Domain Layer](#domain-layer)
3. [Repository Layer](#repository-layer)
4. [Service Layer](#service-layer)
5. [Controller Layer](#controller-layer)
6. [Utilities](#utilities)
7. [Configuration](#configuration)
8. [Running the Application](#running-the-application)

---

## Project Structure

```
src/main/java/org/example/
├── MainApp.java                    # Application entry point
├── domain/                         # Domain entities
├── repository/
│   ├── abstracts/                  # Abstract base repository
│   ├── impl/                       # Repository implementations
│   └── interfaces/                # Repository interfaces
├── service/                        # Business logic
├── controller/                     # UI controllers
└── utils/                         # Utility classes
```

---

## Domain Layer

### [`Entity.java`](src/main/java/org/example/domain/Entity.java)
**Purpose:** Abstract base class for all domain entities.

**Functions:**
- `getId()` - Returns the entity's ID
- `setId(ID id)` - Sets the entity's ID
- Implements `Serializable` for object serialization

---

### [`Cashier.java`](src/main/java/org/example/domain/Cashier.java)
**Purpose:** Represents a cashier (ticket seller) in the system.

**Fields:**
- `id` - Unique identifier (Long)
- `username` - Login username
- `password` - Login password
- `fullName` - Full name of the cashier

**Functions:**
- Getters and setters for all fields
- `toString()` - Returns string representation

---

### [`Customer.java`](src/main/java/org/example/domain/Customer.java)
**Purpose:** Represents a customer who buys tickets.

**Fields:**
- `id` - Unique identifier (Long)
- `name` - Customer's name
- `address` - Customer's address

**Functions:**
- Getters and setters for all fields
- `toString()` - Returns string representation

---

### [`Match.java`](src/main/java/org/example/domain/Match.java)
**Purpose:** Represents a basketball match/event.

**Fields:**
- `id` - Unique identifier (Long)
- `name` - Match name (e.g., "Steaua vs Dinamo")
- `ticketPrice` - Price per seat (double)
- `totalSeats` - Total available seats
- `availableSeats` - Currently available seats

**Functions:**
- `isSoldOut()` - Returns true if no seats available
- Getters and setters for all fields
- `toString()` - Returns string representation

---

### [`Ticket.java`](src/main/java/org/example/domain/Ticket.java)
**Purpose:** Represents a ticket sold to a customer for a specific match.

**Fields:**
- `id` - Unique identifier (Long)
- `customer` - The customer who bought the ticket
- `match` - The match the ticket is for
- `numberOfSeats` - Number of seats purchased

**Functions:**
- Getters and setters for all fields
- `toString()` - Returns string representation

---

## Repository Layer

### Repository Interfaces

#### [`Repository.java`](src/main/java/org/example/repository/interfaces/Repository.java)
**Purpose:** Generic repository interface for CRUD operations.

**Functions:**
- `findById(ID id)` - Find entity by ID, returns Optional
- `findAll()` - Find all entities, returns List
- `save(T entity)` - Save new entity
- `update(T entity)` - Update existing entity
- `delete(ID id)` - Delete entity by ID

---

#### [`CashierRepository.java`](src/main/java/org/example/repository/interfaces/CashierRepository.java)
**Purpose:** Repository interface for Cashier entity.

**Additional Functions:**
- `findByUsernameAndPassword(String username, String password)` - Authenticate cashier
- `findByUsername(String username)` - Find cashier by username

---

#### [`CustomerRepository.java`](src/main/java/org/example/repository/interfaces/CustomerRepository.java)
**Purpose:** Repository interface for Customer entity.

**Additional Functions:**
- `findByName(String name)` - Find customers by name
- `findByAddress(String address)` - Find customers by address
- `findByNameOrAddress(String name, String address)` - Search by name or address

---

#### [`MatchRepository.java`](src/main/java/org/example/repository/interfaces/MatchRepository.java)
**Purpose:** Repository interface for Match entity.

**Additional Functions:**
- `findAvailableMatches()` - Find matches with available seats
- `findSoldOutMatches()` - Find sold-out matches
- `updateAvailableSeats(Long matchId, int seatsDelta)` - Update seat count

---

#### [`TicketRepository.java`](src/main/java/org/example/repository/interfaces/TicketRepository.java)
**Purpose:** Repository interface for Ticket entity.

**Additional Functions:**
- `findByCustomerNameOrAddress(String name, String address)` - Search tickets by customer
- `findByMatchId(Long matchId)` - Find tickets for a specific match
- `findByCustomerId(Long customerId)` - Find tickets for a specific customer

---

### Abstract Repository

#### [`AbstractDBRepository.java`](src/main/java/org/example/repository/abstracts/AbstractDBRepository.java)
**Purpose:** Abstract base class for all SQLite repositories. Implements common CRUD operations.

**Abstract Methods (must be implemented by subclasses):**
- `getTableName()` - Returns the SQL table name
- `getInsertSql()` - Returns INSERT SQL statement
- `getUpdateSql()` - Returns UPDATE SQL statement
- `extractEntity(ResultSet rs)` - Extract entity from ResultSet
- `bindInsert(PreparedStatement ps, T entity)` - Bind parameters for INSERT
- `bindUpdate(PreparedStatement ps, T entity)` - Bind parameters for UPDATE

**Implemented Functions:**
- `findById(ID id)` - Find entity by ID using generic SQL
- `findAll()` - Find all entities
- `save(T entity)` - Save entity and set generated ID
- `update(T entity)` - Update existing entity
- `delete(ID id)` - Delete entity by ID
- `setGeneratedId(T entity, long generatedId)` - Hook for setting auto-generated IDs

---

### Repository Implementations

#### [`CashierRepositoryImpl.java`](src/main/java/org/example/repository/impl/CashierRepositoryImpl.java)
**Purpose:** SQLite implementation of CashierRepository.

**Functions:**
- Implements all abstract methods from AbstractDBRepository
- `findByUsernameAndPassword()` - Custom query for authentication
- `findByUsername()` - Custom query to check username existence

---

#### [`CustomerRepositoryImpl.java`](src/main/java/org/example/repository/impl/CustomerRepositoryImpl.java)
**Purpose:** SQLite implementation of CustomerRepository.

**Functions:**
- Implements all abstract methods from AbstractDBRepository
- `findByName()` - Search customers by exact name (case-insensitive)
- `findByAddress()` - Search customers by address (partial match)
- `findByNameOrAddress()` - Combined search with optional parameters

---

#### [`MatchRepositoryImpl.java`](src/main/java/org/example/repository/impl/MatchRepositoryImpl.java)
**Purpose:** SQLite implementation of MatchRepository.

**Functions:**
- Implements all abstract methods from AbstractDBRepository
- `findAvailableMatches()` - Query matches where availableSeats > 0
- `findSoldOutMatches()` - Query matches where availableSeats <= 0
- `updateAvailableSeats()` - Atomically update available seats (positive = add, negative = remove)

---

#### [`TicketRepositoryImpl.java`](src/main/java/org/example/repository/impl/TicketRepositoryImpl.java)
**Purpose:** SQLite implementation of TicketRepository with JOIN queries.

**Functions:**
- Overrides `findById()` and `findAll()` to use JOIN queries
- `extractEntity()` - Extracts Ticket with joined Customer and Match data
- `findByCustomerNameOrAddress()` - Search tickets by customer criteria
- `findByMatchId()` - Get all tickets for a match
- `findByCustomerId()` - Get all tickets for a customer

---

## Service Layer

### [`Service.java`](src/main/java/org/example/service/Service.java)
**Purpose:** Interface defining business logic operations.

**Functions:**
- `login(String username, String password)` - Authenticate cashier
- `getAllMatches()` - Get all matches
- `sellTicket()` - Sell tickets to a customer
- `searchTickets()` - Search tickets by customer info
- `modifyTicketSeats()` - Change number of seats on a ticket

---

### [`ServiceImpl.java`](src/main/java/org/example/service/ServiceImpl.java)
**Purpose:** Implementation of business logic with transaction management.

**Fields:**
- `cashierRepo` - Cashier repository
- `matchRepo` - Match repository
- `customerRepo` - Customer repository
- `ticketRepo` - Ticket repository
- `observers` - List of MatchUpdateObserver for real-time updates

**Functions:**
- `login()` - Delegates to cashier repository
- `getAllMatches()` - Returns all matches
- `sellTicket()` - Core ticket sales logic:
  1. Validates match exists and has enough seats
  2. Finds or creates customer
  3. Saves ticket
  4. Decrements available seats
  5. Notifies observers of update
- `searchTickets()` - Delegates to ticket repository
- `modifyTicketSeats()` - Modifies ticket:
  1. Validates ticket exists
  2. Checks seat availability if increasing
  3. Updates ticket
  4. Adjusts match seats accordingly
  5. Notifies observers
- Observer management: `addObserver()`, `removeObserver()`, `notifyMatchUpdated()`

---

### [`MatchUpdateObserver.java`](src/main/java/org/example/service/MatchUpdateObserver.java)
**Purpose:** Observer interface for real-time match updates.

**Functions:**
- `onMatchUpdated(Match match)` - Called when a match is updated (e.g., tickets sold)

---

## Controller Layer

### [`LoginController.java`](src/main/java/org/example/controller/LoginController.java)
**Purpose:** Handles login screen logic.

**Fields:**
- `service` - Reference to Service layer

**Functions:**
- `handleLogin(String username, String password)` - Validates credentials and returns authenticated Cashier

---

### [`CashierDashboardController.java`](src/main/java/org/example/controller/CashierDashboardController.java)
**Purpose:** Handles cashier dashboard operations. Implements MatchUpdateObserver for real-time updates.

**Fields:**
- `service` - Reference to Service layer
- `loggedInCashier` - Currently logged in cashier

**Functions:**
- Constructor - Registers as observer to ServiceImpl
- `loadMatches()` - Load all matches for display
- `handleSellTicket()` - Sell ticket to customer
- `handleSearch()` - Search tickets by customer info
- `handleModifySeats()` - Modify ticket seat count
- `handleLogout()` - Unregister observer and cleanup
- `onMatchUpdated(Match match)` - Observer callback for real-time updates

---

## Utilities

### [`DatabaseConfig.java`](src/main/java/org/example/utils/DatabaseConfig.java)
**Purpose:** Manages database connection and schema initialization.

**Fields:**
- `dbUrl` - Database connection URL

**Functions:**
- Constructor - Loads config, initializes schema
- `loadUrl()` - Reads database URL from config.properties
- `getConnection()` - Returns SQL connection
- `initSchema()` - Creates tables if they don't exist:
  - `cashiers` - id, username, password, fullName
  - `matches` - id, name, ticketPrice, totalSeats, availableSeats
  - `customers` - id, name, address
  - `tickets` - id, customerId, matchId, numberOfSeats

---

## Configuration

### [`config.properties`](src/main/resources/config.properties)
```properties
db.url=jdbc:sqlite:basketball.db
db.timeout=30
```

### [`log4j2.xml`](src/main/resources/log4j2.xml)
Logging configuration with:
- Console appender with colored output
- Rolling file appender for persistent logs
- Separate loggers for repository and service layers (DEBUG level)
- Root logger at INFO level

### [`build.gradle`](build.gradle)
Gradle build configuration with:
- Java 21 support
- JavaFX 21 modules (controls, fxml)
- SQLite JDBC driver
- Log4j2 API and core

---

## Running the Application

### Prerequisites
- Java 21 or higher
- Gradle (wrapper included)

### Build and Run
```bash
./gradlew run
```

### Demo Flow
The application runs a demo that:
1. Initializes SQLite database and creates schema
2. Seeds initial data (2 cashiers, 5 matches)
3. Logs in as "ionescu" with password "pass123"
4. Loads and displays all matches
5. Sells a ticket to "Maria Popescu" for 3 seats
6. Searches for tickets by customer name
7. Modifies the ticket from 3 to 5 seats
8. Logs out

### Database
A SQLite database file `basketball.db` is created in the project directory.

### Default Credentials
- Username: `ionescu`, Password: `pass123`
- Username: `popescu`, Password: `pass456`

---

## Architecture Notes

- **Layered Architecture**: Clear separation between UI (Controllers), Business Logic (Service), and Data Access (Repository)
- **Observer Pattern**: Enables real-time updates across multiple clients
- **Factory Pattern**: Abstract repository provides template for implementations
- **Transaction Management**: Service layer coordinates multi-step operations
- **Database**: SQLite for simplicity and portability
