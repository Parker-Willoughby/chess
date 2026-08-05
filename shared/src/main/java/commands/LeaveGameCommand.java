package commands;

public class LeaveGameCommand extends UserGameCommand {
    public LeaveGameCommand(UserGameCommand.CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
