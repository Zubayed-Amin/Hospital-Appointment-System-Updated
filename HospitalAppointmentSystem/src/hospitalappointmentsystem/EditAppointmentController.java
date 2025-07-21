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

    public void setAppointmentId(int id) {
        this.appointmentId = id;
        loadAppointmentData(id); // Load data immediately once ID is set
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

                    String bookedSql = "SELECT appointment_time FROM appointment WHERE doctor_name = ? AND appointment_date = ?";
                    try (PreparedStatement bookedStmt = conn.prepareStatement(bookedSql)) {
                        bookedStmt.setString(1, doctor);
                        bookedStmt.setDate(2, java.sql.Date.valueOf(selectedDate));
                        ResultSet bookedRs = bookedStmt.executeQuery();

                        Set<String> booked = new HashSet<>();
                        while (bookedRs.next()) {
                            booked.add(bookedRs.getString("appointment_time"));
                        }

                        LocalTime current = start;
                        while (!current.isAfter(end.minusMinutes(30))) {
                            String slot = formatTo12Hour(current);
                            if (!booked.contains(slot)) {
                                String[] parts = slot.split("[: ]");
                                hourComboBox.getItems().add(parts[0]);
                                if (!minComboBox.getItems().contains(parts[1])) minComboBox.getItems().add(parts[1]);
                                if (!ampmComboBox.getItems().contains(parts[2])) ampmComboBox.getItems().add(parts[2]);
                            }
                            current = current.plusMinutes(30);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalTime convertTo24Hour(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
        return LocalTime.parse(timeStr.toUpperCase().replaceAll("\\s+", ""), formatter);
    }

    private String formatTo12Hour(LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        return time.format(formatter);
    }

    // ✅ Load appointment by ID
    public void loadAppointmentData(int id) {
        this.appointmentId = id;
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = """
                SELECT a.patient_name, a.patient_contact, a.doctor_name,
                       u.department, a.appointment_date, a.appointment_time
                FROM appointment a
                JOIN users u ON a.doctor_name = u.fullname
                WHERE a.id = ?
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
                loadDoctorsAndDepartmentDescription(department); // 🟢 Load the doctors for that dept
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
                UPDATE appointment
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
                System.out.println("Appointment updated in guest_appointment table.");
            } else {
                System.out.println("No changes made.");
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
