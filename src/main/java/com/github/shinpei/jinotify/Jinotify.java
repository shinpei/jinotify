package com.github.shinpei.jinotify;

import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import java.util.List;

public class Jinotify {

    private int inotifyDescriptor;
    private int watchingFileDescriptor;
    private int epollDescriptor;

    final int MAX_EVENTS = 8;
    private static D D;

    public Jinotify () {

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        D = new D(LoggerFactory.getLogger(this.getClass()));
    }

    public Jinotify (final boolean isVerboseMode, final boolean isDebugMode){

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        if (isVerboseMode) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        }
        if (isDebugMode) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        }
        D = new D(LoggerFactory.getLogger(this.getClass()));
    }

    private List<Boolean> detectOverrideMethod(JinotifyListener listener) throws JinotifyException {

        // detect handler
        final Class klass = listener.getClass();
        try {

            List<Boolean> overrideList = Lists.newArrayList(
                    !klass.getMethod("onAccess", String.class).getDeclaringClass().equals(JinotifyListener.class),
                    !klass.getMethod("onModify", String.class).getDeclaringClass().equals(JinotifyListener.class),
                    !klass.getMethod("onCreate", String.class).getDeclaringClass().equals(JinotifyListener.class),
                    !klass.getMethod("onDelete", String.class).getDeclaringClass().equals(JinotifyListener.class),
                    !klass.getMethod("onMove", String.class).getDeclaringClass().equals(JinotifyListener.class),
                    !klass.getMethod("onClose", String.class).getDeclaringClass().equals(JinotifyListener.class)
                    );
            return overrideList;
        } catch (NoSuchMethodException e) {

            throw new JinotifyException("SEVERE: Couldn't detect overrides methods, something wrong with your listener", e);
        }

    }

    private final int calculateMask(List<Boolean> overrideList) {
        final JinotifyEvent[] eventListForMaskCalculation = {
                JinotifyEvent.ACCESS, JinotifyEvent.MODIFY,
                JinotifyEvent.CREATE, JinotifyEvent.DELETE,
                JinotifyEvent.MOVE, JinotifyEvent.CLOSE
        };

        int mask = 0;
        int idx = 0;
        for (boolean b: overrideList){
            if (b == true) {
                mask |= eventListForMaskCalculation[idx++].value();
            }
        }
        return mask;
    }

    public void addWatch(String absolutePath, JinotifyListener listener)
    throws JinotifyException {

        inotifyDescriptor = Clib.tryInotifyInit();

        List<Boolean> overrideList = detectOverrideMethod(listener);
        final int mask = calculateMask(overrideList);

        watchingFileDescriptor = Clib.tryInotifyAddWatch(inotifyDescriptor, absolutePath, mask);
        epollDescriptor = Clib.tryEpollCreate();

        Clib.EpollEvent.ByReference epollEvent = new Clib.EpollEvent.ByReference();
        epollEvent.events = Clib.EpollConstants.IN.value();
        epollEvent.data.writeField("fd", inotifyDescriptor);

        Clib.tryEpollCtl(epollDescriptor, Clib.EpollConstants.CTL_ADD.value(), inotifyDescriptor, epollEvent);

        listener.initialize(epollDescriptor, inotifyDescriptor, MAX_EVENTS);
        listener.start();
    }

    public void disableEvent(JinotifyEvent disablingEvent) throws JinotifyException {
        if (watchingFileDescriptor == 0) {
            throw new JinotifyException("inotify already watching some resource, please call this method before addWatch");
        }
        // not yet
    }

    public void closeNotifier () {
        Clib.tryInotifyRmWatch(inotifyDescriptor, watchingFileDescriptor);
    }
}
