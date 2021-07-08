import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    public TextField statusBar;
    public Button button;
    public ListView<String> listViewClient;
    private String root = "client/clientFiles";

    //private InputStream is;
    //private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private byte[] buffer;
    private static File file;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[256];
        try{
            File directory = new File(root);
            listViewClient.getItems().clear();
            listViewClient.getItems().addAll(directory.list());
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());


            Thread readThread = new Thread(()->{
                try{
                    while(true){
                        String status = dis.readUTF();
                        Platform.runLater(()->{
                            statusBar.setText(status);
                        });

                    }
                } catch (Exception e){
                    System.err.println("Exception while read");
                    e.printStackTrace();

                }

            });
            readThread.setDaemon(true);
            readThread.start();


        } catch (IOException e){
            e.printStackTrace();
        }
    }



    public void send(ActionEvent actionEvent) throws IOException {
        String fileName = listViewClient.getSelectionModel().getSelectedItem();
        Path filePath = Paths.get(root, fileName);
        long size = Files.size(filePath);
        dos.writeUTF(fileName);
        dos.writeLong(size);
        Files.copy(filePath, dos);
        dos.flush();
        statusBar.setText("File " + fileName + " sent to server.");
    }


}
