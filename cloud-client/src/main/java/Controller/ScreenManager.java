package Controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ScreenManager {

    private static Stage stage;

    public static void setStage(Stage newStage){
        stage = newStage;
    }

    public static void showLoginScreen(){
        Platform.runLater(()->{
            stage.close();
            try {
                stage.setTitle("Client Cloud Storage");
                // stage.getIcons().add(new Image("/icon.png"));
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ScreenManager.class.getResource("/scene.fxml"));
                Parent root = loader.load();
                Controller controller = loader.getController();
                stage.setOnHidden(e -> controller.shutdown());
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.setX(900);
                stage.setY(400);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void showWorkFlowScreen(){
        Platform.runLater(()->{
            // stage.close();
            try {
                stage.setTitle("Client Cloud Storage");
                // stage.getIcons().add(new Image("/icon.png"));
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ScreenManager.class.getResource("/work.fxml"));
                Parent root = loader.load();
                Controller controller = loader.getController();
                stage.setOnHidden(e -> controller.shutdown());
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.setX(900);
                stage.setY(400);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
