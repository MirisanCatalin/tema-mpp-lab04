package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.CashierDashboardController;
import org.example.controller.LoginController;
import org.example.domain.Cashier;
import org.example.domain.Match;
import org.example.domain.Ticket;
import org.example.repository.impl.*;
import org.example.service.ServiceImpl;
import org.example.utils.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Basketball Ticket System Application.
 *
 * Can run in two modes:
 * 1. JavaFX GUI mode (with graphical interface)
 * 2. Console demo mode (for testing without display)
 *
 * Usage:
 *   ./gradlew run              - JavaFX GUI (if display available)
 *   ./gradlew runConsole       - Console demo mode
 */
public class MainApp extends Application {

    private static final Logger logger = LogManager.getLogger(MainApp.class);
    private static ServiceImpl service;

    public static void main(String[] args) {
        // Check if running in console mode
        if (args.length > 0 && "console".equals(args[0])) {
            runConsoleMode();
        } else {
            // Try JavaFX mode first
            try {
                launch(args);
            } catch (Exception e) {
                logger.warn("JavaFX not available (headless environment), running in console mode: {}", e.getMessage());
                runConsoleMode();
            }
        }
    }

    private static void runConsoleMode() {
        logger.info("=== Basketball Ticket System starting (Console Mode) ===");

        // 1. Infrastructure
        DatabaseConfig dbConfig = new DatabaseConfig();

        // 2. Repositories
        var cashierRepo  = new CashierRepositoryImpl(dbConfig);
        var matchRepo    = new MatchRepositoryImpl(dbConfig);
        var customerRepo = new CustomerRepositoryImpl(dbConfig);
        var ticketRepo   = new TicketRepositoryImpl(dbConfig);

        // 3. Service
        service = new ServiceImpl(cashierRepo, matchRepo, customerRepo, ticketRepo);

        // 4. Seed data
        seedDataConsole(cashierRepo, matchRepo);

        // ---- Demo flow ----

        // Login
        LoginController loginCtrl = new LoginController(service);
        Optional<Cashier> cashier = loginCtrl.handleLoginConsole("ionescu", "pass123");

        if (cashier.isEmpty()) {
            logger.error("Login failed — stopping demo");
            return;
        }

        // Open dashboard
        CashierDashboardController dashboard =
                new CashierDashboardController(service, cashier.get());

        // Show matches
        List<Match> matches = dashboard.loadMatchesConsole();
        logger.info("Matches loaded: {}", matches.size());
        matches.forEach(m -> logger.info("  {} | {}/{}seats | {}RON{}",
                m.getName(), m.getAvailableSeats(), m.getTotalSeats(),
                m.getTicketPrice(), m.isSoldOut() ? " [SOLD OUT]" : ""));

        // Sell a ticket
        Ticket ticket = dashboard.handleSellTicketConsole(
                "Maria Popescu", "Str. Florilor 5, Cluj", matches.get(0).getId(), 3);
        logger.info("Sold: {}", ticket);

        // Search
        List<Ticket> found = dashboard.handleSearchConsole("Maria Popescu", null);
        logger.info("Search results: {}", found.size());
        found.forEach(t -> logger.info("  {} | {} | {} seats",
                t.getCustomer().getName(), t.getMatch().getName(), t.getNumberOfSeats()));

        // Modify
        Ticket modified = dashboard.handleModifySeatsConsole(ticket.getId(), 5);
        logger.info("Modified ticket: {}", modified);

        // Logout
        dashboard.handleLogoutConsole();

        logger.info("=== Demo complete ===");
    }

    @Override
    public void init() throws Exception {
        logger.info("=== Basketball Ticket System starting (JavaFX Mode) ===");

        // 1. Infrastructure
        DatabaseConfig dbConfig = new DatabaseConfig();

        // 2. Repositories
        var cashierRepo  = new CashierRepositoryImpl(dbConfig);
        var matchRepo    = new MatchRepositoryImpl(dbConfig);
        var customerRepo = new CustomerRepositoryImpl(dbConfig);
        var ticketRepo   = new TicketRepositoryImpl(dbConfig);

        // 3. Service
        service = new ServiceImpl(cashierRepo, matchRepo, customerRepo, ticketRepo);

        // 4. Seed data
        seedDataConsole(cashierRepo, matchRepo);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            LoginController loginController = new LoginController(service);
            loader.setController(loginController);

            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setTitle("Basketball Ticket System - Login");
            primaryStage.setScene(scene);
            primaryStage.show();

            loginController.setPrimaryStage(primaryStage);

            logger.info("JavaFX UI started successfully");

        } catch (Exception e) {
            logger.error("Failed to start JavaFX: {}", e.getMessage(), e);
            // Fallback to console mode
            logger.info("Falling back to console mode...");
            runConsoleMode();
        }
    }

    private static void seedDataConsole(org.example.repository.interfaces.CashierRepository cashierRepo,
                                 org.example.repository.interfaces.MatchRepository matchRepo) {
        if (cashierRepo.findAll().isEmpty()) {
            cashierRepo.save(new Cashier(null, "ionescu", "pass123", "Ion Ionescu"));
            cashierRepo.save(new Cashier(null, "popescu", "pass456", "Ana Popescu"));
            logger.info("Seeded cashiers");
        }
        if (matchRepo.findAll().isEmpty()) {
            matchRepo.save(new Match(null, "Steaua vs Dinamo",  50.0, 500, 500));
            matchRepo.save(new Match(null, "Rapid vs CFR Cluj", 40.0, 300, 300));
            matchRepo.save(new Match(null, "Semifinala 1",      75.0, 200, 200));
            matchRepo.save(new Match(null, "Semifinala 2",      75.0, 200, 200));
            matchRepo.save(new Match(null, "Finala",           100.0, 100, 100));
            logger.info("Seeded matches");
        }
    }
}