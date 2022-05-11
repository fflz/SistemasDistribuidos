/*
 * ▄▄▄ ..▄▄▄  ▄• ▄▌ ▄▄▄· ▄▄▄▄▄▪         ▐ ▄      ▄▄ • ▄▄▄        ▄• ▄▌ ▄▄▄·
 * ▀▄.▀·▐▀•▀█ █▪██▌▐█ ▀█ •██  ██ ▪     •█▌▐█    ▐█ ▀ ▪▀▄ █·▪     █▪██▌▐█ ▄█
 * ▐▀▀▪▄█▌·.█▌█▌▐█▌▄█▀▀█  ▐█.▪▐█· ▄█▀▄ ▐█▐▐▌    ▄█ ▀█▄▐▀▀▄  ▄█▀▄ █▌▐█▌ ██▀·
 * ▐█▄▄▌▐█▪▄█·▐█▄█▌▐█ ▪▐▌ ▐█▌·▐█▌▐█▌.▐▌██▐█▌    ▐█▄▪▐█▐█•█▌▐█▌.▐▌▐█▄█▌▐█▪·•
  * ▀▀▀ ·▀▀█.  ▀▀▀  ▀  ▀  ▀▀▀ ▀▀▀ ▀█▄▀▪▀▀ █▪    ·▀▀▀▀ .▀  ▀ ▀█▄▀▪ ▀▀▀ .▀   
 */
package com.equationgroup.sistemadistribuido;

/**
 *
 * @author EternalBlue
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

public class EchoServer extends Thread {

    private final Socket socket;

    public EchoServer(Socket socket) {
        this.socket = socket;
    }

    public static Connection connectDb() throws ClassNotFoundException, SQLException, SQLException {
        String connectionUrl = "jdbc:mysql://IP/DATABASE?useSSL=false";       
        String username = "";
        String password = "";
        System.out.println("Loading driver...");

        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("Driver loaded!");
        System.out.println("Connecting database...");
        Connection connection = DriverManager.getConnection(connectionUrl, username, password);
        if (connection.isValid(1)) {
            System.out.println("Database connected!");
            return connection;
        } else {
            System.out.println("Error establishing database connection!");
            return null;
        }
    }

    @Override
    public void run() {
        try {

            Connection connection = connectDb(); // TODO: only need to connect one time (?)

            // The server uses input streams and output streams to receive and send input from/to the client
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Infinite loop that continues responding to client messages and breaks when the client writes "exit"
            while (true) {
                String echoString = clientInput.readLine();
                JSONObject jsonObject = new JSONObject(echoString);
                if (jsonObject.getString("protocolo").equals("100")) {
                    System.out.println("[" + socket.getInetAddress() + "] Received message: " + echoString);

                    Statement stmt = connection.createStatement();
                    String userName = jsonObject.getString("username");
                    String passWord = jsonObject.getString("password");
                    if (userName.isBlank() || userName.isEmpty() || passWord.isBlank() || passWord.isEmpty()) {
                        userName = "-111"; // TODO: return protocol not change user/pass
                        passWord = "-111";
                    }
                    int loginSituation = 0;
                    ResultSet rs = stmt.executeQuery("SELECT count(*) FROM user WHERE username = '" + userName + "' AND password = '" + passWord + "'");

                    while (rs.next()) {
                        loginSituation = rs.getInt(1);
                        System.out.println("user & pass true? : " + rs.getInt(1)); // Only for testing
                    }

                    if (loginSituation == 1) {
                        JSONObject loginAuthorized = new JSONObject();
                        loginAuthorized.put("protocolo", "101");
                        output.println(loginAuthorized.toString());
                        System.out.println("login successful");
                        System.out.println("Sending back to [" + socket.getInetAddress() + "]: " + loginAuthorized);
                    } else {
                        JSONObject loginNotAuthorized = new JSONObject();
                        loginNotAuthorized.put("protocolo", "102");
                        output.println(loginNotAuthorized.toString());
                        System.out.println("login not authorized, username or password incorrect");
                        System.out.println("Sending back to [" + socket.getInetAddress() + "]: " + loginNotAuthorized);
                    }
                } else if ((jsonObject.getString("protocolo").equals("300"))) {
                    String userName = jsonObject.getString("username");
                    String passWord = jsonObject.getString("password");            
                    
                    Statement commandSql = connection.createStatement();
                    ResultSet checkUser = commandSql.executeQuery("SELECT count(*) FROM user WHERE username = '" + userName + "' AND password = '" + passWord + "'");
                    while (!checkUser.next()) {
                        System.out.println("user exists in db? : " + checkUser.getInt(1));
                    }
                    if (userName.isEmpty()) {
                        JSONObject registerFailEmpty = new JSONObject();
                        registerFailEmpty.put("protocolo", "302");
                        output.println(registerFailEmpty.toString());
                        System.out.println("send to client: " + registerFailEmpty);
                        System.out.println("register failed, username empty");
                        System.out.println("I sento to client : " + registerFailEmpty);
                        
                    } else if (checkUser.getInt(1) == 1) {
                        JSONObject registerFail = new JSONObject();
                        registerFail.put("protocolo", "302");
                        output.println(registerFail.toString());
                        System.out.println("send to client: " + registerFail);
                        System.out.println("register failed, username already exist");
                    } else {
                        Statement stmt = connection.createStatement();
                        stmt.executeUpdate("INSERT INTO user (username, password) VALUES ('" + userName + "', '" + passWord + "')");
                        JSONObject registerSuccess = new JSONObject();
                        registerSuccess.put("protocolo", "301");
                        output.println(registerSuccess.toString());
                        System.out.println("I sento to client : " + registerSuccess);
                    }

                }

                if (echoString.toLowerCase().equals(null)) {
                    output.println("xD");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR IOException: " + e.getMessage());
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("ERROR IOException on closing socket: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

        int port = 5000;
        // Create server socket that listens on port 5000.
        try ( ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started at port " + port);
            // Because this is an infinite loop, the server will only terminate when manually terminated.
            while (true) {
                // Listens for and accepts connections to the socket at the specified port.
                new EchoServer(serverSocket.accept()).start();
            }
        }
    }

}
