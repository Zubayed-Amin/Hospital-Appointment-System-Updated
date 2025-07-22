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
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author zubay
 */
public class HistoryForDoctorController implements Initializable {
    @FXML
    private Button logoutBtn;
    @FXML
    private TableView<DocAppointment> DocappointmentTable;
    @FXML
    private TableColumn<DocAppointment, String> colPName;
    @FXML
    private TableColumn<DocAppointment, String> colPcontact;
    @FXML
    private TableColumn<DocAppointment, String> colDate;
    @FXML
    private TableColumn<DocAppointment, String> colTime;
    @FXML
    private TableColumn<DocAppointment, String> colStatus;
    @FXML
    private Button backBtn;

    private String loggedInUsername;

    public void setUsername(String username) {
        this.loggedInUsername = username;
        System.out.println("History username set to: " + username);
        loadHistoryAppointments();
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colPName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPcontact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadHistoryAppointments();
        colStatus.setCellFactory(column -> new TableCell<DocAppointment, String>() {
        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) {
                setText(null);
                setStyle("");
                getStyleClass().removeAll("status-pending", "status-accepted", "status-rejected", "status-completed");
            } else {
                setText(status);
                getStyleClass().removeAll("status-pending", "status-accepted", "status-rejected", "status-completed");

                switch (status.toLowerCase()) {
                    case "pending":
                        getStyleClass().add("status-pending");
                        break;
                    case "accepted":
                        getStyleClass().add("status-accepted");
                        break;
                    case "rejected":
                        getStyleClass().add("status-rejected");
                        break;
                    case "completed":
                        getStyleClass().add("status-completed");
                        break;
                }
            }
        }
    });
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
    
    private void loadHistoryAppointments() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            System.out.println( "Username not set.");
            return;
        }
        ObservableList<DocAppointment> list = FXCollections.observableArrayList();

        try (Connection conn = ConnectionDB.getConnection()) {
            // 1. Get fullname for the logged-in username
            String fullname = null;
            String fullnameQuery = "SELECT fullname FROM users WHERE username = ?";
            PreparedStatement ps1 = conn.prepareStatement(fullnameQuery);
            ps1.setString(1, loggedInUsername);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                fullname = rs1.getString("fullname");
            } else {
                System.out.println( "Fullname not found for username: " + loggedInUsername);
                return; // No fullname found, so no appointments
            }

            // 2. Query rejected and completed appointments by fullname
            String sql = """
                SELECT id, patient_name, contact, appointment_date, appointment_time, status
                FROM rejected_appointments
                WHERE doctor_name = ?

                UNION ALL

                SELECT id, patient_name, contact, appointment_date, appointment_time, status
                FROM completed_appointments
                WHERE doctor_name = ?

                ORDER BY appointment_date DESC
            """;

            PreparedStatement ps2 = conn.prepareStatement(sql);
            ps2.setString(1, fullname);
            ps2.setString(2, fullname);
            ResultSet rs2 = ps2.executeQuery();

            // 3. Read results and add to the list
            while (rs2.next()) {
                    list.add(new DocAppointment(
                            rs2.getInt("id"),
                            rs2.getString("patient_name"),
                            rs2.getString("appointment_date"),
                            rs2.getString("contact"),
                            rs2.getString("appointment_time"),
                            rs2.getString("status")
                    ));
                }

            // 4. Set items to TableView
            DocappointmentTable.setItems(list);

        } catch (SQLException e) {
            System.out.println( "Error loading history: " + e.getMessage());
        }
    }


    @FXML
    private void Logout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Login Form");
        stage.show();
        ((Stage) logoutBtn.getScene().getWindow()).close();
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
        Parent root = loader.load();
        DoctorDashboardController controller = loader.getController();
        controller.setDoctorUsername(loggedInUsername);  // Pass username back ✅

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Doctor Dashboard");
        stage.show();
        ((Stage) backBtn.getScene().getWindow()).close();
    }
    
}
