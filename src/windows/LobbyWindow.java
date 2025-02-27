package windows;

import gamemanager.GameManager;
import enums.GameVariant;
import org.json.JSONObject;
import networking.*;
import savegames.Savegame;
import utils.NetworkUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

public class LobbyWindow extends JPanel implements ActionListener, Runnable {

    private JLabel background_elvenroads;
    private static JButton createButton;
    private static JButton loadButton;
    private static JButton gamesButton;
    private JPanel buttons;
    private JPanel sessions;
    private JScrollPane scrollPanel;
    private JLabel gameToJoin;
    private JLabel available;

    private static String prevPayload = ""; // used for long polling requests

    private static Thread t;
    private static Thread stopper; // use to forcefully stop the other thread when we need to
    private static int flag = 0;

    static MP3Player track1 = new MP3Player("./assets/Music/JLEX5AW-ui-medieval-click-heavy-positive-01.mp3");

    private void initThreads() {
        t = new Thread(this);
        stopper = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e3) {
                        e3.printStackTrace();
                    }
                    if (flag == 1) {
                        Logger.getGlobal().info("Attempting to stop the main update thread.");
                        t.stop();
                        break;
                    }

                }

            }
        });
    }

    public LobbyWindow() {
        initThreads();
        prevPayload = "";
        flag = 0;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());

        // layout is necessary for JLayeredPane to be added to the JPanel
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        background_elvenroads = MainFrame.instance.getElfenroadsBackground();
        background_elvenroads.setBounds(0, 0, getWidth(), getHeight());
        background_elvenroads.setOpaque(false);

        JLayeredPane layers = new JLayeredPane();
        layers.setBounds(0, 0, getWidth(), getHeight());
        layers.setOpaque(false);
        background_elvenroads.add(layers);

        createButton = new JButton("CREATE NEW SESSION");
        createButton.setPreferredSize(new Dimension(170, 70));
        loadButton = new JButton("LOAD SAVED SESSION");
        loadButton.setPreferredSize(new Dimension(170, 70));
        gamesButton = new JButton("JOIN");

        gameToJoin = new JLabel();
        gameToJoin.setText("");

        available = new JLabel();
        available.setText("Available Sessions");
        available.setFont(new Font("Serif", Font.BOLD, 25));
        available.setOpaque(false);
        available.setBounds(getWidth() / 2 - 100, getHeight() / 3 + 100, 250, 30);

        sessions = new JPanel();
        sessions.setBounds(getWidth() / 4, getHeight() / 3 + 150, getWidth() / 2, 120);
        sessions.setOpaque(false);
        scrollPanel = new JScrollPane(sessions);
        scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPanel.setBounds(getWidth() / 4, getHeight() / 3 + 150, getWidth() / 2, 150);
        scrollPanel.setOpaque(false);
        scrollPanel.getViewport().setOpaque(false);
        scrollPanel.setBorder(BorderFactory.createEmptyBorder());

        try {
            initializeGameInfo(sessions);
        } catch (IOException gameProblem) {
            gameProblem.printStackTrace();
        }

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                track1.play();
                flag = 1;
                t.interrupt(); // kill the thread
                remove(background_elvenroads);
                MainFrame.mainPanel.add(new VersionToPlayWindow(), "version");
                MainFrame.cardLayout.show(MainFrame.mainPanel, "version");
            }

        });
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseSaveGame();
            }
        });
        gamesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        buttons = new JPanel();
        buttons.setBounds(0, getHeight() / 3, getWidth(), 100);
        buttons.setOpaque(false);
        buttons.add(createButton);
        buttons.add(loadButton);

        layers.add(buttons, -1);
        layers.add(available, -1);
        layers.add(scrollPanel, -1);

        add(background_elvenroads);

        t.start();
        stopper.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    /**
     * designed to be called inside the LobbyWindow to display game information
     * can be called multiple times--it will clear the games displayed and reset every time
     */
    public static void initializeGameInfo(JPanel sessions) throws IOException {
        String getSessionsResponse = null;

        if (prevPayload.equals("")) // when we first get into the window, we will have to make a synchronous api call to show the information
        {
            Logger.getGlobal().info("Sending the first long polling request now...");
            getSessionsResponse = GameSession.getSessionsReturnString();
            prevPayload = getSessionsResponse;
        } else // if it is not our first request
        {// get a list of game sessions by ID
            try {
                getSessionsResponse = GameSession.getSessions(prevPayload, t);
                Logger.getGlobal().info("Sending another long poll request...");
            } catch (Exception e) {
                // if interruptedException, return.
                if (e instanceof InterruptedException) {
                    return;
                }
                // else, re-call the long polling method (it was probably a timeout)
                else {
                    getSessionsResponse = GameSession.getSessions(prevPayload);
                }
            }

            if (getSessionsResponse == null) {
                Logger.getGlobal().info("Failed to initialize the game info in the LobbyWindow using long polling.");
            }
            prevPayload = getSessionsResponse;
        }

        // now that we have new game information, reset the ui
        sessions.removeAll();

        // parse the String response and turn it into a list of String ids
        ArrayList<String> gameIDs = GameSession.getSessionIDFromSessions(getSessionsResponse);

        // iterate through the IDs and get info for each game & add it to the display
        // if there are no sessions, just clear everything
        for (String id : gameIDs) {
            // get game info

            // if this game is from a savegame, don't show it

            try {
                if (GameSession.isFromSavegame(id)) {
                    // continue and don't display
                    continue;
                }
            } catch (IOException e) {
                Logger.getGlobal().info("There was a problem determining whether session " + id + " was from a savegame.");
            }


            JSONObject sessionDetails = GameSession.getSessionDetails(id);
            JSONObject sessionParameters = sessionDetails.getJSONObject("gameParameters");
            ArrayList<String> playerList = GameSession.getPlayerNames(id);
            int numPlayers = playerList.size();
            int maxPlayers = Integer.parseInt(sessionParameters.get("maxSessionPlayers").toString());
            // we don't want to display sessions that have already been launched, since we cannot join them anyway
            if (GameSession.isLaunched(id) || numPlayers == maxPlayers) {
                continue;
            }

            // separate the game info into pieces
            String creator = sessionDetails.get("creator").toString();
            String maxSessionPlayers = sessionParameters.get("maxSessionPlayers").toString();
            String minSessionPlayers = sessionParameters.get("minSessionPlayers").toString();
            String gameName = sessionParameters.get("name").toString();
            String variant = gameNameToDisplayName(gameName);
            String playersOutOfMax = numPlayers + "/" + minSessionPlayers + "-" + maxSessionPlayers;

            String players = "";

            for (String player : playerList) {
                // conditionals to avoid having a trailing whitespace in the String
                if (players.equals("")) {
                    players = players + player;
                } else {
                    players = players + ", " + player;
                }
            }

            // add the game info to labels
            JLabel creatorLabel = new JLabel("Creator: " + creator);
            JLabel variantLabel = new JLabel("Variant: " + variant);
            JLabel playersInSessionLabel = new JLabel("Players: " + players);
            JLabel playerCountLabel = new JLabel("Number of Players: " + playersOutOfMax);

            // initialize join button
            JButton joinButton = new JButton("JOIN");

            joinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // join the game
                    try {
                        //t.stop();
                        track1.play();
                        flag = 1;
                        t.interrupt();
                        String localIP = NetworkUtils.getLocalIPAddPort();
                        GameSession.joinSession(MainFrame.loggedIn, id, localIP);
                        GameManager.init(Optional.empty(), id, interpretVariant(gameName), localIP);

                        // prompt user to choose a boot colour
                        // this calls the ChooseBootWindow once all players have responded
                        GameManager.getInstance().requestAvailableColours();

                    } catch (Exception ex) {
                        System.out.println("There was a problem attempting to join the session with User" + User.getInstance().getUsername());
                        ex.printStackTrace();
                    }
                }
            });

            // initialize the box
            Box gameInfo = Box.createVerticalBox();
            gameInfo.setBorder(BorderFactory.createLineBorder(Color.black));
            gameInfo.setOpaque(true);

            // add the button and the labels to the box
            gameInfo.add(playersInSessionLabel);
            gameInfo.add(playerCountLabel);
            gameInfo.add(creatorLabel);
            gameInfo.add(variantLabel);
            gameInfo.add(joinButton);

            sessions.add(gameInfo);
        }

        sessions.repaint();
        sessions.revalidate();
    }

    private void chooseSaveGame() {
        JFileChooser f = new JFileChooser("./out/saves");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Savegame files", ".elf");
        f.addChoosableFileFilter(filter);
        f.setFileSelectionMode(JFileChooser.FILES_ONLY);//.DIRECTORIES_ONLY);
        //f.setFileFilter(filter);
        int selection = f.showOpenDialog(this);
        // if the user cancelled without selecting something, go back to the lobby window
        if (selection == JFileChooser.CANCEL_OPTION) {

        } else {
            flag = 1;
            // the user has selected a .elf savegame file
            File saveFile = f.getSelectedFile(); // this file is a .elf file representing a savegame object
            try {
                Savegame loaded = Savegame.read(saveFile);
                String localIP = NetworkUtils.getLocalIPAddPort();
                GameManager.init(Optional.of(loaded), loaded.getSessionID(), loaded.getGameVariant(), localIP);
            } catch (Exception e) {
                Logger.getGlobal().severe("There was a problem reading in the savegame from " + saveFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        while (true) {
            System.out.println("thread alive");
            if (flag == 1) {
                break;
            }
            try {
                initializeGameInfo(sessions);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Logger.getGlobal().info("Ended the thread while loop.");
    }

    /**
     * turns a String into a value from the GameVariant enum
     *
     * @param variant the variant as a String, obtained from the LS
     * @return
     */
    public static GameVariant interpretVariant(String variant) {
        switch (variant) {
            case "Elfenland_Classic":
                return GameVariant.ELFENLAND_CLASSIC;
            case "Elfenland_Long":
                return GameVariant.ELFENLAND_LONG;
            case "Elfenland_Destination":
                return GameVariant.ELFENLAND_DESTINATION; // I know this one is different. It is different in the LS.
            case "Elfengold_Classic":
                return GameVariant.ELFENGOLD_CLASSIC;
            case "Elfengold_Destination":
                return GameVariant.ELFENGOLD_DESTINATION;
            case "Elfengold_RandomGold":
                return GameVariant.ELFENGOLD_RANDOM_GOLD;
            case "Elfengold_Witch":
                return GameVariant.ELFENGOLD_WITCH;

            default:
                return null; // if we set up the LS right, this will never happen
        }
    }

    /**
     * when we fetch the game information for a session , we get the gameName and not the gameDisplayName. The displayName is prettier, so we'll translate it before showing it on the sessions info box
     *
     * @param gameName the game name retrieved from the LS
     * @return
     */
    public static String gameNameToDisplayName(String gameName) {
        switch (gameName) {
            case "Elfenland_Classic":
                return "Elfenland (Classic)";
            case "Elfenland_Long":
                return "Elfenland (Long)";
            case "Elfenland_Destination":
                return "Elfenland (Destination)";
            case "Elfengold_Classic":
                return "Elfengold (Classic)";
            case "Elfengold_Destination":
                return "Elfengold (Destination)";
            case "Elfengold_RandomGold":
                return "Elfengold (Random Gold)";
            case "Elfengold_Witch":
                return "Elfengold (Witch)";
            default:
                return null;
        }
    }

    public static String variantToGameName(GameVariant variant) {
        switch (variant) {
            case ELFENLAND_CLASSIC:
                return "Elfenland_Classic";
            case ELFENLAND_LONG:
                return "Elfenland_Long";
            case ELFENLAND_DESTINATION:
                return "Elfenland_Destination";
            case ELFENGOLD_CLASSIC:
                return "Elfengold_Classic";
            case ELFENGOLD_DESTINATION:
                return "Elfengold_Destination";
            case ELFENGOLD_RANDOM_GOLD:
                return "Elfengold_RandomGold";
            case ELFENGOLD_WITCH:
                return "Elfengold_Witch";
            default:
                return null;
        }
    }


}