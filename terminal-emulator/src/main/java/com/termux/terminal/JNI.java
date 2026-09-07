package com.termux.terminal;


/**
 * Native methods for creating and managing pseudo-terminal sessions. See
 * <p/>
 * https://man.openbsd.org/openpty.3
 */
final class JNI {

    static {
        System.loadLibrary("termux");
    }

    /**
     * Create a subprocess and connect an open pty to it.
     */
    public static native int createSubprocess(String cmd, String cwd, String[] args, String[] envVars, int[] processId, int rows, int columns, int cellWidth, int cellHeight);

    public static native void setPtyWindowSize(int fd, int rows, int cols, int cellWidth, int cellHeight);

    public static native int waitFor(int processId);

    public static native void close(int fileDescriptor);

}
