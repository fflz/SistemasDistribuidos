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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.json.JSONObject;

public class EchoClient {
    
    public static void createInterface(){
        
    }
    
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        
        JFrame f = new JFrame();
        JLabel login = new JLabel("  Login");
        JLabel username = new JLabel("Usuario:");
        JLabel password = new JLabel("Senha:");
        JTextPane usernameBox = new JTextPane();
        JPasswordField passwordBox = new JPasswordField();
        JButton botaoCadastro = new JButton("Registro");
        JButton botaoLogin = new JButton("Login");
        
        
        botaoLogin.setBounds(100, 140, 100, 40);
        botaoCadastro.setBounds(100, 190, 100, 40);
        usernameBox.setBounds(110, 55, 75, 25);  
        passwordBox.setBounds(110, 95, 75, 25);  
        login.setBounds(110, 1, 75, 75);
        username.setBounds(60, 55, 75, 25);
        password.setBounds(68, 95, 75, 25);
        login.setFont(new Font("Times New Roman",Font.BOLD,20));
        
        
        f.add(botaoLogin);
        f.add(botaoCadastro);
        f.add(login);
        f.add(username);
        f.add(password);        
        f.add(usernameBox);          
        f.add(passwordBox);
        f.setSize(300,300);  
        f.setLayout(null); 
        f.setVisible(true); 
        
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
            break;
            }
        }
        
        try (Socket socket = new Socket("localhost", 5000)) {
            socket.setSoTimeout(5000);

            // Use PrintWriter to create output stream to send server and use BufferedReader to read the stream coming from server
            PrintWriter outputStreamToSendServer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverResponseStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Scanner s = new Scanner(System.in); // TODO: remove
            String outputMessage, response;
            
            botaoLogin.addActionListener((ActionListener) new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e){
                    try{
                        String username = usernameBox.getText();
                        char[] passwordChar = passwordBox.getPassword();
                        String password = String.valueOf(passwordChar);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("protocolo", "100");
                        jsonObject.put("username", username); 
                        jsonObject.put("password", password); 
                        String resposta;
                        outputStreamToSendServer.println(jsonObject.toString());
                        resposta = serverResponseStream.readLine();
                        JSONObject loginAnswer = new JSONObject(resposta);
                        System.out.println("I received : " + loginAnswer);
                        if (loginAnswer.getString("protocolo").equals("101")) {
                            System.out.println("Login successful");
                            JOptionPane.showMessageDialog(null, "login authorized", "Login", 1);
                        } else {
                            System.out.println("login not authorized, username or password incorrect");
                            JOptionPane.showMessageDialog(null, "Incorrect username or password", "Login", 1);
                        }
                } catch (IOException ex) {
                    Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }});
            
            botaoCadastro.addActionListener((ActionListener) new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                        int success = -1;
                    
                        String username = usernameBox.getText();
                        char[] passwordChar = passwordBox.getPassword();
                        String password = String.valueOf(passwordChar);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("protocolo", "300");
                        jsonObject.put("username", username); 
                        jsonObject.put("password", password); 
                        String resposta;
                        
                        outputStreamToSendServer.println(jsonObject.toString());
                        System.out.println("I sent " + jsonObject.toString());
                        
                        String registerLine = serverResponseStream.readLine();
                        JSONObject registerAnswer = new JSONObject(registerLine);
                        System.out.println("I received: " + registerAnswer);
                        System.out.println(registerAnswer);
                        
                    switch (registerAnswer.getString("protocolo")) {
                        case "301" -> { // '->'' java 14 
                            System.out.println("register successful");
                            JOptionPane.showMessageDialog(null, "registration done", "Register", 1);
                        }
                        case "302" -> {
                            System.out.println("user already exists");
                            JOptionPane.showMessageDialog(null, "User already exists", "Register", 1);
                        }
                        case "303" -> {
                            System.out.println("field empty");
                            JOptionPane.showMessageDialog(null, "blank user field", "Register", 1);
                        }
                        default -> {
                                System.out.println("unk protocol");
                                JOptionPane.showMessageDialog(null, "?????", "Register", 1);
                        }
                    }                      
                    
                } catch (IOException ex) {
                    Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                    
        });
            
            do {
                outputMessage = s.nextLine();
                outputStreamToSendServer.println(outputMessage);

                if (!outputMessage.toLowerCase().equals(null)) {
                    response = serverResponseStream.readLine();
                    System.out.println(response);
                }
            } while (!outputMessage.equals(null));

            response = serverResponseStream.readLine();
            System.out.println(response);
            
            
            
        } catch (SocketTimeoutException e) {
            System.out.println("ERROR socket timeout: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR connecting to server: " + e.getMessage());
        }
        
        
                
        
    }
}