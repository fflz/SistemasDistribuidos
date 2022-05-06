/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;  

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;


import java.sql.SQLException;

/**
 *
 * @author EternalBlue
 */
public class Client {
    
    public static boolean login(OutputStreamWriter writer,BufferedReader reader, String username, String password) throws IOException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("protocolo", "100");
        jsonObject.put("username", username); // get interface login field
        jsonObject.put("password", password); // get interface password field
        writer.write(jsonObject.toString() + "\n");
        writer.flush();
        String line = reader.readLine();
        jsonObject = new JSONObject(line);
        System.out.println("[LOGIN] sent to server: " + jsonObject.toString(2));
        String loginLine = reader.readLine();
        JSONObject loginAnswer = new JSONObject(loginLine);
        reader.readLine();
        System.out.println(loginAnswer);
        if (loginAnswer.getString("protocolo").equals("101")) { 
            System.out.println("Login successful");
            return true;
        }else{
            System.out.println("login not authorized, username or password incorrect");
            return false;
        }
    }
    
    public static int register(OutputStreamWriter writer,BufferedReader reader, String username, String password) throws IOException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("protocolo", "300");
        jsonObject.put("username", username); 
        jsonObject.put("password", password); 
        writer.write(jsonObject.toString() + "\n");
        writer.flush();
        String line2 = reader.readLine();
        jsonObject = new JSONObject(line2);
        System.out.println("[REGISTER] sent to server: " + jsonObject.toString(2));
        String registerLine = reader.readLine();
        JSONObject registerAnswer = new JSONObject(registerLine);
        reader.readLine();
        System.out.println(registerAnswer);
        
        if (registerAnswer.getString("protocolo").equals("301")) {
            System.out.println("register successful");
            return 0;
        }else if (registerAnswer.getString("protocolo").equals("302")) {
            System.out.println("user already exists");
            return 1;
        }else if (registerAnswer.getString("protocolo").equals("303")) {
            System.out.println("field empty");
            return 2;
        }else{
            System.out.println("unk protocol");
            return -1;
        }
    }
    
    public static void createInterface() throws IOException{
        
         JFrame f = new JFrame();
        JLabel login = new JLabel("  Login");
        JLabel username = new JLabel("User:");
        JLabel password = new JLabel("Pass:");
        JTextPane usernameBox = new JTextPane();
        JPasswordField passwordBox = new JPasswordField();
        JButton botaoCadastro = new JButton("Register");
        JButton botaoLogin = new JButton("Login");
        
        
        botaoLogin.setBounds(100, 140, 100, 40);
        botaoCadastro.setBounds(100, 190, 100, 40);
        usernameBox.setBounds(110, 55, 75, 25);  
        passwordBox.setBounds(110, 95, 75, 25);  
        login.setBounds(110, 1, 75, 75);
        username.setBounds(60, 55, 75, 25);
        password.setBounds(68, 95, 75, 25);
        login.setFont(new Font("Times New Roman",Font.BOLD,20));
        
        // existence
        f.add(botaoLogin);
        f.add(botaoCadastro);
        f.add(login);
        f.add(username);
        f.add(password);        
        f.add(usernameBox);          
        f.add(passwordBox);
        
        var ip = "localhost";
        var port = 8080;
        Socket socket = new Socket(ip, port);
        
        OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        BufferedReader reader = new BufferedReader (new InputStreamReader(socket.getInputStream(), "UTF-8"));
        
               
        
        botaoLogin.addActionListener((ActionListener) new ActionListener() {
            @Override
            @SuppressWarnings("empty-statement")
                    public void actionPerformed(ActionEvent e) {try {;
                        String username = usernameBox.getText(); 
                        char[] passwordChar = passwordBox.getPassword(); 
                        String password = String.valueOf(passwordChar);
                        boolean success = login(writer, reader, username, password);
                        if (success){
                            JOptionPane.showMessageDialog(null, "login authorized", "Login", 1);
                        }else{
                            JOptionPane.showMessageDialog(null, "Incorrect username or password", "Login", 1);
                        }
                        
                      
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        botaoCadastro.addActionListener((ActionListener) new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    OutputStreamWriter writer2 = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    BufferedReader reader2 = new BufferedReader (new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String username = usernameBox.getText(); 
                    char[] passwordChar = passwordBox.getPassword(); 
                    String password = String.valueOf(passwordChar); 
                    
                    int success = register(writer2, reader2, username, password);
                    switch (success) {
                        case 0 -> JOptionPane.showMessageDialog(null, "registration done", "Register", 1);
                        case 1 -> JOptionPane.showMessageDialog(null, "User already exists", "Register", 1);
                        case 2 -> JOptionPane.showMessageDialog(null, "blank user field", "Register", 1);
                        default -> JOptionPane.showMessageDialog(null, "?????", "Cadastro", 1);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                    
        });
        
        f.setSize(300,300);  
        f.setLayout(null); 
        f.setVisible(true);  
   
    } 
    
    
    public static void main(String[] args) throws IOException {
        createInterface();       
    }
    
}
