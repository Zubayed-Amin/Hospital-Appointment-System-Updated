/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package hospitalappointmentsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.stage.Stage;



/**
 * FXML Controller class
 *
 * @author zubay
 */
public class AdminViewController implements Initializable {

    @FXML
    private TableView<AdminAppointment> appointmentTable;
    @FXML
    private TableColumn<AdminAppointment, String> colPName;
    @FXML
    private TableColumn<AdminAppointment, String> colDName;
    @FXML
    private TableColumn<AdminAppointment, String> colDate;
    @FXML
    private TableColumn<AdminAppointment, String> colTime;
    @FXML
    private TableColumn<AdminAppointment, String> colStatus;
    @FXML
    private Button backBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private Button completedBtn;
    @FXML
    private Button rejectedBtn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colPName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatientName()));
        colDName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctorName()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        loadAllAppointments();
        
        colStatus.setCellFactory(column -> new TableCell<AdminAppointment, String>() {
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

    private void loadAllAppointments() {
        ObservableList<AdminAppointment> list = FXCollections.observableArrayList();

        // 1. Load from 'appointment' table (registered patients)
        String sql1 = "SELECT id, patient_name, doctor_name, appointment_date, appointment_time, status FROM appointment";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql1);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new AdminAppointment(
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading appointment table: " + e.getMessage());
        }

        // 2. Load from 'guest_appointment' table
        String sql2 = "SELECT id, patient_name, doctor_name, appointment_date, appointment_time, status FROM guest_appointment";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql2);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new AdminAppointment(
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading guest_appointment table: " + e.getMessage());
        }

        appointmentTable.setItems(list);
    }
    
    @FXML
    private void Completed(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminCompleted.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Completed");
        stage.show();
        ((Stage) backBtn.getScene().getWindow()).close();
    }

    @FXML
    private void Rejected(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminRejected.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Rejected");
        stage.show();
        ((Stage) backBtn.getScene().getWindow()).close();
    }
    
    @FXML
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Dashboard");
        stage.show();
        ((Stage) backBtn.getScene().getWindow()).close();
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
}
