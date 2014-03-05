package com.github.shinpei.jinotify;

import com.google.common.collect.Lists;

import java.util.List;

public class Jinotify {
    private int inotifyDescriptor;
    private int watchingFileDescriptor;
    private int epollDescriptor;

    final int MAX_EVENTS = 1;

    private enum JinotifyEvents {
        CREATE(Clib.InotifyConstants.CREATE),
        ACCESS(Clib.InotifyConstants.ACCESS),
        CLOSE(Clib.InotifyConstants.CLOSE),
        MODIFY(Clib.InotifyConstants.MODIFY);

        private final Clib.InotifyConstants value;

        JinotifyEvents(final Clib.InotifyConstants value) {
            this.value = value;
        }

        public final int ivalue() {
            return value.value();
        }

        // TODO: need to define bit operators '|'
    }

    public static final JinotifyEvents CREATE = JinotifyEvents.CREATE;
    public static final JinotifyEvents CLOSE = JinotifyEvents.CLOSE;
    public static final JinotifyEvents ACCESS = JinotifyEvents.ACCESS;
    public static final JinotifyEvents MODIFY = JinotifyEvents.MODIFY;



    private List<Boolean> detectOverrideMethod(JinotifyListener listener) throws JinotifyException {
        // detect handler
        final Class klass = listener.getClass();
        try {
            List<Boolean> overrideList = Lists.newArrayList(
                    klass.getMethod("onAccess").getDeclaringClass().equals(JinotifyListener.class),
                    klass.getMethod("onModify").getDeclaringClass().equals(JinotifyListener.class),
                    klass.getMethod("onCreate").getDeclaringClass().equals(JinotifyListener.class),
                    klass.getMethod("onDelete").getDeclaringClass().equals(JinotifyListener.class)
            );
            return overrideList;
        } catch (NoSuchMethodException e) {
            throw new JinotifyException("SEVERE ERROR", e);
        }

    }

    private final int calculateMask(List<Boolean> overrideList) {
        int mask = 0;
        if (overrideList.get(0) == true) {
            // access
            mask |= Clib.InotifyConstants.ACCESS.value();
        }
        if (overrideList.get(1) == true) {
            mask |= Clib.InotifyConstants.MODIFY.value();
        }
        if (overrideList.get(2) == true) {
            mask |= Clib.InotifyConstants.CREATE.value();
        }
        if (overrideList.get(3) == true) {
            mask |= Clib.InotifyConstants.DELETE.value();
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

    public void disableEvent(JinotifyEvents disablingEvent) throws JinotifyException{
        if (watchingFileDescriptor == 0) {
            throw new JinotifyException("inotify already watching some resource, please call this method before addWatch");
        }
        // not yet
    }

    public void closeNotifier () {
        Clib.tryInotifyRmWatch(inotifyDescriptor, watchingFileDescriptor);
    }
}
