package com.jue;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;


public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;


    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private  void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String TextLine;
        while((TextLine = reader.readLine()) != null){
            String[] tokens = StringUtils.split(TextLine) ;
            if(  tokens.length >0 && tokens != null) {
                String cmd = tokens[0];
                if (  "quit".equalsIgnoreCase(cmd) || "logoff".equals(cmd)) {
                    handleLogoff();
                    break;
                }
                else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(TextLine, null, 3);
                    handleMessage(tokensMsg);
                }
                else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                }

                else{
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }

            }
        }
        clientSocket.close();
    }
    // format: "msg" "login" mainBody...
    private void handleMessage(String[] tokens) throws IOException {
        String sendToUser = tokens[1];
        String mainBody = tokens[2];

        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList) {
            if (sendToUser.equalsIgnoreCase(worker.getLogin())) {
                String outMsg = "msg " + login + " " + mainBody + "\n";
                worker.send(outMsg);
            }
        }
    }

    private void handleLogoff() throws IOException {
        List<ServerWorker> workerList = server.getWorkerList();
        server.removeWorker(this);
        // send other online users current user's status
        String onlineMsg = "offline " + login + "\n";
        for(ServerWorker worker : workerList) {
            if(!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];

            if(login.equals("guest") && password.equals("guest") || login.equals("jue") && password.equals("jue")|| login.equals("cr7") && password.equals("cr7")){
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully: " + login);

                List<ServerWorker> workerList = server.getWorkerList();

                // send current user all other online login users
                for(ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if(!login.equals(worker.getLogin())) {
                            String msg1 = "online " + worker.getLogin() + "\n";
                            send(msg1);
                        }
                    }
                }

                // send other online users about current user's status
                String onlineMsg = "online " + login + "\n";
                for(ServerWorker worker : workerList) {
                    if(!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }

            }
            // login failed case
            else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for user " + login);
            }
        }
    }

    private void send(String msg) throws IOException {
        if(login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
