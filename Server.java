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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author EternalBlue
 */
public class Server {
    
    public static void main(String[] args) throws IOException {
        
        ServerSocket serverSocket = new ServerSocket(8080);
        try {
            while (true){
                Socket socket = serverSocket.accept();
                startHandler(socket);
            }
        } finally {
            serverSocket.close();
        }
    }
    
    private static void startHandler(Socket socket) throws IOException {
        Thread thread = new Thread(){
            @Override
            public void run(){
                try {
                    OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader (new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    
                    String line = reader.readLine();
                    JSONObject jsonObject = new JSONObject(line);
                    
                    writer.write(jsonObject.toString() + "\n");
                    if (jsonObject.getString("protocol").equals("100")) { 
                        // SELECT * FROM usuarios WHERE login = 'var login' AND senha = 'var senha'
                        if (jsonObject.getString("username").equals("exemplo") && jsonObject.getString("password").equals("123")){
                            
                            JSONObject loginAuthorized = new JSONObject();
                            loginAuthorized.put("protocol", "101");
                            writer.write(loginAuthorized.toString() + "\n");
                            System.out.println(loginAuthorized);
                            writer.flush();
                            System.out.println("login successful");
                        }else{
                            JSONObject loginAuthorized = new JSONObject();
                            loginAuthorized.put("protocol", "102");
                            writer.write(loginAuthorized.toString() + "\n");
                            System.out.println(loginAuthorized);
                            writer.flush();
                            System.out.println("login not authorized, username or password incorrect");
                        }   
                    }
                    else{
                        System.out.println("unknown protocol");
                    }
                    System.out.println("Client send: " + line);
                    writer.flush();
                    
                    } catch (IOException ex) {
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
