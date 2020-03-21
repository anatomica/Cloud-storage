package Controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private FileService fileService;
    public String filename;

    @FXML
    public MenuItem closeButton;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passField;
    @FXML
    public HBox authPanel;
    @FXML
    public VBox chatPanel;
    @FXML
    public Button sendButton;

    @FXML
    public ListView<String> filesListOnClient;
    @FXML
    public ListView<String> sizeListOnClient;
    @FXML
    public ListView<String> filesListOnServer;
    @FXML
    public ListView<String> sizeListOnServer;

    @FXML
    public Button sendButtonFromClient;
    @FXML
    public Button sendButtonFromServer;
    @FXML
    public Button deleteOnClient;
    @FXML
    public Button deleteOnServer;
    @FXML
    public Button refreshOnClient;
    @FXML
    public Button refreshOnServer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.fileService = new FileService(this);
        } catch (Exception e) {
            showError(e);
        }
    }

    public void refreshFilesList() {
        Platform.runLater(() -> {
            try {
                filesListOnClient.getItems().clear();
                sizeListOnClient.getItems().clear();
                filesListOnServer.getItems().clear();
                sizeListOnServer.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListOnClient.getItems().add(o));
                Files.list(Paths.get("client_storage")).map(Path::toFile).map(File::length).forEach(o -> sizeListOnClient.getItems().add((o) + " bytes"));
                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListOnServer.getItems().add(o));
                Files.list(Paths.get("server_storage")).map(Path::toFile).map(File::length).forEach(o -> sizeListOnServer.getItems().add((o) + " bytes"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void showError (Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Упс! ЧТо-то пошло не так!");
        alert.setHeaderText(e.getMessage());
        VBox dialogPaneContent = new VBox();
        Label label = new Label("Stack Trace:");
        String stackTrace = ExceptionUtils.getStackTrace(e);
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);
        dialogPaneContent.getChildren().addAll(label, textArea);
        alert.getDialogPane().setContent(dialogPaneContent);
        alert.setResizable(true);
        alert.showAndWait();
        e.printStackTrace();
    }

    public void shutdown() {
        try {
            fileService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFromClientButtonAction(ActionEvent actionEvent) throws IOException, InterruptedException {
        filename = filesListOnClient.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.sendFile(Paths.get("client_storage/" + filename));
    }

    public void sendFromServerButtonAction(ActionEvent actionEvent) {
        filename = filesListOnServer.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.receiveFile(filename);
    }

    public void deleteOnClientButtonAction(ActionEvent actionEvent) throws IOException {
        filename = filesListOnClient.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.deleteFile(filename, "client_storage/");
    }

    public void deleteOnServerButtonAction(ActionEvent actionEvent) throws IOException {
        filename = filesListOnServer.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.deleteFile(filename, "server_storage/");
    }

    public void refreshOnAllButtonAction(ActionEvent actionEvent) {
        refreshFilesList();
    }
}