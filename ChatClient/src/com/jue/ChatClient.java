package com.jue;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverI;
    private OutputStream serverO;
    private BufferedReader bufferedI;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("Online: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("Offline: " + login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String mainBody) {
                System.out.println("You have a message from " + fromLogin + "==>" + mainBody) ;
            }
        });

        if(client.connect()) {
            System.out.println("Connection successful");
            if (client.login("guest", "guest")) {
                System.out.println("Login successful");


            }
            else {
                System.err.println("Login failed");
            }
        }
        else {
            System.err.println("Connection failed");

            //client.logoff();
        }
    }

    public void msg(String sendToUser, String mainBody) throws IOException {
        String cmd = "msg " + sendToUser + " " + mainBody + "\n";
        serverO.write(cmd.getBytes());
    }

    public boolean login(String login, String password) throws IOException {
        String cmd = "Login " + login + " " + password + "\n";
        serverO.write(cmd.getBytes());

        String response = bufferedI.readLine();
        System.out.println("ResponsibleLine: " + response);

        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        }
        else {
            return false;
        }
    }

    public void logoff() throws IOException {
        String cmd = "Logoff\n";
        serverO.write(cmd.getBytes());
    }

    private void startMessageReader() {
        Thread T = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        T.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedI.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if(  tokens.length >0 && tokens != null) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd) ) {
                        handleOnline(tokens);
                    }
                    else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line,null,3);
                        handleMessage(tokensMsg);
                    }
                    else if ("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    }

                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String mainBody = tokensMsg[2];

        for(MessageListener listener : messageListeners) {
            listener.onMessage(login,mainBody );
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }

    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName,serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverO = socket.getOutputStream();
            this.serverI = socket.getInputStream();
            this.bufferedI = new BufferedReader(new InputStreamReader(serverI));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }
}
