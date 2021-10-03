import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML Button removeClientFile, removeServerFile;
    @FXML ListView<String> clientFileList;
    @FXML ListView<String> serverFileList;
    @FXML HBox mainWorkPanel;
    @FXML VBox authentication;
    @FXML TextField loginField;
    @FXML PasswordField passwordField;
    @FXML Label authLabel;
    private static final String ROOT = "client/clientFiles";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(true);
        Network.start();

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    RandomMessage randomMessage = Network.readObject();
                    if (randomMessage instanceof AuthMessage) {
                        AuthMessage authMessage = (AuthMessage) randomMessage;
                        if (Command.AUTH_OK.equals(authMessage.getMessage())) {
                            setAuthorized(true);
                            break;
                        }
                        if (Command.NULL_USER_ID.equals(authMessage.getMessage())) {
                            Platform.runLater(() -> authLabel.setText("WRONG LOGIN OR PASS"));
                        }
                    }
                }

                Network.sendMsg(new RenewServerList());

                while (true) {
                    RandomMessage randomMessage = Network.readObject();
                    if (randomMessage instanceof FileMessage) {
                        FileMessage fileMessage = (FileMessage) randomMessage;
                        Files.write(Paths.get(ROOT + fileMessage.getFilename()),
                                fileMessage.getData(), StandardOpenOption.CREATE); //StandardOpenOption.CREATE всегда оздаёт/перезаписывает новые объекты
                        renewCleentFiles();
                    }
                    if (randomMessage instanceof RenewServerList) {
                        RenewServerList refreshServerMsg = (RenewServerList) randomMessage;
                        renewServerFiles(refreshServerMsg.getServerFileList());
                    }
                }
            } catch (ClassNotFoundException | IOException ex) {
                ex.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        thread.setDaemon(true);
        thread.start();
        renewCleentFiles();
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
        }
    }

    public void tryToAuth() {
        Network.sendMsg(new AuthMessage(loginField.getText(), passwordField.getText()));
        loginField.clear();
        passwordField.clear();
    }

    public void takeOutFromServer(ActionEvent actionEvent) {
        Network.sendMsg(new DownloadRequest(serverFileList.getSelectionModel().getSelectedItem()));
    }

    public void sendOnServer(ActionEvent actionEvent) {
        try {
            Network.sendMsg(new FileMessage(Paths.get(ROOT + clientFileList.getSelectionModel().getSelectedItem())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeFile(ActionEvent actionEvent) {
        Button currntButton = (Button) actionEvent.getSource();

        if (removeServerFile.equals(currntButton)) {
            try {
                Files.delete(Paths.get(ROOT + clientFileList.getSelectionModel().getSelectedItem()));
                renewCleentFiles();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (removeServerFile.equals(currntButton)) {
            Network.sendMsg(new DeleteRequest(serverFileList.getSelectionModel().getSelectedItem()));
        }
    }

    private void renewCleentFiles() {
        updateUI(() -> {
            try {
                clientFileList.getItems().clear();
                Files.list(Paths.get(ROOT)).map(p -> p.getFileName().toString()).forEach(f -> clientFileList.getItems().add(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void renewServerFiles(ArrayList<String> filesList) {
        updateUI(() -> {
            serverFileList.getItems().clear();
            serverFileList.getItems().addAll(filesList);
        });
    }

    private static void updateUI(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}
