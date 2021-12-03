import org.json.JSONObject;

public class GameState {

    // this class will contain all of the information we need to send regarding game state
    // created this class so that I can easily serialize and send important info to each computer
    // for now, this will only keep track of boot locations and towns visited because that's all we need for the demo


    /* as of now, we need to keep track of and be able to serialize:
    1. which Elfen towns have been visited (show on the UI by the town pieces)
    2. where each player's boot is
    3.

     */




    public GameState ()
    {


    }

    // TODO: implement this second constructor
    // public GameState (JSONObject gameStateJSON)

    public JSONObject serialize()
    {
        // we will call the JSONObject constructor with the GameState object as an argument
        // that class comes from org.json (see documentation for details)
        return new JSONObject(this);
    }





}
