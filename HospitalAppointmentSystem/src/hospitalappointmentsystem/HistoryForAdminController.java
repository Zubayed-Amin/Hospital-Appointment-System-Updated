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
public class HistoryForAdminController implements Initializable {
    @FXML
    private Button backBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private TableView<AdminAppointment> appointmentTable;
    @FXML
    private TableColumn<AdminAppointment, String> colPName;
    @FXML
    private TableColumn<AdminAppointment, String> colDName;
    @FXML
    private TableColumn<AdminAppointment, String> colDate;
    @FXML
    private TableColumn<AdminAppointment, String> colTime;
    @FXML
    private TableColumn<AdminAppointment, String> colStatus;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Dashboard");
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
