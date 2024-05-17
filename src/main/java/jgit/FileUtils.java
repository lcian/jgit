package jgit;


public class FileUtils {
    static {
        System.loadLibrary("fileutils");
    }

    public static native long getDeviceId(String file);

    public static native long getInode(String file);

    public static native int getMode(String file);

    public static native int getUid(String file);

    public static native int getGid(String file);
}
