import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthorizationController implements Initializable {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button loginButton;
    @FXML
    public Hyperlink registrationButton;
    @FXML
    public Label error;


    private static final NetworkBySingleton network = NetworkBySingleton.getInstance();
    private DataInputStream in;
    private DataOutputStream out;

    /**
     * Метод обрабатывает нажатие кнопки LOGIN
     */
    public void clickLoginButton() {
        sendAuthorizationRequest();
    }

    /**
     * Метод обрабатывает нажатие клавиши ENTER при фокусе на поле ввода пароля.
     * @param keyEvent - событие вызывается нажатием клавиш, при фокусе на поле ввода пароля
     */
    public void pressEnter(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)){
            sendAuthorizationRequest();
        }
    }

    /**
     * Метод отправлеяет запрос на авторизацию в формате "/authorization login password"
     */
    private void sendAuthorizationRequest() {
        try {
            out.writeUTF("/authorization " + loginField.getText() + " " + passwordField.getText());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickRegistrationButton() {
        // TODO: 31.05.2020 Реализовать интерфейс регистрации нового пользователя
    }

    /**
     * Открытие основного окна чата
     */
    private void openChatWindow(){
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("chat-client.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        network.connect("localhost", 8189);
        in = network.getInputStream();
        out = network.getOutputStream();

        Thread thread = new Thread(()->{
            String message;

            while (true) {
                try {
                    message = in.readUTF();
                    System.out.println(message);

                    if (message.equals("/authorizationConfirmed")) {
                        Platform.runLater(this::openChatWindow);
                        Platform.runLater(()-> loginButton.getScene().getWindow().hide());
                        break;
                    }
                    if (message.equals("/authorizationError")) {
                        Platform.runLater(()-> error.setText("The login or password incorrect."));
                    }
                } catch (IOException e) {
                    System.out.println("Потеря связи с сервером");
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}

