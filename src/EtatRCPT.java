import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class EtatRCPT extends Etat {
    public EtatRCPT(Communication2 server, String command) {
        super(server, command);
    }

    @Override
    String makeAnswer(String content) {
        if(server.isStateAuthentified()){
            if(server.getStateNum().equals(4) || server.getStateNum().equals(5)) {
                String[] s = extractContent(content);

                String domain = s[2].split("@")[1].split("\\.")[0];
                domain = domain.toUpperCase();

                if (destInFile(s[2], domain)) {
                    server.setStateNum(5);
                    return "250 OK";
                } else {
                    return "550 Unknown User";
                }
            }

        }

        return "CODE ERREUR - ERR";
    }

    @Override
    String[] extractContent(String content) {
        return content.split(" ");
    }

    boolean destInFile(String dest, String domain) {
        File file = new File("src//Users//Users"+ domain.toUpperCase() +".txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null) {
                System.out.println(st);
                if(dest.equals(st)){
                    return true;
                }
            }
            return false;

        } catch(IOException e) {
            e.getMessage();
        }

        return false;
    }
}
