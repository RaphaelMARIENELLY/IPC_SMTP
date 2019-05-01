import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.copyOf;

public class Client2 implements Runnable{

    private static final String CLIENT_DNS = "alpha.edu";
    private SSLSocket connexion;
    private int port;
    private OutputStream out;
    private String msgToSend;
    private InputStream in;
    private String msgReceived;
    private Scanner sc;
    private SocketFactory connexionFactory;

    public Client2(String hostServer){
        connexion=null;
        port = 1025;
        connexionFactory = SSLSocketFactory.getDefault();
        try {
            connexion = (SSLSocket) connexionFactory.createSocket(hostServer , port);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        listen();
    }

    public void run(){
        System.out.println("Bienvenue sur la messagerie!");
        msgToSend = "EHLO " + CLIENT_DNS;
        send();
        listen();
        ready();
    }

    public void ready(){
        switch (msgReceived.substring(0, 3)) {
            case "250":
                System.out.println("Veuillez choisir l'action à réaliser.");
                System.out.println("1 : Envoyer un mail");
                System.out.println("2 : Quitter");
                String choix = sc.nextLine();
                if (choix.equals("1")) {
                    System.out.println("Veuillez saisir l'expéditeur");
                    String expediteur = sc.nextLine();
                    msgToSend = "MAIL FROM <" + expediteur + ">";
                    send();
                    listen();
                    startMailTransaction();
                } else {
                    quit();
                }
                break;

            default:
                System.out.println("ERREUR EHLO :" + msgReceived);
                break;
        }
    }

    private void startMailTransaction() {
        switch (msgReceived.substring(0, 3)) {
            case "250":
                System.out.println("Veuillez choisir l'action à réaliser.");
                System.out.println("1 : Ajouter un destinataire");
                System.out.println("2 : Reset");
                System.out.println("3 : Quitter");
                String choix = sc.nextLine();
                if (choix.equals("1")) {
                    addRecipient();
                } else if (choix.equals("2")) {
                    reset();
                }
                else{
                    quit();
                }
                break;
            case "550":
                msgReceived = "250";
                ready();
                break;
        }
    }

    private void quit() {
        msgToSend = "QUIT";
        send();
        listen();
        if (msgReceived.substring(0,3).equals("221")){
            try {
                connexion.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Erreur réponse QUIT");
        }
    }

    private void addRecipient() {
        System.out.println("Entrez le destinataire");
        String destinataire = sc.nextLine();
        msgToSend = "RCPT TO <" + destinataire + ">";
        send();
        listen();
        switch (msgReceived.substring(0, 3)){
            case "250":
                System.out.println("Veuillez choisir l'action à réaliser.");
                System.out.println("1 : Ajouter un destinataire");
                System.out.println("2 : Envoyer le message");
                System.out.println("3 : Reset");
                System.out.println("4 : Quitter");
                String choix = sc.nextLine();
                if(choix.equals("1")){
                    addRecipient();
                }
                else if (choix.equals("2")){
                    msgToSend = "DATA ";
                    send();
                    listen();
                    data();

                } else if (choix.equals("3")) {
                    reset();
                }
                else{
                    quit();
                }
                break;
            case "550":
                System.out.println("Adresse " + destinataire + " erronée");
                addRecipient();
                break;
        }
    }

    public void data() {
        switch (msgReceived.substring(0, 3)) {
            case "354":
                System.out.println("Ecrivez le contenu du mail");
                msgToSend = sc.nextLine() + "\r\n.\r\n";
                send();
                listen();
                run();
                break;
        }
    }

    public static void main(String[] args) {
        String hostserveur =  "localhost"; //"192.168.43.66";
        Client2 client = new Client2(hostserveur) ;
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
        int sizeIn = 0;
        while (sizeIn < 512 && byteIn[sizeIn] != 0) {
            sizeIn++;
        }
        byteIn = copyOf(byteIn, sizeIn);
        msgReceived = new String(byteIn, UTF_8);
        System.out.println("/* Message reçu : \n" + msgReceived + " */");
    }

    public void reset() {
        msgToSend = "RSET";
        send();
        listen();
        ready();
    }
}


