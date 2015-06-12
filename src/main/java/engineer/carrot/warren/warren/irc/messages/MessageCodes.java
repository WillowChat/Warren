package engineer.carrot.warren.warren.irc.messages;

public class MessageCodes {
    public static final String JOIN = "JOIN";
    public static final String PART = "PART";
    public static final String QUIT = "QUIT";
    public static final String PONG = "PONG";
    public static final String PING = "PING";
    public static final String PRIVMSG = "PRIVMSG";
    public static final String NOTICE = "NOTICE";
    public static final String NICK = "NICK";
    public static final String USER = "USER";
    public static final String MODE = "MODE";

    public static class RPL {
        public static final String WELCOME = "001";
        public static final String YOURHOST = "002";
        public static final String CREATED = "003";
        public static final String MYINFO = "004";
        public static final String ISUPPORT = "005"; // Non standard
        public static final String AWAY = "301";
        public static final String UNAWAY = "305";
        public static final String NOWAWAY = "306";
        public static final String NOTOPIC = "331";
        public static final String TOPIC = "332";
        public static final String TOPICWHOTIME = "333";
        public static final String NAMREPLY = "353";
        public static final String INFO = "371";
        public static final String MOTD = "372";
        public static final String MOTDSTART = "375";
        public static final String ENDOFMOTD = "376";
    }
}
