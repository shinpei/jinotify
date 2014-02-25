package com.github.shinpei.jinotify;

import com.sun.jna.*;

import java.util.Arrays;
import java.util.List;

public class Clib {
    static
    {
        try {
            Native.register("/lib64/libc.so.6");
        }
        catch (NoClassDefFoundError e) {

        }
        catch (UnsatisfiedLinkError e) {

        }
        catch (NoSuchMethodError e) {

        }
    }

    // from sys/inotify.h
    final static int PATH_MAX = 16;

    public static class InotifyEvent extends Structure {
        public int wd;
        public int mask;
        public int cookie;
        public int len;
        public byte[] name = new byte[PATH_MAX]; // __flexarr, but we limit it to 16

        protected List getFieldOrder () {
            return Arrays.asList("wd", "mask", "cookie", "len", "name");
        }

        public static class ByReference extends InotifyEvent implements Structure.ByReference {}
        public static class ByValue extends InotifyEvent implements Structure.ByValue {}
    }

    public static native int inotify_init();
    public static native int inotify_add_watch(int fd, String path, int mask);
    public static native int inotify_rm_watch(int fd, int wd);


    public static final int IN_ACCESS = 0x1;
    public static final int IN_MODIFY = 0x2;
    public static final int IN_ATTRIB = 0x4;
    public static final int IN_CLOSE_WRITE = 0x8;
    public static final int IN_CLOSE_NOWRITE = 0x10;
    public static final int IN_OPEN = 0x20;
    public static final int IN_CLOSE = (IN_CLOSE_WRITE | IN_CLOSE_NOWRITE);
    public static final int IN_MOVED_FROM = 0x40;
    public static final int IN_MOVED_TO = 0x80;
    public static final int IN_MOVE = (IN_MOVED_FROM | IN_MOVED_TO);
    public static final int IN_CREATE = 0x100;
    public static final int IN_DELETE = 0x200;
    public static final int IN_DELETE_SELF = 0x400;
    public static final int IN_MOVE_SELF = 0x800;

    // from sys/epoll.h
    public static class EpollData extends Union {
        public int fd;
        public long u64; // we don't need, but make size 8.
        public EpollData() {
            super();
        }
        public EpollData(int fd_or_u32) {
            super();
            this.fd = fd_or_u32;
            setType(Integer.TYPE);
        }
        public EpollData(long u64) {
            super();
            this.u64 = u64;
            setType(Long.TYPE);
        }
        public static class ByReference extends EpollData implements Structure.ByReference {}
        public static class ByValue extends EpollData implements Structure.ByValue {}
    }

    public static class EpollEvent extends Structure {
        public int events;
        public EpollData data;

        public EpollEvent() {
            super(Structure.ALIGN_NONE);
            //setAlignType(Structure.ALIGN_NONE);
        }

        protected List getFieldOrder() {
            return Arrays.asList("events", "data");
        }

        public static class ByReference extends EpollEvent implements  Structure.ByReference { }
        public static class ByValue extends EpollEvent implements Structure.ByValue { }
    }

    public static native int epoll_create(int size);
    public static native int epoll_create1(int flags);
    public static native int epoll_ctl(int epfd, int op, int fd, EpollEvent.ByReference ev);
    public static native int epoll_wait (int epfd, Pointer /*EpollEvent[] */ ev, int maxEvents, int timeout);

    public static final int EPOLLIN = 0x1;
    public static final int EPOLLPRI = 0x2;
    public static final int EPOLLOUT = 0x4;
    public static final int EPOLLRDNORM = 0x40;
    public static final int EPOLLRDBAND = 0x80;
    public static final int EPOLLWRNORM = 0x100;
    public static final int EPOLLWRBAND = 0x200;
    public static final int EPOLLMSG = 0x400;
    public static final int EPOLLERR = 0x8;
    public static final int EPOLLHUP = 0x10;
    public static final int EPOLLRDHUP = 0x2000;
    public static final int EPOLLONESHOT = (1 << 30);
    public static final int EPOLLLET = (1 << 31);

    public static final int  EPOLL_CTL_ADD = 1;
    public static final int EPOLL_CTL_DEL = 2;
    public static final int EPOLL_CTL_MOD = 3;

    public static native int close(int fd);
    public static native int read (int fd, Pointer buf, int size);
    public static native void perror(String msg);

    public static native Pointer malloc (int size);
    public static native void free (Pointer ptr);

}

