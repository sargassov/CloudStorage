import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
    @FXML public VBox registration;
    @FXML public Label regLabel;
    @FXML public PasswordField regPasswordField;
    @FXML public TextField regLoginField;
    @FXML Button removeClientFile, removeServerFile;
    @FXML ListView<String> clientFileList;
    @FXML ListView<String> serverFileList;
    @FXML HBox mainWorkPanel;
    @FXML VBox authentication;
    @FXML TextField loginField;
    @FXML PasswordField passwordField;
    @FXML Label authLabel;
    private static final String ROOT = "client/clientFiles";
    private Path currentClientDir = Paths.get(ROOT).toAbsolutePath();
    private boolean isAuthorized = false;
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setAuthorized(false);
        Network.start();
        refreshClientView();
        addNavigationListeners();

        Thread thread = new Thread(() -> {
            try{

                while (!isAuthorized) {
                    Command command = (Command) Network.getIn().readObject();
                    System.out.println(command.getCommandList());
                    if(command.getCommandList().equals(CommandName.EXIT_COMMAND)){
                        System.out.println("server disconnected us");
                        throw new RuntimeException("server disconnected us");
                    }

                    if(command.getCommandList().equals(CommandName.AUTH_PASSED)){
                        setAuthorized(true);
                        Network.sendMsg(new PathInRequest(""));
                        Network.sendMsg(new ListRequest());
                        break;
                    }

                    if(command.getCommandList().equals(CommandName.AUTH_FAILED)){
                        Platform.runLater(() -> authLabel.setText("WRONG LOGIN OR PASS"));
                        System.out.println("ALL FUCK");
                    }

                    if (command.getCommandList().equals(CommandName.REG_PASSED)) {
                        regController.resultTryToReg(true);
                    }

                    if (command.getCommandList().equals(CommandName.REG_FAILED)) {
                        regController.resultTryToReg(false);
                    }
                }

                Network.sendMsg(new ListResponce());

                Thread t = new Thread(()->{ // отдельный поток для отображения изменений директории клиента
                    while (true){
                        try {
                            refreshClientView();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.setDaemon(true);
                t.start();

                while (true){
                    Command command = (Command) Network.getIn().readObject();


                    if(command.getCommandList().equals(CommandName.LIST_RESPONCE)){
                        ListResponce listResponce = (ListResponce) command;
                        ArrayList<String> names = listResponce.getServerFileList();
                        refreshServerView(names);
                    }

                    if(command.getCommandList().equals(CommandName.PATH_RESPONCE)){
                        PathResponce pathResponce = (PathResponce) command;
                        String path = pathResponce.getPath().substring("server/serverFiles/".length());
                        System.out.println(path);
                        Platform.runLater(()->{
                            serverPath.setText(path);
                        });
                    }

                    if(command.getCommandList().equals(CommandName.FILE_MESSAGE)) {
                        FileMessage fileMessage = (FileMessage) command;
                        Files.write(currentClientDir.resolve(fileMessage.getFilename()), fileMessage.getData());
                        refreshClientView();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
        refreshClientView();

        Platform.runLater(() -> {
            stage = (Stage) serverPath.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (Network.getSocket() != null && !Network.getSocket().isClosed()) {
                    try {
                        Network.getOut().writeObject(new ExitCommand());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
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

        Network.sendMsg(new AuthRequest(loginField.getText().trim(), passwordField.getText().trim()));
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

    public void clickRegButton(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reg.fxml"));
            Parent root = fxmlLoader.load();

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage = new Stage();
            regStage.setTitle("Registration mode");
            regStage.setScene(new Scene(root, 600, 400));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(String login, String password, String nickname) {
//        if (Network.getSocket() == null || Network.getSocket().isClosed()) {
//            Network.start();
//        }

        Network.sendMsg(new RegRequest(login, password, nickname));
    }
}
