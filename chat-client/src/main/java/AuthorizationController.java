import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import java.net.Socket;
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

    /*private Network network = Client.network;
    private Socket socket = network.socket;
    private DataInputStream in = network.in;
    private DataOutputStream out = network.out;*/
    private Network network;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void clickLoginButton(ActionEvent actionEvent) {
        // TODO: 31.05.2020 Реализовать передачу логина и пароля в чат
        //отправляем запрос на авторизацию
        try {
            out.writeUTF("/authorization " + loginField.getText() + " " + passwordField.getText());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*if ( isUserDataConfirmed(loginField.getText())) {
            loginButton.getScene().getWindow().hide();
            openChatWindow();
        } else {
            // TODO: 31.05.2020 Вывод информации о неверном логине и пароле
            error.setText("The login or password incorrect.");
        }*/
    }

    public void clickRegistrationButton(ActionEvent actionEvent) {
        // TODO: 31.05.2020 Реализовать интерфейс регистрации нового пользователя
    }
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

    /*
     * Метод делает запрос в базу данных по логину и получает в ответ пароль.
     * Если пароль совпадает с тем, что введен в поле ввода пароля, то метод возвращает true.
     * @param login
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException

    private boolean isUserDataConfirmed(String login)  {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String password = null;
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:authorizationDB.db")) {

            PreparedStatement passwordRequest = connection.prepareStatement("select Password from Users where Login=?");
            passwordRequest.setString(1, login);
            ResultSet resultSet = passwordRequest.executeQuery();
            password = resultSet.getString("Password");

        } catch (SQLException throwables) {
            System.out.println("Ошибка. Возможно запрашиваемый пользователь не найден в базе данных");
        }

        if (password != null) {
            return password.equals(passwordField.getText());
        } else {
            return false;
        }



    } */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*network = new Network();
        in = network.getIn();
        out = network.getOut();
        socket = network.getSocket();*/

        network = Client.network;
        socket = network.socket;
        in = network.in;
        out = network.out;

        Thread thread = new Thread(()->{
            String message;

            while (true) {
                try {
                    message = in.readUTF();

                    System.out.println(message);

                    if (message.startsWith("/")) { //распознаем сообщения с командами
                        if (message.equals("/authorizationConfirmed")) {
                            Platform.runLater(()->{
                                loginButton.getScene().getWindow().hide();
                            });
                            openChatWindow();
                            break;
                        }

                        if (message.equals("/authorisationError")) {
                            error.setText("The login or password incorrect.");
                        }


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

