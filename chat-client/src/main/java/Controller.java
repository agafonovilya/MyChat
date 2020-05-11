import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;


public class Controller implements Initializable {

    public ListView<String> outputField;
    public TextField entryField;
    public ListView<String> listOfMembers;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    /**
     * Обработка нажатия на клавишу "Send".
     * @param actionEvent
     */
    public void clickSend(ActionEvent actionEvent) {
        sendMessage();
    }

    /**
     * Обработка нажатия на клавишу Enter
     * @param keyEvent
     */
    public void keyListener(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER) ) {
            sendMessage();
        }
    }

    /**
     * Метод отправляет сообщение на сервер.
     * Затем переносит сообщение из поля ввода в поле переписки, добавляя к нему текущие дату и время.
     */
    private void sendMessage() {
        if (!entryField.getText().equals("")) {
            try {
                out.writeUTF(entryField.getText());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Date date = new Date();
            SimpleDateFormat formatOfDate = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
            outputField.getItems().addAll(formatOfDate.format(date) + " " + entryField.getText());

            entryField.clear();
            entryField.requestFocus();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        entryField.requestFocus();


        try{
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            boolean running = true;

            Thread thread = new Thread(()->{
                String message;

                while (running) {
                    try {
                        message = in.readUTF();

                        if (message.startsWith("/")) { //распознаем сообщения с командами
                            if (message.equals("/exit")) {
                                in.close();
                                out.close();
                                break;
                            }

                            if (message.startsWith("/deleteFromListOfMembers ")){
                                // TODO: 11.05.2020 Реализовать удаление отключившихся пользователей
                            }

                            if (message.startsWith("/addToListOfMembers ")) {
                                listOfMembers.getItems().addAll(message.substring(20));
                            }

                        } else { //иначе принимаем как обычное сообщение
                            Date date = new Date();
                            SimpleDateFormat formatOfDate = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
                            //String mes = formatOfDate.format(date) + " " + message + "\n";
                            outputField.getItems().addAll(formatOfDate.format(date) + " " + message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickToMember(MouseEvent mouseEvent) {
        // TODO: 11.05.2020 По нажатии на имя участника в списке участников,
        //  добавлять его имя в поле ввода для отправки приватных сообщений
    }
}
