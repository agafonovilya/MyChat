import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Server {

    private final static int PORT = 8189;
    private final static String HOST = "localhost";
    private static int cnt = 1;

    private boolean running;
    private static ConcurrentLinkedDeque<ClientHandler> clients;

    public static ConcurrentLinkedDeque<ClientHandler> getClients() {
        return clients;
    }

    public Server(int port) {
        running = true;
        clients = new ConcurrentLinkedDeque<>();
        try (ServerSocket srv = new ServerSocket(PORT)) {
            System.out.println("Server started!");
            while (running) {
                Socket socket = srv.accept();
                ClientHandler client = new ClientHandler(socket, "client #" + cnt);
                cnt++;
                clients.add(client); // can produce CME (concurrent modification exception)
                System.out.println(client.getNickName() + " accepted!");
                new Thread(client).start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server(PORT);
    }
}
