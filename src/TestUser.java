import java.io.IOException;

public class TestUser {


    public static void main (String [] args) throws IOException
    {
        String username = "nick2";
        String password = "Nick12345";
        String sessionName = "Nick's game";

        User newUser = new User(username, password);

        GameSession newSession = new GameSession(newUser, "game session 1", "save game 1");
    }








}
