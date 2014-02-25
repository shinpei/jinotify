package com.github.shinpei.jinotify;


public abstract class JinotifyListener extends Thread {

    protected int epollDescriptor;
    protected int inotifyDescriptor;
    protected int maxEvents;
    private Clib.EpollEvent[] events;

    public static Class<?>[] getArgumentTypes() {
        Class<?>[] ret = {int.class, int.class, int.class};
        return ret;
    }

    protected JinotifyListener (int epollDescriptor, int inotifyDescriptor, int maxEvents) {
        this.epollDescriptor = epollDescriptor;
        this.inotifyDescriptor = inotifyDescriptor;
        this.events = (Clib.EpollEvent[])(new Clib.EpollEvent()).toArray(maxEvents);
        this.maxEvents = maxEvents;
    }


    public void onAccess () {
        // do nothing
    }

    public void onModify () {
        // do nothing
    }

    public void onDelete () {
        // do nothing
    }

    public void onCreate() {
        // do nothing
    }
    public void run () {
        int numEvents = 0;
        try {
            while ((numEvents = Clib.epoll_wait(epollDescriptor, events[0].getPointer(), maxEvents, -1)) > 0) {
                int i = 0;
                for (i = 0; i < numEvents; i++) {
                    Clib.EpollEvent event = events[i];
                    if ((
                            (event.events & Clib.EPOLLERR)
                                    | (event.events & Clib.EPOLLHUP)
                                    | -(event.events & Clib.EPOLLIN)) == 0
                            )
                    {
                        // Must be error
                        // one of the watching inotify decriptor dies
                        Clib.close(event.data.fd);
                        continue;
                    }
                    else if (inotifyDescriptor == event.data.fd) {
                        // Must be ready for read inotify
                        Clib.InotifyEvent eventBuf = new Clib.InotifyEvent();

                        int length = Clib.read(
                                event.data.fd, eventBuf.getPointer(), eventBuf.size());
                        if (length == -1) {
                            Clib.perror("error occured while reading fd=" + event.data.fd);
                        }
                        if ((eventBuf.mask & Clib.IN_CREATE) == 0) {
                                this.onCreate();
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
