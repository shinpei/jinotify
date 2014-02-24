package com.github.shinpei.jinotify;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Jinotify {
    private int inotifyDescriptor;
    private int watchingFileDescriptor;
    private int epollDescriptor;


    final int MAX_EVENTS = 1;

    private int tryInotifyInit () throws JinotifyException {
        int fd = Clib.INSTANCE.inotify_init();
        if (fd < 0) {
            throw new JinotifyException("Couldn't initiate inotify");
        }
        return fd;
    }

    private int tryInotifyAddWatch(int fd, String path, int mask) throws JinotifyException {
        int wd = Clib.INSTANCE.inotify_add_watch(fd, path,  mask);
        if (wd < 0) {
            throw new JinotifyException("Couldn't add inotify watch");
        }
        return wd;
    }

    private int tryEpollCreate(int size) throws JinotifyException {
        int epfd = Clib.INSTANCE.epoll_create(size);
        if (epfd < 0) {
            throw new JinotifyException("epoll_create failed");
        }
        return epfd;
    }

    private void tryEpollCtl (int epfd, int flag, int fd, Clib.EpollEvent.ByReference ev) throws JinotifyException {
        int retVal = Clib.INSTANCE.epoll_ctl(epfd, flag, fd, ev);
        if (retVal < 0) {
            throw new JinotifyException("epoll_ctl failed");
        }
        // retVal is always 0 if success. we don't need to return this.
    }

    public <LISTENER extends JinotifyListener>
    void addWatch(String absolutePath, int mask, Class<LISTENER> klass)
            throws JinotifyException {

        inotifyDescriptor = tryInotifyInit();
        watchingFileDescriptor = tryInotifyAddWatch(inotifyDescriptor, absolutePath, mask);
        epollDescriptor = tryEpollCreate(MAX_EVENTS);

        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        eevent.events = Clib.EPOLLIN;
        eevent.data.writeField("fd", inotifyDescriptor);

        tryEpollCtl(epollDescriptor, Clib.EPOLL_CTL_ADD, inotifyDescriptor, eevent);

        // start thread
        Constructor<LISTENER> constructor = null;
        try {
            constructor = klass.getConstructor(LISTENER.getArgumentTypes());
        } catch (NoSuchMethodException e) {
            throw new JinotifyException("cannot get constructor" + e.getMessage(), e);
        }
        Object[] args = {epollDescriptor, inotifyDescriptor, MAX_EVENTS};
        try {
            LISTENER listener = constructor.newInstance(args);
            listener.start();
        } catch (IllegalArgumentException e) {
            throw new JinotifyException(e);
        } catch (InstantiationException e) {
            throw new JinotifyException(e);
        } catch (IllegalAccessException e) {
            throw new JinotifyException(e);
        } catch (InvocationTargetException e) {
            throw new JinotifyException(e);
        }

    }

    public void closeNotifier () {
        Clib.INSTANCE.inotify_rm_watch(inotifyDescriptor, watchingFileDescriptor);
    }
}
