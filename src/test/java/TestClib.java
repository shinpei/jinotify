import com.github.shinpei.jinotify.Clib;
import com.google.common.io.Files;
import com.sun.jna.Structure;
import org.junit.Test;
import static org.junit.Assert.*;


public class TestClib {
    @Test
    public void inotifyEvent() throws Exception {

    }

    @Test
    public void inotifyInit () throws Exception {
        int fd = Clib.inotify_init();
        assertNotSame(-1, fd);
    }

    @Test
    public void epollCreate() throws Exception {
        int fd = Clib.epoll_create(1);

    }

    @Test
    public void epollCtl () throws Exception {
        int epfd = Clib.epoll_create(1);
        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        Clib.epoll_ctl(epfd, Clib.EPOLL_CTL_ADD, 0, eevent);
        eevent.events = Clib.EPOLLIN;
        eevent.data.writeField("fd", 0);
    }

    @Test
    public void inotifyAndEpoll() throws Exception {
        int fd = Clib.inotify_init();
        int wd = Clib.inotify_add_watch(fd, "/tmp", Clib.IN_CREATE);
        int epfd = Clib.epoll_create(1);
        Clib.EpollEvent.ByReference eevent = new Clib.EpollEvent.ByReference();
        eevent.events = Clib.EPOLLIN;
        eevent.data.writeField("fd", fd);
        Clib.epoll_ctl(epfd, Clib.EPOLL_CTL_ADD, fd, eevent);
        Clib.EpollEvent[] events = (Clib.EpollEvent[])(new Clib.EpollEvent()).toArray(1);
        // cannot wath because it's blocking.
    }

}
