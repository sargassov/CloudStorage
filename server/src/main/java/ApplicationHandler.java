import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationHandler extends SimpleChannelInboundHandler<Command> {

    private String userId;
    private static String ROOT = "server/serverFiles";
    private Path currentPath;


    ApplicationHandler(String userId) throws IOException {
        currentPath = Paths.get(ROOT + "/" + userId);
        this.userId = userId;

        if(!Files.exists(currentPath)) Files.createDirectory(currentPath);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {

        System.out.println("Client " + userId + " connected");
        ctx.writeAndFlush(new ListResponce(currentPath));
        ctx.writeAndFlush(new PathResponce(currentPath.toString()));
        System.out.println(currentPath);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {

            if (command == null) { return; }

            if (command.getCommandList().equals(CommandName.FILE_REQUEST)) {
                FileRequest fileRequest = (FileRequest) command;
                FileMessage fileMessage = new FileMessage(currentPath.resolve(fileRequest.getFileName()));
                ctx.writeAndFlush(fileMessage);
            }

            if (command.getCommandList().equals(CommandName.DELETE_REQUEST)) {
                DeleteRequest deleteRequest = (DeleteRequest) command;
                Files.delete(Paths.get(currentPath + "/" + deleteRequest.getFilename()));
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if (command.getCommandList().equals(CommandName.FILE_MESSAGE)) {
                FileMessage fileMessage = (FileMessage) command;
                Files.write(currentPath.resolve(fileMessage.getFilename()), fileMessage.getData());
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if(command.getCommandList().equals(CommandName.LIST_REQUEST)){
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if(command.getCommandList().equals(CommandName.PATH_UP_REQUEST)){
                if(currentPath.toString().endsWith(userId)){
                    return;
                }

                currentPath = currentPath.getParent();
                ctx.writeAndFlush(new PathResponce(currentPath.toString()));
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if(command.getCommandList().equals(CommandName.PATH_UP_REQUEST)){
                ctx.writeAndFlush(new PathResponce(currentPath.toString()));
                ctx.writeAndFlush(new ListResponce(currentPath));
            }

            if(command.getCommandList().equals(CommandName.PATH_IN_REQUEST)){
                PathInRequest pathInRequest = (PathInRequest) command;
                Path newPath = currentPath.resolve(pathInRequest.getDirectory());

                if(Files.isDirectory(newPath)){
                    currentPath = newPath;
                    ctx.writeAndFlush(new PathResponce(currentPath.toString()));
                    ctx.writeAndFlush(new ListResponce(currentPath));
                }
            }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client " + userId + " disconnected!");
        ctx.writeAndFlush(new ExitCommand());
    }
}
