package com.longseong.logcenter;

import static com.longseong.logcenter.util.Utils.Now;
import static com.longseong.logcenter.util.Utils.getLogFolder;

import android.content.Context;

import com.longseong.logcenter.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class LogCenter {

    private static final String TAG_TIME = "caused time: ";

    private static final int MAX_LOG_FILES = 100;

    private static boolean LOG_FOLDER_EXIST;

    private static LogPostedListener mLogPostedListener;

    private static String stackTraceToString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private static void maintenanceLogFiles(Context context) {
        if (!LOG_FOLDER_EXIST) {
            Utils.initLogDirectory(context);
            LOG_FOLDER_EXIST = true;
        }
        File rootFile = getLogFolder(context);

        File[] logList = rootFile.listFiles();

        if (logList == null) {
            return;
        }
        Arrays.sort(logList);
        for (int i = 0; logList.length - i >= MAX_LOG_FILES; i++) {
            logList[i].delete();
        }
    }

    public static void postLog(Context context, String log) {
        maintenanceLogFiles(context);

        long time = Now().getTimeInMillis();
        File rootFile = getLogFolder(context);
        File logFile;
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        try {
            int i = 0;
            while (true) {
                if (i > 0) {
                    logFile = new File(rootFile, time + "_" + i + ".log");
                } else {
                    logFile = new File(rootFile, time + ".log");
                }
                if (logFile.createNewFile()) {
                    fileWriter = new FileWriter(logFile);
                    bufferedWriter = new BufferedWriter(fileWriter);

                    bufferedWriter.write(TAG_TIME + Now().getTimeInMillis() + "\n");
                    StringTokenizer tokenizer = new StringTokenizer(log, "\n");
                    while (tokenizer.hasMoreTokens()) {
                        bufferedWriter.write(tokenizer.nextToken() + "\n");
                    }
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    fileWriter.close();
                    break;
                } else {
                    i++;
                }
            }

            if (mLogPostedListener != null) {
                mLogPostedListener.onLogPosted(rootFile.list().length - 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void postLog(Context context, Exception exception) {
        postLog(context, stackTraceToString(exception));
    }

    public static LinkedList<Log> getLogSet(Context context) {
        LinkedList<Log> logSet = new LinkedList<>();

        File rootFile = getLogFolder(context);
        FileReader fileReader;
        BufferedReader bufferedReader;

        try {
            if (rootFile.exists() && rootFile.isDirectory()) {
                File[] logList = rootFile.listFiles();
                Arrays.sort(logList);
                for (File logFile : logList) {
                    fileReader = new FileReader(logFile);
                    bufferedReader = new BufferedReader(fileReader);

                    long time = Long.parseLong(bufferedReader.readLine().replace(TAG_TIME, ""));
                    StringBuilder logString = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        logString.append(line).append("\n");
                    }

                    logSet.add(new Log(time, logString.toString().trim()));

                    bufferedReader.close();
                }
            }
        } catch (IOException | NullPointerException e) {

        }


        return logSet;
    }

    /*package-private*/
    static void clearLogcat(Context context) {
        File rootFile = getLogFolder(context);

        File[] logList = rootFile.listFiles();
        if (logList == null) {
            return;
        }
        for (File file : logList) {
            file.delete();
        }
    }

    public static void registerLogAddedListener(LogPostedListener logPostedListener) {
        mLogPostedListener = logPostedListener;
    }

    private LogCenter() {

    }

    interface LogPostedListener {
        void onLogPosted(int postedIndex);
    }

    public static class Log {
        private final long milliSecond;
        private final String logString;

        private Log(long milliSecond, String stackTracedString) {
            this.milliSecond = milliSecond;
            this.logString = stackTracedString;
        }

        public long getMilliSecond() {
            return milliSecond;
        }

        public String getLogString() {
            return logString;
        }
    }

}
