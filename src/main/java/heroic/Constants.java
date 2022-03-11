package heroic;

public class Constants {

    public final static String COUNT_COMMAND = "2contar";
    public final static String STOP_COMMAND = "2parar";
    public final static String EMAIL_USER = System.getenv("EMAIL_USER"); // user to send emails
    public final static String EMAIL_PASS = System.getenv("EMAIL_PASS"); // password to send emails
    public final static String HEROIC_EMAIL = System.getenv("HEROIC_EMAIL"); // "email1;email2;email3"
    public final static int MINUTES_TO_WATCH = 100; // minutes to watch the channel for
    public final static int DELAY = 10; // minutes to wait before recounting

}
