jinotify: Java wrapper for inotify, Linux
==================================

Jinotify is a low-level bindings for inotify(7). This library simply provides inotify interfaces for Java application, which is useful for java6 or under. From java7, we have nio2 which is providing much better APIs.


Interface
===============
Please refer the following document.
*[inofity(7)](http://man7.org/linux/man-pages/man7/inotify.7.html)

Example
===============

```java
Jinotify jinotify = new Jinotify();
MyListener listner = new MyListener();
jinotify.addWatch("/tmp", Jinotify.IN_CREATE, listner);

jinotify.closeNotifier();
```
