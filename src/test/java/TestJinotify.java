import org.junit.Ignore;
import org.junit.Test;
import org.shinpeinkt.jinotify.Jinotify;
import org.shinpeinkt.jinotify.JinotifyListener;
import org.shinpeinkt.jinotify.JinotifyTest;

import static java.lang.Thread.sleep;

public class TestJinotify {

    class TestListner extends JinotifyListener {

        @Override
        public void onCreate () {
            System.out.println("hey, you created something, huh?");
        }
    }
    final String PATH = "/tmp";

    @Test
    public void testCreateHandler () throws Exception {
        Jinotify jinotify = new Jinotify();
        TestListner listner = new TestListner();
        System.out.println("Watching " + PATH);
        jinotify.addWatch(PATH, Jinotify.Libc.IN_CREATE, listner);
        jinotify.closeNotifier();
    }

}
