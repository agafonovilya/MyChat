import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ExampleClient {
    public static void main(String[] args) {
        try(Socket socket = new Socket("localhost", 8189)){
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            boolean running = true;
            Scanner cin = new Scanner(System.in);

            Thread thread = new Thread(()->{
                while (running) {
                    String message = null;
                    try {
                        message = in.readUTF();
                        if (message.equals("_exit_")) {
                            in.close();
                            out.close();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(message);
                }
            });
            thread.setDaemon(true);
            thread.start();

            while(running) {
                String line = cin.nextLine();
                if (line.equals("exit")) {
                    out.writeUTF("_exit_");
                    out.flush();
                    break;
                }
                out.writeUTF(line);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
