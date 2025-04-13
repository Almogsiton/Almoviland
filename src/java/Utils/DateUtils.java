package Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    public static String getMinReleaseDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -100);
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    public static String getMaxReleaseDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 10);
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }
}
