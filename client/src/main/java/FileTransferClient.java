import java.io.*;
import java.net.Socket;

public class FileTransferClient {
    private static Socket socket;
    private static File file;
    private static byte[] buffer;
    private static InputStream is;
    private static FileInputStream fis;
    private final static String address = "localhost";
    private final static int port = 8199;
    private final static int bufferSize = 8192;
    private final static String pic = "src/pic.jpg";
    private static BufferedInputStream bis;
    private static BufferedOutputStream bos;


    //FileInputStream fis;
    //BufferedInputStream bis;
    //BufferedOutputStream out;
   // byte[] buffer = new byte[8192];


    public static void main(String[] argv) throws IOException {

        socket = new Socket(address, port);
        buffer = new byte[bufferSize];
        is = socket.getInputStream();
        file = new File(pic);
        fis = new FileInputStream(file);
        System.out.println();

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(socket.getOutputStream());
            int count;
            while ((count = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, count);

            }
            bos.close();
            fis.close();
            bis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bos.close();

    }
}
