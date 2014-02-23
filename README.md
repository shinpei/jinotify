jinotify: Java file event notifier on Linux
==================================
Jinotify is a file change notificator implemented with inotify(7) and epoll(7). Jinotify provides simple and strong interface for wathcing file events, such as new file creation, open, delete, and so on. Jinotify is useful for java6 or under, but your Linux kernel should be more than 2.6.13+. From java7, we have nio2 which is providing much better APIs.

Interface
===============
Document is not ready yet.


Example
===============
First, define your watcher. Constructor will be removed, but we need to define this for now.
```java
import org.shinpeinkt.jinotify.JinotifyListener;

public class MyTestListner extends JinotifyListener {

    public MyTestListner (int epollDescriptor, int inotifyDescriptor, int maxEvents) {
        super(epollDescriptor, inotifyDescriptor, maxEvents);
    }

    public void onCreate () {
        System.out.println("hey, you created something, huh?");
    }
}
```

And, call. 

```java
Jinotify jinotify = new Jinotify();
jinotify.addWatch("/tmp", Clib.IN_CREATE, MyTestListner.class);
Files.createTempDir();
jinotify.closeNotifier();
```

cheers.
