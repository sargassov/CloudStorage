import java.io.Serializable;

public class Command implements Serializable {

    private CommandName commandName;

    public CommandName getCommandName() {
        return commandName;
    }
}
