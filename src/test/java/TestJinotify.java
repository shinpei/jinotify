import com.google.common.io.Files;
import org.junit.Test;
import org.shinpeinkt.jinotify.Clib;
import org.shinpeinkt.jinotify.Jinotify;
import org.shinpeinkt.jinotify.JinotifyListener;


public class TestJinotify {

    //TODO: should be accessible from addWatch, means public
    public class TestListner extends JinotifyListener {
        // TODO: passing argument should move to setter
        public TestListner (int epollDescriptor, int inotifyDescriptor, int maxEvents) {
            super(epollDescriptor, inotifyDescriptor, maxEvents);
        }

        public void onCreate () {
            System.out.println("hey, you created something, huh?");
        }
    }

    final String PATH = "/tmp";

    @Test
    public void basicEventHandling () throws Exception {
        Jinotify jinotify = new Jinotify();

        //jinotify.addWatch(PATH, Clib.IN_CREATE, TestListner.class);
        jinotify.addWatch(PATH, Clib.IN_CREATE, JinotifyListener.class);

        Files.createTempDir();

        jinotify.closeNotifier();
    }

}
