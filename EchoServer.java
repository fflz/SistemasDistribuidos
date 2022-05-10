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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class EchoServer extends Thread {

    private Socket socket;

    public EchoServer(Socket socket) {
        this.socket = socket;
    }

    public static Connection connectDb() throws ClassNotFoundException, SQLException, SQLException {
        String connectionUrl = "jdbc:mysql://35.235.79.243:3306/distribuidosapp?useSSL=false";
        //String connectionUrl = "jdbc:mysql://sql3.freesqldatabase.com:3306/sql3489677";       
        String username = "equation";
        //String username = "sql3489677";
        String password = "CElOBmCMeiEuj0eD";
        //String password = "AuMJ7uDQXW";
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

            Connection connection = connectDb(); // only need to connect one time (?)

            // The server uses input streams and output streams to receive and send input from/to the client
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            InetAddress inetAddress = InetAddress.getByName(null);
            SocketAddress socketAddress = new InetSocketAddress(inetAddress, 5000);

            // Infinite loop that continues responding to client messages and breaks when the client writes "exit"
            while (true) {
                String echoString = clientInput.readLine();
                JSONObject jsonObject = new JSONObject(echoString);
                if (jsonObject.getString("protocolo").equals("100")) {
                    System.out.println("[" + socket.getInetAddress() + "] Received message: " + echoString);
                }
                Statement stmt = connection.createStatement();
                String userName = jsonObject.getString("username");
                String passWord = jsonObject.getString("password");
                if (userName.isBlank() || userName.isEmpty() || passWord.isBlank() || passWord.isEmpty()){ // evita ler nulo se enviado em branco
                    userName = "-111";
                    passWord = "-111";                    
                }                
                int loginSituation = 0;
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM user WHERE username = '" + userName + "' AND password = " + passWord);

                while (rs.next()) {
                    loginSituation = rs.getInt(1);
                    System.out.println("user & pass true? : " + rs.getInt(1));
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

                if (echoString.toLowerCase().equals("exit")) {
                    output.println("Goodbye.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR IOException: " + e.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
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
        Connection connection = connectDb();
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
