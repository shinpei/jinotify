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
        D.d("invokeing default Access handler {}", path);
    }

    public void onModify (String path) {
        // do nothing
        D.d("invokeing default Modify handler {}", path);
    }

    public void onCreate(String path) {
        // do nothing
        D.d("invokeing default Create handler {}", path);
    }

    public void onDelete (String path) {
        // do nothing
        D.d("invokeing default Delete handler {}", path);
    }

    public void onMove (String path) {
        // do nothing
        D.d("invokeing default Move handler {}", path);
    }

    public void onClose(String path) {
        // do nothing
        D.d("invokeing default Close handler {}", path);
    }

    public void run () {

        int numEvents = 0;
        D.d("MAX events={}, epfd={},fd={}", maxEvents, epollDescriptor, inotifyDescriptor);
        while ((numEvents = Clib.tryEpollWait(epollDescriptor, events[0].getPointer(), maxEvents, -1)) > 0) {
            D.d("Arrived Events={}", numEvents);
            for (int i = 0; i < numEvents; i++) {
                events[i].read();
                Clib.EpollEvent event = events[i];
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
                    continue;
                }
                else if (inotifyDescriptor == event.data.fd) {
                    // Must be ready for read inotify
                    Clib.InotifyEvent eventBuf = new Clib.InotifyEvent();
                    int length = Clib.read(event.data.fd, eventBuf.getPointer(), eventBuf.size());
                    eventBuf.read();
                    D.d("length={}, mask={}, create={}", length, Integer.toHexString(eventBuf.mask), Integer.toHexString(Clib.InotifyConstants.CREATE.value()));
                    D.d("mask={}", Integer.toBinaryString(eventBuf.mask));
                    if (length == -1) {
                        Clib.perror("error occurred while reading fd=" + event.data.fd);
                    }
                    else {
                        String path = new String(eventBuf.name);
                        if ((eventBuf.mask & Clib.InotifyConstants.ACCESS.value()) != 0) {
                            //this.onAccess(new String(eventBuf.name));
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
            break;
        }
    }

    final public void initialize (int epollDescriptor, int inotifyDescriptor, int maxEvents){

        // int cannot be under 0;
        this.epollDescriptor = epollDescriptor;
        this.inotifyDescriptor = inotifyDescriptor;
        this.maxEvents = maxEvents;

        this.events = (Clib.EpollEvent[])(new Clib.EpollEvent()).toArray(maxEvents);
    }

}
