import org.junit.Test;
import org.shinpeinkt.jinotify.Jinotify;
import org.shinpeinkt.jinotify.JinotifyListener;

public class TestJinotify {

    class TestListner extends JinotifyListener {

        @Override
        public void onCreate () {
            System.out.println("hei, you created");
        }
    }

    @Test
    public void initiation () throws Exception {
        Jinotify jinotify = new Jinotify();

    }

    @Test
    public void testCreateHandler () throws Exception {
        Jinotify jinotify = new Jinotify();
        TestListner listner = new TestListner();
        jinotify.addWatch("/tmp", listner);
    }

}
