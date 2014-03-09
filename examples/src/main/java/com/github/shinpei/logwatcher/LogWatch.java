package com.github.shinpei.logwatcher;

import com.github.shinpei.jinotify.Jinotify;
import com.github.shinpei.jinotify.JinotifyListener;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;

import static java.util.Arrays.asList;

public class LogWatch {
    static public void main(String[] args) {

        OptionParser optionParser = new OptionParser()
        {
            {
                acceptsAll(asList("h", "help"), "show help");
                acceptsAll(asList("v", "verbose"), "show info log");
                acceptsAll(asList("f", "file")).withRequiredArg().ofType(String.class)
                        .describedAs("specify file or dir you want to watch");
                acceptsAll(asList("d", "debug"), "show debug log");
            }
        };

        OptionSet options = optionParser.parse(args);
        boolean isVerboseMode = false;
        boolean isDebugMode = false;

        try {
            if (options.has("help")) {
                optionParser.printHelpOn(System.out);
            }
            if (options.has("debug")) {
                isDebugMode = true;
            }
            if (options.has("verbose")) {
                isVerboseMode = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        Jinotify jinotify = new Jinotify(isVerboseMode, isDebugMode);
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
