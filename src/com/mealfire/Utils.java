package com.mealfire;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

public class Utils {
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
	
	// Future dates not supported.
	public static String prettyDate(DateTime date) {
		Interval interval = new Interval(date, new DateTime());
		Duration duration = interval.toDuration();
		
		if (duration.isShorterThan(Duration.standardSeconds(10))) {
			return "Just now";
		} else if (duration.isShorterThan(Duration.standardMinutes(1))) {
			int seconds = Seconds.secondsIn(interval).getSeconds();
			return String.format("%d seconds ago", seconds);
		} else if (duration.isShorterThan(Duration.standardHours(1))) {
			int minutes = Minutes.minutesIn(interval).getMinutes();
			
			if (minutes == 1) {
				return "1 minute ago";
			} else {
				return String.format("%d minutes ago", minutes);
			}
		} else {
			int hours = Hours.hoursIn(interval).getHours();
			DateTime today = new DateTime().withHourOfDay(1);
			DateTime yesterday = new DateTime().minusDays(1).withHourOfDay(1);
			DateTime twoDaysAgo = new DateTime().minusDays(2).withHourOfDay(1);
			
			if (hours > 6 && date.isAfter(yesterday) && date.isBefore(today)) {
				return "Yesterday";
			} else if (date.isAfter(twoDaysAgo) && date.isBefore(yesterday)) {
				return "2 days ago";
			} else if (hours < 24) {
				if (hours == 1) {
					return "1 hour ago";
				} else {
					return String.format("%d hours ago", hours);
				}
			} else if (date.isAfter(new DateTime().withDayOfYear(1))) {
				return date.toString("MMMM d");
			} else {
				return date.toString("MMMM d, YYYY");
			}
		}
	}
}
