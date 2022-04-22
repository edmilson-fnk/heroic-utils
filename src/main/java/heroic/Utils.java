package heroic;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static final long DIFF = 0;
//    public static final long DIFF = 1000 * 60 * 60 * 3;

    public static String convertMsToHour(long dateTimeMs) {
        return new SimpleDateFormat("HH:mm").format(new Date(dateTimeMs - DIFF));
    }

    public static String convertMsToHourName(long dateTimeMs) {
        return new SimpleDateFormat("HH-mm").format(new Date(dateTimeMs - DIFF));
    }

    public static String convertMsToDate(long dateTimeMs) {
        return new SimpleDateFormat("dd_MM_yyyy").format(new Date(dateTimeMs - DIFF));
    }

}
