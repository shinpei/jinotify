import com.github.shinpei.jinotify.Clib;
import com.google.common.io.Files;
import com.sun.jna.Structure;
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
        int wd = Clib.tryInotifyAddWatch(fd, tempPath, Clib.IN_CREATE);
        Clib.tryInotifyReWatch(fd, wd);
    }

    @Test
    public void epollCreate() throws Exception {
        int fd = Clib.tryEpollCreate(1);
    }


    @Test
    public void epollCtl () throws Exception {
        int epfd = Clib.tryEpollCreate(1);
        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        Clib.tryEpollCtl(epfd, Clib.EPOLL_CTL_ADD, 0, eevent);
        eevent.events = Clib.EPOLLIN;
        eevent.data.writeField("fd", 0);
    }

    @Test
    public void inotifyAndEpoll() throws Exception {
        int fd = Clib.tryInotifyInit();
        int wd = Clib.tryInotifyAddWatch(fd, tempPath, Clib.IN_CREATE);
        int epfd = Clib.tryEpollCreate(1);
        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        eevent.events = Clib.EPOLLIN;
        eevent.data.writeField("fd", fd);
        Clib.tryEpollCtl(epfd, Clib.EPOLL_CTL_ADD, fd, eevent);
        Clib.EpollEvent[] events = (Clib.EpollEvent[])(new Clib.EpollEvent()).toArray(1);
        // cannot wath because it's blocking.
    }

}
