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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class DoctorDashboardController implements Initializable {

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
    private Button logoutBtn;
    @FXML
    private Button historyBtn;

    private String doctorUsername;
    

    public void setDoctorUsername(String username) {
        this.doctorUsername = username;
        loadAppointments();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colPName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPcontact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new TableCell<DocAppointment, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>(
                    FXCollections.observableArrayList("Pending", "Accepted", "Rejected", "Completed")
            );

            {
                comboBox.setOnAction(event -> {
                    DocAppointment appt = getTableView().getItems().get(getIndex());
                    if (appt != null) {
                        String selectedStatus = comboBox.getValue();
                        appt.setStatus(selectedStatus);
                        updateStatus(appt.getId(), selectedStatus);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(item);
                    setGraphic(comboBox);
                }
            }
        });
    
    }

    private void loadAppointments() {
        ObservableList<DocAppointment> list = FXCollections.observableArrayList();

        try (Connection conn = ConnectionDB.getConnection()) {
            String doctorFullname = null;
            String nameQuery = "SELECT fullname FROM users WHERE username = ?";
            try (PreparedStatement nameStmt = conn.prepareStatement(nameQuery)) {
                nameStmt.setString(1, doctorUsername);
                ResultSet nameRs = nameStmt.executeQuery();
                if (nameRs.next()) {
                    doctorFullname = nameRs.getString("fullname");
                } else {
                    System.out.println("Doctor fullname not found!");
                    return;
                }
            }

            String sql = """
                    SELECT * FROM (
                        SELECT a.id, a.patient_name, u.contact, a.appointment_date,
                               TIME_FORMAT(a.appointment_time, '%h:%i %p') AS formatted_time,
                               a.status
                        FROM appointment a
                        JOIN users u ON a.patient_name = u.fullname
                        WHERE a.doctor_name = ?

                        UNION

                        SELECT ga.id, ga.patient_name, gu.contact, ga.appointment_date,
                               TIME_FORMAT(ga.appointment_time, '%h:%i %p') AS formatted_time,
                               ga.status
                        FROM guest_appointment ga
                        JOIN guest_users gu ON ga.patient_name = gu.fullname
                        WHERE ga.doctor_name = ?
                    ) AS all_appointments
                    ORDER BY appointment_date DESC
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, doctorFullname);
                stmt.setString(2, doctorFullname);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    list.add(new DocAppointment(
                            rs.getInt("id"),
                            rs.getString("patient_name"),
                            rs.getString("appointment_date"),
                            rs.getString("contact"),
                            rs.getString("formatted_time"),
                            rs.getString("status")
                    ));
                }
            }

            DocappointmentTable.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(int appointmentId, String status) {
        try (Connection conn = ConnectionDB.getConnection()) {

            boolean isGuest = false;
            String patientName = null, contact = null, date = null, time = null, doctorName = null;

            // First, check and collect from appointment
            String query = """
                SELECT a.patient_name, u.contact, a.appointment_date, 
                       TIME_FORMAT(a.appointment_time, '%h:%i %p') AS formatted_time, 
                       a.doctor_name 
                FROM appointment a
                JOIN users u ON a.patient_name = u.fullname
                WHERE a.id = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, appointmentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    patientName = rs.getString("patient_name");
                    contact = rs.getString("contact");
                    date = rs.getString("appointment_date");
                    time = rs.getString("formatted_time");
                    doctorName = rs.getString("doctor_name");
                } else {
                    // Try guest_appointment
                    isGuest = true;
                    query = """
                        SELECT ga.patient_name, gu.contact, ga.appointment_date, 
                               TIME_FORMAT(ga.appointment_time, '%h:%i %p') AS formatted_time, 
                               ga.doctor_name 
                        FROM guest_appointment ga
                        JOIN guest_users gu ON ga.patient_name = gu.fullname
                        WHERE ga.id = ?
                    """;
                    try (PreparedStatement guestStmt = conn.prepareStatement(query)) {
                        guestStmt.setInt(1, appointmentId);
                        ResultSet grs = guestStmt.executeQuery();
                        if (grs.next()) {
                            patientName = grs.getString("patient_name");
                            contact = grs.getString("contact");
                            date = grs.getString("appointment_date");
                            time = grs.getString("formatted_time");
                            doctorName = grs.getString("doctor_name");
                        }
                    }
                }
            }

            if ("Completed".equals(status) || "Rejected".equals(status)) {
                // Determine target table
                String targetTable = "Completed".equals(status) ? "completed_appointments" : "rejected_appointments";

                // Insert into appropriate table
                String insertSQL = "INSERT INTO " + targetTable + " (id, patient_name, contact, appointment_date, appointment_time, status, doctor_name, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    insertStmt.setInt(1, appointmentId);
                    insertStmt.setString(2, patientName);
                    insertStmt.setString(3, contact);
                    insertStmt.setString(4, date);
                    insertStmt.setString(5, time);
                    insertStmt.setString(6, status);
                    insertStmt.setString(7, doctorName);
                    insertStmt.setString(8, isGuest ? "guest" : "registered");
                    insertStmt.executeUpdate();
                }

                // Delete from original table
                String deleteSQL = isGuest ?
                    "DELETE FROM guest_appointment WHERE id = ?" :
                    "DELETE FROM appointment WHERE id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                    deleteStmt.setInt(1, appointmentId);
                    deleteStmt.executeUpdate();
                }

                System.out.println("Moved appointment ID " + appointmentId + " to " + targetTable + " table.");
            } else {
                // If not completed, just update status in both tables (only one will succeed)
                String sql1 = "UPDATE appointment SET status = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                    stmt.setString(1, status);
                    stmt.setInt(2, appointmentId);
                    stmt.executeUpdate();
                }

                String sql2 = "UPDATE guest_appointment SET status = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                    stmt.setString(1, status);
                    stmt.setInt(2, appointmentId);
                    stmt.executeUpdate();
                }

                System.out.println("Updated appointment ID " + appointmentId + " to " + status);
            }

            // Reload table
            loadAppointments();

        } catch (Exception e) {
            e.printStackTrace();
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
    private void History(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("HistoryForDoctor.fxml"));
        Parent root = loader.load();

        // Get the controller instance and pass the username
        HistoryForDoctorController controller = loader.getController();
        controller.setUsername(doctorUsername); // Make sure loggedInUsername is defined

        // Show the new scene in the current window
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("My History");
        stage.show();
    }
}
