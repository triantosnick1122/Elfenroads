import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.net.InetAddress;

public class GameSession {

    private User creator;
    private String gameName; // matches up to a GameService object, but we will not use a GameService object because we just need the name
    private String saveGameName;
    private String locationIP; // this should be the system IP address

    // this class will represent a GameSession
    // its constructor will intialize a GameSession


    public GameSession(User pCreator, String pGameName, String pSaveGameName) throws IOException
    {
        creator = pCreator;
        gameName = pGameName;
        saveGameName = pSaveGameName;
        InetAddress ip = InetAddress.getLocalHost();
        locationIP = ip.getHostAddress();
        createNewSession();
    }



    private void createNewSession() throws IOException
    {
        String token = creator.getAccessToken();


        URL url = new URL("http://127.0.0.1:4242/api/sessions?access_token=" + token + "&location=" + locationIP);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        /* Payload support */
        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes("{\n");
        out.writeBytes("    \"creator\": \"" + creator.getUsername() + "\",\n");
        out.writeBytes("    \"game\": \"" + gameName + "\",\n");
        out.writeBytes("    \"savegame\": \"\"\n");
        out.writeBytes("}");
        out.flush();
        out.close();

        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        System.out.println("Response status: " + status);
        System.out.println(content.toString());

    }


    public User getCreator() {
        return creator;
    }

    public String getGameName() {
        return gameName;
    }

    public String getSaveGameName() {
        return saveGameName;
=======
    
    public static List<JSONObject> getSessions() throws IOException
    {
    	URL url = new URL("http://127.0.0.1:4242/api/gameservices");
    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");

    	int status = con.getResponseCode();
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    	String inputLine;
    	StringBuffer content = new StringBuffer();
    	while((inputLine = in.readLine()) != null) {
    		content.append(inputLine);
    	}
    	in.close();
    	con.disconnect();
    	System.out.println("Response status: " + status);
    	//System.out.println(content.toString());
    	
    	// Start here
        String session = "";
        List<JSONObject> allSessions = new ArrayList<>();
        String sessionAsString = content.toString();
        
        int flag = 0;
        for (int i = 0; i < content.length(); i++)
        {
        	if (sessionAsString.charAt(i) == '}')
        	{
        		session += sessionAsString.charAt(i);
        		flag = 0;
        		
        		String str = session.toString();
        		JSONObject json = new JSONObject(str);
        		session = "";
        		allSessions.add(json);
        		continue;
        	}
        	
        	if (flag == 1)
        	{
        		session += sessionAsString.charAt(i);
        		continue;
        	}
        	
        	if (sessionAsString.charAt(i) == '{')
        	{
        		session += sessionAsString.charAt(i);
        		flag = 1;
        		continue;
        	}
        	
        }
        
        for (JSONObject j : allSessions)
        {
        	System.out.println(j.toString());
        }
    	
    	return allSessions;
  
    }

    public String getLocationIP() {
        return locationIP;
    }
}
