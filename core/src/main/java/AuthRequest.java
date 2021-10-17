public class AuthRequest extends Command {
    private String login;
    private String password;
    private String message;

    public AuthRequest(){}

    public AuthRequest(String message) {
        this.message = message;
    }

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getMessage() {
        return message;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.AUTH_REQUEST;
    }
}
