import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

public class AuthHandler extends SimpleChannelInboundHandler<Command> {
    private boolean authPassed = false;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {

        try{
            if(authPassed){
                ctx.fireChannelRead(command);
                return;
            }

            System.out.println(command.getCommandList());

            if(command.getCommandList().equals(CommandName.AUTH_REQUEST)){
                AuthRequest authRequest = (AuthRequest) command;
                String userId = DBStorage.getNicknameByLoginAndPassword(authRequest.getLogin().trim(),
                        authRequest.getPassword().trim());

                if(userId != null){
                    System.out.println("have");
                    authPassed = true;
                    ctx.pipeline().addLast(new ApplicationHandler(userId));
                    ctx.writeAndFlush(new AuthPassed());
                }
                else {
                    System.out.println("NULL");
                    ctx.writeAndFlush(new AuthFailed());
                }
            }

        }finally {
            ReferenceCountUtil.release(command);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
