import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

public class AuthHandler extends SimpleChannelInboundHandler<Command> {
    private boolean authPassed = false;
    private DBStorage dbStorage;

    public AuthHandler(DBStorage dbStorage){
        this.dbStorage = dbStorage;
    }

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

            if (command.getCommandList().equals(CommandName.REG_REQUEST)) {
                RegRequest regRequest = (RegRequest) command;
                dbStorage.setAuthHandler(this);

                System.out.println("fwsredthfgyujtdrhgeswfatghj");
                authPassed = dbStorage.registration(regRequest.getLogin(), regRequest.getPassword(),
                        regRequest.getNickName());
                if (authPassed) {
                    ctx.pipeline().addLast(new ApplicationHandler(regRequest.getNickName()));
                    ctx.writeAndFlush(new RegPassed());
                } else {
                    ctx.writeAndFlush(new RegFailed());
                }
            }

        }finally {
            ReferenceCountUtil.release(command);
        }
    }

    public void setAuthPassed(boolean authPassed) {
        this.authPassed = authPassed;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
