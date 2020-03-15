import Controller.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class Client extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        ScreenManager.setStage(stage);
        ScreenManager.showLoginScreen();

//        stage.setTitle("Client Cloud Storage");
//        // stage.getIcons().add(new Image("/icon.png"));
//        FXMLLoader loader = new FXMLLoader();
//        loader.setLocation(getClass().getResource("/scene.fxml"));
//        Parent root = loader.load();
//        Controller controller = loader.getController();
//        stage.setOnHidden(e -> controller.shutdown());
//        stage.setScene(new Scene(root));
//        stage.setResizable(false);
//        stage.setX(900);
//        stage.setY(400);
//        stage.show();
    }
}