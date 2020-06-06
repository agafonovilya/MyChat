import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Server {

    private final static int PORT = 8189;
    private String lastConnectedNickName;

    private static ConcurrentLinkedDeque<ClientHandler> clients;

    public static ConcurrentLinkedDeque<ClientHandler> getClients() {
        return clients;
    }

    public Server(int port) {
        clients = new ConcurrentLinkedDeque<>();
        try (ServerSocket srv = new ServerSocket(port)) {
            System.out.println("Server started!");
            while (true) {
                Socket socket = srv.accept();

                if (auth(socket)){
                    ClientHandler client = new ClientHandler(socket, lastConnectedNickName);
                    clients.add(client); // can produce CME (concurrent modification exception)
                    System.out.println(client.getNickName() + " accepted!");
                    new Thread(client).start();
                }
            }
        } catch (Exception e) {
            System.out.println("Неудачная попытка авторизации.");
        }
    }

    private boolean auth(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        while (true){
            if(socket.isConnected()) {
                String clientMessage = in.readUTF();
                System.out.println(clientMessage);

                if (clientMessage.startsWith("/authorization")) { //приватное сообщение
                    String[] splitClientMessage = clientMessage.split(" ", 3);

                    if (isUserDataConfirmed(splitClientMessage[1], splitClientMessage[2])) {
                        System.out.println("Успешная авторизация");
                        out.writeUTF("/authorizationConfirmed");
                        out.flush();
                        lastConnectedNickName = splitClientMessage[1];
                        break;

                    } else {
                        out.writeUTF("/authorizationError");
                        out.flush();
                        System.out.println("Авторизация отклонена");
                    }



                }
            }
        }

        return true;
    }

    public boolean isUserDataConfirmed(String login, String password)  {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String passwordFromDB = null;

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:authorizationDB.db")) {

            PreparedStatement passwordRequest = connection.prepareStatement("select Password from Users where Login=?;");
            passwordRequest.setString(1, login);
            ResultSet resultSet = passwordRequest.executeQuery();
            passwordFromDB = resultSet.getString("Password");


        } catch (SQLException throwable) {
            System.out.println("Ошибка. Возможно запрашиваемый пользователь не найден в базе данных");
        }

        if (passwordFromDB != null) {
            return passwordFromDB.equals(password);
        } else {
            return false;
        }



    }


    public static void main(String[] args) {
        new Server(PORT);
    }
}
