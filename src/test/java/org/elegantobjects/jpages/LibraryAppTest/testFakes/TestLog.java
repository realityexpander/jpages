package org.elegantobjects.jpages.LibraryAppTest.testFakes;

import org.elegantobjects.jpages.App2.common.util.log.Log;

public class TestLog extends Log {

    private final boolean shouldPrintAllStatements;

    public
    TestLog(boolean shouldPrintAllStatements) {
        super();
        this.shouldPrintAllStatements = shouldPrintAllStatements;
    }

    @Override
    public void d(Object tag, String msg) {
        if(!shouldPrintAllStatements)
            return;
        super.d(tag, msg);
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
