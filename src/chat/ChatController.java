package chat;


import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ChatController {

    public TextArea outputField;
    public TextField entryField;



    /**
     * Обработка нажатия на клавишу "Send".
     * @param actionEvent
     */
    public void clickSend(ActionEvent actionEvent) {
        messageRecording();
    }

    /**
     * Обработка нажатия на клавишу Enter
     * @param keyEvent
     */
    public void keyListener(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER) ) {
            messageRecording();
        }
    }

    /**
     * Метод переносит сообщение из поля ввода в поле переписки, добавляя к нему текущие дату и время.
     */
    private void messageRecording() {
        if (!entryField.getText().equals("")) {
            Date date = new Date();
            SimpleDateFormat formatOfDate = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
            outputField.appendText(formatOfDate.format(date) + " " + entryField.getText() + "\n");
            entryField.clear();
        }
    }

}
