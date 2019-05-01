public class EtatQUIT extends Etat {

        public EtatQUIT(Communication2 server, String command) {
            super(server, command);
        }

        @Override
        String makeAnswer(String content) {
            if(server.getStateNum().equals(3) || server.getStateNum().equals(6)) {
                server.setClose(true);
                return "221 " + server.getServerDomain() + " Fermeture de la connexion";
            }

            return "CODE ERREUR - Bad Request";
        }

        @Override
        String[] extractContent(String content) {
            return new String[0];
        }

}
