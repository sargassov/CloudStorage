import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j
public class ApplicationHandler extends SimpleChannelInboundHandler<Command> { //обработчик команд со стороны клиента после пройденной регистариции

    private String userId;
    private static String ROOT = "server/serverFiles";
    private Path currentPath;


    ApplicationHandler(String userId) throws IOException {
        currentPath = Paths.get(ROOT + "/" + userId);
        this.userId = userId;
// если для хранения файлов пользователя на сервере нет директории, она создается с его именем
        if(!Files.exists(currentPath)) Files.createDirectory(currentPath);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
// обработчик информации, которую необходимо направить на клиента после регистрации
        System.out.println("Client " + userId + " connected");
        ctx.writeAndFlush(new ListResponce(currentPath));
        ctx.writeAndFlush(new PathResponce(currentPath.toString()));
        System.out.println(currentPath);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        //обработка команд с клиента
            if (command == null) { return; }
//            channelActive(ctx);
        //запрос на отправку файла
            if (command.getCommandName().equals(CommandName.FILE_REQUEST)) {
                FileRequest fileRequest = (FileRequest) command;
                FileMessage fileMessage = new FileMessage(currentPath.resolve(fileRequest.getFileName()));
                ctx.writeAndFlush(fileMessage);
            }
            // запрос на удаление файла в личной директории пользователя на сервере
            if (command.getCommandName().equals(CommandName.DELETE_REQUEST)) {
                DeleteRequest deleteRequest = (DeleteRequest) command;
                Files.delete(Paths.get(currentPath + "/" + deleteRequest.getFilename()));
                ctx.writeAndFlush(new ListResponce(currentPath));
            }
            // запрос на добавление файла
            if (command.getCommandName().equals(CommandName.FILE_MESSAGE)) {
                FileMessage fileMessage = (FileMessage) command;
                Files.write(currentPath.resolve(fileMessage.getFilename()), fileMessage.getData());
                ctx.writeAndFlush(new ListResponce(currentPath));
            }
            // запрос на список файлов в индивидуальной директории пользователя на сервере
            if(command.getCommandName().equals(CommandName.LIST_REQUEST)){
                ctx.writeAndFlush(new ListResponce(currentPath));
            }
            // запрос на переход на директорию выше в рамках индивидуальной папки пользователя на сервере
            if(command.getCommandName().equals(CommandName.PATH_UP_REQUEST)){
                if(currentPath.toString().endsWith(userId)){
                    return;
                }

                currentPath = currentPath.getParent();
                ctx.writeAndFlush(new PathResponce(currentPath.toString()));
                ctx.writeAndFlush(new ListResponce(currentPath));
            }
            // запрос на переход в подпапку в отдельной директории пользователя на сервере
            if(command.getCommandName().equals(CommandName.PATH_IN_REQUEST)){
                PathInRequest pathInRequest = (PathInRequest) command;
                Path newPath = currentPath.resolve(pathInRequest.getDirectory());

                if(Files.isDirectory(newPath)){
                    currentPath = newPath;
                    ctx.writeAndFlush(new PathResponce(currentPath.toString()));
                    ctx.writeAndFlush(new ListResponce(currentPath));
                }
            }
    }

    @Override //обработка исключительной ситуации
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override //отклбчение пользователя от сервера
    public void channelInactive(ChannelHandlerContext ctx){
        log.info("Client " + userId + " disconnected!");
        ctx.writeAndFlush(new ExitCommand());
    }
}
