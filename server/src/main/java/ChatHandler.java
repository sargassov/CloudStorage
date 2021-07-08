import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatHandler implements Runnable{

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private byte buffer[];
    //private FileInputStream fis;
    //private FileOutputStream fos;
    //private BufferedInputStream bis;
    //private BufferedOutputStream bos;
    private final int bufferSize = 256;
    private String root = "server/serverFiles";

    public ChatHandler(Socket socket) {
        this.socket = socket;
        buffer = new byte[bufferSize];
    }

    @Override
    public void run() {
        try{
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            while(true){
                processFileMessage();
            }
        } catch (IOException e){
            System.err.println("Client disconnected");
        } finally {
            try {
                dis.close();
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void processFileMessage() throws IOException {
        String fileName = dis.readUTF();
        System.out.println("Received filename " + fileName);
        long size = dis.readLong();
        System.out.println("Received filesize " + size);
        try(FileOutputStream fos = new FileOutputStream(root + "/"+ fileName)){
            for (int i = 0; i < (size + 255)/ bufferSize; i++) {
                int read = dis.read(buffer);
                fos.write(buffer, 0, read);
            }
        }

        dos.writeUTF("File " + fileName + " received");
    }

//    void sendFile(File file) throws IOException {
//        buffer = new byte[8192];
//        try{
//            fis = new FileInputStream(file);
//            bis = new BufferedInputStream(fis);
//            bos = new BufferedOutputStream(socket.getOutputStream());
//            int count;
//            while ((count = bis.read(buffer)) > 0){
//                bos.write(buffer, 0, count);
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        } finally {
//            fis.close();
//            bos.close();
//            bis.close();
//        }
//    }

//    void receiveFile(String fileName) throws IOException {
//        try {
//            is = socket.getInputStream();
//            bufferSize = socket.getReceiveBufferSize();
//            System.out.println("Buffer size = " + bufferSize);
//            fos = new FileOutputStream(fileName);
//            bos = new BufferedOutputStream(fos);
//            byte[] bytes = new byte[bufferSize];
//            int count;
//            while((count = fis.read(bytes)) >= 0){
//                bos.write(bytes, 0, count);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            bos.close();
//            fis.close();
//        }
//    }


}
