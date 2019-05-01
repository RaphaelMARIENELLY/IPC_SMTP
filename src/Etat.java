public abstract class Etat {
    //FIELDS
    Communication2 server;
    private String command;
    String clientDomain;

    //CONSTRUCTEUR
    Etat(Communication2 server, String command) {
        this.server = server;
        this.command = command;
    }

    //GETTER SETTER
    public String getCommand() {
        return command;
    }

    //COMMANDES
    public void answerCommand(String content)
    {
        server.sendResponse(makeAnswer(content));
    }

    //ABSTRACT
    abstract String makeAnswer(String content);
    abstract String[] extractContent(String content);


}
