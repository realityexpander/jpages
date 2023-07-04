package org.elegantobjects.jpages.LibraryAppTest;

import org.elegantobjects.jpages.App2.common.util.log.Log;

public class TestLog extends Log {
    @Override
    public void d(Object tag, String msg) {
        // ignore debug messages in tests
        return;
    }

    @Override
    public void w(Object tag, String msg) {
        super.w(tag, msg);
    }

    @Override
    public void e(Object tag, String msg) {
        super.e(tag, msg);
    }

    @Override
    public void e(Object tag, String msg, Exception e) {
        super.e(tag, msg, e);
    }
}
