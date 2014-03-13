package com.github.shinpei.jinotify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
public abstract class JinotifyListener extends Thread {

    protected int epollDescriptor;
    protected int inotifyDescriptor;
    protected int maxEvents;
    private Clib.EpollEvent[] events;

    private static final D D = new D(LoggerFactory.getLogger(Jinotify.class));

    protected JinotifyListener () {
        // do nothing.
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

    // please override this one

    public void onEventArrived(List<JinotifyEvent> events) {
        D.d(events.toString());
    }

    private List<JinotifyEvent> handlingEvents;

    public List<JinotifyEvent> getHandlingEvents () {
        return handlingEvents;
    }

    public List<Boolean> detectOverrideMethod() throws JinotifyException {

        final Class klass = this.getClass();
        List<List<String, JinotifyEvent>> methodNames = Lists.newArrayList(
                Lists.newArrayList("onAccess", JinotifyEvent.ACCESS),
                ImmutableMap.of("onModify", JinotifyEvent.MODIFY),
                ImmutableMap.of("onCreate", JinotifyEvent.CREATE),
                ImmutableMap.of("onDelete", JinotifyEvent.DELETE),
                ImmutableMap.of("onMove", JinotifyEvent.MOVE),
                ImmutableMap.of("onClose", JinotifyEvent.CLOSE)
        );

        try {
            for (Map<String, JinotifyEvent> pair : methodNames) {
                if (klass.getMethod(pair.get(0), String.class).getDeclaringClass().equals(JinotifyListener.class)) {

                }
            }

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

    public int getEventMask() throws JinotifyException {
        int mask = 0;
        if (handlingEvents == null) {
            handlingEvents = this.detectOverrideMethod();
        }
        return mask;
    }

    public List<JinotifyEvent> transferMaskToEvents(int mask) {

        List<JinotifyEvent> events = new ArrayList<JinotifyEvent>(EnumSet.allOf(Clib.InotifyConstants.class).size());

        // FIXME: leave this as todo
        if ((mask & JinotifyEvent.CREATE.value()) != 0) {
            events.add(JinotifyEvent.CREATE);
        }
        if ((mask & JinotifyEvent.ACCESS.value()) != 0) {
            events.add(JinotifyEvent.ACCESS);
        }
        if ((mask & JinotifyEvent.MODIFY.value()) != 0) {
            events.add(JinotifyEvent.MODIFY);
        }
        if ((mask & JinotifyEvent.CLOSE.value()) != 0) {
            events.add(JinotifyEvent.CLOSE);
        }
        if ((mask & JinotifyEvent.ATTRIB.value()) != 0) {
            events.add(JinotifyEvent.ATTRIB);
        }
        if ((mask & JinotifyEvent.CLOSE_NOWRITE.value()) != 0) {
            events.add(JinotifyEvent.CLOSE_NOWRITE);
        }
        if ((mask & JinotifyEvent.CLOSE_WRITE.value()) != 0) {
            events.add(JinotifyEvent.CLOSE_WRITE);
        }
        if ((mask & JinotifyEvent.DELETE.value()) != 0) {
            events.add(JinotifyEvent.DELETE);
        }
        if ((mask & JinotifyEvent.DELETE_SELF.value()) != 0) {
            events.add(JinotifyEvent.DELETE_SELF);
        }
        if ((mask & JinotifyEvent.MOVE.value()) != 0) {
            events.add(JinotifyEvent.MOVE);
        }
        if ((mask & JinotifyEvent.MOVE_SELF.value()) != 0) {
            events.add(JinotifyEvent.MOVE_SELF);
        }
        if ((mask & JinotifyEvent.MOVED_FROM.value()) != 0) {
            events.add(JinotifyEvent.MOVED_FROM);
        }
        if ((mask & JinotifyEvent.OPEN.value()) != 0) {
            events.add(JinotifyEvent.OPEN);
        }
        return events;
    }

    // TODO: for detecting thread sleep. not yet implemented
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
                    Thread.currentThread().suspend();
                }
                else {
                    String path = new String(eventBuf.name);
                    List<JinotifyEvent> detectedEvents = transferMaskToEvents(eventBuf.mask);
                    this.onEventArrived(detectedEvents);
                    // we suppose event stacks in event que, and no need to handle them at once.
                    if (detectedEvents.contains(JinotifyEvent.ACCESS)) {
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
