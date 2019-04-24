import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.copyOf;

public class Communication implements Runnable {

    private static final String SERVER_DNS = "beta.gov";
    private String sender;
    private Socket clientSocket;
    private Timer time;
    private InputStream in;
    private OutputStream out;
    private final String MAILBOX_PATH = "mailbox/";
    private boolean welcomed;
    private String clientDNS;
    private Set<String> mailRecipients;
    private Set<String> mails;
    private boolean receivingData;

    public Communication(Socket pSock) {
        clientSocket = pSock;
        time = new Timer();
        this.welcomed = false;
        this.sender = null;
        this.mailRecipients = new HashSet<>();
        this.mails = new HashSet<>();
        this.receivingData = false;
        resetTimer();
    }

    public void resetTimer() {
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                System.err.println("PAS D'INTERACTION, FERMETURE DE LA CONNEXION");
                time.cancel(); //Terminate the thread
                try {
                    clientSocket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

            }
        }, 36000000);
    }

    @Override
    public void run() {
        try {
            in = clientSocket.getInputStream();
            if (clientSocket.isConnected()) {
                try {
                    out = clientSocket.getOutputStream();
                    String welcome = "+OK SMTP Server ready";
                    out.write(welcome.getBytes("UTF-8"));
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!clientSocket.isClosed()) {
            System.out.println("Reading InputStream.");
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
            String clientInput = new String(byteIn, UTF_8);
            System.out.println("Server received " + clientInput);
            if (clientInput.length() > 0) {
                String commandName = clientInput.substring(0, 4);
                try {
                    out = clientSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String answer;
                switch (commandName) {
                    case "EHLO":
                        updateClientDNS(clientInput);
                        answer = "250 " + SERVER_DNS;
                        welcomed = true;
                        break;
                    case "MAIL":
                        if (welcomed && this.sender == null && clientInput.contains("@")) {
                            updateSender(clientInput);
                            answer = "250 OK";
                        } else {
                            answer = "550";
                        }
                        break;
                    case "RCPT":
                        if (welcomed && this.sender != null && clientInput.contains("@")) {
                            addRecipient(clientInput);
                            answer = "250 OK";
                        } else {
                            answer = "550";
                        }
                        break;
                    case "DATA":
                        if (welcomed && this.sender != null && !this.mailRecipients.isEmpty()) {
                            answer = "354";
                            receivingData = true;
                            for (String mailRecipient : this.mailRecipients) {
                                File recipientDirectory = new File(MAILBOX_PATH + mailRecipient);
                                recipientDirectory.mkdir();
                                int mailNumber = recipientDirectory.list().length;
                                File mail = new File (MAILBOX_PATH + mailRecipient + "/mail" + mailNumber);
                                mails.add(mail.toString());
                            }
                        } else {
                            answer = "550";
                        }
                        break;
                    case "RSET":
                        answer = "250 OK";
                        this.sender = null;
                        this.mailRecipients.clear();
                        this.mails.clear();
                        this.receivingData = false;
                        break;
                    case "QUIT":
                        answer = "221 " + SERVER_DNS + " Service closing";
                        this.sender = null;
                        this.mailRecipients.clear();
                        this.mails.clear();
                        this.receivingData = false;
                        this.welcomed = false;
                        break;
                    default:
                        if (receivingData) {
                            for (String mailPath : mails) {
                                appendMailBody(clientInput, mailPath, byteIn);
                            }
                            answer = "250 OK";
                            this.sender = null;
                            this.mailRecipients.clear();
                            this.mails.clear();
                        } else {
                            answer = "550";
                        }
                        break;
                }
                System.out.println("\nSending :  \n" + answer);
                try {
                    out.write(answer.getBytes("UTF-8"));
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (commandName.equals("QUIT")) {
                    try {
                        in.close();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void appendMailBody(String clientInput, String mailPath, byte[] byteIn) {
        String textToAppend = clientInput;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(mailPath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(textToAppend);
        printWriter.close();
        int inputSize = byteIn.length;
        if (byteIn[inputSize-5] == 13 && byteIn[inputSize-4] == 10 && byteIn[inputSize-3] == 46
                && byteIn[inputSize-2] == 13 && byteIn[inputSize-1] == 10) {
            this.receivingData = false;
        }
    }

    private boolean addRecipient(String clientInput) {
        int separator1 = clientInput.indexOf(" ");
        int separator2 = clientInput.indexOf(" ", separator1 + 1);
        String recipient = clientInput.substring(separator2);
        recipient = recipient.replace("<", "");
        recipient = recipient.replace(">", "");
        return this.mailRecipients.add(recipient);
    }

    private void updateSender(String clientInput) {
        int separator1 = clientInput.indexOf(" ");
        int separator2 = clientInput.indexOf(" ", separator1 + 1);
        String sender = clientInput.substring(separator2);
        sender = sender.replace("<", "");
        sender = sender.replace(">", "");
        this.sender = sender;
    }

    private void updateClientDNS(String clientInput) {
        int separator1 = clientInput.indexOf(" ");
        int separator2 = clientInput.indexOf(" ", separator1 + 1);
        String clientDNS = clientInput.substring(separator1 + 1, separator2);
        this.clientDNS = clientDNS;
    }

}
