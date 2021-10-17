import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private String userId;
    private static String ROOT = "server/serverFiles";
    private Path currentPath;


    FileMessageHandler(String userId) throws IOException {
        currentPath = Paths.get(ROOT + "/" + userId);
        this.userId = userId;

        if(!Files.exists(currentPath)) Files.createDirectory(currentPath);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {

        System.out.println("Client " + userId + " connected");
        ctx.writeAndFlush(new ListResponce(currentPath));
        ctx.writeAndFlush(new PathResponce(currentPath.toString()));
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {


            ctx.writeAndFlush(new AuthRequest());
            if (command == null) { return; }
            //System.out.println(command.getClass());

            if (command.getCommandType().equals(CommandType.FILE_REQUEST)) {
                FileRequest fileRequest = (FileRequest) command;
                FileMessage fileMessage = new FileMessage(currentPath.resolve(fileRequest.getFileName()));
                ctx.writeAndFlush(fileMessage);
//                if (Files.exists(Paths.get(ROOT + fileRequest.getFileName()))) {
//                    FileMessage fileMessage = new FileMessage(Paths.get(ROOT + fileRequest.getFileName()));
//                    ctx.writeAndFlush(fileMessage);
//                }
            }

            if (command.getCommandType().equals(CommandType.DELETE_REQUEST)) {
                DeleteRequest deleteRequest = (DeleteRequest) command;
                Files.delete(Paths.get(currentPath + "/" + deleteRequest.getFilename()));
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if (command.getCommandType().equals(CommandType.FILE_MESSAGE)) {
                FileMessage fileMessage = (FileMessage) command;
                Files.write(currentPath.resolve(fileMessage.getFilename()), fileMessage.getData());
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if(command.getCommandType().equals(CommandType.LIST_REQUEST)){
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if(command.getCommandType().equals(CommandType.PATH_UP_REQUEST)){
                if(currentPath.toString().endsWith(userId)){
                    return;
                }

                currentPath = currentPath.getParent();
                ctx.writeAndFlush(new PathResponce(currentPath.toString()));
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if(command.getCommandType().equals(CommandType.PATH_IN_REQUEST)){
                PathInRequest pathInRequest = (PathInRequest) command;
                Path newPath = currentPath.resolve(pathInRequest.getDirectory());

                if(Files.isDirectory(newPath)){
                    currentPath = newPath;
                    ctx.writeAndFlush(new PathResponce(currentPath.toString()));
                    ctx.writeAndFlush(new ListResponce(currentPath));
                }
            }

//            else if (message instanceof ListResponce) {
//                refreshServerListView(ctx);
//            }




         //finally {
//            //ReferenceCountUtil.release(message);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void refreshServerListView(ChannelHandlerContext ctx) {
        try {

            ArrayList<String> serverFileList = new ArrayList<>();
            Files.list(Paths.get(ROOT)).map(p -> p.getFileName().toString()).forEach(serverFileList::add);
            ctx.writeAndFlush(new ListResponce(serverFileList));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
