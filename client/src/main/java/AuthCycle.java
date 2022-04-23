import javafx.application.Platform;

public class AuthCycle {

    private Controller controller;
    private RegController regController;
    private boolean isAuthorized;

    public AuthCycle(Controller controller, boolean isAuthorized){
        this.controller = controller;
        this.isAuthorized = isAuthorized;
    }

    public void setRegController(RegController regController) {
        this.regController = regController;
    }

    public void setCommand(Command command){
        if(command.getCommandName().equals(CommandName.EXIT_COMMAND)){
            System.out.println("server disconnected us");
            throw new RuntimeException("server disconnected us");
        }

        if(command.getCommandName().equals(CommandName.AUTH_PASSED)){
            controller.setAuthorized(true);
            Network.writeObject(new PathInRequest(""));
            Network.writeObject(new ListRequest());
        }

        if(command.getCommandName().equals(CommandName.AUTH_FAILED)){
            Platform.runLater(() -> controller.authLabel.setText("WRONG LOGIN OR PASS"));
        }

        if (command.getCommandName().equals(CommandName.REG_PASSED)) {
            regController.resultTryToReg(true);
        }

        if (command.getCommandName().equals(CommandName.REG_FAILED)) {
            regController.resultTryToReg(false);
        }
    }
}
