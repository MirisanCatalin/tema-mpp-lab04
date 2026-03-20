package org.example.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.domain.Cashier;
import org.example.domain.Match;
import org.example.domain.Ticket;
import org.example.service.MatchUpdateObserver;
import org.example.service.Service;
import org.example.service.ServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


public class CashierDashboardController implements MatchUpdateObserver {

    private static final Logger logger = LogManager.getLogger(CashierDashboardController.class);

    private final Service service;
    private final Cashier loggedInCashier;
    private LoginController loginController;
    private javafx.stage.Stage dashboardStage;

    @FXML
    private Label welcomeLabel;

    @FXML
    private TableView<Match> matchesTable;

    @FXML
    private TextField customerNameField;

    @FXML
    private TextField customerAddressField;

    @FXML
    private TextField matchIdField;

    @FXML
    private TextField seatsField;

    @FXML
    private TextField searchNameField;

    @FXML
    private TextField searchAddressField;

    @FXML
    private TableView<Ticket> ticketsTable;

    @FXML
    private TextField ticketIdField;

    @FXML
    private TextField newSeatsField;

    @FXML
    private Label statusLabel;

    private ObservableList<Match> matchesList = FXCollections.observableArrayList();
    private ObservableList<Ticket> ticketsList = FXCollections.observableArrayList();

    public CashierDashboardController(Service service, Cashier loggedInCashier) {
        this.service = service;
        this.loggedInCashier = loggedInCashier;

        if (service instanceof ServiceImpl si) {
            si.addObserver(this);
        }

        logger.info("Dashboard opened for cashier: {}", loggedInCashier.getUsername());
    }

    public void setLoginController(LoginController controller) {
        this.loginController = controller;
    }

    public void setDashboardStage(javafx.stage.Stage stage) {
        this.dashboardStage = stage;
    }


    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + loggedInCashier.getFullName());

        matchesTable.getColumns().clear();
        TableColumn<Match, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<Match, String> nameCol = new TableColumn<>("Match Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Match, Double> priceCol = new TableColumn<>("Price (RON)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
        priceCol.setPrefWidth(100);

        TableColumn<Match, Integer> availCol = new TableColumn<>("Available");
        availCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        availCol.setPrefWidth(100);

        TableColumn<Match, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalSeats"));
        totalCol.setPrefWidth(80);

        matchesTable.getColumns().addAll(idCol, nameCol, priceCol, availCol, totalCol);
        matchesTable.setItems(matchesList);

        ticketsTable.getColumns().clear();
        TableColumn<Ticket, Long> ticketIdCol = new TableColumn<>("Ticket ID");
        ticketIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        ticketIdCol.setPrefWidth(80);

        TableColumn<Ticket, String> custCol = new TableColumn<>("Customer");
        custCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getName()));
        custCol.setPrefWidth(150);

        TableColumn<Ticket, String> matchCol = new TableColumn<>("Match");
        matchCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMatch().getName()));
        matchCol.setPrefWidth(150);

        TableColumn<Ticket, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfSeats"));
        seatsCol.setPrefWidth(80);

        ticketsTable.getColumns().addAll(ticketIdCol, custCol, matchCol, seatsCol);
        ticketsTable.setItems(ticketsList);

        loadMatches();
    }

    public void loadMatches() {
        logger.debug("loadMatches()");
        List<Match> matches = service.getAllMatches();
        matchesList.clear();
        matchesList.addAll(matches);
        logger.info("Loaded {} matches", matches.size());
    }


    public List<Match> loadMatchesConsole() {
        logger.debug("loadMatchesConsole()");
        return service.getAllMatches();
    }

    public Ticket handleSellTicketConsole(String customerName, String customerAddress,
                                   Long matchId, int numberOfSeats) {
        logger.debug("handleSellTicketConsole: customer='{}', matchId={}, seats={}",
                customerName, matchId, numberOfSeats);
        return service.sellTicket(customerName, customerAddress, matchId, numberOfSeats);
    }

    public List<Ticket> handleSearchConsole(String name, String address) {
        logger.debug("handleSearchConsole: name='{}', address='{}'", name, address);
        return service.searchTickets(name, address);
    }

    public Ticket handleModifySeatsConsole(Long ticketId, int newSeats) {
        logger.debug("handleModifySeatsConsole: ticketId={}, newSeats={}", ticketId, newSeats);
        return service.modifyTicketSeats(ticketId, newSeats);
    }

    public void handleLogoutConsole() {
        logger.info("Cashier {} logged out (console)", loggedInCashier.getUsername());
        if (service instanceof ServiceImpl si) {
            si.removeObserver(this);
        }
    }


    @FXML
    public void handleSellTicket() {
        try {
            String customerName = customerNameField.getText();
            String customerAddress = customerAddressField.getText();
            Long matchId = Long.parseLong(matchIdField.getText());
            int seats = Integer.parseInt(seatsField.getText());

            logger.debug("handleSellTicket: customer='{}', matchId={}, seats={}",
                    customerName, matchId, seats);

            Ticket ticket = service.sellTicket(customerName, customerAddress, matchId, seats);
            statusLabel.setText("Ticket sold successfully! Ticket ID: " + ticket.getId());
            statusLabel.setStyle("-fx-text-fill: green;");

            customerNameField.clear();
            customerAddressField.clear();
            matchIdField.clear();
            seatsField.clear();

            loadMatches(); // Refresh matches table

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid input: Please enter valid numbers");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (IllegalArgumentException e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ---- Search (JavaFX) ----

    @FXML
    public void handleSearch() {
        String name = searchNameField.getText();
        String address = searchAddressField.getText();

        logger.debug("handleSearch: name='{}', address='{}'", name, address);

        List<Ticket> tickets = service.searchTickets(
            name.isBlank() ? null : name,
            address.isBlank() ? null : address
        );

        ticketsList.clear();
        ticketsList.addAll(tickets);
        statusLabel.setText("Found " + tickets.size() + " ticket(s)");
        statusLabel.setStyle("-fx-text-fill: black;");
    }

    // ---- Modify (JavaFX) ----

    @FXML
    public void handleModify() {
        try {
            Long ticketId = Long.parseLong(ticketIdField.getText());
            int newSeats = Integer.parseInt(newSeatsField.getText());

            logger.debug("handleModify: ticketId={}, newSeats={}", ticketId, newSeats);

            Ticket modified = service.modifyTicketSeats(ticketId, newSeats);
            statusLabel.setText("Ticket modified successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");

            ticketIdField.clear();
            newSeatsField.clear();

            loadMatches(); // Refresh matches table

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid input: Please enter valid numbers");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (IllegalArgumentException e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ---- Logout (JavaFX) ----

    @FXML
    public void handleLogout() {
        logger.info("Cashier {} logged out", loggedInCashier.getUsername());
        if (service instanceof ServiceImpl si) {
            si.removeObserver(this);
        }

        // Close dashboard
        if (dashboardStage != null) {
            dashboardStage.close();
        }

        // Could reopen login window here if needed
    }

    // ---- Observer callback ----

    /**
     * Called automatically by ServiceImpl whenever any match changes
     * (because another cashier sold/modified a ticket).
     */
    @Override
    public void onMatchUpdated(Match match) {
        logger.info("Match updated notification received: {}", match);
        Platform.runLater(this::loadMatches);
    }
}