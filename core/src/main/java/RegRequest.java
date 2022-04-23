public class RegRequest extends Command{

    private String login;
    private String password;

    public RegRequest(String login, String password){
        this.login = login;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public CommandName getCommandName() {
        return CommandName.REG_REQUEST;
    }
}
