import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Authorization.fxml"));
        //Parent root = FXMLLoader.load(getClass().getResource("chat-client.fxml"));
        primaryStage.setTitle("MyChat");
        primaryStage.setScene(new Scene(root) );
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
