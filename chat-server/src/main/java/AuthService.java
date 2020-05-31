import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AuthService implements Runnable{

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private static ConcurrentLinkedDeque<ClientHandler> clients;

    public static ConcurrentLinkedDeque<ClientHandler> getClients() {
        return clients;
    }



    public AuthService(Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public boolean isUserDataConfirmed(String login, String password)  {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String passwordFromDB = null;

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:authorizationDB.db")) {

            PreparedStatement passwordRequest = connection.prepareStatement("select Password from Users where Login=?");
            passwordRequest.setString(1, login);
            ResultSet resultSet = passwordRequest.executeQuery();
            passwordFromDB = resultSet.getString("Password");


        } catch (SQLException throwables) {
            System.out.println("Ошибка. Возможно запрашиваемый пользователь не найден в базе данных");
        }

        if (passwordFromDB != null) {
            return passwordFromDB.equals(password);
        } else {
            return false;
        }



    }


    @Override
    public synchronized void run() {
        while (true) {
            try {
                if (socket.isConnected()) {
                    String clientMessage = in.readUTF();

                    System.out.println(clientMessage);

                    if (clientMessage.startsWith("/authorization")) { //приватное сообщение
                        String[] splitClientMessage = clientMessage.split(" ", 3);

                        if (isUserDataConfirmed(splitClientMessage[1], splitClientMessage[2])) {
                            System.out.println("Успешная авторизация");
                            out.writeUTF("/authorizationConfirmed");
                            out.flush();

                            clients = new ConcurrentLinkedDeque<>();
                            ClientHandler client = new ClientHandler(socket, splitClientMessage[1]);
                            clients.add(client);
                            System.out.println(client.getNickName() + " accepted!");
                            new Thread(client).start();

                        } else System.out.println("Авторизация отклонена");


                    }

                }
            } catch (IOException e) {
                System.out.println("Неудачная попытка авторизации");
                try {
                    socket.close();
                    in.close();
                    out.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    break;
                }

                break;
            }
        }
    }
}
