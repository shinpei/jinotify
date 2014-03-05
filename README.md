jinotify: Java file event notifier on Linux
==================================
Jinotify is a file change notifier implemented with inotify(7) and epoll(7). Jinotify provides simple and strong interface for watching file events, such as new file creation, open, delete, and so on. Jinotify is useful for java6 or under, but your Linux kernel should be more than 2.6.13+. From java7, we have nio2 which is providing much better APIs.

Example
===============
```java
import org.shinpeinkt.jinotify.JinotifyListener;

class MyListner extends JinotifyListener {
    @Override
    public void onCreate () {
        System.out.println("hey, you created something, huh?");
    }
}

Jinotify jinotify = new Jinotify();
MyListener listener = new MyListener();
jinotify.addWatch("/tmp", listener);
jinotify.closeNotifier();
```

Install (Maven)
===============
```xml
<dependency>
    <groupId>com.github.shinpei</groupId>
    <artifactId>jinotify</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Events
===============
You can define following event with Jinotify. If you override methods below, it'll automatically watch event.

+ `Jinotify.onCreate`
	It works on when file or dir is created. 

+ `Jinotify.onAccess`
	It works on when file is opened, and read contents. For example, touch.

+ `Jinotify.onModified`
	It works on when file contents are modified.

+ `Jinotify.onDelete`
	It works on when watching file or dir has removed.

+ `Jinotify.onMoved`
	It works on when watching file or dir has moved.

+ `Jinotify.onClose`
	It works on when file is closed.

License, contact info, contribute
===============
It's under [ASL2.0](http://www.apache.org/licenses/LICENSE-2.0). If you find bug or improvement request, please contact me through twitter, @shinpeintk. And always welcoming heartful pull request.

cheers, :beers:
