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
                    SELECT a.id, a.patient_name, u.contact, a.appointment_date,
                           TIME_FORMAT(a.appointment_time, '%h:%i %p') AS formatted_time,
                           a.status
                    FROM appointment a
                    JOIN users u ON a.patient_name = u.fullname
                    WHERE a.doctor_name = ?
                    ORDER BY a.appointment_date DESC
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, doctorFullname);
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
            String sql = "UPDATE appointment SET status = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setInt(2, appointmentId);
                stmt.executeUpdate();
                System.out.println("Updated appointment ID " + appointmentId + " to " + status);
            }
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
}
