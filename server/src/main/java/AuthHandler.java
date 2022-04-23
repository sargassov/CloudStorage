import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthHandler extends SimpleChannelInboundHandler<Command> {
    private boolean authPassed = false;
    private boolean regPassed = false;
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

            log.info(command.getCommandName().name());

            if(command.getCommandName().equals(CommandName.AUTH_REQUEST)){ //запрос на аутентификацию
                AuthRequest authRequest = (AuthRequest) command;
                String userId = DBStorage.userIdVerify(authRequest.getLogin().trim(),
                        authRequest.getPassword().trim());

                if(userId != null){
                    log.info("Auth was passed by " + userId);
                    authPassed = true;
                    ctx.pipeline().addLast(new ApplicationHandler(userId));
                    ctx.writeAndFlush(new AuthPassed());
                }
                else {
                    log.error("auth failed");
                    ctx.writeAndFlush(new AuthFailed());
                }
            }

            if (command.getCommandName().equals(CommandName.REG_REQUEST)) {
                RegRequest regRequest = (RegRequest) command;

                regPassed = dbStorage.registration(regRequest.getLogin(), regRequest.getPassword());
                if (regPassed) {
                    ctx.pipeline().addLast(new ApplicationHandler(regRequest.getLogin()));
                    ctx.writeAndFlush(new RegPassed());
                } else {
                    ctx.writeAndFlush(new RegFailed());
                }
            }

        }finally {
            ReferenceCountUtil.release(command);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
