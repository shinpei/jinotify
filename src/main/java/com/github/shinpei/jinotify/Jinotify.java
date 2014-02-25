package com.github.shinpei.jinotify;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Jinotify {
    private int inotifyDescriptor;
    private int watchingFileDescriptor;
    private int epollDescriptor;


    final int MAX_EVENTS = 1;


    public <LISTENER extends JinotifyListener>
    void addWatch(String absolutePath, int mask, Class<LISTENER> klass)
            throws ClibException, JinotifyException {

        inotifyDescriptor = Clib.tryInotifyInit();
        watchingFileDescriptor = Clib.tryInotifyAddWatch(inotifyDescriptor, absolutePath, mask);
        epollDescriptor = Clib.tryEpollCreate(MAX_EVENTS);

        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        eevent.events = Clib.EPOLLIN;
        eevent.data.writeField("fd", inotifyDescriptor);

        Clib.tryEpollCtl(epollDescriptor, Clib.EPOLL_CTL_ADD, inotifyDescriptor, eevent);

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

    public void closeNotifier () throws Exception {
        Clib.tryInotifyReWatch(inotifyDescriptor, watchingFileDescriptor);
    }
}
