package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;
import static dao.DbConnectivityClass.status;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

public class DB_GUI_Controller implements Initializable {

    StorageUploader store = new StorageUploader();
    @FXML
    ProgressBar progressBar;
    @FXML
    private ComboBox<Major> majorComboBox;
    @FXML
    private Label statusLabel;
    @FXML
    TextField first_name, last_name, department, major, email, imageURL;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    @FXML
    private Button addBtn, editBtn, deleteBtn;
    @FXML
    private MenuItem editItem, deleteItem;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            editBtn.setDisable(true);
            deleteBtn.setDisable(true);

            editItem.setDisable(true);
            deleteItem.setDisable(true);

            tv.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean hasSelection = (newSelection != null);
                editBtn.setDisable(!hasSelection);
                deleteBtn.setDisable(!hasSelection);
                editItem.setDisable(!hasSelection);
                deleteItem.setDisable(!hasSelection);
            });

            first_name.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
            last_name.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
            email.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
            department.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
            imageURL.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

            majorComboBox.getItems().addAll(Major.values());
            majorComboBox.setValue(Major.CS);
            majorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());

            first_name.textProperty().addListener((obs, oldVal, newVal) -> validateField(first_name, "^[A-Za-z//s]+$"));
            last_name.textProperty().addListener((obs, oldVal, newVal) -> validateField(last_name, "^[A-Za-z//s]+$"));
            email.textProperty().addListener((obs, oldVal, newVal) -> validateField(email, "^[a-zA-Z0-9._%+-]+@farmingdale.edu$"));

            newRowAddition();
            editableColumns();
            tv.setEditable(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validateField(TextField field, String regex) {
        if (!field.getText().matches(regex)) {
            field.setStyle("-fx-border-color: red;");
        } else {
            field.setStyle("");
        }
        validateForm();
    }

    private void validateForm() {
        boolean isValid = !first_name.getText().isEmpty() && !last_name.getText().isEmpty() && !email.getText().isEmpty() && !department.getText().isEmpty() && majorComboBox.getValue() != null && !imageURL.getText().isEmpty();
        addBtn.setDisable(!isValid);
    }

    @FXML
    protected void addNewRecord() {
        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(), majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.insertUser(p);
        cnUtil.retrieveId(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();

        statusLabel.setText("Record added successfully");
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() ->
                        statusLabel.setText(""));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        majorComboBox.setValue(null);
        email.clear();
        imageURL.clear();
        statusLabel.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(), majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);

        statusLabel.setText("Record edited successfully");
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() ->
                        statusLabel.setText(""));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
            Task<Void> uploadTask = createUploadTask(file, progressBar);
            progressBar.progressProperty().bind(uploadTask.progressProperty());
            new Thread(uploadTask).start();
        }
    }


    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        majorComboBox.setValue(Major.valueOf(p.getMajor()));
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(results.fname + " " + results.lname + " " + results.major);
        });
    }

    public enum Major {CS, CPIS, EGL, MTH, STS, BIO, PSY}

    private static class Results {
        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100);
                    }
                }

                return null;
            }
        };
    }


    @FXML
    protected void importCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Import CSV File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            try (Scanner sc = new Scanner(file)) {
                sc.nextLine();
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!line.isEmpty()) {
                        String[] parts = line.split(",");
                        cnUtil.insertUser(new Person(parts[0], parts[1], parts[2], parts[3], parts[4], ""));
                    }
                }
                statusLabel.setText("CSV file imported successfully.");
                tv.setItems(cnUtil.getData());
            } catch (FileNotFoundException e) {
                statusLabel.setText("Error importing CSV: file not found.");
            } catch (Exception e) {
                statusLabel.setText("Error importing CSV: " + e.getMessage());
            }
        } else {
            statusLabel.setText("No file selected");
        }
    }


    @FXML
    protected void exportCSV() throws IOException {
        System.out.println("ExportCSV");
        FileWriter fw = new FileWriter("src/main/resources/export.csv");
        File file = new File("src/main/resources/export.csv");
        file.createNewFile();

        fw.write("firstname,lastname,department,major,email\n");
        fw.write(cnUtil.stringAllUsers());

        status = "Export to " + file.getAbsolutePath();
        statusLabel.setText(status);
        fw.close();
        FileChooser fc = new FileChooser();
        fc.setTitle("Export CSV File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            statusLabel.setText("Data exported to CSV successfully");
        }
    }
    private void newRowAddition() {
        tv.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2 && tv.getSelectionModel().getSelectedItem() == null) {
                Person newPerson = new Person("","","","","","");
                data.add(newPerson);
                tv.getSelectionModel().select(newPerson);
                tv.edit(tv.getItems().size()-1,tv_fn);
            }
        });
    }

    private void editableColumns(){
        tv_fn.setCellFactory(TextFieldTableCell.forTableColumn());
        tv_ln.setCellFactory(TextFieldTableCell.forTableColumn());
        tv_department.setCellFactory(TextFieldTableCell.forTableColumn());
        tv_major.setCellFactory(TextFieldTableCell.forTableColumn());
        tv_email.setCellFactory(TextFieldTableCell.forTableColumn());

        tv_fn.setOnEditCommit(event -> {
            Person person = event.getRowValue();
            person.setFirstName(event.getNewValue());
            updatePersonInDatabase(person);
        });
        tv_ln.setOnEditCommit(event -> {
            Person person = event.getRowValue();
            person.setLastName(event.getNewValue());
            updatePersonInDatabase(person);
        });
        tv_department.setOnEditCommit(event -> {
            Person person = event.getRowValue();
            person.setDepartment(event.getNewValue());
            updatePersonInDatabase(person);
        });
        tv_major.setOnEditCommit(event -> {
            Person person = event.getRowValue();
            person.setMajor(event.getNewValue());
            updatePersonInDatabase(person);
        });
        tv_email.setOnEditCommit(event -> {
            Person person = event.getRowValue();
            person.setEmail(event.getNewValue());
            updatePersonInDatabase(person);
        });
    }

    private void updatePersonInDatabase(Person person) {
        cnUtil.editUser(person.getId(),person);
        statusLabel.setText("Record updated successfully");
    }
    @FXML
    protected void copyEntry(){
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null){
            tv.getSelectionModel().clearSelection();
            Person copiedPerson = new Person(
        p.getFirstName() +"_copy",
                    p.getLastName() +"_copy",
                    p.getDepartment(),
                    p.getMajor(),
                    generateUniqueEmail(p.getEmail()),
                            p.getImageURL());
        cnUtil.insertUser(copiedPerson);
        data.add(copiedPerson);
        statusLabel.setText("Copied user successfully");
        tv.refresh();
    } else{
            statusLabel.setText("No user selected to copy");
        }
}

private String generateUniqueEmail(String originalEmail) {
    String [] parts = originalEmail.split("@");
    return parts[0] + "_copy@" + parts[1];
    }
}