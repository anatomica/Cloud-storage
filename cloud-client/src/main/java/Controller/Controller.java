package Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private FileService fileService;

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
    public TextField tfFileName;
    public String filename;

    @FXML
    public ListView<String> filesListOnClient;
    @FXML
    public ListView<String> filesListOnServer;

    @FXML
    public Button receiveButtonFromServer;
    @FXML
    public Button sendButtonFromClient;
    @FXML
    public Button receiveButtonFromClient;
    @FXML
    public Button sendButtonFromServer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.fileService = new FileService(this);
        } catch (Exception e) {
            showError(e);
        }
    }

    public void refreshFilesList() {
        updateUI(() -> {
            try {
                filesListOnClient.getItems().clear();
                filesListOnServer.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListOnClient.getItems().add(o));
                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListOnServer.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
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
    
    public void receiveFromServerButtonAction(ActionEvent actionEvent) {
        filename = filesListOnServer.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.receiveFile(filename);
    }

    public void sendFromClientButtonAction(ActionEvent actionEvent) throws IOException, InterruptedException {
        filename = filesListOnClient.getSelectionModel().getSelectedItem();
        if (filename != null && !filename.equals("")) fileService.sendFile(Paths.get("client_storage/" + filename));
    }

    public void receiveFromClientButtonAction(ActionEvent actionEvent) {

    }

    public void sendFromServerButtonAction(ActionEvent actionEvent) {

    }
}