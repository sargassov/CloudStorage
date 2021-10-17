import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    @FXML public TextField serverPath;
    @FXML public TextField clentPath;
    @FXML public Button pathOutClient;
    @FXML public Button pathOutServer;
    @FXML Button removeClientFile, removeServerFile;
    @FXML ListView<String> clientFileList;
    @FXML ListView<String> serverFileList;
    @FXML HBox mainWorkPanel;
    @FXML VBox authentication;
    @FXML TextField loginField;
    @FXML PasswordField passwordField;
    @FXML Label authLabel;
    private static final String ROOT = "client";
    private Path currentClientDir = Paths.get(ROOT).toAbsolutePath();
    private boolean isAuthorized = true;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(true);
        Network.start();
        refreshClientView();
        addNavigationListeners();

        Thread thread = new Thread(() -> {
            try{

                while (!isAuthorized) {
                    Command command = (Command) Network.getIn().readObject();
                    if (command.getCommandType().equals(CommandType.AUTH_REQUEST)) {
                        AuthRequest authRequest = (AuthRequest) command;
                        if (Reply.AUTH_OK.equals(authRequest.getMessage())) {
                            setAuthorized(true);
                            break;
                        }
                        if (Reply.NULL_USER_ID.equals(authRequest.getMessage())) {
                            Platform.runLater(() -> authLabel.setText("WRONG LOGIN OR PASS"));
                        }
                    }
                }

                Network.sendMsg(new ListResponce());

                Thread t = new Thread(()->{ // отдельный поток для отображения изменений директории клиента
                    while (true){           // раз в секунду
                        try {
                            refreshClientView();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.setDaemon(true);
                t.start();

                while (true){
                    Command command = (Command) Network.getIn().readObject();

                    if(command.getCommandType().equals(CommandType.LIST_RESPONCE)){
                        ListResponce listResponce = (ListResponce) command;
                        ArrayList<String> names = listResponce.getServerFileList();
                        refreshServerView(names);
                    }

                    if(command.getCommandType().equals(CommandType.PATH_RESPONCE)){
                        PathResponce pathResponce = (PathResponce) command;
                        String path = pathResponce.getPath().substring("server/serverFiles/".length());
                        Platform.runLater(()->{
                            serverPath.setText(path);
                        });
                    }

                    if(command.getCommandType().equals(CommandType.FILE_MESSAGE)) {
                        FileMessage fileMessage = (FileMessage) command;
                        Files.write(currentClientDir.resolve(fileMessage.getFilename()), fileMessage.getData());
                        refreshClientView();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
//            try {
//                while (true) {
//                    Command command = Network.readObject();
//                    if (command instanceof AuthMessage) {
//                        AuthMessage authMessage = (AuthMessage) command;
//                        if (Reply.AUTH_OK.equals(authMessage.getMessage())) {
//                            setAuthorized(true);
//                            break;
//                        }
//                        if (Reply.NULL_USER_ID.equals(authMessage.getMessage())) {
//                            Platform.runLater(() -> authLabel.setText("WRONG LOGIN OR PASS"));
//                        }
//                    }
//                }
//
//                Network.sendMsg(new ListResponce());
//
//                while (true) {
//                    Command command = Network.readObject();
//                    if (command instanceof FileMessage) {
//                        FileMessage fileMessage = (FileMessage) command;
//                        Files.write(Paths.get(ROOT + fileMessage.getFilename()),
//                                fileMessage.getData(), StandardOpenOption.CREATE); //StandardOpenOption.CREATE всегда оздаёт/перезаписывает новые объекты
//                        refreshClientView();
//                    }
//                    if (command instanceof ListResponce) {
//                        ListResponce refreshServerMsg = (ListResponce) command;
//                        renewServerFiles(refreshServerMsg.getServerFileList());
//                    }
//                }
//            } catch (ClassNotFoundException | IOException ex) {
//                ex.printStackTrace();
//            } finally {
//                Network.stop();
//            }
        });
        thread.setDaemon(true);
        thread.start();
        refreshClientView();
    }

    private void addNavigationListeners() {
        clientFileList.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2){
                String item = clientFileList.getSelectionModel().getSelectedItem();
                Path newPath = currentClientDir.resolve(item);

                if(Files.isDirectory(newPath)){
                    try {
                        currentClientDir = newPath;
                        refreshClientView();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        serverFileList.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2){
                String item = serverFileList.getSelectionModel().getSelectedItem();
                try {
                    Network.getOut().writeObject(new PathInRequest(item));
                    Network.getOut().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setAuthorized(boolean isAuthorized) {
        if (!isAuthorized) {
            authentication.setVisible(true);
            authentication.setManaged(true);
            mainWorkPanel.setVisible(false);
            mainWorkPanel.setManaged(false);
        } else {
            authentication.setVisible(false);
            authentication.setManaged(false);
            mainWorkPanel.setVisible(true);
            mainWorkPanel.setManaged(true);
            this.isAuthorized = true;
        }
    }

    public void tryToAuth() {
        Network.sendMsg(new AuthRequest(loginField.getText(), passwordField.getText()));
        loginField.clear();
        passwordField.clear();
    }

    public void takeOutFromServer(ActionEvent actionEvent) {
        Network.sendMsg(new FileRequest(serverFileList.getSelectionModel().getSelectedItem()));
    }

    @SneakyThrows
    public void sendOnServer(ActionEvent actionEvent) {
        String fileName = clientFileList.getSelectionModel().getSelectedItem();
        FileMessage fileMessage = new FileMessage(currentClientDir.resolve(fileName));
        Network.getOut().writeObject(fileMessage);
        Network.getOut().flush();
        Network.getOut().writeObject(new ListRequest());
        Network.getOut().flush();
//        try {
//            Network.sendMsg(new FileMessage(Paths.get(currentClientDir + clientFileList.getSelectionModel().getSelectedItem())));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void removeFile(ActionEvent actionEvent) {
        Button currntButton = (Button) actionEvent.getSource();

        if (removeClientFile.equals(currntButton)) {
            try {
                Files.delete(Paths.get(currentClientDir + "/" + clientFileList.getSelectionModel().getSelectedItem()));
                refreshClientView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (removeServerFile.equals(currntButton)) {
            Network.sendMsg(new DeleteRequest(serverFileList.getSelectionModel().getSelectedItem()));
        }
    }

    private void refreshClientView() throws IOException {
        clentPath.setText(currentClientDir.toString());
        List<String> files = Files.list(currentClientDir)
                .map(f->f.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
             clientFileList.getItems().clear();
             clientFileList.getItems().addAll(files);
        });
    }

    private void refreshServerView(ArrayList<String> filesList) {
        Platform.runLater(() -> {
            serverFileList.getItems().clear();
            serverFileList.getItems().addAll(filesList);
        });
    }

    @SneakyThrows
    public void pathOutClientPressing(ActionEvent actionEvent) {
        if(currentClientDir.getParent() != null){
            currentClientDir = currentClientDir.getParent();
            refreshClientView();
        }

    }

    public void pathOutServerPressing(ActionEvent actionEvent) {
        Network.sendMsg(new PathUpRequest());
    }
}
