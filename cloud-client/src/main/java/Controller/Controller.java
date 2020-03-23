package Controller;

import Json.*;
import File.*;
import GuiHelper.*;
import Protocol.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private FileService fileService;
    public static String pathToFileOfUser;
    public String filename;

    private static final Controller CONTROLLER = new Controller();

    public static Controller getController() {
        return CONTROLLER;
    }

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
    public Button sendButtonFromClient;
    @FXML
    public Button sendButtonFromServer;
    @FXML
    public Button deleteOnClient;
    @FXML
    public Button deleteOnServer;
    @FXML
    public Button renameOnClient;
    @FXML
    public Button refreshOnServer;

    @FXML
    public TableView<FileAbout> localFilesTable;
    @FXML
    public TableView<FileAbout> cloudFilesTable;

    public static ObservableList<FileAbout> localFilesList;
    public static ObservableList<FileAbout> cloudFilesList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.fileService = new FileService(this);
            localFilesList = FXCollections.observableArrayList();
            cloudFilesList = FXCollections.observableArrayList();
            GuiHelper.prepareTableView(localFilesTable);
            GuiHelper.prepareTableView(cloudFilesTable);
            localFilesTable.setItems(localFilesList);
            cloudFilesTable.setItems(cloudFilesList);
        } catch (Exception e) {
            showError(e);
        }
    }

    public void refreshFilesList() {
        Platform.runLater(() -> {
            try {
                localFilesList.clear();
                cloudFilesList.clear();
                localFilesList.addAll(Files.list(Paths.get("client_storage")).map(Path::toFile).map(FileAbout::new).collect(Collectors.toList()));
                ProtocolRefreshFiles.refreshFile(pathToFileOfUser, Network.getInstance().getCurrentChannel());
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
        filename = localFilesTable.getSelectionModel().getSelectedItem().getName();
        if (filename != null && !filename.equals("")) fileService.sendFile(Paths.get("client_storage/" + filename));
    }

    public void sendFromServerButtonAction(ActionEvent actionEvent) throws IOException {
        filename = cloudFilesTable.getSelectionModel().getSelectedItem().getName();
        if (filename != null && !filename.equals("")) fileService.receiveFile(filename);
    }

    public void deleteOnClientButtonAction(ActionEvent actionEvent) throws IOException {
        filename = localFilesTable.getSelectionModel().getSelectedItem().getName();
        if (filename != null && !filename.equals("")) fileService.deleteLocalFiles(filename);
    }

    public void deleteOnServerButtonAction(ActionEvent actionEvent) throws IOException {
        filename = cloudFilesTable.getSelectionModel().getSelectedItem().getName();
        if (filename != null && !filename.equals("")) fileService.deleteCloudFiles(filename);
    }

    public void renameOnClientButtonAction(ActionEvent actionEvent) throws IOException {
        filename = localFilesTable.getSelectionModel().getSelectedItem().getName();
        String result = JOptionPane.showInputDialog("Введите новое имя файла:");
        if (filename != null && !filename.equals("") && result != null && !result.equals("")) fileService.renameFiles(filename, result);
    }

    public void refreshOnAllButtonAction(ActionEvent actionEvent) {
        refreshFilesList();
    }

    public void sendAuth (ActionEvent actionEvent) throws IOException, InterruptedException {
        fileService.startConnectionToServer();
        String login = loginField.getText();
        String password = passField.getText();
        pathToFileOfUser = loginField.getText() + "/";
        AuthMessage msg = new AuthMessage();
        msg.login = login;
        msg.password = password;
        Message authMsg = Message.createAuth(msg);
        ProtocolAuthSend.authSend(authMsg.toJson(), Network.getInstance().getCurrentChannel());
    }

    public void Exit(ActionEvent actionEvent) {
        shutdown();
    }

    public void shutdown() {
        try {
            fileService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}