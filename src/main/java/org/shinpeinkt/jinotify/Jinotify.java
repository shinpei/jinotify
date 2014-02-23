package org.shinpeinkt.jinotify;


import com.sun.jna.*;

import java.util.Arrays;
import java.util.List;

public class Jinotify {
    private int inotifyDescriptor;
    private int watchingFileDescriptor;
    private int epollDescriptor;

    public interface Libc extends Library {

        Libc INSTANCE = (Libc) Native.loadLibrary("libc.so.6", Libc.class);

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

    final int MAX_EVENTS = 1;

    private int tryInotifyInit () throws JinotifyException {
        int fd = Libc.INSTANCE.inotify_init();
        if (fd < 0) {
            throw new JinotifyException("Couldn't initiate inotify");
        }
        return fd;
    }

    private int tryInotifyAddWatch(int fd, String path, int mask) throws JinotifyException {
        int wd = Libc.INSTANCE.inotify_add_watch(fd, path,  mask);
        if (wd < 0) {
            throw new JinotifyException("Couldn't add inotify watch");
        }
        return wd;
    }

    private int tryEpollCreate(int size) throws JinotifyException {
        int epfd = Libc.INSTANCE.epoll_create(size);
        if (epfd < 0) {
            throw new JinotifyException("epoll_create failed");
        }
        return epfd;
    }

    private int tryEpollCtl (int epfd, int flag, int fd, Libc.EpollEvent.ByReference ev) throws JinotifyException {
        int retVal = Libc.INSTANCE.epoll_ctl(epfd, flag, fd, ev);
        if (retVal < 0) {
            throw new JinotifyException("epoll_ctl failed");
        }
        return retVal; // if success, it's always 0
    }

    public void addWatch(String absolutePath, int mask,  JinotifyListener listener) throws JinotifyException {

        inotifyDescriptor = tryInotifyInit();
        watchingFileDescriptor = tryInotifyAddWatch(inotifyDescriptor, absolutePath, mask);
        epollDescriptor = tryEpollCreate(MAX_EVENTS);

        Libc.EpollEvent.ByReference eevent = new Libc.EpollEvent.ByReference();
        eevent.events = Libc.EPOLLIN;
        eevent.data.writeField("fd", inotifyDescriptor);

        tryEpollCtl(epollDescriptor, Libc.EPOLL_CTL_ADD, inotifyDescriptor, eevent);

        // start thread
        JinotifyWatcher watcher = new JinotifyWatcher();
        watcher.start();
    }

    class JinotifyWatcher extends Thread {
        Libc.EpollEvent[] events = (Libc.EpollEvent[])(new Libc.EpollEvent()).toArray(MAX_EVENTS);

        public void run () {
            int numEvents = 0;
            try {
                while ((numEvents = Libc.INSTANCE.epoll_wait(epollDescriptor, events, MAX_EVENTS, -1)) > 0) {
                    int i = 0;
                    for (i = 0; i < numEvents; i++) {
                        Libc.EpollEvent event = events[i];
                        if ((
                                (event.events & Libc.EPOLLERR)
                                | (event.events & Libc.EPOLLHUP)
                                | -(event.events & Libc.EPOLLIN)) == 0
                            )
                        {
                            // Must be error
                            // one of the watching inotify decriptor dies
                            Libc.INSTANCE.close(event.data.fd);
                            continue;
                        }
                        else if (inotifyDescriptor == event.data.fd) {
                            // Must be ready for read inotify
                            Libc.InotifyEvent eventBuf = new Libc.InotifyEvent();

                            int length = Libc.INSTANCE.read(
                                    event.data.fd, eventBuf.getPointer(), eventBuf.size());
                            if (length == -1) {
                                Libc.INSTANCE.perror("error occured while reading fd=" + event.data.fd);
                            }
                            if ((eventBuf.mask & Libc.IN_CREATE) == 0) {

                            }

                        }
                        else {
                            // Could happened??
                            System.err.println("get epoll event for fd=" + event.data.fd);
                        }
                    }
                    break;
                }
                //} catch (JinotifyException e) {

            } finally {
                //
            }
        }
    }

    public void closeNotifier () {
        Libc.INSTANCE.inotify_rm_watch(inotifyDescriptor, watchingFileDescriptor);
    }
}
