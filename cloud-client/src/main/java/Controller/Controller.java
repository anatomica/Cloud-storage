package Controller;

import Json.*;
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
import java.util.ResourceBundle;

public class Controller implements Initializable {

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

    private FileService fileService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.fileService = new FileService(this);
        } catch (Exception e) {
            showError(e);
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

    public void sendButtonAction(ActionEvent actionEvent) {
        fileService.sendFile(new File("Answers.txt"));
    }
}