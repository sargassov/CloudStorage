import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class FileHandler extends ChannelInboundHandlerAdapter {
    private boolean authPassed = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {

        try {
            if (authPassed) {
                ctx.fireChannelRead(message);
                return;
            }
            System.out.println(message.getClass());
            if (message instanceof AuthRequest) {
                AuthRequest authRequest = (AuthRequest) message;
                String userId = DBStorage.getIdByLoginAndPass(authRequest.getLogin(), authRequest.getPassword());
                if (userId != null) {
                    authPassed = true;
                    ctx.pipeline().addLast(new FileMessageHandler(userId));
                    ctx.writeAndFlush(new AuthRequest(Reply.AUTH_OK));
                } else {
                    ctx.writeAndFlush(new AuthRequest(Reply.NULL_USER_ID));
                }
            }
        } finally {
            ReferenceCountUtil.release(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
