public class AuthMessage extends RandomMessage {
    private String login;
    private String password;
    private String message;

    public AuthMessage(){}

    public AuthMessage(String message) {
        this.message = message;
    }

    public AuthMessage(String login, String password) {
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
}
