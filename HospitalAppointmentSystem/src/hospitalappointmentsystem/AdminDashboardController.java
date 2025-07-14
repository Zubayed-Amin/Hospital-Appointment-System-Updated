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
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author zubay
 */
public class AdminDashboardController implements Initializable {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> colID;
    @FXML
    private TableColumn<User, String> colFullname;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colPass;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, String> colContact;
    @FXML
    private Button addBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private Button viewAppointBtn;

    ObservableList<User> userList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFullname.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPass.setCellValueFactory(new PropertyValueFactory<>("password"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        loadUserData();
    } 
    
    private void loadUserData() {
        userList.clear();
        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "SELECT * FROM users";
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                userList.add(new User(
                    rs.getInt("id"),
                    rs.getString("fullname"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("contact")
                ));
            }
            userTable.setItems(userList);
        } catch (Exception e) {
            e.printStackTrace();
    }
}
    
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle("DocSetGo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("Styles.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("glass-background");
        alert.showAndWait();
    }

    @FXML
    private void AddUser(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminAddUser.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Add User Form");
        stage.show();
        ((Stage) addBtn.getScene().getWindow()).close();
    }

    @FXML
    private void EditUser(ActionEvent event) {
        
    }

    @FXML
    private void DeleteUser(ActionEvent event) {
        Object selectedItem = userTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.ERROR, "No user selected.");
            return;
        }

        int userId = ((User) selectedItem).getId();

        try (Connection conn = ConnectionDB.getConnection()) {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "User deleted successfully.");
            userTable.getItems().remove(selectedItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ViewApointment(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminView.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin View");
        stage.show();
        ((Stage) viewAppointBtn.getScene().getWindow()).close();
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
