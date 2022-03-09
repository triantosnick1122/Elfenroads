package loginwindow;

import networking.PlayerServer;
import networking.User;
import utils.NetworkUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.swing.Box.createVerticalStrut;


public class LoginWindowNoNgrok extends JPanel implements Runnable{

    private JLabel background_elvenroads;
    private static Box labelBox;
    private static Box textFieldBox;
    private static Box boxPanel;
    private static JTextField usernameTextField;
    private static JPasswordField passwordTextField;
    private static JLabel usernameLabel;
    private static JLabel passwordLabel;
    private static JButton loginButton;
    private static JButton backButton;
    private JPanel infoPanel;

    private static Box box_paste_button;
    private static JButton createNewAccountButton;

    private JLabel popup;
    private Thread t;
    private int flag_username = 0;
    private int flag_password = 0;
    private int flag_ngrok = 0;
    private int flag_error = 0;

    private void initThread()
    {
        t = new Thread(this);
    }

    LoginWindowNoNgrok()
    {
	initThread();
	MP3Player track1 = new MP3Player("./assets/Music/JLEX5AW-ui-medieval-click-heavy-positive-01.mp3");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());

        infoPanel = new JPanel(new BorderLayout());

        background_elvenroads = MainFrame.getInstance().getElfenroadsBackground();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.gridwidth = 1;
        gbc.gridheight= 3;

        labelBox = Box.createVerticalBox();
        textFieldBox = Box.createVerticalBox();

        usernameTextField = new JTextField(15);
        passwordTextField = new JPasswordField(15);
        usernameLabel = new JLabel();
        passwordLabel = new JLabel();
        popup = new JLabel();
        usernameLabel.setText("Username:");
        passwordLabel.setText("Password:");
        loginButton = new JButton("Enter");
        createNewAccountButton = new JButton("Create a new account");
        createNewAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // open a new createNewAccount window
                MainFrame.mainPanel.add(new CreateAccountWindow(), "createAccount");
                MainFrame.cardLayout.show(MainFrame.mainPanel, "createAccount");
            }
        });

        loginButton.addActionListener(new ActionListener()
        {

            @SuppressWarnings("deprecation")
			@Override
            public void actionPerformed(ActionEvent e) 
            {
                
            	String username = usernameTextField.getText();
            	String password = passwordTextField.getText();
            	boolean u = false;
            	try 
            	{
					u = User.doesUsernameExist(username);
				} 
            	catch (Exception e1) // TODO: create a custom exception for the one raised in doesUsernameExist and edit this to catch both an IOException and the custom one
            	{
					e1.printStackTrace();
				}
            	boolean p = NetworkUtils.isValidPassword(password);
                try 
                {
                    // first, make sure the username exists. if not, display the appropriate pop-up.
                    if (!u)
                    {
                        synchronized(t)
                        {
                            flag_username = 1;
                            initThread();
                            t.start();
                        }
                        return;     
                    }

                    // log into the LS
                    MainFrame.loggedIn = null; // unecessary to set to null probably but I just want to make sure that we have no issues with User
                    MainFrame.loggedIn = User.init(username, password);

                } 
                catch (Exception loginProblem)
                {
                    loginProblem.printStackTrace();
                    synchronized(t)
                    {
                        flag_password = 1;
                        initThread();
                        t.start();
                    }
                    return;
                }

            	remove(background_elvenroads);
                MainFrame.mainPanel.add(new LobbyWindow(), "lobby");
                MainFrame.cardLayout.show(MainFrame.mainPanel,"lobby");
            }
            
        });

        // making it so that the user can log in by pressing enter
        // the default action listener for a text field is the enter key, so we don't have to specify
        passwordTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameTextField.getText();
                String password = passwordTextField.getText();
                boolean u = false;
                try 
            	{
					u = User.doesUsernameExist(username);
				} 
            	catch (Exception e1) // TODO: create a custom exception for the one raised in doesUsernameExist and edit this to catch both an IOException and the custom one
            	{
					e1.printStackTrace();
				}
            	boolean p = NetworkUtils.isValidPassword(password);
                try 
                {
                    // first, make sure the username exists. if not, display the appropriate pop-up.
                    if (!u)
                    {
                        synchronized(t)
                        {
                            flag_username = 1;
                            initThread();
                            t.start();
                        }
                        return;     
                    }

                    // log into the LS

                    MainFrame.loggedIn = User.init(username, password);

                    //MainFrame.loggedIn = null; // unecessary to set to null probably but I just want to make sure that we have no issues with User
                    //MainFrame.loggedIn = User.init(username, password);

                } 
                catch (Exception loginProblem)
                {
                    loginProblem.printStackTrace();
                    synchronized(t)
                    {
                        flag_password = 1;
                        initThread();
                        t.start();
                    }
                    return;
                }

            	remove(background_elvenroads);
                MainFrame.mainPanel.add(new LobbyWindow(), "lobby");
                MainFrame.cardLayout.show(MainFrame.mainPanel,"lobby");
            }
        });
        

        labelBox.add(usernameLabel);
        usernameLabel.setAlignmentX(LEFT_ALIGNMENT);
        labelBox.add(createVerticalStrut(10)); // small separation between lines
        labelBox.add(passwordLabel);
        passwordLabel.setAlignmentX(LEFT_ALIGNMENT);
        labelBox.add(createVerticalStrut(10));

        textFieldBox.add(usernameTextField);
        textFieldBox.add(passwordTextField);

        boxPanel = Box.createHorizontalBox();
        boxPanel.add(labelBox);
        boxPanel.add(textFieldBox);
        boxPanel.add(loginButton);
        boxPanel.add(createNewAccountButton);

        box_paste_button = Box.createHorizontalBox();
        box_paste_button.add(popup);
        popup.setAlignmentX(RIGHT_ALIGNMENT);
        
        infoPanel.add(boxPanel,BorderLayout.CENTER);
        infoPanel.add(box_paste_button,BorderLayout.SOUTH);

        background_elvenroads.setLayout(layout);
        background_elvenroads.add(infoPanel,gbc);

        add(background_elvenroads);

    }

     // Method executed by Thread t
     @Override
     public void run() 
     {
         // Print error message for wrong username
         if (flag_username == 1)
         {
             synchronized(t)
             {        
                 flag_error++;
                 popup.setText("That username does not exist in the LS system. Please try again. ERROR#" + flag_error);
                 try
                 {
                     t.sleep(3000);
                 } 
                 catch (InterruptedException e) 
                 {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 popup.setText(null);
         
                 flag_username = 0;
                 t.stop();
             }
         }
 
         // print error message for wrong password
         else if(flag_password == 1)
         {
             synchronized(t)
             {        
                 flag_error++;
                 popup.setText("The password entered is incorrect. Please try again. ERROR#" + flag_error);
                 try
                 {
                     t.sleep(3000);
                 } 
                 catch (InterruptedException e) 
                 {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 popup.setText(null);
         
                 flag_password = 0;
                 t.stop();
             }
         }
 

 
     }

}
