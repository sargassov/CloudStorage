public class RegRequest extends Command{

    private String login;
    private String password;
    private String nickName;

    public RegRequest(String login, String password, String nickName){
        this.login = login;
        this.password = password;
        this.nickName = nickName;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public CommandName getCommandList() {
        return CommandName.REG_REQUEST;
    }
}
