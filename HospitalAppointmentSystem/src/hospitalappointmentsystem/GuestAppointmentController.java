package hospitalappointmentsystem;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class GuestAppointmentController implements Initializable {

    @FXML
    private ImageView logoImage;
    @FXML
    private Label Pname;
    @FXML
    private Label Pnum;
    @FXML
    private Label Dname;
    @FXML
    private Label Dnum;
    @FXML
    private Label AppDate;
    @FXML
    private Label AppTime;

    private int appointmentId; // Optional, if needed for editing
    private String guestContact;
    @FXML
    private Button backBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Optional: load data here if guestContact is preset
    }

    // Call this method from EditAppointmentController after updating
    public void loadGuestAppointment(String contact) {
        this.guestContact = contact;

        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = """
                SELECT g.id, a.patient_name, g.patient_contact, g.doctor_name,
                       u.contact AS doctor_contact,
                       g.appointment_date, g.appointment_time
                FROM guest_appointment g
                JOIN users u ON g.doctor_name = u.fullname
                WHERE g.patient_contact = ?
                ORDER BY g.id DESC
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, contact);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                appointmentId = rs.getInt("id"); // Store for editing
                Pname.setText(rs.getString("patient_name"));
                Pnum.setText(rs.getString("patient_contact"));
                Dname.setText(rs.getString("doctor_name"));
                Dnum.setText(rs.getString("doctor_contact"));
                AppDate.setText(rs.getString("appointment_date"));
                AppTime.setText(rs.getString("appointment_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setAppointmentFromDB(int appointmentId) {
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = """
                SELECT g.patient_name, g.patient_contact, g.doctor_name, 
                       u.contact AS doctor_contact, 
                       g.appointment_date, g.appointment_time
                FROM guest_appointment g
                JOIN users u ON g.doctor_name = u.fullname
                WHERE g.id = ?
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Pname.setText(rs.getString("patient_name"));
                Pnum.setText(rs.getString("patient_contact"));
                Dname.setText(rs.getString("doctor_name"));
                Dnum.setText(rs.getString("doctor_contact"));
                AppDate.setText(rs.getString("appointment_date"));
                AppTime.setText(rs.getString("appointment_time"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    // When Back button is clicked
    @FXML
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LaunchPage.fxml")); // or previous screen
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Launch Page");
        stage.show();

        // Close current
        ((Stage) Pname.getScene().getWindow()).close();
    }
}
