/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package hospitalappointmentsystem;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author zubay
 */
public class LoginController implements Initializable {

    @FXML
    private VBox leftPane;
    @FXML
    private Label lblError;
    @FXML
    private TextField tfUsername;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private Button loginBtn;
    @FXML
    private Hyperlink SignupLink;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        playEntryAnimation();
    }    

    private void playEntryAnimation() {
        // Set initial opacity and position
        leftPane.setOpacity(0); // Start fully transparent
        leftPane.setTranslateX(500); // Start off-screen to the left

        // Slide animation
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(1), leftPane);
        slideIn.setFromX(500);
        slideIn.setToX(0);

        // Fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), leftPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Play both animations together
        ParallelTransition parallel = new ParallelTransition(slideIn, fadeIn);
        parallel.play();
    }
    
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle("DocSetGo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("Styles.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("glass-background");
        alert.showAndWait();
    }
    
    @FXML
    private void Login(ActionEvent event) throws IOException {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,"Username and Password must not be empty.");
            return;
        }

        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");

                        String fxmlFile = null;
                        String title = null;

                        if ("Admin".equalsIgnoreCase(role)) {
                            fxmlFile = "AdminDashboard.fxml";
                            title = "Admin Dashboard";
                        } else if ("Doctor".equalsIgnoreCase(role)) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
                            Parent root = loader.load();


                            DoctorDashboardController controller = loader.getController();
                            controller.setDoctorUsername(username);


                            Stage stage = (Stage) lblError.getScene().getWindow(); 
                            stage.setScene(new Scene(root));
                            stage.setTitle("Doctor Dashboard");
                            stage.show();

                            return;
                        } else if ("Patient".equalsIgnoreCase(role)) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserAppointment.fxml"));
                            Parent root = loader.load();


                            UserAppointmentController controller = loader.getController();
                            controller.setPatientUsername(username);


                            Stage stage = (Stage) lblError.getScene().getWindow(); 
                            stage.setScene(new Scene(root));
                            stage.setTitle("User Appointment Dashboard");
                            stage.show();

                            return;
                        }


                        if (fxmlFile != null) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                            Parent root = loader.load();


                            Stage stage = (Stage) lblError.getScene().getWindow(); 
                            stage.setScene(new Scene(root));
                            stage.setTitle(title);
                            stage.show();
                        } else {
                            showAlert(Alert.AlertType.ERROR,"Unknown role.");
                        }

                    } else {
                        showAlert(Alert.AlertType.ERROR,"Invalid username or password.");
                    }
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,"Login failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignup(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SignUp.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Sign Up Form");
        stage.show();
        ((Stage) SignupLink.getScene().getWindow()).close();
    }
    
}
