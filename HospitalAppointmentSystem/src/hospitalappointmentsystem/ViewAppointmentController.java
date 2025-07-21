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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
public class ViewAppointmentController implements Initializable {

    @FXML
    private TableView<PatAppointment> appointmentTable;
    @FXML
    private TableColumn<PatAppointment, String> colDoctor;
    @FXML
    private TableColumn<PatAppointment, String> colContact;
    @FXML
    private TableColumn<PatAppointment, String> colDate;
    @FXML
    private TableColumn<PatAppointment, String> colTime;
    @FXML
    private TableColumn<PatAppointment, String> colStatus;
    @FXML
    private Button backBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private Button myhistoryBtn;
    
    private String username;
    
    
    public void setUsername(String username) {
        this.username = username;
        loadAppointments(); // Load when set
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set cell factory for the status column
        colStatus.setCellFactory(column -> new TableCell<PatAppointment, String>() {
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

    private void loadAppointments() {
        if (username == null || username.isEmpty()) {
            System.out.println("Username not set.");
            return;
        }

        ObservableList<PatAppointment> list = FXCollections.observableArrayList();

        try (Connection conn = ConnectionDB.getConnection()) {
            // Step 1: Get fullname from username
            String fullName = null;
            String nameQuery = "SELECT fullname FROM users WHERE username = ?";
            PreparedStatement nameStmt = conn.prepareStatement(nameQuery);
            nameStmt.setString(1, username);
            ResultSet nameResult = nameStmt.executeQuery();

            if (nameResult.next()) {
                fullName = nameResult.getString("fullname");
            } else {
                System.out.println("No fullname found for username: " + username);
                return;
            }

            // Step 2: Get Appointments
            String sql = """
                SELECT a.id, a.doctor_name, d.contact AS doctor_contact, 
                       a.appointment_date,
                       TIME_FORMAT(a.appointment_time, '%h:%i %p') AS formatted_time,
                       a.status
                FROM appointment a
                JOIN users d ON a.doctor_name = d.fullname     -- doctor info
                JOIN users p ON a.patient_name = p.fullname    -- patient info
                WHERE p.username = ?
                ORDER BY a.appointment_date DESC
            """;


            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new PatAppointment(
                    rs.getInt("id"),
                    rs.getString("doctor_name"),
                    rs.getString("doctor_contact"),
                    rs.getString("appointment_date"),
                    rs.getString("formatted_time"),
                    rs.getString("status"), rs.getString("status")
                ));
            }

            appointmentTable.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UserAppointment.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Appointment Dashboard");
        stage.show();

        // Close current window
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

    @FXML
    private void MyHistory(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("HistoryForPatient.fxml"));
        Parent root = loader.load();

        // Get the controller instance and pass the username
        HistoryForPatientController controller = loader.getController();
        controller.setUsername(username); // Make sure loggedInUsername is defined

        // Show the new scene in the current window
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("My History");
        stage.show();
    }
    
}
