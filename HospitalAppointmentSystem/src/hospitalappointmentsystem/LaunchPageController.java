/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package hospitalappointmentsystem;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author zubay
 */
public class LaunchPageController implements Initializable {

    @FXML
    private VBox leftPane;
    @FXML
    private ImageView logoImage;
    @FXML
    private Button guestBtn;
    @FXML
    private Label lblError;
    @FXML
    private Button loginBtn;
    @FXML
    private Hyperlink SignupLink;
    @FXML
    private ImageView logoImage1;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        playEntryAnimation();
    }    

    private void playEntryAnimation() {
        // Set initial opacity and position
        leftPane.setOpacity(0); // Start fully transparent
        leftPane.setTranslateX(500); // Start off-screen to the left

        // Slide animation
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(1), leftPane);
        slideIn.setFromX(500);
        slideIn.setToX(0);

        // Fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), leftPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Play both animations together
        ParallelTransition parallel = new ParallelTransition(slideIn, fadeIn);
        parallel.play();
    }
    
    @FXML
    private void Guest(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BookGuestAppointment.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Guest Appointment Dashboard");
        stage.show();
        ((Stage) guestBtn.getScene().getWindow()).close();
    }

    @FXML
    private void Login(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Login Form");
        stage.show();
        ((Stage) loginBtn.getScene().getWindow()).close();
    }

    @FXML
    private void goToSignup(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SignUp.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Sign Up Form");
        stage.show();
        ((Stage) SignupLink.getScene().getWindow()).close();
    }
    
}
