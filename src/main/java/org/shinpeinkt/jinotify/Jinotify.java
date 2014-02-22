package org.shinpeinkt.jinotify;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

public class Jinotify {
    private int watchingFileDescriptor;
    private int inotifyDescriptor;

    public interface Libc extends Library {

        public static class INotifyEvent extends Structure {
            int watchFileDescriptor;
            int mask;
            int cookie;
            int length;
            char name;
        }

        Libc INSTANCE = (Libc) Native.loadLibrary("libc.so.6", Libc.class);
        int inotify_init();
        int inotify_add_watch(int fd, String path, int mask);
        int inotify_rm_watch(int fd, int wd);

        final int IN_ACCESS = 0x1;
        final int IN_MODIFY = 0x2;
        final int IN_ATTRIB = 0x4;
        final int IN_CLOSE_WRITE = 0x8;
        final int IN_CLOSE_NOWRITE = 0x10;
        final int IN_OPEN = 0x20;
        final int IN_MOVED_FROM = 0x40;
        final int IN_MOVED_TO = 0x80;
        final int IN_CREATE = 0x100;
        final int IN_DELETE = 0x200;
        final int IN_DELETE_SELF = 0x400;
        final int IN_MOVE_SELF = 0x800;

    }

    public void addWatch(String absolutePath, int mask,  JinotifyListener listener) throws JinotifyException {
        watchingFileDescriptor = Libc.INSTANCE.inotify_init();
        if (watchingFileDescriptor < 0) {
            throw new JinotifyException("Couldn't init inotify");
        }
        inotifyDescriptor = Libc.INSTANCE.inotify_add_watch(watchingFileDescriptor, absolutePath,  mask );

    }

    public void closeNotifier () {
        Libc.INSTANCE.inotify_rm_watch(watchingFileDescriptor, inotifyDescriptor);
    }
}
