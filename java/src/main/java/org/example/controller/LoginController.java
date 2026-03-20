package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.domain.Cashier;
import org.example.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Handles the Login screen logic.
 * Injected with the Service and wired to Login.fxml.
 */
public class LoginController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    private final Service service;
    private javafx.stage.Stage primaryStage;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    public LoginController(Service service) {
        this.service = service;
    }

    public void setPrimaryStage(javafx.stage.Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Called when the cashier clicks "Login" (JavaFX).
     */
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        logger.debug("handleLogin for username={}", username);

        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("Please enter username and password");
            errorLabel.setVisible(true);
            return;
        }

        Optional<Cashier> cashier = service.login(username, password);

        if (cashier.isPresent()) {
            logger.info("Login successful for username={}", username);
            errorLabel.setVisible(false);
            // Open CashierDashboard window
            openDashboard(cashier.get());
        } else {
            logger.warn("Login failed for username={}", username);
            errorLabel.setText("Invalid username or password");
            errorLabel.setVisible(true);
        }
    }

    /**
     * Console mode login (no JavaFX).
     * @return the authenticated Cashier, or empty if credentials are wrong
     */
    public Optional<Cashier> handleLoginConsole(String username, String password) {
        logger.debug("handleLoginConsole for username={}", username);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            logger.warn("Login attempted with empty credentials");
            return Optional.empty();
        }

        Optional<Cashier> cashier = service.login(username, password);

        if (cashier.isPresent()) {
            logger.info("Login successful for username={}", username);
        } else {
            logger.warn("Login failed for username={}", username);
        }

        return cashier;
    }

    private void openDashboard(Cashier cashier) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/CashierDashboard.fxml"));

            CashierDashboardController dashboardController = new CashierDashboardController(service, cashier);
            dashboardController.setLoginController(this);
            loader.setController(dashboardController);

            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);

            javafx.stage.Stage dashboardStage = new javafx.stage.Stage();
            dashboardStage.setTitle("Cashier Dashboard - " + cashier.getFullName());
            dashboardStage.setScene(scene);
            dashboardStage.show();

            // Close login window
            if (primaryStage != null) {
                primaryStage.close();
            }

        } catch (Exception e) {
            logger.error("Failed to open dashboard: {}", e.getMessage(), e);
            errorLabel.setText("Error opening dashboard: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}
