import java.io.IOException;

public class TestUser {


    public static void main (String [] args) throws IOException
    {
        String username = "maex";
        String password = "abc123_ABC123";
        String sessionName = "Nick's game";

        User newUser = new User(username, password);

        // System.out.println(User.doesUserExist("nick2", newUser));
        GameService dumbGame = new GameService(newUser, "gamenick", "nicksgame", "Cheese12345", 5, 10);
    }








}
