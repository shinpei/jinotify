import com.google.common.io.Files;
import org.junit.Ignore;
import org.junit.Test;
import com.github.shinpei.jinotify.Clib;
import com.github.shinpei.jinotify.Jinotify;

public class TestJinotify {

    final String PATH = "/tmp";

    @Test
    public void basicEventHandling () throws Exception {
        Jinotify jinotify = new Jinotify();

        jinotify.addWatch(PATH, Clib.IN_CREATE, MyTestListner.class);

        Files.createTempDir();

        jinotify.closeNotifier();
    }

}
