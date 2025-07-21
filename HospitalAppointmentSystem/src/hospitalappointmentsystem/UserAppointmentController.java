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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
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

        loadDepartments();
        doctorComboBox.setOnAction(e -> onDoctorOrDateChanged());
        datePicker.setOnAction(e -> onDoctorOrDateChanged());

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
        tfDesc.clear();

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
                    tfDesc.setText(rs.getString("description"));
                    first = false;
                }
            }

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
            String dutySql = "SELECT duty_time FROM users WHERE fullname = ?";
            try (PreparedStatement dutyStmt = conn.prepareStatement(dutySql)) {
                dutyStmt.setString(1, doctor);
                ResultSet dutyRs = dutyStmt.executeQuery();

                if (dutyRs.next()) {
                    String dutyTime = dutyRs.getString("duty_time");
                    String[] dutySplit = dutyTime.split("-");
                    LocalTime start = convertTo24Hour(dutySplit[0]);
                    LocalTime end = convertTo24Hour(dutySplit[1]);

                    String bookedSql = """
                            SELECT appointment_time FROM appointment 
                            WHERE doctor_name = ? AND appointment_date = ?
                            UNION
                            SELECT appointment_time FROM guest_appointment 
                            WHERE doctor_name = ? AND appointment_date = ?
                        """;
                    try (PreparedStatement bookedStmt = conn.prepareStatement(bookedSql)) {
                        bookedStmt.setString(1, doctor);
                        bookedStmt.setDate(2, java.sql.Date.valueOf(selectedDate));
                        bookedStmt.setString(3, doctor);
                        bookedStmt.setDate(4, java.sql.Date.valueOf(selectedDate));
                        ResultSet bookedRs = bookedStmt.executeQuery();

                        Set<String> booked = new HashSet<>();
                        while (bookedRs.next()) {
                            booked.add(bookedRs.getTime("appointment_time").toLocalTime().toString());  // 24-hour format, like "14:30"
                        }

                        LocalTime current = start;
                        while (!current.isAfter(end.minusMinutes(30))) {
                            String slotKey = current.toString(); // e.g. "14:30"
                            if (!booked.contains(slotKey)) {
                                String slotDisplay = formatTo12Hour(current); // for showing in combo boxes
                                String[] parts = slotDisplay.split("[: ]");
                                hourComboBox.getItems().add(parts[0]);
                                if (!minComboBox.getItems().contains(parts[1])) minComboBox.getItems().add(parts[1]);
                                if (!ampmComboBox.getItems().contains(parts[2])) ampmComboBox.getItems().add(parts[2]);
                            }

                            current = current.plusMinutes(30);
                            System.out.println("Duty Start: " + start);
                            System.out.println("Duty End: " + end);

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalTime convertTo24Hour(String timeStr) {
    timeStr = timeStr.trim().toUpperCase().replaceAll("\\s+", ""); // Remove spaces
    DateTimeFormatter formatter;

    if (timeStr.contains(":")) {
        formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
    } else {
        formatter = DateTimeFormatter.ofPattern("ha", Locale.ENGLISH); // e.g., 2PM
    }

    return LocalTime.parse(timeStr, formatter);
    }


    private String formatTo12Hour(LocalTime time) {
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

        String timeStr = hour + ":" + minute + " " + ampm;
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        LocalTime timeObj = LocalTime.parse(timeStr, inputFormatter);
        String time = timeObj.toString();
        
        String patientFullname = null;
        String patientContact = null;

        try (Connection conn = ConnectionDB.getConnection()) {
            // Check if selected time is within duty hours
            String dutySql = "SELECT duty_time FROM users WHERE fullname = ?";
            try (PreparedStatement dutyStmt = conn.prepareStatement(dutySql)) {
                dutyStmt.setString(1, doctor);
                ResultSet dutyRs = dutyStmt.executeQuery();

                if (dutyRs.next()) {
                    String[] dutySplit = dutyRs.getString("duty_time").split("-");
                    LocalTime start = convertTo24Hour(dutySplit[0]);
                    LocalTime end = convertTo24Hour(dutySplit[1]);

                    if (timeObj.isBefore(start) || timeObj.isAfter(end.minusMinutes(30))) {
                        System.out.println("Selected time is outside doctor's duty hours.");
                        return;
                    }
                }
            }

            String fetchNameSQL = "SELECT fullname, contact FROM users WHERE username = ?";
            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchNameSQL)) {
                fetchStmt.setString(1, patientUsername);
                ResultSet rs = fetchStmt.executeQuery();
                if (rs.next()) {
                    patientFullname = rs.getString("fullname");
                    patientContact = rs.getString("contact");
                } else {
                    System.out.println("Patient not found.");
                    return;
                }
            }

            String sql = "INSERT INTO appointment (patient_name, doctor_name, patient_contact, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientFullname);
            ps.setString(2, doctor);
            ps.setString(3, patientContact);
            ps.setDate(4, java.sql.Date.valueOf(date));
            ps.setString(5, time);

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
        ((Stage) logoutBtn.getScene().getWindow()).close();
    }

    
}
