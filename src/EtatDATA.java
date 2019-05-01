import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EtatDATA extends Etat {
    public EtatDATA(Communication2 server, String command) {
        super(server, command);
    }

    @Override
    String makeAnswer(String content) {
        String[] s = extractContent(content);

        server.sendResponse("354 Debut des entrees du message ; fin avec <CRLF>.<CRLF>");

        String data = "";
        String mail = "";
        try{
            do {
                System.out.println(data);
                mail+=data+"\n";
            } while (!(data = server.inputdata.readLine()).equals("."));
            mail+=".";

            if(writeFile(mail)){
                server.setStateNum(6);
            }

            return "250 OK ";
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        return "-ERR";
    }

    @Override
    String[] extractContent(String content) {
        return content.split(" ");
    }

    boolean writeFile(String data) {
        try {
            File file = new File("mailbox/" + server.getServerDomain() + server.getAutoincrement() + ".txt");
            BufferedWriter outFile = new BufferedWriter(new FileWriter(file));
            outFile.write(data);
            outFile.close();
            return true;
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

}
