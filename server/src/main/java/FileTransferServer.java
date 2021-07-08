import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class FileTransferServer {

    private static ServerSocket serverSocket;
    private static File file;
    private static Socket socket;
    private static byte[] buffer;
    private static BufferedOutputStream bos;
    private static BufferedReader br;
    private static FileOutputStream fos;
    private static InputStream is;
    private final static int port = 8199;
    private static int bufferSize;
    private final static String name = "pic.jpg";

    public static void main(String[] args) throws IOException {
        try{
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(20000);
            while (true) {
                // Подключение к порту. По сути, начало работы сервера.
                socket = serverSocket.accept();
                // Получение данных от клиента.
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = br.readLine();
                // Ответ клиенту.
                PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            try {
                if (e instanceof SocketTimeoutException) {
                    throw new SocketTimeoutException();
                } else {
                    e.printStackTrace();
                }
            } catch (SocketTimeoutException ste) {
                System.out.println("Turn off the server by timeout");
            }
        }
    }
}
