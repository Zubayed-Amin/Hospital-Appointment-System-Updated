/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package hospitalappointmentsystem;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author zubay
 */
public class EditAppointmentController implements Initializable {

    @FXML
    private ImageView logoImage;
    @FXML
    private TextField tffullname;
    @FXML
    private TextField tfcontact;
    @FXML
    private ComboBox<String> deptComboBox;
    @FXML
    private TextField tfDesc;
    @FXML
    private ComboBox<String> doctorComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> hourComboBox;
    @FXML
    private ComboBox<String> minComboBox;
    @FXML
    private ComboBox<String> ampmComboBox;
    @FXML
    private Button updateBtn;

    private int appointmentId;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load combo options
        hourComboBox.getItems().addAll("1","2","3","4","5","6","7","8","9","10","11","12");
        minComboBox.getItems().addAll("00", "15", "30", "45");
        ampmComboBox.getItems().addAll("AM", "PM");
        loadDepartments();

        // Load doctor + description when dept selected
        deptComboBox.setOnAction(e -> {
            String dept = deptComboBox.getValue();
            if (dept != null) {
                loadDoctorsAndDescription(dept);
            }
        });
    }

    private void loadDepartments() {
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT DISTINCT department FROM users WHERE role = 'doctor'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deptComboBox.getItems().add(rs.getString("department"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDoctorsAndDescription(String department) {
        doctorComboBox.getItems().clear();
        tfDesc.clear();
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT fullname, description FROM users WHERE department = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, department);
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                doctorComboBox.getItems().add(rs.getString("fullname"));
                if (first) {
                    tfDesc.setText(rs.getString("description"));
                    first = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Load appointment by ID
    public void loadAppointmentData(int id) {
        this.appointmentId = id;
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = """
                SELECT g.patient_name, g.patient_contact, g.doctor_name,
                       u.department, g.appointment_date, g.appointment_time
                FROM guest_appointment g
                JOIN users u ON g.doctor_name = u.fullname
                WHERE g.id = ?
            """;


            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Set patient details
                tffullname.setText(rs.getString("patient_name"));
                tfcontact.setText(rs.getString("patient_contact"));

                // Store department and doctor
                String department = rs.getString("department");
                String doctor = rs.getString("doctor_name");

                // 🟡 Step 1: Load departments first
                deptComboBox.getItems().clear();
                try (PreparedStatement deptStmt = conn.prepareStatement(
                    "SELECT DISTINCT department FROM users WHERE role = 'doctor'")) {
                    ResultSet deptRs = deptStmt.executeQuery();
                    while (deptRs.next()) {
                        deptComboBox.getItems().add(deptRs.getString("department"));
                    }
                }

                deptComboBox.setValue(department); // 🟢 Set the department
                loadDoctorsAndDescription(department); // 🟢 Load the doctors for that dept
                doctorComboBox.setValue(doctor); // 🟢 Now safely set doctor

                // 🟢 Set department description
                tfDesc.setText(getDepartmentDescription(department));

                // 🟢 Set date
                datePicker.setValue(rs.getDate("appointment_date").toLocalDate());

                // 🟢 Set time
                String[] time = rs.getString("appointment_time").split("[: ]"); // 10:30 AM ➜ ["10", "30", "AM"]
                hourComboBox.setValue(time[0]);
                minComboBox.setValue(time[1]);
                ampmComboBox.setValue(time[2]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private String getDepartmentDescription(String dept) {
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT description FROM users WHERE department = ? AND role = 'doctor' LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, dept);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    // ✅ Update in MySQL when clicking Update button
    @FXML
    private void updateAppointment(ActionEvent event) throws IOException {
        String name = tffullname.getText().trim();
        String contact = tfcontact.getText().trim();
        String doctor = doctorComboBox.getValue();
        String dept = deptComboBox.getValue();
        String hour = hourComboBox.getValue();
        String min = minComboBox.getValue();
        String ampm = ampmComboBox.getValue();
        java.time.LocalDate localDate = datePicker.getValue();

        if (name.isEmpty() || contact.isEmpty() || doctor == null || dept == null ||
            hour == null || min == null || ampm == null || localDate == null) {
            System.out.println("Please fill all fields.");
            return;
        }

        String time = hour + ":" + min + " " + ampm;

        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = """
                UPDATE guest_appointment
                SET patient_name = ?, patient_contact = ?, doctor_name = ?, department = ?,
                    appointment_date = ?, appointment_time = ?
                WHERE id = ?
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, contact);
            ps.setString(3, doctor);
            ps.setString(4, dept);
            ps.setDate(5, Date.valueOf(localDate));
            ps.setString(6, time);
            ps.setInt(7, appointmentId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Appointment updated in guest_appointment table.");
            } else {
                System.out.println("⚠️ No changes made.");
            }
        } catch (SQLException e) {
            System.out.println("Update Error: " + e.getMessage());
        }

        // Reload GuestAppointment.fxml with updated info
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GuestAppointment.fxml"));
        Parent root = loader.load();

        GuestAppointmentController controller = loader.getController();
        controller.loadGuestAppointment(contact);


        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Your Appointment");
        stage.show();

        ((Stage) updateBtn.getScene().getWindow()).close();
    }

    
}
