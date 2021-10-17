import java.io.Serializable;

public class Command implements Serializable {

    private CommandType commandType;

    public CommandType getCommandType() {
        return commandType;
    }

}
