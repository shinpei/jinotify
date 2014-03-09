package com.github.shinpei.logwatcher;

import com.github.shinpei.jinotify.Jinotify;
import com.github.shinpei.jinotify.JinotifyListener;

public class LogWatch {
    static public void main(String[] args) {

        Jinotify jinotify = new Jinotify();
        class MyListener extends JinotifyListener {
            @Override
            public void onCreate(String path) {
                System.out.println("Created: " + path);
            }

            @Override
            public void onAccess(String path) {
                System.out.println("Access: " + path);
            }
        }

        MyListener listener = new MyListener();
        try {
            jinotify.addWatch("/tmp", listener);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        while(true) {

        }
//        jinotify.closeNotifier();
    }
}
