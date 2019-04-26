import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.*;

public class Client implements Runnable{

    private static final String CLIENT_DNS = "alpha.edu";
    private String name;

    private Socket connexion;
    private int port;
    private boolean isRunning;

    private OutputStream out;
    private String msgToSend;
    private InputStream in;
    private String msgReceived;
    private boolean authenticated;
    private String action;
    private Scanner sc;
    private int index;

    public Client(String hostServer){
        isRunning = true;
        connexion=null;
        port = 1040;
        connexion = new Socket();
        authenticated = false;
        index = 0;
        sc = new Scanner(System.in);


        if (!connexion.isConnected()){
            try {
                connexion.connect(new InetSocketAddress(hostServer, port));
                System.out.println("/* Connexion tcp réussie.*/");
                out = connexion.getOutputStream();
                in = connexion.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run(){
        while(isRunning){
            action();
            byte[] byteOut = msgToSend.getBytes();
            try {
                out.write(byteOut);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("*Attente de reponse*");
            byte[] byteIn = new byte[512];
            try {
                in.read(byteIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            msgReceived = new String(byteIn, UTF_8);
            System.out.println("/* Réponse du serveur :" + msgReceived + "*/");
            switch (action){
                case "QUIT":
                    System.out.println("Déconnexion");
                    break;
            }
        }
    }

    public void action(){
        if (!authenticated) {
            System.out.println("Bienvenue sur la messagerie!");
            action = "EHLO " + CLIENT_DNS;
        }
        else{
            System.out.println("Veuillez choisir l'action à réaliser.");
            System.out.println("1 : Envoyer un mail");
            System.out.println("2 : Quitter");
            String choix = sc.nextLine();
            switch (choix){
                case "1" :
                    startMailTransaction();
                    break;
                case "2":
                    action = "QUIT";
                    msgToSend = "QUIT";
                    break;
            }
        }
    }

    private void startMailTransaction() {
        System.out.println("Envoi du mail");
        System.out.println("Veuillez saisir l'expéditeur");
        String expediteur = sc.nextLine();
        msgToSend = "MAIL FROM <" + expediteur + ">";
        send();
        listen();
        if (msgReceived.substring(0,3).equals("250")) {
            addRecipientTransaction();
        } else {
            System.out.println("Echec");
        }
    }

    private void addRecipientTransaction() {
        System.out.println("Ajout d'un destinataire");
        String destinataire = sc.nextLine();
        msgToSend = "RCPT TO <" + destinataire + ">";
        send();
        listen();
        if (msgReceived.substring(0,3).equals("250")) {
            addRecipientTransaction();
        } else {
            System.out.println("Echec");
        }
    }

    public static void main(String[] args) {
        String hostserveur = "192.168.43.66";
        //String hostserveur = "localhost";

        Client client = new Client(hostserveur) ;
        client.run();
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
    public void listen() {
        byte[] byteIn = new byte[512];
        try {
            in.read(byteIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msgReceived = new String(byteIn, UTF_8);
    }
}
