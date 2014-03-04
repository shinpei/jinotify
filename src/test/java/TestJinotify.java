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
        Jinotify jinotify = new Jinotify();
        MyTestListner listner = new MyTestListner();
        jinotify.addWatch(PATH, Jinotify.CREATE, listner);

        Files.createTempDir();

        jinotify.closeNotifier();
    }

    @Test
    @Ignore
    public void onModify () throws Exception {
        class MyListenerModify extends JinotifyListener {
            @Override
            public void onModify() {
                System.out.println("hey, you modified, huh?");
            }
        }

        Jinotify jinotify = new Jinotify();
        MyListenerModify listener = new MyListenerModify();
        jinotify.addWatch(PATH, Jinotify.MODIFY, listener);
        File f = Files.createTempDir();

        jinotify.closeNotifier();
    }

}
