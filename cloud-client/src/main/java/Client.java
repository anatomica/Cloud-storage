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
    }
}