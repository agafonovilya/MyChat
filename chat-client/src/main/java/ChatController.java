import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.commons.io.input.ReversedLinesFileReader;

import javax.swing.*;


public class ChatController implements Initializable {

    ObservableList<String> test = FXCollections.observableArrayList();
    @FXML
    public ListView<String> outputField = new ListView<String>(test);
    @FXML
    public TextField entryField;
    @FXML
    public ListView<String> listOfMembers;

    private static NetworkBySingleton network = NetworkBySingleton.getInstance();
    private DataInputStream in;
    private DataOutputStream out;

    private File history;


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

        //network.connect("localhost", 8189); //удалить когда будет переход на окно авторизации
        in = network.getInputStream();
        out = network.getOutputStream();

        history = new File("chat-client/history.txt");

        try {
            history.createNewFile();
        } catch (IOException e) {
            System.out.println("Ошибка создания файла");
        }
        
        try {
            BufferedReader readHistoryFromFile = new BufferedReader(new FileReader(history));
            while(true) {
                String line = readHistoryFromFile.readLine();
                if(line == null){
                    break;
                }
                outputField.getItems().addAll(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


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
                            String finalMessage2 = message;
                            Platform.runLater(()->{
                            listOfMembers.getItems().remove(finalMessage2.substring(25));
                            });
                        }

                        if (message.startsWith("/addToListOfMembers ")) {
                            String finalMessage1 = message;
                            Platform.runLater(()->{
                            listOfMembers.getItems().addAll(finalMessage1.substring(20));
                            });
                        }

                    } else { //иначе принимаем как обычное сообщение
                        Date date = new Date();
                        SimpleDateFormat formatOfDate = new SimpleDateFormat("yy.MM.dd HH:mm:ss");

                        String finalMessage = formatOfDate.format(date) + " " + message;
                        Platform.runLater(()->{
                            outputField.getItems().addAll(finalMessage);
                        });

                        writeMessageToFile(history, finalMessage);


                    }
                } catch (IOException e) {
                    outputField.getItems().addAll("Потеря связи с сервером");
                    break;
                }

            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void writeMessageToFile(File history, String Message) {
        //Запись полученного сообщения в файл
        try {
            PrintWriter saveMessageToFile = new PrintWriter(new FileOutputStream(history,true));
            saveMessageToFile.println(Message);
            saveMessageToFile.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void clickToMember(MouseEvent mouseEvent) {
        // TODO: 11.05.2020 По нажатии на имя участника в списке участников,
        //  добавлять его имя в поле ввода для отправки приватных сообщений
    }
}
