package com.uptech.accounted.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import com.uptech.accounted.bean.User;
import com.uptech.accounted.config.StageManager;
import com.uptech.accounted.service.UserServiceImpl;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

@Controller
public class UserController implements Initializable {

  @FXML
  private Label userId;

  @FXML
  private TextField firstName;

  @FXML
  private TextField lastName;

  @FXML
  private DatePicker dob;

  @FXML
  private RadioButton rbMale;

  @FXML
  private ToggleGroup gender;

  @FXML
  private RadioButton rbFemale;

  @FXML
  private ComboBox<String> cbRole;
  Callback<TableColumn<User, Boolean>, TableCell<User, Boolean>> cellFactory = new Callback<TableColumn<User, Boolean>, TableCell<User, Boolean>>() {
    @Override
    public TableCell<User, Boolean> call(final TableColumn<User, Boolean> param) {
      final TableCell<User, Boolean> cell = new TableCell<User, Boolean>() {
        final Button btnEdit = new Button();
        Image imgEdit = new Image(getClass().getResourceAsStream("/images/edit.png"));

        @Override
        public void updateItem(Boolean check, boolean empty) {
          super.updateItem(check, empty);
          if (empty) {
            setGraphic(null);
            setText(null);
          } else {
            btnEdit.setOnAction(e -> {
              User user = getTableView().getItems().get(getIndex());
              updateUser(user);
            });

            btnEdit.setStyle("-fx-background-color: transparent;");
            ImageView iv = new ImageView();
            iv.setImage(imgEdit);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setCache(true);
            btnEdit.setGraphic(iv);

            setGraphic(btnEdit);
            setAlignment(Pos.CENTER);
            setText(null);
          }
        }

        private void updateUser(User user) {
          userId.setText(Long.toString(user.getId()));
          firstName.setText(user.getFirstName());
          lastName.setText(user.getLastName());
          dob.setValue(user.getDob());
          if (user.getGender().equals("Male"))
            rbMale.setSelected(true);
          else
            rbFemale.setSelected(true);
          cbRole.getSelectionModel().select(user.getRole());
        }
      };
      return cell;
    }
  };
  @FXML
  private TextField email;
  @FXML
  private PasswordField password;
  @FXML
  private Button reset;
  @FXML
  private Button saveUser;
  @FXML
  private TableView<User> userTable;
  @FXML
  private TableColumn<User, Long> colUserId;
  @FXML
  private TableColumn<User, String> colFirstName;
  @FXML
  private TableColumn<User, String> colLastName;
  @FXML
  private TableColumn<User, LocalDate> colDOB;
  @FXML
  private TableColumn<User, String> colGender;
  @FXML
  private TableColumn<User, String> colRole;
  @FXML
  private TableColumn<User, String> colEmail;
  @FXML
  private TableColumn<User, Boolean> colEdit;
  @FXML
  private MenuItem deleteUsers;
  @Lazy
  @Autowired
  private StageManager stageManager;
  @Autowired
  private UserServiceImpl userService;
  private ObservableList<User> userList = FXCollections.observableArrayList();
  private ObservableList<String> roles = FXCollections.observableArrayList("Admin", "User");

  @FXML
  private void exit(ActionEvent event) {
    Platform.exit();
  }

  @FXML
  void reset(ActionEvent event) {
    clearFields();
  }

  private void clearFields() {
    userId.setText(null);
    firstName.clear();
    lastName.clear();
    dob.getEditor().clear();
    rbMale.setSelected(true);
    rbFemale.setSelected(false);
    cbRole.getSelectionModel().clearSelection();
    email.clear();
    password.clear();
  }

  @FXML
  private void saveUser(ActionEvent event) {
    User user = new User();
    user.setFirstName(getFirstName());
    user.setLastName(getLastName());
    user.setDob(getDob());
    user.setGender(getGender());
    user.setRole(getRole());
    user.setEmail(getEmail());
    user.setPassword(getPassword());

    User newUser = userService.save(user);

    saveAlert(newUser);
  }

  private void saveAlert(User user) {

    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("User saved successfully.");
    alert.setHeaderText(null);
    alert.setContentText("The user " + user.getFirstName() + " " + user.getLastName() + " has been created and \n"
        + getGenderTitle(user.getGender()) + " id is " + user.getId() + ".");
    alert.showAndWait();
  }

  private String getGenderTitle(String gender) {
    return (gender.equals("Male")) ? "his" : "her";
  }

  private void updateAlert(User user) {

    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("User updated successfully.");
    alert.setHeaderText(null);
    alert.setContentText("The user " + user.getFirstName() + " " + user.getLastName() + " has been updated.");
    alert.showAndWait();
  }

  public String getFirstName() {
    return firstName.getText();
  }

  public String getLastName() {
    return lastName.getText();
  }

  public LocalDate getDob() {
    return dob.getValue();
  }

  public String getGender() {
    return rbMale.isSelected() ? "Male" : "Female";
  }

  public String getRole() {
    return cbRole.getSelectionModel().getSelectedItem();
  }

  public String getEmail() {
    return email.getText();
  }

  public String getPassword() {
    return password.getText();
  }

  private void loadUserDetails() {
    userList.clear();
    userTable.setItems(userList);
  }

  private boolean validate(String field, String value, String pattern) {
    if (!value.isEmpty()) {
      Pattern p = Pattern.compile(pattern);
      Matcher m = p.matcher(value);
      if (m.find() && m.group().equals(value)) {
        return true;
      } else {
        validationAlert(field, false);
        return false;
      }
    } else {
      validationAlert(field, true);
      return false;
    }
  }

  private void validationAlert(String field, boolean empty) {
    Alert alert = new Alert(AlertType.WARNING);
    alert.setTitle("Validation Error");
    alert.setHeaderText(null);
    if (field.equals("Role"))
      alert.setContentText("Please Select " + field);
    else {
      if (empty)
        alert.setContentText("Please Enter " + field);
      else
        alert.setContentText("Please Enter Valid " + field);
    }
    alert.showAndWait();
  }

  private boolean emptyValidation(String field, boolean empty) {
    if (!empty) {
      return true;
    } else {
      validationAlert(field, true);
      return false;
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    cbRole.setItems(roles);

    userTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    setColumnProperties();

    loadUserDetails();
  }

  private void setColumnProperties() {

    colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
    colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
    colDOB.setCellValueFactory(new PropertyValueFactory<>("dob"));
    colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
    colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    colEdit.setCellFactory(cellFactory);
  }
}