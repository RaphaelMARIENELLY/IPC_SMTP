import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import lib.JSONArray;
import lib.JSONObject;

public class SMTPClient {

    private DataOutputStream out;
    private BufferedReader in;
    private SSLSocket clientSocket;
    private Boolean connected = false;
    private String userMail;
    private final String USERS_FILE = "src/ServerINF/InfoServ.json";

    public SMTPClient(String userMail){
        this.userMail = userMail;
        connected = true;
        initClientSSL();
        System.out.println("Bonjour, tapez 1 pour écrire un nouveau message");
        start();
    }


    private void initClientSSL() {
        try{
            SocketFactory sslSocketFactory = SSLSocketFactory.getDefault();
            clientSocket = (SSLSocket) sslSocketFactory.createSocket();
            String[] allCipherSuites = clientSocket.getSupportedCipherSuites();
            List<String> listAnonCipherSuites = new ArrayList<>();
            for(String cipherSuites : allCipherSuites){
                if(cipherSuites.contains("anon")){
                    listAnonCipherSuites.add(cipherSuites);
                }
            }
            String[] newCipherSuites = new String[listAnonCipherSuites.size()];
            for(int i=0;i<newCipherSuites.length;i++){
                newCipherSuites[i] = listAnonCipherSuites.get(i);
            }
            clientSocket.setEnabledCipherSuites(newCipherSuites);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void start() {
        Scanner scan = new Scanner(System.in);
        String commande;
        while(connected){
            commande = scan.nextLine();
            if(commande.equals("1")){
                composeMessage();
                System.out.println("Commande reçue, ecrivez 'Nouveau message' pour ecrire un nouveau message.");
            }
            else if(commande.equalsIgnoreCase("quit")){
                System.out.println("A bientot");
                connected = false;
            }
            else{
                System.out.println("La commande n'est pas connue, ecrivez 'Nouveau message' pour ecrire un nouveau message.");
            }
        }

    }

    public void composeMessage(){
        //Prendre les mails des recepteurs
        List<List<String>> listMails = regroupMails(getRCPT());

        //Prendre le message
        StringBuilder data = new StringBuilder();

        //From
        data.append("From: <"); data.append(userMail); data.append(">\n");

        //To
        data.append("To:");
        for(List<String> mails : listMails){
            for(String mail : mails){
                data.append(" <");
                data.append(mail);
                data.append(">,");
            }
        }
        data.deleteCharAt(data.length()-1);
        data.append("\n");

        //Subject
        data.append("Subject: ");
        Scanner scan = new Scanner(System.in);
        System.out.println("Objet du mail ?");
        String objetMail = scan.nextLine();
        data.append(objetMail); data.append("\n");

        //Date
        data.append("Date: "); data.append(new Date()); data.append("\n");

        //message ID
        Random rand = new Random();
        data.append("Message-ID: <");data.append(rand.nextInt());data.append(userMail);data.append(">\n");

        //Corps
        data.append("\n");
        System.out.println("Tapez le contenu du message en terminant par point + entrée à la fin");
        String corpsMail = scan.nextLine();
        while(!corpsMail.equalsIgnoreCase(".")){
            data.append(corpsMail);
            data.append("\n");
            corpsMail = scan.nextLine();
        }
        data.append(".");

        //Valider l'envoie
        System.out.println("Si votre saisie est terminée, tapez 1 pour envoyez ou 2 pour annuler");
        String commande = scan.nextLine();
        boolean commandCorrect = false;
        while(!commandCorrect){
            if(commande.equals("1")){
                commandCorrect = true;
                sendData(listMails, data.toString());
            }
            else if(commande.equals("2")){
                System.out.println("Vous avez annulé, tapez 1 pour ecrire un nouveau message.");
                commandCorrect = true;
                start();
            }
            else{
                System.out.println("Commande incorrect, tapez 1 pour confirmer cette action ou 2 pour annuler");
                commande = scan.nextLine();
            }
        }
    }


    /**
     * Regrouper les mails par nom de domaines
     * @param listMails tous les mails
     * @return les mails tries
     */
    public List<List<String>> regroupMails(List<String> listMails){
        List<List<String>> regroupedMails = new ArrayList<>();
        boolean added;
        String domainMail, domainAdded;
        for(String mail : listMails){
            domainMail =  mail.split("@")[1];
            added = false;
            for(int i=0;!added && i<regroupedMails.size();i++){
                List<String> group = regroupedMails.get(i);
                domainAdded = group.get(0).split("@")[1];
                if(domainAdded.equalsIgnoreCase(domainMail)){
                    group.add(mail);
                    added = true;
                }
            }
            if(!added){
                List<String> newGroup = new ArrayList<>();
                newGroup.add(mail);
                regroupedMails.add(newGroup);
            }
        }
        return regroupedMails;
    }


    public void sendData(List<List<String>> listMails, String data){
        try{
            String domain;
            InetAddress ip = null;
            Integer port = null;
            JSONArray users = null;
            try {
                users = new JSONObject(readFile(USERS_FILE, StandardCharsets.UTF_8)).getJSONArray("users");
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(List<String> mails : listMails){
                domain = mails.get(0).split("@")[1];
                /*File file = new File("src/ServerINF/InfoServ.txt");
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String serverName = reader.readLine();
                boolean found = false;
                while(!found && serverName != null){
                    String[] info = serverName.split(" ");
                    if(info[0].equalsIgnoreCase(domain)){
                        ip = InetAddress.getByName(info[1]);
                        port = Integer.parseInt(info[2]);
                        found = true;
                    }else{
                        serverName = reader.readLine();
                    }
                    */

                }
                if(!found){
                    System.out.println("le domaine "+domain+" n'existe pas.");
                }
                else{
                    if(this.connecte(ip,port,domain)){
                        try{//Pas de gestion d'erreur

                            //EHLO
                            String commande = "EHLO "+domain+"\r";
                            out.writeBytes(commande);
                            out.flush();
                            in.readLine();
                            //System.out.println(in.readLine());

                            //MAIL FROM
                            commande = "MAIL FROM "+userMail+"\r";
                            out.writeBytes(commande);
                            out.flush();
                            in.readLine();
                            //System.out.println(in.readLine());

                            //RCPT TO
                            String response;
                            for(String mail : mails){
                                commande = "RCPT TO "+mail+"\r";
                                out.writeBytes(commande);
                                out.flush();
                                response = in.readLine();
                                if(response.startsWith("550")){
                                    System.out.println(mail+" est inconnu. Le mail ne lui sera donc pas envoyé");
                                }
                            }

                            //DATA
                            commande = "DATA\r";
                            out.writeBytes(commande);
                            out.flush();
                            in.readLine();
                            //System.out.println(in.readLine());
                            out.writeBytes(data+"\r");
                            out.flush();
                            in.readLine();
                            //System.out.println(in.readLine());

                            //QUIT
                            commande = "QUIT\r";
                            out.writeBytes(commande);
                            out.flush();
                            in.readLine();
                            //System.out.println(in.readLine());
                            //String response = in.readLine();
                            //System.out.println(response);
                            //System.out.println(in.readLine());//seulement pour voir qu'on recoit bien la reponse
                        }
                        catch (IOException e){
                            System.out.println(e.getLocalizedMessage());
                            System.out.println(e.getMessage());
                        }
                    }
                    clientSocket.close();
                    initClientSSL();
                }
                reader.close();
            }
        }
        catch(IOException e){
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
        }
    }

    public List<String> getRCPT(){
        //Pour verifier la validite du mail
        Pattern pattern = Pattern.compile("^(.+)@(.+)\\.(.+)$");

        System.out.println("A qui voulez vous envoyer votre mail?");
        Scanner scan = new Scanner(System.in);
        String mail = scan.nextLine();
        List<String> listMails = new ArrayList<>();
        while(!mail.equalsIgnoreCase("Fin")){
            if(pattern.matcher(mail).matches()){
                listMails.add(mail.toLowerCase());
                System.out.println("Ecrivez le prochain recepteur ou simplement 'Fin' pour terminer");
            }
            else{
                System.out.println("Mail invalide, reessayez");
            }
            mail = scan.nextLine();
        }
        if(!listMails.isEmpty()){
            return listMails;
        }
        else{
            System.out.println("Aucun mail n'a ete enregistre, veuillez indiquer au moins un mail");
            return getRCPT();
        }
    }


    private boolean connecte(InetAddress ip, int port, String domain){
        try{
            clientSocket.connect(new InetSocketAddress(ip, port));
            try{
                out = new DataOutputStream(clientSocket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            }
            catch(IOException e){
                System.out.println(e.getLocalizedMessage());
                System.out.println(e.getMessage());
            }
            String[] response = in.readLine().split(" ");

            return true;
        }
        catch (IOException e){
            System.out.println("Impossible de trouver le serveur correspondant à "+domain);
            return false;
        }
    }

    public static void main(String[] args) {
        SMTPClient client = new SMTPClient("hu@gmail.com");

    }

}
