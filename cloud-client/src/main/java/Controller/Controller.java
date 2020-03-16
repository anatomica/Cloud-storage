package Controller;

import Json.*;
import Protocol.ProtocolAuthSend;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private FileService fileService;
    public static String pathToFileOfUser;
    public String filename;

    @FXML
    public MenuItem closeButton;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passField;
    @FXML
    public VBox imageBox;
    @FXML
    public HBox authPanel;
    @FXML
    public BorderPane workPanel;

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

    private static final Controller CONTROLLER = new Controller();

    public static Controller getController() {
        return CONTROLLER;
    }

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
                File directory = new File("server_storage/" + pathToFileOfUser);
                if (!directory.exists()) directory.mkdir();
                // Files.createDirectory(Paths.get("server_storage/" + nickname));
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListOnClient.getItems().add(o));
                Files.list(Paths.get("client_storage")).map(Path::toFile).map(File::length).forEach(o -> sizeListOnClient.getItems().add((o) + " bytes"));
                Files.list(Paths.get("server_storage/" + pathToFileOfUser)).map(p -> p.getFileName().toString()).forEach(o -> filesListOnServer.getItems().add(o));
                Files.list(Paths.get("server_storage/" + pathToFileOfUser)).map(Path::toFile).map(File::length).forEach(o -> sizeListOnServer.getItems().add((o) + " bytes"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void showError (Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Нет соединения с сервером!");
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

    public void sendFromClientButtonAction(ActionEvent actionEvent) throws IOException {
        filename = filesListOnClient.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.sendFile(Paths.get("client_storage/" + filename));
    }

    public void sendFromServerButtonAction(ActionEvent actionEvent) throws IOException {
        filename = filesListOnServer.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.receiveFile(filename);
    }

    public void deleteOnClientButtonAction(ActionEvent actionEvent) throws IOException {
        filename = filesListOnClient.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.deleteFile(filename, "client_storage/");
    }

    public void deleteOnServerButtonAction(ActionEvent actionEvent) throws IOException {
        filename = filesListOnServer.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.deleteFile(filename, "server_storage/" + pathToFileOfUser);
    }

    public void refreshOnAllButtonAction(ActionEvent actionEvent) {
        refreshFilesList();
    }

    public void sendAuth (ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String password = passField.getText();
        pathToFileOfUser = loginField.getText() + "/";
        AuthMessage msg = new AuthMessage();
        msg.login = login;
        msg.password = password;
        Message authMsg = Message.createAuth(msg);
        ProtocolAuthSend.authSend(authMsg.toJson(), Network.getInstance().getCurrentChannel());
    }

    public void shutdown() {
        try {
            fileService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}