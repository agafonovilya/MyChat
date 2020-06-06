import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ChatController implements Initializable {

    @FXML
    public ListView<String> outputField;
    @FXML
    public TextField entryField;
    @FXML
    public ListView<String> listOfMembers;
    @FXML
    public Button sendButton;


    private static final NetworkBySingleton network = NetworkBySingleton.getInstance();

    private DataInputStream in;
    private DataOutputStream out;

    private File history;


    /**
     * Обработка нажатия на клавишу "Send".
     */
    public void clickSend() {
        sendMessage();
    }

    /**
     * Обработка нажатия на клавишу Enter
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
            String finalMessage = formatOfDate.format(date) + " " + entryField.getText();
            outputField.getItems().addAll(finalMessage);
            writeMessageToFile(history, finalMessage);
            entryField.clear();
            entryField.requestFocus();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        entryField.requestFocus();

        in = network.getInputStream();
        out = network.getOutputStream();

        readHistoryFromFile();
        setEventToDoubleClickOnMembers();

        Thread thread = new Thread(this::readMessageFromServer);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Считываем историю сообщений в окно чата
     */
    private void readHistoryFromFile() {
        history = new File("chat-client/history.txt");

        try {
            history.createNewFile();
            BufferedReader readHistoryFromFile = new BufferedReader(new FileReader(history));
            while(true) {
                String line = readHistoryFromFile.readLine();
                if(line == null){
                    break;
                }
                outputField.getItems().addAll(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения истории сообщений");
        }
    }

    /**
     * При двойном нажатии на участника чата в списке участников,
     * имя добавляется в поле ввода для отправки приватного сообщения
     */
    private void setEventToDoubleClickOnMembers() {
        listOfMembers.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2) {
                String nick = listOfMembers.getSelectionModel().getSelectedItems().get(0);
                if (nick != null){
                    entryField.setText("@" + nick + " ");
                    entryField.requestFocus();
                }
            }
        });
    }

    /**
     * Запись сообщения в файл
     * @param history - ссылка на файл
     * @param Message - записываемое сообщение
     */
    private void writeMessageToFile(File history, String Message) {
        //Запись полученного сообщения в файл
        try (PrintWriter saveMessageToFile = new PrintWriter(new FileOutputStream(history,true))) {
            saveMessageToFile.println(Message);
            saveMessageToFile.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readMessageFromServer() {
        String message;

        while (true) {
            try {
                message = in.readUTF();

                if (message.startsWith("/")) { //распознаем сообщения с командами
                    if (message.equals("/exit")) {
                        in.close();
                        out.close();
                        break;
                    }
                    if (message.startsWith("/deleteFromListOfMembers ")) {
                        String finalMessage2 = message;
                        Platform.runLater(() -> listOfMembers.getItems().remove(finalMessage2.substring(25)));
                    }
                    if (message.startsWith("/addToListOfMembers ")) {
                        String finalMessage1 = message;
                        Platform.runLater(() -> listOfMembers.getItems().addAll(finalMessage1.substring(20)));
                    }
                } else { //иначе принимаем как обычное сообщение
                    Date date = new Date();
                    SimpleDateFormat formatOfDate = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
                    String finalMessage = formatOfDate.format(date) + " " + message;
                    Platform.runLater(() -> outputField.getItems().addAll(finalMessage));
                    writeMessageToFile(history, finalMessage);
                }
            } catch (IOException e) {
                outputField.getItems().addAll("Потеря связи с сервером");
                break;
            }
        }
    }
}
