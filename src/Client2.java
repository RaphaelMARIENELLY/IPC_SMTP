import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.copyOf;

public class Client2 implements Runnable{

    private static final String CLIENT_DNS = "alpha.edu";
    private Socket connexion;
    private int port;
    private OutputStream out;
    private String msgToSend;
    private InputStream in;
    private String msgReceived;
    private Scanner sc;

    public Client2(String hostServer){
        connexion=null;
        port = 1041;
        connexion = new Socket();
        sc = new Scanner(System.in);

        if (!connexion.isConnected()){
            try {
                connexion.connect(new InetSocketAddress(hostServer, port));
                System.out.println("/* Connexion tcp .*/");
                out = connexion.getOutputStream();
                in = connexion.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("go listen");
        listen();
        System.out.println("end listen");
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
            case "220":
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
                    ready();
                }
                else{
                    quit();
                }
                break;
            case "550":
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
                    ready();
                }
                else{
                    quit();
                }
                break;
            case "550":
                //todo
                break;
        }
    }

    public void data() {
        switch (msgReceived.substring(0, 3)) {
            case "250":
                System.out.println("Ecrivez le contenu du mail");
                msgToSend = sc.nextLine() + ".\n";
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
            System.out.println("read");
            in.read(byteIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("wile");
        int sizeIn = 0;
        while (sizeIn < 512 && byteIn[sizeIn] != 0) {
            sizeIn++;
        }
        System.out.println("end whle");
        byteIn = copyOf(byteIn, sizeIn);
        msgReceived = new String(byteIn, UTF_8);
        System.out.println("/* Message reçu : \n" + msgReceived + " */");
    }
}


