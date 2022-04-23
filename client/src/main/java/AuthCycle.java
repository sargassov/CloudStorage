import javafx.application.Platform;
import lombok.extern.log4j.Log4j;

import java.io.IOException;

@Log4j
public class AuthCycle{ //цикл аутентификации. Обработка всех команд так или иначе связвнных с регистрацией или аутентификацией

    private final Controller controller;

    public AuthCycle(Controller controller){
        this.controller = controller;
    }

    public void encludeLoop() throws IOException, ClassNotFoundException {
        while (!controller.isAuthorized()) {
            Command command = Network.readObject(); //принримает команду с сервера
            log.debug(command.getCommandName());
            if(command.getCommandName().equals(CommandName.EXIT_COMMAND)){ //обработка команды на выход
                log.info("server disconnected us");
                throw new RuntimeException("server disconnected us");
            }

            if(command.getCommandName().equals(CommandName.AUTH_PASSED)){ //обработка команды регистрация пройдена
                log.info("AUTH PASSED");
                controller.setAuthorized(true);
                Network.writeObject(new PathInRequest(""));
                Network.writeObject(new ListRequest());
                break;
            }

            if(command.getCommandName().equals(CommandName.AUTH_FAILED)){ //обработка команды регистрация провалена
                log.info("AUTH FAILED");
                Platform.runLater(() -> controller.getAuthLabel().setText("WRONG LOGIN OR PASS"));
            }

            if (command.getCommandName().equals(CommandName.REG_PASSED)) { //добавление нового пользователя
                log.info("REG PASSED");
                controller.getRegController().resultTryToReg(true);
            }

            if (command.getCommandName().equals(CommandName.REG_FAILED)) { //добавление пользователя не прошло
                log.info("REG FAILED");
                controller.getRegController().resultTryToReg(false);
            }
        }
    }
}
