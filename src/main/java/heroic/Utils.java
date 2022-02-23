package heroic;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String convertMsToHour(long dateTimeMs) {
        return new SimpleDateFormat("HH:mm").format(new Date(dateTimeMs));
    }

    public static String convertMsToDate(long dateTimeMs) {
        return new SimpleDateFormat("dd_MM_yyyy").format(new Date(dateTimeMs));
    }

}
