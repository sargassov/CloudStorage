public class AuthRequest extends Command {
    private String login;
    private String password;

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() { return login; }

    public String getPassword() {
        return password;
    }

    @Override
    public CommandName getCommandName() {
        return CommandName.AUTH_REQUEST;
    }
}
