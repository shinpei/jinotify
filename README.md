jinotify: Java wrapper for inotify, Linux
==================================

Jinotify is a file change notificator implemented with inotify(7) and epoll(7). Jinotify provides simple and strong interface for wathcing file events, such as new file creation, open, delete, and so on. Jinotify is useful for java6 or under, but your Linux kernel should be more than 2.6.13+. From java7, we have nio2 which is providing much better APIs.


Interface
===============
Please refer the following document.
*[inofity(7)](http://man7.org/linux/man-pages/man7/inotify.7.html)
*[epoll(7)](http://man7.org/linux/man-pages/man7/epoll.7.html)

Example
===============

```java
Jinotify jinotify = new Jinotify();
MyListener listner = new MyListener();
jinotify.addWatch("/tmp", listner);
```
