import com.github.shinpei.jinotify.JinotifyListener;
import com.google.common.io.Files;
import org.junit.Ignore;
import org.junit.Test;
import com.github.shinpei.jinotify.Jinotify;

import java.io.File;

public class TestJinotify {

    final String PATH = "/tmp";

    @Test
    public void basicEventHandling () throws Exception {

        class MyListenerCreate extends  JinotifyListener {
            @Override
            public void onCreate (String path) {
                System.out.println("hey, you created " + path + " , hun?");
            }
        }

        Jinotify jinotify = new Jinotify();
        MyListenerCreate listener = new MyListenerCreate();
        jinotify.addWatch(PATH, listener);
        Files.createTempDir();
        jinotify.closeNotifier();
    }

    @Test
    public void onModify () throws Exception {

        class MyListenerModify extends JinotifyListener {
            @Override
            public void onModify(String path) {
                System.out.println("hey, you modified " + path + ", huh?");
            }
        }

        Jinotify jinotify = new Jinotify();
        MyListenerModify listener = new MyListenerModify();
        jinotify.addWatch(PATH, listener);

        jinotify.closeNotifier();
    }

}
