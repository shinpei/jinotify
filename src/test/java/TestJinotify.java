import org.junit.Test;
import org.shinpeinkt.jinotify.Jinotify;
import org.shinpeinkt.jinotify.JinotifyListener;

import static java.lang.Thread.sleep;

public class TestJinotify {

    class TestListner extends JinotifyListener {

        @Override
        public void onCreate () {
            System.out.println("hey, you created something, huh?");
        }
    }

    @Test
    public void initiation () throws Exception {
        Jinotify jinotify = new Jinotify();

    }
    final String PATH = "/tmp";

    @Test
    public void testCreateHandler () throws Exception {

        Jinotify jinotify = new Jinotify();
        TestListner listner = new TestListner();
        System.out.println("Watching " + PATH);
        jinotify.addWatch(PATH, Jinotify.Libc.IN_CREATE, listner);
        sleep(10000);

        jinotify.closeNotifier();
    }

}
