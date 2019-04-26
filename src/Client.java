import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.copyOf;

public class Client {

    private String name;
    private SSLSocket connexion;
    private SocketFactory connexionFactory;
    private String timeStamp;

    private int port;
    private boolean isRunning;
    private OutputStream out;
    private String msgToSend;
    private InputStream in;
    private String msgReceived;
    private boolean authenticated;
    private String step;
    private Scanner sc;
    private int index;
    private String hostDomain;

    public Client(String hostServer){
        isRunning = false;
        connexion=null;
        port = 1040;
        connexionFactory = SSLSocketFactory.getDefault();
        try {
            connexion = (SSLSocket) connexionFactory.createSocket(hostServer, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //connexion = new Socket();
        authenticated = false;
        index = 0;
        sc = new Scanner(System.in);

        if (!connexion.isConnected()){
            try {
                connexion.connect(new InetSocketAddress(hostServer, port));
                out = connexion.getOutputStream();
                in = connexion.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            String[] suites = connexion.getSupportedCipherSuites();
            connexion.setEnabledCipherSuites(suites);
            try {
                out = connexion.getOutputStream();
                in = connexion.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("/* Connexion tcp réussie.*/");
        }
        if(connexion.isConnected()){
            byte[] byteIn = new byte[512];
            try {
                in.read(byteIn);
                isRunning = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            int sizeIn = 0;
            while (sizeIn < 512 && byteIn[sizeIn] != 0) {
                sizeIn++;
            }
            byteIn = copyOf(byteIn, sizeIn);
            msgReceived = new String(byteIn, UTF_8);
            System.out.println("/* Réponse du serveur :" + msgReceived + "*/");
            String[] msgSplit= msgReceived.split(" ");
            if (msgSplit[0].contains("+OK") && msgSplit.length >= 5) {
                timeStamp = msgSplit[4];
            }
        }
        step = "TCP OK+";
    }

    public void run(){
        while(isRunning){
            System.out.print("Command :");
            msgToSend =  sc.nextLine();
            System.out.println(msgToSend +" test run)");

            switch(msgToSend.substring(0,4)) {
                case "EHLO":
                    if (step.equals("TCP OK+")) {
                        send();
                        step = "Ready";
                        listen();
                    }
                    break;
                case "MAIL":
                    if (step.equals("Ready+")) {
                        send();
                        step = "Starting transaction";
                        listen();
                    }
                    break;
                case "RCPT":
                    if (step.equals("Starting transaction+"))
                        send();
                        step = "Sending Recipient";
                        listen();
                    }
                    break;
                case "QUIT":
                    //todo
                    break;
            }
        }
    }

    public void listen() {
        byte[] byteIn = new byte[512];
        try {
            in.read(byteIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msgReceived = new String(byteIn, UTF_8);
        System.out.println("/* Réponse du serveur :" + msgReceived + "*/");

        switch (msgReceived.substring(0, 3)) {
            case "250":
                if (step.equals("Ready")) {
                    step = "Ready+";

                } else if (step.equals("Start transaction")) {
                    step = "Starting transaction+";
                    hostDomain = msgReceived.substring(4);
                }
                else if (step.equals("Sending recipient")) {
                    step = "Sending recipient+";
                }
                break;

            case "550":
                if (step.equals("Ready")) {
                    // Do nothing
                }
                break;
        }
    }

    public void send(){
        byte[] byteOut = msgToSend.getBytes();
        try {
            out.write(byteOut);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("* Envoyé - Attente de réponse *");
    }

    /*


            case "APOP":
                if (msgReceived.substring(0,3).equals("+OK")){
                    authenticated = true;
                    System.out.println("Authentification réussi!");

                }
                else{
                    System.out.println("Erreur d'authentification.");
                }
                break;
            case "STAT":
                if (msgReceived.substring(0,3).equals("+OK")){
                    String affichage = msgReceived.split(" ")[1] + "message(s). Taille totale : " + msgReceived.split(" ")[2] + "octets.";
                    System.out.println(affichage);
                }
                else{
                    System.out.println("Erreur lors de l'actualisation.");
                }
                break;
            case "RETR":
                if (msgReceived.substring(0,3).equals("+OK")) {
                    System.out.println(msgReceived);
                    index++;
                    PrintWriter writer = null;
                    try {
                        writer = new PrintWriter("./archiveClient/mail" + index + ".txt");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    writer.println(msgReceived.substring(4));
                    writer.close();
                }
                else{
                    System.out.println("Erreur lors de la lecture");
                }
                break;
    */


    public static void main(String[] arg) {
    //String hostserveur = "192.168.43.1";
        String hostserveur = "localhost";

        Client client = new Client(hostserveur) ;
        client.run();
    }

}
