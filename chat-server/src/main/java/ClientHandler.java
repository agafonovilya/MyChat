import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickName;
    private boolean running;


    public ClientHandler(Socket socket, String nickName) throws IOException {
        this.socket = socket;
        this.nickName = nickName;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        running = true;
        welcome();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * Оповещение всех участников о том, что подключился новый клиент. Новый клиент так же получает список всех участников.
     * @throws IOException
     */
    public synchronized void welcome() throws IOException {
        for (ClientHandler client : Server.getClients()) {
            client.sendMessage("/addToListOfMembers " + this.nickName);
            this.sendMessage("/addToListOfMembers " + client.getNickName());
        }
        out.writeUTF("Hello " + nickName);
        out.flush();
    }

    /**
     * Отправляет комманду всем участникам о том, что необходимо удалить участника из списка подключенных участников
     * @throws IOException
     */
    private void sendCommandToDelete() throws IOException {
        Server.getClients().remove(this);
        for (ClientHandler client : Server.getClients()) {
            client.sendMessage("/deleteFromListOfMembers " + this.nickName);
        }
    }

    /**
     * Метод отправляет сообщение всем участникам чата
     * @param message
     * @throws IOException
     */
    public synchronized void broadCastMessage(String message) throws IOException {
        for (ClientHandler client : Server.getClients()) {
            if (!client.equals(this)) {
                client.sendMessage(this.nickName + "> " + message);
           }
        }
    }

    /**
     * Метод отправки приватных сообщений
     * @param message
     * @param nick
     * @throws IOException
     */
    public synchronized void privateMessage(String message, String nick) throws IOException {
        for (ClientHandler client : Server.getClients()) {
            if (nick.equals(client.getNickName())) {
                client.sendMessage("#" + this.nickName + " " + message);
            }
        }
    }

    /**
     * Отправка сообщения
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        out.writeUTF(message.trim());
        out.flush();
    }

    @Override
    public synchronized void run() {
        while (running) {
            try {
                if (socket.isConnected()) {
                    String clientMessage = in.readUTF();

                    if (clientMessage.equals("/exit")) { //Запрос на отключение клиента
                        sendMessage(clientMessage);
                        sendCommandToDelete();
                        break;
                    }

                    if (clientMessage.startsWith("#")) { //приватное сообщение
                        String[] splitClientMessage = clientMessage.split(" ", 2);
                        splitClientMessage[0] = splitClientMessage[0].substring(1);//удаляем @
                        privateMessage(splitClientMessage[1], splitClientMessage[0]);
                    } else {
                        broadCastMessage(clientMessage);
                    }

                }
            } catch (IOException e) {
                System.out.println("Потеряна связь с клиентом: " + this.nickName);
                try {
                    socket.close();
                    in.close();
                    out.close();
                    sendCommandToDelete();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    break;
                }

                break;
            }
        }
    }

}
