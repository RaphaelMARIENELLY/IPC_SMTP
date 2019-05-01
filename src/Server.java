import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private SSLServerSocket serverSocket;
    private Socket clientSocket;

    private boolean isRunning;

    public Server() {
        try {
            ServerSocketFactory sslServerSocketFactory = SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(1025);
            String[] supportedCipherSuites = serverSocket.getSupportedCipherSuites();
            serverSocket.setEnabledCipherSuites(supportedCipherSuites);
            this.isRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void open(){
        Thread t = new Thread(new Runnable(){
            public void run(){
                while(isRunning){

                    try {
                        System.out.println("Accepting new connections.");
                        clientSocket = serverSocket.accept();
                        Thread t = new Thread(new Communication(clientSocket));
                        t.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    serverSocket = null;
                }
            }
        });

        t.start();
    }
    public void close(){
        isRunning = false;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.open();
    }
}
