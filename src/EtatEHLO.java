public class EtatEHLO extends Etat {
    public EtatEHLO(Communication2 server, String command) {
        super(server, command);
    }

    @Override
    String makeAnswer(String content) {
        String[] s = extractContent(content);

        server.setStateNum(3);

        return "250 OK " + server.getServerDomain() + " greets "+s[1];
    }

    @Override
    String[] extractContent(String content) {
        return content.split(" ");
    }
}
