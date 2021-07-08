import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static ServerSocket serverSocket;
    private static Socket socket;

    public static void main(String[] args) throws IOException {

        serverSocket = new ServerSocket(8189);
        System.out.println("Server started");
        while(true){
            socket = serverSocket.accept();
            System.out.println("Client accepted");
            new Thread(new ChatHandler(socket)).start();
        }

    }
}
