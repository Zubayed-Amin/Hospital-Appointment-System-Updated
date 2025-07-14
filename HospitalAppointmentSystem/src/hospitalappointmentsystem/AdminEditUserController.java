/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package hospitalappointmentsystem;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author zubay
 */
public class AdminEditUserController implements Initializable {

    @FXML
    private VBox leftPane;
    @FXML
    private ImageView logoImage;
    @FXML
    private TextField tfFullname;
    @FXML
    private TextField tfUsername;
    @FXML
    private TextField tfContact;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private PasswordField pfConfirmPassword;
    @FXML
    private ComboBox<String> comboBoxRole;
    @FXML
    private VBox doctorFields;
    @FXML
    private TextField tfDept;
    @FXML
    private TextField tfDesc;
    @FXML
    private ComboBox<String> SthourComboBox;
    @FXML
    private ComboBox<String> StminComboBox;
    @FXML
    private ComboBox<String> StampmComboBox;
    @FXML
    private ComboBox<String> EndhourComboBox;
    @FXML
    private ComboBox<String> EndminComboBox;
    @FXML
    private ComboBox<String> EndampmComboBox;
    @FXML
    private Button signupBtn;
    @FXML
    private Hyperlink SigninLink;
    @FXML
    private ImageView logoImage1;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void SignUp(ActionEvent event) {
    }

    @FXML
    private void goToLogin(ActionEvent event) {
    }
    
}
