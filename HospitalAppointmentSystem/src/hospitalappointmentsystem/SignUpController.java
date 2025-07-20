/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package hospitalappointmentsystem;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.ResultSet;


/**
 * FXML Controller class
 *
 * @author zubay
 */
public class SignUpController implements Initializable {

    @FXML
    private VBox leftPane;
    @FXML
    private ImageView logoImage;
    @FXML
    private TextField tfFullname;
    @FXML
    private TextField tfUsername;
    @FXML
    private TextField tfContact;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private PasswordField pfConfirmPassword;
    @FXML
    private ComboBox<String> comboBoxRole;
    @FXML
    private VBox doctorFields;
    @FXML
    private TextField tfDept;
    @FXML
    private TextField tfDesc;
    @FXML
    private ComboBox<String> SthourComboBox;
    @FXML
    private ComboBox<String> StminComboBox;
    @FXML
    private ComboBox<String> StampmComboBox;
    @FXML
    private ComboBox<String> EndhourComboBox;
    @FXML
    private ComboBox<String> EndminComboBox;
    @FXML
    private ComboBox<String> EndampmComboBox;
    @FXML
    private Button signupBtn;
    @FXML
    private Hyperlink SigninLink;
    @FXML
    private ImageView logoImage1;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboBoxRole.getItems().addAll("Patient", "Doctor");

        comboBoxRole.setOnAction(event -> {
            String selected = comboBoxRole.getValue();
            boolean isDoctor = "Doctor".equalsIgnoreCase(selected);
            doctorFields.setVisible(isDoctor);
            doctorFields.setManaged(isDoctor);
        });
        
        SthourComboBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        StminComboBox.getItems().addAll("00",  "30");
        StampmComboBox.getItems().addAll("AM", "PM");

        EndhourComboBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        EndminComboBox.getItems().addAll("00", "30");
        EndampmComboBox.getItems().addAll("AM", "PM");
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
        alert.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("glass-background");
        alert.showAndWait();
    }

    
    @FXML
    private void SignUp(ActionEvent event) throws IOException {
        String fullname = tfFullname.getText().trim();
        String username = tfUsername.getText().trim();
        String contact = tfContact.getText().trim();
        String password = pfPassword.getText();
        String confirmPassword = pfConfirmPassword.getText();
        String role = comboBoxRole.getValue();

        if (fullname.isEmpty() || username.isEmpty() || contact.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Please fill in all required fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Passwords do not match.");
            return;
        }

        try (Connection conn = ConnectionDB.getConnection()) {
            PreparedStatement stmt;
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.ERROR, "Username already exists. Please choose a different one.");
                return;
            }


            if (role.equalsIgnoreCase("Doctor")) {
                String department = tfDept.getText().trim();
                String description = tfDesc.getText().trim();
                String sth = SthourComboBox.getValue();
                String stm = StminComboBox.getValue();
                String stap = StampmComboBox.getValue();
                String endh = EndhourComboBox.getValue();
                String endm = EndminComboBox.getValue();
                String endap = EndampmComboBox.getValue();

                if (department.isEmpty() || description.isEmpty() || sth == null || stm == null || stap == null || endh == null || endm == null || endap == null) {
                    showAlert(Alert.AlertType.ERROR, "Please fill in all doctor fields.");
                    return;
                }

                String dutyTime = sth + ":" + stm + stap + "-" + endh + ":" + endm + endap;

                String sql = "INSERT INTO users (fullname, username, department, description, contact, password, role, duty_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, fullname);
                stmt.setString(2, username);
                stmt.setString(3, department);
                stmt.setString(4, description);
                stmt.setString(5, contact);
                stmt.setString(6, password);
                stmt.setString(7, role.toLowerCase());
                stmt.setString(8, dutyTime);

                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Doctor registered successfully!\nDuty time: " + dutyTime);

            } else if (role.equalsIgnoreCase("Patient")) {
                String sql = "INSERT INTO users (fullname, username, contact, password, role) VALUES (?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, fullname);
                stmt.setString(2, username);
                stmt.setString(3, contact);
                stmt.setString(4, password);
                stmt.setString(5, role.toLowerCase());

                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Patient registered successfully!");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Login Form");
            stage.show();
            ((Stage) signupBtn.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error occurred during registration.");
        }
    }


    @FXML
    private void goToLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Login Form");
        stage.show();
        ((Stage) SigninLink.getScene().getWindow()).close();
    }
    
}
