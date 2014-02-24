package com.github.shinpei.jinotify.fake;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import org.shinpeinkt.jinotify.JinotifyException;

import java.util.Arrays;
import java.util.List;

public class JinotifyTest {
    public interface FakeClib extends Library {

        FakeClib INSTANCE = (FakeClib) Native.loadLibrary("pei", FakeClib.class);

        int finotify_init();
        int finotify_add_watch(int fd, String path, int mask);
        int finotify_rm_watch(int fd, int wd);

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

        // fake epoll bindings
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

        int fepoll_create(int size);
        int fepoll_ctl(int epfd, int op, int fd, EpollEvent.ByReference ev);
        int fepoll_wait (int epfd, EpollEvent[] ev, int maxEvents, int timeout);

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
    public void use () throws JinotifyException {
        int inotifyDescriptor = FakeClib.INSTANCE.finotify_init();
        if (inotifyDescriptor < 0) {
            throw new JinotifyException("Couldn't initiate inotify");
        }

        int watchingDescriptor = FakeClib.INSTANCE.finotify_add_watch(inotifyDescriptor, "/tmp", FakeClib.IN_CREATE);
        //watchingDescriptor = Clib.INSTANCE.inotify_add_watch(inotifyDescriptor, "/tmp",  Clib.IN_CREATE );
        System.err.println("fd=" + inotifyDescriptor + ", wd=" + watchingDescriptor);
        System.err.println("INCREATE=" + FakeClib.IN_CREATE + ", EPOLLIN=" + FakeClib.EPOLLIN);
        int epollDescriptor = FakeClib.INSTANCE.fepoll_create(MAX_EVENTS);
        if (epollDescriptor < 0) {
            throw new JinotifyException("Couldn't initiate epoll");
        }
        FakeClib.EpollEvent.ByReference eevent = new FakeClib.EpollEvent.ByReference();
        eevent.events = FakeClib.EPOLLIN;
        eevent.data.writeField("fd", inotifyDescriptor);
        int ret = FakeClib.INSTANCE.fepoll_ctl(epollDescriptor, FakeClib.EPOLL_CTL_ADD, inotifyDescriptor, eevent);
        FakeClib.EpollEvent[] ret_events = (FakeClib.EpollEvent[])(new FakeClib.EpollEvent()).toArray(MAX_EVENTS);
        if (ret < 0) {
            throw new JinotifyException("epoll_ctl failed");
        }
        System.err.println("Waiting event");
        int numEvents = 0;
        try {
            while ((numEvents = FakeClib.INSTANCE.fepoll_wait(epollDescriptor, ret_events, MAX_EVENTS, -1)) > 0) {

            }
        } finally {
            //
        }
    }
}
