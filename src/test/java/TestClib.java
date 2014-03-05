import com.github.shinpei.jinotify.Clib;
import org.junit.Test;
import static org.junit.Assert.*;


public class TestClib {
    static final String tempPath = "/tmp";
    @Test
    public void inotifyInit () throws Exception {
        int fd = Clib.tryInotifyInit();
    }

    @Test
    public void inotifyAddWatch () throws Exception {
        int fd = Clib.tryInotifyInit();
        int wd = Clib.tryInotifyAddWatch(fd, tempPath, Clib.InotifyConstants.CREATE.value());
        Clib.tryInotifyRmWatch(fd, wd);
    }

    @Test
    public void epollCreate() throws Exception {
        int fd = Clib.tryEpollCreate();
    }


    @Test
    public void epollCtl () throws Exception {
        int epfd = Clib.tryEpollCreate();
        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        Clib.tryEpollCtl(epfd, Clib.EpollConstants.CTL_ADD.value(), 0, eevent);
        eevent.events = Clib.EpollConstants.IN.value();
        eevent.data.writeField("fd", 0);
    }

    @Test
    public void inotifyAndEpoll() throws Exception {
        int fd = Clib.tryInotifyInit();
        int epfd = Clib.tryEpollCreate();
        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        eevent.events = Clib.EpollConstants.IN.value();
        eevent.data.writeField("fd", fd);
        Clib.tryEpollCtl(epfd, Clib.EpollConstants.CTL_ADD.value(), fd, eevent);
        Clib.EpollEvent[] events = (Clib.EpollEvent[])(new Clib.EpollEvent()).toArray(1);
        assertNotNull(events);
    }

}
