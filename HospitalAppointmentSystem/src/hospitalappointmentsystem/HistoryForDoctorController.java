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
public class HistoryForDoctorController implements Initializable {
    @FXML
    private Button logoutBtn;
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
    private Button backBtn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Doctor Dashboard");
        stage.show();
        ((Stage) backBtn.getScene().getWindow()).close();
    }
    
}
