package com.termux.terminal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class Logger {

    private static final String DEFAULT_LOG_TAG = "Termux";

    public static void logError(TerminalSessionClient client, String logTag, String message) {
        if (client != null)
            client.logError(getLogTag(logTag), message);
    }

    public static void logWarn(TerminalSessionClient client, String logTag, String message) {
        if (client != null)
            client.logWarn(getLogTag(logTag), message);
    }

    public static void logInfo(TerminalSessionClient client, String logTag, String message) {
        if (client != null)
            client.logInfo(getLogTag(logTag), message);
    }

    public static void logDebug(TerminalSessionClient client, String logTag, String message) {
        if (client != null)
            client.logDebug(getLogTag(logTag), message);
    }

    public static void logVerbose(TerminalSessionClient client, String logTag, String message) {
        if (client != null)
            client.logVerbose(getLogTag(logTag), message);
    }

    public static void logStackTraceWithMessage(TerminalSessionClient client, String tag, String message, Throwable e) {
        logError(client, tag, getStackTraceLogString(message, e));
    }

    public static void logStackTrace(TerminalSessionClient client, String tag, Throwable e) {
        logStackTraceWithMessage(client, tag, null, e);
    }

    public static String getMessageAndStackTraceString(String message, Throwable throwable) {
        if (message == null && throwable == null) return null;
        else if (message != null && throwable == null) return message;
        else if (message == null) return getStackTraceString(throwable);
        else return message + "\n" + getStackTraceString(throwable);
    }

    public static String getStackTraceLogString(String message, Throwable throwable) {
        return getMessageAndStackTraceString(message, throwable);
    }

    public static String getStackTraceString(Throwable throwable) {
        if (throwable == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        throwable.printStackTrace(new PrintStream(outputStream));
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private static String getLogTag(String logTag) {
        return (logTag != null && !logTag.trim().isEmpty()) ? logTag : DEFAULT_LOG_TAG;
    }

}
