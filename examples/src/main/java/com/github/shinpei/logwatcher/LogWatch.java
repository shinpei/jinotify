package com.github.shinpei.logwatcher;

import com.github.shinpei.jinotify.Jinotify;
import com.github.shinpei.jinotify.JinotifyListener;

public class LogWatch {
    static public void main(String[] args) {

        Jinotify jinotify = new Jinotify();
        class MyListener extends JinotifyListener {
            @Override
            public void onAccess() {
                System.out.println("Accessed!!");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        MyListener listener = new MyListener();
        try {
            jinotify.addWatch("/tmp", listener);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        jinotify.closeNotifier();
    }
}
