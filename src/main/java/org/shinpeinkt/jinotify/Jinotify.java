package org.shinpeinkt.jinotify;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Union;

import java.util.Arrays;
import java.util.List;

public class Jinotify {
    private int inotifyDescriptor;
    private int watchingDescriptor;

    public interface Libc extends Library {

        // inotify bindings
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
        final int IN_CLOSE = (IN_CLOSE_WRITE | IN_CLOSE_NOWRITE);
        final int IN_MOVED_FROM = 0x40;
        final int IN_MOVED_TO = 0x80;
        final int IN_MOVE = (IN_MOVED_FROM | IN_MOVED_TO);
        final int IN_CREATE = 0x100;
        final int IN_DELETE = 0x200;
        final int IN_DELETE_SELF = 0x400;
        final int IN_MOVE_SELF = 0x800;

        // epoll bindings
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

    }
    final int MAX_EVENTS = 1;
    public void addWatch(String absolutePath, int mask,  JinotifyListener listener) throws JinotifyException {
        inotifyDescriptor = Libc.INSTANCE.inotify_init();
        if (inotifyDescriptor < 0) {
            throw new JinotifyException("Couldn't initiate inotify");
        }

        watchingDescriptor = Libc.INSTANCE.inotify_add_watch(inotifyDescriptor, absolutePath,  mask );
        //watchingDescriptor = Libc.INSTANCE.inotify_add_watch(inotifyDescriptor, "/tmp",  Libc.IN_CREATE );
        System.err.println("fd=" + inotifyDescriptor + ", wd=" + watchingDescriptor);
        System.err.println("INCREATE=" + Libc.IN_CREATE + ", EPOLLIN=" + Libc.EPOLLIN);
        int epollDescriptor = Libc.INSTANCE.epoll_create(MAX_EVENTS);
        if (epollDescriptor < 0) {
            throw new JinotifyException("Couldn't initiate epoll");
        }
        Libc.EpollEvent.ByReference eevent = new Libc.EpollEvent.ByReference();
        eevent.events = Libc.EPOLLIN;
        eevent.data.writeField("fd", inotifyDescriptor);
        int ret = Libc.INSTANCE.epoll_ctl(epollDescriptor, Libc.EPOLL_CTL_ADD, watchingDescriptor, eevent);
        Libc.EpollEvent[] ret_events = (Libc.EpollEvent[])(new Libc.EpollEvent()).toArray(MAX_EVENTS);
        if (ret < 0) {
            throw new JinotifyException("epoll_ctl failed");
        }
        System.err.println("Waiting event");
        int numEvents = 0;
        try {
            while ((numEvents = Libc.INSTANCE.epoll_wait(epollDescriptor, ret_events, MAX_EVENTS, -1)) > 0) {
                System.err.println("got " + numEvents + " events.");
            }
        } finally {
            //
        }
    }

    public void closeNotifier () {
        Libc.INSTANCE.inotify_rm_watch(inotifyDescriptor, watchingDescriptor);
    }
}
