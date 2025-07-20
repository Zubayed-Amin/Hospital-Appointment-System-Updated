/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package hospitalappointmentsystem;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author zubay
 */
public class HistoryForPatientController implements Initializable {

    @FXML
    private TableView<PatAppointment> appointmentTable;
    @FXML
    private TableColumn<PatAppointment, String> colDName;
    @FXML
    private TableColumn<PatAppointment, String> colDcontact;
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UserAppointment.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("User Appointment Dashboard");
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
