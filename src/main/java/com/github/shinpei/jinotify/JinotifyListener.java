package com.github.shinpei.jinotify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JinotifyListener extends Thread {

    protected int epollDescriptor;
    protected int inotifyDescriptor;
    protected int maxEvents;
    private Clib.EpollEvent[] events;

    private static final Logger logger = LoggerFactory.getLogger(Jinotify.class);

    public static final Class<?>[] getArgumentTypes() {
        Class<?>[] ret = {};
        return ret;
    }

    protected JinotifyListener () {
        // do nothing.
    }

    static private enum OverrideList {
        ACCESS(0), MODIFY(1), CREATE(2), DELETE(3), MOVE(4),
        CLOSE(5);
        public int value;
        OverrideList(int i) { this.value = i; }
    }

    public void onAccess () {
        // do nothing
        System.out.println("Default");
    }

    public void onModify () {
        // do nothing
    }

    public void onCreate() {
        // do nothing
    }

    public void onDelete () {
        // do nothing
    }

    public void onMove() {
        // do nothing
    }

    public void onClose() {
        // do nothing
    }

    public void run () {

        int numEvents = 0;
        logger.info("MAX events={}, epfd={},fd={}", maxEvents, epollDescriptor, inotifyDescriptor);
        while ((numEvents = Clib.tryEpollWait(epollDescriptor, events[0].getPointer(), maxEvents, -1)) > 0) {
            logger.info("Arrived Events={}", numEvents);
            for (int i = 0; i < numEvents; i++) {
                events[i].read();
                Clib.EpollEvent event = events[i];
                if (((event.events & Clib.EpollConstants.ERR.value())
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
                    logger.info("length={}, mask={}, create={}", length, eventBuf.mask, Clib.InotifyConstants.CREATE.value());
                    if (length == -1) {
                        Clib.perror("error occurred while reading fd=" + event.data.fd);
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.ACCESS.value()) == 0) {
                        this.onAccess();
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.MODIFY.value()) == 0) {
                        this.onModify();
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.CREATE.value()) == 0) {
                        this.onCreate();
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.DELETE.value()) == 0) {
                        this.onDelete();
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.MOVE.value()) == 0) {
                        this.onMove();
                    }
                    else if ((eventBuf.mask & Clib.InotifyConstants.CLOSE.value()) == 0) {
                        this.onClose();
                    }
                }
                else {
                    // Could happened??
                    logger.error("get epoll event for fd={}", event.data.fd);
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
