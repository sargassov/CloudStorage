import javafx.application.Platform;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

@Log4j
public class WorkCycle {
    private Controller controller;

    public WorkCycle(Controller controller) {
        this.controller = controller;
    }

    public void encludeLoop() throws IOException, ClassNotFoundException { //главный цикл обработки ответов с сервера
        while (true){
            Command command = Network.readObject();

            if(command.getCommandName().equals(CommandName.LIST_RESPONCE)){ //ответ на запрос о списке файлов
                log.info("WORK LIST RESPONCE");
                ListResponce listResponce = (ListResponce) command;
                ArrayList<String> names = listResponce.getServerFileList();
                controller.refreshServerView(names);
            }

            if(command.getCommandName().equals(CommandName.PATH_RESPONCE)){ //ответ на запрос о конкретной директории
                log.info("WORK PATH RESPONCE");
                PathResponce pathResponce = (PathResponce) command;
                String path = pathResponce.getPath().substring("server/serverFiles/".length());
                System.out.println(path);
                Platform.runLater(()->{
                    controller.getServerPath().setText(path);
                });
            }

            if(command.getCommandName().equals(CommandName.FILE_MESSAGE)) { //прием файла с сервера
                log.info("WORK FILE MESSAGE");
                FileMessage fileMessage = (FileMessage) command;
                Files.write(controller.getCurrentClientDir().resolve(fileMessage.getFilename()), fileMessage.getData());
                controller.refreshClientView();
            }
        }
    }
}
