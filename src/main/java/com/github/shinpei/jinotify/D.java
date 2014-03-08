package com.github.shinpei.jinotify;

import org.slf4j.Logger;

public class D {
    private static Logger logger;

    D(final Logger logger) {
        this.logger = logger;
    }

    final public void d(final String msg) {
        logger.debug(msg);
    }

    final public void d(final String maker, final Object... objs) {
        logger.debug(maker, objs);
    }

    final public void i(final String msg) {
        logger.info(msg);
    }

    final public void i(final String maker, final Object... objs) {
        logger.info(maker, objs);
    }

    final public void e(final String msg) {
        logger.error(msg);
    }

    final public void e(final String maker, final Object... objs) {
        logger.error(maker, objs);
    }

    final public void w(final String msg)  {
        logger.warn(msg);
    }

    final public void w(final String maker, final Object... objs) {
        logger.warn(maker, objs);
    }
}
