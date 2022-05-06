/*
 *
 */
package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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


/**
 *
 * @author EternalBlue
 */
public class Server {
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        
        // database 
                    
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
                    if (connection.isValid(1)){
                        System.out.println("Database connected!");
                    }else{
                        System.out.println("Error establishing database connection!");
                    }
        
       
        
        ServerSocket serverSocket = new ServerSocket(8080);
        try {
            while (true){
                Socket socket = serverSocket.accept();
                startHandler(socket, connection);
            }
        } finally {
            serverSocket.close();
        }
    }
    
    private static void register(Connection connection, JSONObject jsonObject, OutputStreamWriter writer) throws SQLException, IOException{
                        String userName = jsonObject.getString("username");
                        String passWord = jsonObject.getString("password");
                        // user exists
                        /*Statement commandSql = connection.createStatement();
                        ResultSet checkUser = commandSql.executeQuery("SELECT count(*) FROM user WHERE username = '" + userName + "' AND password = " + passWord);
                        while(!checkUser.next())
                            {
                                System.out.println("user exists in db? : " + checkUser.getInt(1));
                            }
                        if(userName.isEmpty()){ // pra que isso? so checar no cliente se o username box é vazio.
                            JSONObject registerFailEmpty = new JSONObject();
                            registerFailEmpty.put("protocolo", "302");
                            writer.write(registerFailEmpty.toString() + "\n");
                            System.out.println("send to client: " + registerFailEmpty);
                            writer.flush();
                            System.out.println("register failed, username empty");
                        }else if(checkUser.getInt(1) == 1){
                            JSONObject registerFail = new JSONObject();
                            registerFail.put("protocolo", "302");
                            writer.write(registerFail.toString() + "\n");
                            System.out.println("send to client: " + registerFail);
                            writer.flush();
                            System.out.println("register failed, username already exist");
                        }else{*/
                            Statement stmt = connection.createStatement();                       
                            // ResultSet rs = 
                            stmt.executeUpdate("INSERT INTO user (username, password) VALUES ('" + userName + "', '" + passWord + "')");
                            JSONObject registerSuccess = new JSONObject();
                            registerSuccess.put("protocolo", "301");
                            writer.write(registerSuccess.toString() + "\n");
                            System.out.println("REGISTER SUCCESS:" + registerSuccess);
    }
    private static void login(Connection connection, JSONObject jsonObject, OutputStreamWriter writer) throws SQLException, IOException{
                        Statement stmt = connection.createStatement();
                        String userName = jsonObject.getString("username");
                        String passWord = jsonObject.getString("password");
                        int loginSituation = 0;
                        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM user WHERE username = '" + userName + "' AND password = " + passWord);
                        //System.out.println(rs);
                        while(rs.next())
                            {
                                loginSituation = rs.getInt(1);
                                System.out.println("user & pass true? : " + rs.getInt(1));
                            }
                        // if (jsonObject.getString("username").equals("exemplo") && jsonObject.getString("password").equals("123")){
                        if (loginSituation == 1){ 
                            JSONObject loginAuthorized = new JSONObject();
                            loginAuthorized.put("protocolo", "101");
                            writer.write(loginAuthorized.toString() + "\n");
                            System.out.println(loginAuthorized);
                            writer.flush();
                            System.out.println("login successful");
                        }else{
                            JSONObject loginNotAuthorized = new JSONObject();
                            loginNotAuthorized.put("protocolo", "102");
                            writer.write(loginNotAuthorized.toString() + "\n");
                            System.out.println("send to client: " + loginNotAuthorized);
                            writer.flush();
                            System.out.println("login not authorized, username or password incorrect");
                        }
    }
    
    
    private static void startHandler(Socket socket, Connection connection) throws IOException {
        Thread thread = new Thread(){
            @Override
            public void run(){
                try {
                    
                    OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader (new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String line = reader.readLine();
                    JSONObject jsonObject = new JSONObject(line);

                    writer.write(jsonObject.toString() + "\n");
                    while(true){
                        if (jsonObject.getString("protocolo").equals("100") ) { 
                            login(connection, jsonObject, writer);
                            writer.flush();
                        }else if (jsonObject.getString("protocolo").equals("300")){
                            register(connection, jsonObject, writer);
                            writer.flush();  
                        }else{
                             System.out.println("Client a unk protocol!: " + line);
                        }
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        line = reader.readLine();
                        
                    }              
                    
                    } catch (IOException | SQLException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        thread.start(); 
    }
}

