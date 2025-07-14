/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package hospitalappointmentsystem;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author zubay
 */
public class AdminViewController implements Initializable {

    @FXML
    private TableView<DocAppointment> appointmentTable;
    @FXML
    private TableColumn<DocAppointment, String> colPName;
    @FXML
    private TableColumn<DocAppointment, String> colPcontact;
    @FXML
    private TableColumn<DocAppointment, LocalDate> colDate;
    @FXML
    private TableColumn<DocAppointment, String> colTime;
    @FXML
    private TableColumn<DocAppointment, String> colStatus;
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
    private void goBack(ActionEvent event) {
        
    }

    @FXML
    private void Logout(ActionEvent event) {
        
    }
    
}
