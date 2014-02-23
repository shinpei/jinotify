import org.shinpeinkt.jinotify.JinotifyListener;

public class MyTestListner extends JinotifyListener {

    // TODO: passing argument should move to setter
    public MyTestListner (int epollDescriptor, int inotifyDescriptor, int maxEvents) {
        super(epollDescriptor, inotifyDescriptor, maxEvents);
    }

    public void onCreate () {
        System.out.println("hey, you created something, huh?");
    }
}
