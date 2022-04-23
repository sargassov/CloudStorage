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
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Log4j
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
            try {
//цикл рагистрации
                while (!isAuthorized) {
                    Command command = (Command) Network.getIn().readObject();
                    log.debug(command.getCommandName());
                    if(command.getCommandName().equals(CommandName.EXIT_COMMAND)){
                        System.out.println("server disconnected us");
                        throw new RuntimeException("server disconnected us");
                    }

                    if(command.getCommandName().equals(CommandName.AUTH_PASSED)){
                        System.out.println("AUTH PASSED");
                        setAuthorized(true);
                        Network.writeObject(new PathInRequest(""));
                        Network.writeObject(new ListRequest());
                        break;
                    }

                    if(command.getCommandName().equals(CommandName.AUTH_FAILED)){
                        System.out.println("AUTH FAILED");
                        Platform.runLater(() -> authLabel.setText("WRONG LOGIN OR PASS"));
                    }

                    if (command.getCommandName().equals(CommandName.REG_PASSED)) {
                        System.out.println("REG PASSED");
                        regController.resultTryToReg(true);
                    }

                    if (command.getCommandName().equals(CommandName.REG_FAILED)) {
                        System.out.println("REG FAILED");
                        regController.resultTryToReg(false);
                    }
                }

                Network.writeObject(new ListResponce());

                Thread t = new Thread(()->{ // отдельный поток для отображения изменений директории клиента
                    while (true){
                        try {
                            log.info("REFRESH CLIENT VIEW");
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
//цикл работы
                while (true){
                    Command command = (Command) Network.getIn().readObject();

                    if(command.getCommandName().equals(CommandName.LIST_RESPONCE)){
                        log.info("WORK LIST RESPONCE");
                        ListResponce listResponce = (ListResponce) command;
                        ArrayList<String> names = listResponce.getServerFileList();
                        refreshServerView(names);
                    }

                    if(command.getCommandName().equals(CommandName.PATH_RESPONCE)){
                        log.info("WORK PATH RESPONCE");
                        PathResponce pathResponce = (PathResponce) command;
                        String path = pathResponce.getPath().substring("server/serverFiles/".length());
                        System.out.println(path);
                        Platform.runLater(()->{
                            serverPath.setText(path);
                        });
                    }

                    if(command.getCommandName().equals(CommandName.FILE_MESSAGE)) {
                        log.info("WORK FILE MESSAGE");
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
                log.info("EXIT");
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

        serverFileList.setOnMouseClicked(event -> { ;
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

    public void setAuthorized(boolean isAuthorized) {
        log.info("Controller.setAuthorized");
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
        log.info("Controller.tryToAuth");
        Network.writeObject(new AuthRequest(loginField.getText().trim(), passwordField.getText().trim()));
        loginField.clear();
        passwordField.clear();

    }

    public void takeOutFromServer(ActionEvent actionEvent) {
        log.info("Controller.takeOutFromServer");
        Network.writeObject(new FileRequest(serverFileList.getSelectionModel().getSelectedItem()));
    }

    @SneakyThrows
    public void sendOnServer(ActionEvent actionEvent) {
        log.info("Controller.senfOnServer");
        String fileName = clientFileList.getSelectionModel().getSelectedItem();
        FileMessage fileMessage = new FileMessage(currentClientDir.resolve(fileName));
        System.out.println(fileMessage.getSize());
        System.out.println(fileMessage.getFilename());
        Network.getOut().writeObject(fileMessage);
        Network.getOut().flush();
        Network.getOut().writeObject(new ListRequest());
        Network.getOut().flush();
    }

    public void removeFile(ActionEvent actionEvent) {
        log.info("Controller.removeFile");
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
            Network.writeObject(new DeleteRequest(serverFileList.getSelectionModel().getSelectedItem()));
        }
    }

    public void refreshClientView() throws IOException {
        log.info("Controller.refreshClientView");
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
        log.info("Controller.refredhServerView");
        Platform.runLater(() -> {
            serverFileList.getItems().clear();
            serverFileList.getItems().addAll(filesList);
        });
    }

    @SneakyThrows
    public void pathOutClientPressing(ActionEvent actionEvent) {
        log.info("Controller.pathOutClientPressing");
        if(currentClientDir.getParent() != null){
            currentClientDir = currentClientDir.getParent();
            refreshClientView();
        }

    }

    public void pathOutServerPressing(ActionEvent actionEvent) {
        log.info("Controller.pathOutServerPressing");
        Network.writeObject(new PathUpRequest());
    }

    public void clickRegButton(ActionEvent actionEvent) {
        log.info("Controller.clickRegButton");
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() {
        log.info("Controller.createRegWidow");
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

    public void tryToReg(String login, String password) {
        log.info("Controller.tryToReg");
        if (Network.getSocket() == null || Network.getSocket().isClosed()) {
            Network.start();
        }

        Network.writeObject(new RegRequest(login, password));
    }
}
