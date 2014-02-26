package com.github.shinpei.jinotify;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Jinotify {
    private int inotifyDescriptor;
    private int watchingFileDescriptor;
    private int epollDescriptor;

    final int MAX_EVENTS = 1;

    public enum JinotifyEvents {
        CREATE(Clib.InotifyConstants.CREATE);

        private Clib.InotifyConstants value;
        JinotifyEvents(Clib.InotifyConstants value) {
            this.value = value;
        }
        public int ivalue() {
            return value.value();
        }

        // TODO: need to define bit operators '|'
    }

    public static final JinotifyEvents CREATE = JinotifyEvents.CREATE;

    public <LISTENER extends JinotifyListener>
    void addWatch(String absolutePath, JinotifyEvents mask, Class<LISTENER> klass)
            throws ClibException, JinotifyException {

        inotifyDescriptor = Clib.tryInotifyInit();
        watchingFileDescriptor = Clib.tryInotifyAddWatch(inotifyDescriptor, absolutePath, mask.ivalue());
        epollDescriptor = Clib.tryEpollCreate();

        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        eevent.events = Clib.EpollConstants.IN.value();
        eevent.data.writeField("fd", inotifyDescriptor);

        Clib.tryEpollCtl(epollDescriptor, Clib.EpollConstants.CTL_ADD.value(), inotifyDescriptor, eevent);

        // start thread
        Constructor<LISTENER> constructor = null;
        try {
            constructor = klass.getConstructor(LISTENER.getArgumentTypes());
        } catch (NoSuchMethodException e) {
            throw new JinotifyException("cannot get constructor" + e.getMessage(), e);
        }

        try {
            LISTENER listener = constructor.newInstance();
            listener.initialize(epollDescriptor, inotifyDescriptor, MAX_EVENTS);
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
