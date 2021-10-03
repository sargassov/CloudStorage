import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private String userId;
    private static String ROOT = "server/serverFiles";

    ClientHandler(String userId) {
        this.userId = userId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client " + userId + " connected");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        try {

            ctx.writeAndFlush(new AuthMessage());
            if (message == null) { return; }
            System.out.println(message.getClass());


            if (message instanceof DownloadRequest) {
                DownloadRequest downloadRequest = (DownloadRequest) message;
                if (Files.exists(Paths.get(ROOT + downloadRequest.getFilename()))) {
                    FileMessage fileMessage = new FileMessage(Paths.get(ROOT + downloadRequest.getFilename()));
                    ctx.writeAndFlush(fileMessage);
                }
            }

            else if (message instanceof DeleteRequest) {
                DeleteRequest deleteRequest = (DeleteRequest) message;
                Files.delete(Paths.get(ROOT + deleteRequest.getFilename()));
                refreshServerListView(ctx);
            }

            else if (message instanceof FileMessage) {
                FileMessage fileMessage = (FileMessage) message;
                Files.write(Paths.get(ROOT + fileMessage.getFilename()), fileMessage.getData(), StandardOpenOption.CREATE);
                refreshServerListView(ctx);
            }

            else if (message instanceof RenewServerList) {
                refreshServerListView(ctx);
            }

        } finally {
            ReferenceCountUtil.release(message);
        }
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
            ctx.writeAndFlush(new RenewServerList(serverFileList));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
