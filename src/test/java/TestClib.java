import org.junit.Test;
import static org.junit.Assert.*;
import org.shinpeinkt.jinotify.Clib;

public class TestClib {
    @Test
    public void inotifyEvent() throws Exception {

    }

    @Test
    public void inotifyInit () throws Exception {
        int fd = Clib.inotify_init();
        assertNotSame(-1, fd);
    }
}
