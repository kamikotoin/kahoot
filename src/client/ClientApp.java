package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    private static Stage primary;

    @Override
    public void start(Stage stage) throws Exception {
        primary = stage;
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/client/views/login.fxml"));
        Scene scene = new Scene(fxml.load());
        stage.setScene(scene);
        stage.setTitle("ToDo Client");
        stage.show();
    }

    public static void switchScene(Scene s) {
        primary.setScene(s);
    }

    public static void main(String[] args) {
        launch();
    }
}
