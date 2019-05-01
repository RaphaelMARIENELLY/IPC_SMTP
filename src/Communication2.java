
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Objects;

public class Communication2 implements Runnable {

    private String serverDomain;
    private Integer stateNum = 1;
    private Integer autoincrement;

    //INITIALIZE FOR CLIENT
    public BufferedReader inputdata;
    public DataOutputStream outputdata;
    private SSLSocket client;

    //FOR OTHER SERVERS
    private boolean close;
    private ArrayList<Etat> CommandesList = new ArrayList<>();

    //CONSTANTS
    int NUMBER_OF_CHANCES = 3;
    final String STATE_AUTHORIZATION = "authorization";

    private String state = STATE_AUTHORIZATION;

    //CONSTRUCTOR
    private void setCommandesList(){
        CommandesList.add(new EtatEHLO(this,"EHLO"));
        CommandesList.add(new EtatMAILFROM(this,"MAIL"));
        CommandesList.add(new EtatRCPT(this,"RCPT"));
        CommandesList.add(new EtatDATA(this,"DATA"));
        CommandesList.add(new EtatQUIT(this,"QUIT"));
    }

    private void initConstructor(SSLSocket aClientSocket){
        setCommandesList();
        try {
            client = aClientSocket;
            inputdata = new BufferedReader( new InputStreamReader(client.getInputStream()));
            outputdata =new DataOutputStream( client.getOutputStream());
        }
        catch(IOException e) {
            System.out.println("Connection: "+e.getMessage());
        }
    }

    public Communication2(SSLSocket aClientSocket, String serverDomain, Integer autoincrement){
        this.autoincrement = autoincrement;
        this.serverDomain = serverDomain;
        initConstructor(aClientSocket);
    }

    public Communication2(SSLSocket aClientSocket){
        initConstructor(aClientSocket);

    }

    public void run(){/////////// A CHANGER EVIDEMENT
        try {
            // an echo server
            String data = "220 "+getServerDomain()+" Simple Mail Transfer Service Ready"+"\r";

            System.out.println ("New connection: " + client.getPort() + ", " + client.getInetAddress());
            outputdata.writeBytes(data); // UTF is a string encoding
            outputdata.flush();
            System.out.println ("send: " + data);

            setStateNum(2);

            if(client.isConnected())
                readCommand();
        }
        catch(EOFException e) {
            System.out.println("EOF: "+e.getMessage()); }
        catch(IOException e) {
            System.out.println("IO: "+e.getMessage());}
    }

    private void readCommand(){
        System.out.println("Reading from stream:");
        try {
            String command;
            try {
                while ((command = inputdata.readLine()) != null && !close) {
                    System.out.println("receive from : " + client.getInetAddress() + " : " + client.getPort() + ", command : " + command);
                    answerCommand(command);
                    if (close)
                        break;
                }
            }catch (Exception e){
                System.out.println("\n Connexion avec le client :" + client.getInetAddress() + " : " + client.getPort() + " interrompu !");
            }
            if(close)
            {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void answerCommand(String data){
        String command = data.split("\\s+")[0];
        command = command.toUpperCase();

        String content = "";
        for (String s : data.split("\\s+"))
            content += s + " ";

        for (Etat commande : CommandesList)
        {
            if(Objects.equals(commande.getCommand(), command))
            {
                commande.answerCommand(content);
                return;
            }
        }
        sendResponse("-ERR unknown command");
    }

    public void sendResponse(String data){
        data += "\r";
        try {
            outputdata.writeBytes(data);
            outputdata.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isStateAuthentified(){
        if(Objects.equals(state, STATE_AUTHORIZATION))
        {
            if (NUMBER_OF_CHANCES == 0)
            {
                close = true;
            }
            NUMBER_OF_CHANCES --;
            return true;
        }
        else
            return false;
    }

    public void accessNewServer(){

    }

    //GETTER & SETTER
    public Integer getStateNum() { return this.stateNum; }

    public void setStateNum(Integer stateNum) { this.stateNum = stateNum; }

    public void setClose(boolean close) {
        this.close = close;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }

    public Integer getAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(Integer autoincrement) {
        this.autoincrement = autoincrement;
    }
}
