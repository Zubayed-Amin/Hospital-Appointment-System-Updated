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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
public class UserAppointmentController implements Initializable {

    @FXML
    private ImageView logoImage;
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
    private Button bookappointBtn;
    @FXML
    private Button myappointBtn;
    @FXML
    private Button logoutBtn;

    private String patientUsername;
    
    private int patient_id;
    public void setPatientId(int id) {
    this.patient_id = id;
    }

    public void setPatientUsername(String username) {
        this.patientUsername = username;
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    this.patient_id = rs.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        hourComboBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
//
//        minComboBox.getItems().addAll("00", "30");
//
//        ampmComboBox.getItems().addAll("AM", "PM");

        loadDepartments();
        onDoctorOrDateChanged();

        // 🔄 When a department is selected, show related doctors + description
        deptComboBox.setOnAction(e -> {
            String selectedDept = deptComboBox.getValue();
            if (selectedDept != null) {
                loadDoctorsAndDepartmentDescription(selectedDept);
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

    
    private void loadDoctorsAndDepartmentDescription(String department) {
        doctorComboBox.getItems().clear();
        String deptDescription = "";

        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT fullname, description FROM users WHERE role = 'doctor' AND department = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, department);
            ResultSet rs = stmt.executeQuery();

            boolean first = true;
            while (rs.next()) {
                doctorComboBox.getItems().add(rs.getString("fullname"));

                // Only take description from first doctor found
                if (first) {
                    deptDescription = rs.getString("description");
                    first = false;
                }
            }

            tfDesc.setText(deptDescription != null ? deptDescription : "");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onDoctorOrDateChanged() {
        String doctor = doctorComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (doctor == null || selectedDate == null) return;

        hourComboBox.getItems().clear();
        minComboBox.getItems().clear();
        ampmComboBox.getItems().clear();

        try (Connection conn = ConnectionDB.getConnection()) {
            // Step 1: Get doctor’s duty time
            String dutySql = "SELECT duty_time FROM users WHERE fullname = ?";
            PreparedStatement dutyStmt = conn.prepareStatement(dutySql);
            dutyStmt.setString(1, doctor);
            ResultSet dutyRs = dutyStmt.executeQuery();

            if (dutyRs.next()) {
                String dutyTime = dutyRs.getString("duty_time"); // Example: 7:00AM-12:00PM
                String[] dutySplit = dutyTime.split("-");
                String startTime = dutySplit[0];
                String endTime = dutySplit[1];

                // Convert to 24-hour
                java.time.LocalTime start = convertTo24Hour(startTime);
                java.time.LocalTime end = convertTo24Hour(endTime);

                // Step 2: Get booked appointment times
                String bookedSql = "SELECT appointment_time FROM appointment WHERE doctor_name = ? AND appointment_date = ?";
                PreparedStatement bookedStmt = conn.prepareStatement(bookedSql);
                bookedStmt.setString(1, doctor);
                bookedStmt.setDate(2, java.sql.Date.valueOf(selectedDate));
                ResultSet bookedRs = bookedStmt.executeQuery();

                java.util.List<String> booked = new java.util.ArrayList<>();
                while (bookedRs.next()) {
                    booked.add(bookedRs.getString("appointment_time"));
                }

                // Step 3: Generate available slots every 30 mins
                java.time.LocalTime current = start;
                while (!current.isAfter(end.minusMinutes(30))) {
                    String slot = formatTo12Hour(current);
                    if (!booked.contains(slot)) {
                        String[] parts = slot.split("[: ]");
                        hourComboBox.getItems().add(parts[0]);
                        if (!minComboBox.getItems().contains(parts[1])) {
                            minComboBox.getItems().add(parts[1]);
                        }
                        if (!ampmComboBox.getItems().contains(parts[2])) {
                            ampmComboBox.getItems().add(parts[2]);
                        }
                    }
                    current = current.plusMinutes(30);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private java.time.LocalTime convertTo24Hour(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
        return java.time.LocalTime.parse(timeStr.toUpperCase().replaceAll("\\s+", ""), formatter);
    }

    private String formatTo12Hour(java.time.LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        return time.format(formatter);
    }  

    @FXML
    private void BookAppointment(ActionEvent event) throws SQLException {
        String department = deptComboBox.getValue();
        String doctor = doctorComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String hour = hourComboBox.getValue();
        String minute = minComboBox.getValue();
        String ampm = ampmComboBox.getValue();
        if (department == null || doctor == null || date == null || hour == null || minute == null || ampm == null) {
            System.out.println("Please fill in all fields.");
            return;
        }

        String time = hour + ":" + minute + " " + ampm;
        
        String patientFullname = null;

        try (Connection conn = ConnectionDB.getConnection()) {
            String fetchNameSQL = "SELECT fullname FROM users WHERE username = ?";
            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchNameSQL)) {
                fetchStmt.setString(1, patientUsername);
                ResultSet rs = fetchStmt.executeQuery();
                if (rs.next()) {
                    patientFullname = rs.getString("fullname");
                } else {
                    System.out.println("Patient not found.");
                    return;
                }
            }

            String sql = "INSERT INTO appointment (patient_name, doctor_name, doctor_contact, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?, 'pending')";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientFullname);
            ps.setString(2, doctor);
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, time);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Appointment booked successfully.");
                } else {
                    System.out.println("Failed to book appointment.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void MyAppointment(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewAppointment.fxml"));
        Parent root = loader.load();

        // Pass username to ViewAppointmentsController
        ViewAppointmentController controller = loader.getController();
        controller.setUsername(patientUsername); // ✅ Passing the logged-in username

        // Show the new scene
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("View Appointments");
        stage.show();
    }

    @FXML
    private void LogOut(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Login Form");
        stage.show();

        // Close current window
        ((Stage) logoutBtn.getScene().getWindow()).close();
    }

    
}
