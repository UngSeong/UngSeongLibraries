package com.ungseong.logcenter.util;

import android.content.Context;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    public static final int FLAG_MILLIS = 1;
    public static final int FLAG_SECOND = 1 << 1;
    public static final int FLAG_MINUTE = 1 << 2;
    public static final int FLAG_HOUR = 1 << 3;
    public static final int FLAG_W_DATE = 1 << 4;
    public static final int FLAG_DATE = 1 << 5;
    public static final int FLAG_MONTH = 1 << 6;
    public static final int FLAG_YEAR = 1 << 7;

    public static final String[] DATE_FORMAT_FLAG = {
            " SSS밀리초||SSS초",
            " ss초|| ss.",
            " mm분",
            " a h시|| HH시",
            " (E)",
            " d일",
            " M월",
            " YYYY년"
    };

    public static final String[][] FORMAT_DATE = {
            {"a h시 mm분", "HH시 mm분"},
            {"a h시 mm분 ss초", "HH시 mm분 ss초"},
            {"d일 a h시 mm분", "d일 HH시 mm분"},
            {"M월 d일", "M월 d일"},
            {"M월 d일 (E)", "M월 d일 (E)"},
            {"M월 d일 a h시 mm분", "M월 d일 HH시 mm분"},
            {"M월 d일 (E) a h시 mm분", "M월 d일 (E) HH시 mm분"}
    };

    public static final String[] FORMAT_NUMBER = {"###-####-####"};

    public static final int ID_FORMAT_H_M = 0;
    public static final int ID_FORMAT_H_M_S = 1;
    public static final int ID_FORMAT_D_H_M = 2;
    public static final int ID_FORMAT_M_D = 3;
    public static final int ID_FORMAT_M_D_E = 4;
    public static final int ID_FORMAT_M_D_H_N = 5;
    public static final int ID_FORMAT_M_D_E_H_N = 6;

    public static final int ID_FORMAT_PHONE_NUMBER = 0;

    public static Calendar Now() {
        return Calendar.getInstance(TimeZone.getDefault());
    }

    public static void initLogDirectory(Context context) {
        File rootFile = getLogFolder(context);
        if (!rootFile.isDirectory()) {
            rootFile.delete();
            rootFile.mkdirs();
        }
    }

    public static File getLogFolder(Context context) {
        return new File(context.getFilesDir(), "log/");
    }

    public static String formatFlags(Context context, int flags) {
        boolean is24Hour = DateFormat.is24HourFormat(context);
        boolean isDecimalSecond = (flags & 0b11) == 0b11;

        StringBuilder format = new StringBuilder();

        for (int i = 0; i < DATE_FORMAT_FLAG.length; i++) {
            if ((flags & 1 << i) > 0) {
                if (isDecimalSecond && i < 2) {
                    String singleFormat = DATE_FORMAT_FLAG[i].split("\\|\\|")[1];
                    format.insert(0, singleFormat);
                } else if (is24Hour && i == 3) {
                    String singleFormat = DATE_FORMAT_FLAG[i].split("\\|\\|")[1];
                    format.insert(0, singleFormat);
                } else {
                    format.insert(0, DATE_FORMAT_FLAG[i]);
                }
            }
        }
        return format.toString();
    }

    public static String enhancedFormatDate(@Nullable Context context, @NonNull Calendar calendar, int flag) {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat(formatFlags(context, flag).trim(), Locale.KOREA);
        return formatter.format(calendar.getTime());
    }

    public static String formatDate(@Nullable Context context, @NonNull Calendar calendar, @Nullable Locale locale, int formatId) {
        SimpleDateFormat formatter;
        if (locale != null) {
            formatter = new SimpleDateFormat(dateFormatSelector(context, formatId), locale);
        } else {
            formatter = new SimpleDateFormat(dateFormatSelector(context, formatId));
        }
        return formatter.format(calendar.getTime());
    }

    public static String dateFormatSelector(Context context, int formatId) {
        int key;
        if (context != null) {
            key = DateFormat.is24HourFormat(context) ? 1 : 0;
        } else {
            key = 0;
        }
        return FORMAT_DATE[formatId][key];
    }

    public static String formatPhoneNumber(@NonNull String phoneNumber, int formatId) {
        int length = phoneNumber.length();
        if (length <= 3 || length > 12) {
            return phoneNumber;
        } else if (length <= 7) {
            return phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3);
        } else if (length == 12) {
            return phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 7) + "-" + phoneNumber.substring(7);
        } else {
            return phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, length - 4) + "-" + phoneNumber.substring(length - 4);
        }
    }
}
