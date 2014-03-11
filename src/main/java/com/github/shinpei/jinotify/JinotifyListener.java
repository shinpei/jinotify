package com.github.shinpei.jinotify;

import org.slf4j.LoggerFactory;

public abstract class JinotifyListener extends Thread {

    protected int epollDescriptor;
    protected int inotifyDescriptor;
    protected int maxEvents;
    private Clib.EpollEvent[] events;

    private static final D D = new D(LoggerFactory.getLogger(Jinotify.class));

    protected JinotifyListener () {
        // do nothing.
    }

    static private enum OverrideList {
        ACCESS(0), MODIFY(1), CREATE(2), DELETE(3), MOVE(4),
        CLOSE(5);
        public int value;
        OverrideList(int i) { this.value = i; }
    }

    public void onAccess (String path) {
        // do nothing
        D.d("invoking default Access handler {}", path);
    }

    public void onModify (String path) {
        // do nothing
        D.d("invoking default Modify handler {}", path);
    }

    public void onCreate(String path) {
        // do nothing
        D.d("invoking default Create handler {}", path);
    }

    public void onDelete (String path) {
        // do nothing
        D.d("invoking default Delete handler {}", path);
    }

    public void onMove (String path) {
        // do nothing
        D.d("invoking default Move handler {}", path);
    }

    public void onClose(String path) {
        // do nothing
        D.d("invoking default Close handler {}", path);
    }

    public void onEventArrived(int mask) {
        if ((mask & Clib.InotifyConstants.CREATE.value()) != 0) {
            D.d("IN_CREATE");
        }
        if ((mask & Clib.InotifyConstants.ACCESS.value()) != 0) {
            D.d("IN_ACCESS");
        }
        if ((mask & Clib.InotifyConstants.MODIFY.value()) != 0) {
            D.d("IN_MODIFY");
        }
        if ((mask & Clib.InotifyConstants.CLOSE.value()) != 0) {
            D.d("IN_CLOSE");
        }
        if ((mask & Clib.InotifyConstants.ATTRIB.value()) != 0) {
            D.d("IN_ATTRIB");
        }
        if ((mask & Clib.InotifyConstants.CLOSE_NOWRITE.value()) != 0) {
            D.d("IN_CLOSE_NOWRITE");
        }
        if ((mask & Clib.InotifyConstants.CLOSE_WRITE.value()) != 0) {
            D.d("IN_CLOSE_WRITE");
        }
        if ((mask & Clib.InotifyConstants.DELETE.value()) != 0) {
            D.d("IN_DELETE");
        }
        if ((mask & Clib.InotifyConstants.DELETE_SELF.value()) != 0) {
            D.d("IN_DELETE_SELF");
        }
        if ((mask & Clib.InotifyConstants.MOVE.value()) != 0) {
            D.d("IN_MOVE");
        }
        if ((mask & Clib.InotifyConstants.MOVE_SELF.value()) != 0) {
            D.d("IN_MOVE_SELF");
        }
        if ((mask & Clib.InotifyConstants.OPEN.value()) != 0) {
            D.d("IN_OPEN");
        }
    }

    private volatile boolean isFinished = false;

    public void run () {

        int numEvents = 0;
        D.d("max_events={}, epfd={},fd={}", maxEvents, epollDescriptor, inotifyDescriptor);
        while ((numEvents = Clib.tryEpollWait(epollDescriptor, events[0].getPointer(), 1, -1)) > 0) {
            events[0].read();
            Clib.EpollEvent event = events[0];
            if (
                    ((event.events & Clib.EpollConstants.ERR.value())
                    | (event.events & Clib.EpollConstants.HUP.value())
                    | -(event.events & Clib.EpollConstants.IN.value())) == 0)
            {
                // Must be error
                // one of the watching inotify decriptor dies

                Clib.close(event.data.fd);
                // TODO: we need to stop this thread
                Clib.perror("Seems watching descriptor closed");
                D.e("Error, or interruption occured while reading fd. thread is going to suspend");
                Thread.currentThread().suspend();
            }
            else if (inotifyDescriptor == event.data.fd) {
                // Must be ready for read inotify
                Clib.InotifyEvent eventBuf = new Clib.InotifyEvent();
                D.d("Arrived Events={}, size={}", numEvents, eventBuf.size());
                int length = Clib.read(event.data.fd, eventBuf.getPointer(), eventBuf.size());
                eventBuf.read();
                D.d("readByteLength={}, len={}, mask={}, create={}", length, eventBuf.len, Integer.toHexString(eventBuf.mask), Integer.toHexString(Clib.InotifyConstants.CREATE.value()));
                D.d("mask={}", Integer.toBinaryString(eventBuf.mask));
                if (length == -1) {
                    D.e("Error occured while reading fd, seems detecting path is too big, thread is going to suspend");
                    Clib.perror("error occurred while reading fd=" + event.data.fd);
                    // TODO: we need to stop this tread
                    Thread.currentThread().suspend();
                }
                else {
                    String path = new String(eventBuf.name);
                    this.onEventArrived(eventBuf.mask);
                    if ((eventBuf.mask & Clib.InotifyConstants.ACCESS.value()) != 0) {
                        this.onAccess(path);
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.MODIFY.value()) != 0) {
                        this.onModify(path);
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.CREATE.value()) != 0) {
                        this.onCreate(path);
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.DELETE.value()) != 0) {
                        this.onDelete(path);
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.MOVE.value()) != 0) {
                        this.onMove(path);
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.CLOSE.value()) != 0) {
                        this.onClose(path);
                    }
                }
            }
            else {
                // Could happened??
                D.e("get epoll event for fd={}", event.data.fd);
            }
        }
        D.d("finished running");
    }

    final public void initialize (int epollDescriptor, int inotifyDescriptor, int maxEvents){

        // int cannot be under 0;
        this.epollDescriptor = epollDescriptor;
        this.inotifyDescriptor = inotifyDescriptor;
        this.maxEvents = maxEvents;

        this.events = (Clib.EpollEvent[])(new Clib.EpollEvent()).toArray(maxEvents);
    }

}
