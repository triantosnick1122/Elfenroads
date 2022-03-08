package networking;

// is a server socket that listens for game updates and lets the CommunicationsManager it is a part of know when there is an update
// the player will never have to interact with this class directly. It will all be automated by the CommunicationsManager

import commands.GameCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.Stack;

public class GameUpdateListener implements Runnable
{
    private int port;
    private ServerSocket listener;
    private GameState mostRecentUpdate; // not using this for now, trying out sending commands instead
    // private GameCommand command;
    private Queue<GameCommand> commands;
    private CommunicationsManager managedBy;

    public GameUpdateListener(int pPort, CommunicationsManager pManagedBy)
    {
        port = pPort;
        managedBy = pManagedBy;
    }

    @Override
    public void run() {

        while (true) {
            try {
                listener = new ServerSocket(port);
                System.out.println("Going into accept() method and waiting for information...");
                Socket update = listener.accept(); // the accept () will sit there and wait until an update is received
                System.out.println("Got a message from the thing! Accept method terminated.");
                InputStream updateContents = update.getInputStream();
                // ObjectInputStream gameStateReceived = new ObjectInputStream(updateContents);
                ObjectInputStream commandReceived = new ObjectInputStream(updateContents);
                readInCommand(commandReceived);
                notifyManager(); // tell the CommunicationsManager that an update has been received
                listener.close(); // close the connection and do it again

            } catch (Exception e) {
                System.out.println("There was a problem setting up the ServerSocket.");
                e.printStackTrace();
                break;
            }

        }

    }

    // this will read in the Game State being received and save it so that the CommunicationsManager knows how to update it
    private void readInGameState(ObjectInputStream received) throws Exception
    {
        mostRecentUpdate = (GameState) received.readObject();
    }

    private void readInCommand(ObjectInputStream received) throws Exception
    {
        commands.add((GameCommand) received.readObject());
    }

    private void notifyManager()
    {
        // notify the communications manager when an update has been received
        managedBy.updateFromListener();
    }

    /**
     * this method will be called by a CommunicationsManager after it has been told there is a game update available
     * @return the last command received by this listener
     */
    public Queue <GameCommand> getCommands()
    {
        return commands;
    }
}
