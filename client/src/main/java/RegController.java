import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegController {


    @FXML public TextField loginField;
    @FXML public PasswordField passwordField;
    @FXML public TextField nicknameField;
    @FXML public TextArea textArea;
    private Controller controller;

    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nickname = loginField.getText().trim();

        if (login.length() * password.length() * nickname.length() == 0) {
            textArea.appendText("Your Entry Incorrectly\n");
            return;
        }

        if (login.contains(" ") || password.contains(" ") || nickname.contains(" ")) {
            textArea.appendText("Your Entry Incorrectly\n");
            return;
        }

        controller.tryToReg(login, password, nickname);
    }

    public void resultTryToReg(boolean flag) {
        if (flag) {
            textArea.appendText("REGISTRATION PASSED\n");
            loginField.clear();
            passwordField.clear();
            nicknameField.clear();
        } else {
            textArea.appendText("REGISTRATION FAILED\n");
            loginField.clear();
            passwordField.clear();
            nicknameField.clear();
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
