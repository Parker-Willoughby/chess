package websocket.commands;

public class ResignCommand extends UserGameCommand{
    public ResignCommand(UserGameCommand.CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
