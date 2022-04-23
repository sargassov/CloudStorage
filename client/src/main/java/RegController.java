import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;

@Log4j
public class RegController { //контроллер регистрации. Обработка запросов на регистрацию


    @FXML public TextField loginField;
    @FXML public PasswordField passwordField;
    @FXML public TextField nicknameField;
    @FXML public TextArea textArea;
    private Controller controller;

    public void tryToReg(ActionEvent actionEvent) {
        log.info("RegController.tryToReg");
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() * password.length() == 0) {
            textArea.appendText("Your Entry Incorrectly\n");
            return;
        }

        if (login.contains(" ") || password.contains(" ")) {
            textArea.appendText("Your Entry Incorrectly\n");
            return;
        }

        controller.tryToReg(login, password);
    }

    public void resultTryToReg(boolean flag) { //результирующий метод регистрации
        log.info("RegController.resultTryReg");
        if (flag) {
            textArea.appendText("REGISTRATION PASSED\n");
            loginField.clear();
            passwordField.clear();
        } else {
            textArea.appendText("REGISTRATION FAILED LOGIN IS ALREADY USED\n");
            loginField.clear();
            passwordField.clear();
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
