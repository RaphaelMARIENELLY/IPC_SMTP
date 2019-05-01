import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SMTPServeur {
    private String domain;
    private Integer autoincrement;

    //CONSTANTS
    final Boolean SERVER_IS_RUNNING = true;
    final int SERVER_PORT= 1025;

    //INITIALIZE
    private SSLServerSocket server;
    private SSLSocket client;

    public SMTPServeur() {
        initServeurSSL();
    }

    public SMTPServeur(String domain){
        autoincrement = 0;
        this.domain = domain;
        initServeurSSL();
    }

    private void initServeurSSL(){
        try{
            ServerSocketFactory sslServerSocketFactory = SSLServerSocketFactory.getDefault();
            server = (SSLServerSocket) sslServerSocketFactory.createServerSocket(SERVER_PORT);
            String[] allCipherSuites = server.getSupportedCipherSuites();
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
            server.setEnabledCipherSuites(newCipherSuites);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void run() {
        try {

            while(SERVER_IS_RUNNING) {
                client = (SSLSocket) server.accept();
                autoincrement++;
                new Thread(new Communication2(client, domain,autoincrement)).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Lancement du serveur
    public static void main(String[] args) {
        SMTPServeur srv = new SMTPServeur("gmail.com");
        srv.run();
    }
}
