package com.github.shinpei.jinotify;

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


    public void addWatch(String absolutePath, JinotifyEvents mask, JinotifyListener listener)
    throws JinotifyException {

        inotifyDescriptor = Clib.tryInotifyInit();
        watchingFileDescriptor = Clib.tryInotifyAddWatch(inotifyDescriptor, absolutePath, mask.ivalue());
        epollDescriptor = Clib.tryEpollCreate();

        Clib.EpollEvent.ByReference epollEvent = new Clib.EpollEvent.ByReference();
        epollEvent.events = Clib.EpollConstants.IN.value();
        epollEvent.data.writeField("fd", inotifyDescriptor);

        Clib.tryEpollCtl(epollDescriptor, Clib.EpollConstants.CTL_ADD.value(), inotifyDescriptor, epollEvent);

        listener.initialize(epollDescriptor, inotifyDescriptor, MAX_EVENTS);

        // detect handler
        Class klass = listener.getClass();
        try {
            System.out.println("Overrided??: " + klass.getMethod("onCreate").getDeclaredAnnotations());
        } catch (NoSuchMethodException e) {
            throw new JinotifyException("SEVERE ERROR", e);
        }

        listener.start();

    }

    public void closeNotifier () {
        Clib.tryInotifyRmWatch(inotifyDescriptor, watchingFileDescriptor);
    }
}
