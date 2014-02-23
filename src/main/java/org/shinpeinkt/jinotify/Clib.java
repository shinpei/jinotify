package org.shinpeinkt.jinotify;

import com.sun.jna.*;

import java.util.Arrays;
import java.util.List;

public interface Clib extends Library {

    Clib INSTANCE = (Clib) Native.loadLibrary("libc.so.6", Clib.class);

    // from sys/inotify.h
    final int PATH_MAX = 16;

    public static class InotifyEvent extends Structure {
        public int wd;
        public int mask;
        public int cookie;
        public int len;
        public byte[] name = new byte[PATH_MAX]; // __flexarr, but we limit it to 16

        public static class ByReference extends InotifyEvent implements Structure.ByReference {}
        public static class ByValue extends InotifyEvent implements Structure.ByValue {}
    }

    int inotify_init();
    int inotify_add_watch(int fd, String path, int mask);
    int inotify_rm_watch(int fd, int wd);

    final int IN_ACCESS = 0x1;
    final int IN_MODIFY = 0x2;
    final int IN_ATTRIB = 0x4;
    final int IN_CLOSE_WRITE = 0x8;
    final int IN_CLOSE_NOWRITE = 0x10;
    final int IN_OPEN = 0x20;
    final int IN_CLOSE = (IN_CLOSE_WRITE | IN_CLOSE_NOWRITE);
    final int IN_MOVED_FROM = 0x40;
    final int IN_MOVED_TO = 0x80;
    final int IN_MOVE = (IN_MOVED_FROM | IN_MOVED_TO);
    final int IN_CREATE = 0x100;
    final int IN_DELETE = 0x200;
    final int IN_DELETE_SELF = 0x400;
    final int IN_MOVE_SELF = 0x800;

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
            super();
            setAlignType(Structure.ALIGN_NONE);
        }

        protected List getFieldOrder() {
            return Arrays.asList("events", "data");
        }

        public static class ByReference extends EpollEvent implements  Structure.ByReference { }
        public static class ByValue extends EpollEvent implements Structure.ByValue { }
    }

    int epoll_create(int size);
    int epoll_create1(int flags);
    int epoll_ctl(int epfd, int op, int fd, EpollEvent.ByReference ev);
    int epoll_wait (int epfd, EpollEvent[] ev, int maxEvents, int timeout);

    final int EPOLLIN = 0x1;
    final int EPOLLPRI = 0x2;
    final int EPOLLOUT = 0x4;
    final int EPOLLRDNORM = 0x40;
    final int EPOLLRDBAND = 0x80;
    final int EPOLLWRNORM = 0x100;
    final int EPOLLWRBAND = 0x200;
    final int EPOLLMSG = 0x400;
    final int EPOLLERR = 0x8;
    final int EPOLLHUP = 0x10;
    final int EPOLLRDHUP = 0x2000;
    final int EPOLLONESHOT = (1 << 30);
    final int EPOLLLET = (1 << 31);

    final int  EPOLL_CTL_ADD = 1;
    final int EPOLL_CTL_DEL = 2;
    final int EPOLL_CTL_MOD = 3;

    int close(int fd);
    int read (int fd, Pointer buf, int size);
    void perror(String msg);

    Pointer malloc (int size);
    void free (Pointer ptr);
}

