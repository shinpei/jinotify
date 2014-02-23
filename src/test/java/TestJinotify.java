import com.google.common.io.Files;
import org.junit.Test;
import org.shinpeinkt.jinotify.Clib;
import org.shinpeinkt.jinotify.Jinotify;

public class TestJinotify {


    final String PATH = "/tmp";

    @Test
    public void basicEventHandling () throws Exception {
        Jinotify jinotify = new Jinotify();

        jinotify.addWatch(PATH, Clib.IN_CREATE, MyTestListner.class);
        //jinotify.addWatch(PATH, Clib.IN_CREATE, JinotifyListener.class);

        Files.createTempDir();

        jinotify.closeNotifier();
    }

}
