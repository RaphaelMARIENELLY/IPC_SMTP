public class EtatMAILFROM extends Etat {
    public EtatMAILFROM(Communication2 server, String command) {
        super(server, command);
    }

    @Override
    String makeAnswer(String content) {
        if(server.getStateNum().equals(3)){
            server.setStateNum(4);

            return "250 OK";
        }

        return "CODE ERREUR - Bad request";
    }

    @Override
    String[] extractContent(String content) {
        return content.split(" ");
    }
}
