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
import lombok.Data;
import lombok.Getter;
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
@Data
public class Controller implements Initializable { //основной класс обработки и управления всплывающим окном
    @FXML private TextField serverPath;
    @FXML private TextField clentPath;
    @FXML private Button removeClientFile, removeServerFile;
    @FXML private ListView<String> clientFileList;
    @FXML private ListView<String> serverFileList;
    @FXML private HBox mainWorkPanel;
    @FXML private VBox authentication;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label authLabel;
    private static final String ROOT = "client/clientFiles";
    private Path currentClientDir = Paths.get(ROOT).toAbsolutePath();
    private boolean isAuthorized = false;
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) { //инициализация отображения всплывающего окна

        setAuthorized(false);
        Network.start();
        refreshClientView();
        addNavigationListeners();

        WindowThread thread = new WindowThread(this);
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
                        Network.stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void addNavigationListeners() { //установка слушателей на клики мышкой на список файлов на клиенте
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

        serverFileList.setOnMouseClicked(event -> { ; //установка слушателей на клики мышкой на список файлов на сервере
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

    public void setAuthorized(boolean isAuthorized) { //управление видимостью окно в зависимости от пройденной решистрации
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

    public void tryToAuth() { //попытка решистрации
        log.info("Controller.tryToAuth");
        Network.writeObject(new AuthRequest(loginField.getText().trim(), passwordField.getText().trim()));
        loginField.clear();
        passwordField.clear();

    }

    public void takeOutFromServer(ActionEvent actionEvent) { //загрузить с сервера
        log.info("Controller.takeOutFromServer");
        Network.writeObject(new FileRequest(serverFileList.getSelectionModel().getSelectedItem()));
    }

    @SneakyThrows
    public void sendOnServer(ActionEvent actionEvent) { //отправить на сервер
        log.info("Controller.senfOnServer");
        String fileName = clientFileList.getSelectionModel().getSelectedItem();
        FileMessage fileMessage = new FileMessage(currentClientDir.resolve(fileName));
        Network.getOut().writeObject(fileMessage);
        Network.getOut().flush();
        Network.getOut().writeObject(new ListRequest());
        Network.getOut().flush();
    }

    public void removeFile(ActionEvent actionEvent) { //удаление файла в директории клиента
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

    public void refreshClientView() throws IOException { //обновление списка файлов в конкретной директории на сервере
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

    public void refreshServerView(ArrayList<String> filesList) { //обновление списка файлов в конкретной директории на сервере
        log.info("Controller.refredhServerView");
        Platform.runLater(() -> {
            serverFileList.getItems().clear();
            serverFileList.getItems().addAll(filesList);
        });
    }

    @SneakyThrows
    public void pathOutClientPressing(ActionEvent actionEvent) { //переход на директорию выше на коиенте
        log.info("Controller.pathOutClientPressing");
        if(currentClientDir.getParent() != null){
            currentClientDir = currentClientDir.getParent();
            refreshClientView();
        }
    }

    public void pathOutServerPressing(ActionEvent actionEvent) { //переход на директорию выше на сервере
        log.info("Controller.pathOutServerPressing");
        Network.writeObject(new PathUpRequest());
    }

    public void clickRegButton(ActionEvent actionEvent) { //слушатель на кнопку регистриации
        log.info("Controller.clickRegButton");
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() { //Создание окна регистарации
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

    public void tryToReg(String login, String password) { //попытка регистарици
        log.info("Controller.tryToReg");
        if (Network.getSocket() == null || Network.getSocket().isClosed()) {
            Network.start();
        }

        Network.writeObject(new RegRequest(login, password));
    }

}
