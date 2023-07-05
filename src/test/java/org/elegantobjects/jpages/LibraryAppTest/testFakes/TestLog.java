package org.elegantobjects.jpages.LibraryAppTest.testFakes;

import org.elegantobjects.jpages.App2.common.util.log.Log;
import org.jetbrains.annotations.NotNull;

public class TestLog extends Log {

    private final boolean shouldOnlyPrintWarningsAndErrors;

    public
    TestLog(boolean shouldOnlyPrintWarningsAndErrors) {
        super();
        this.shouldOnlyPrintWarningsAndErrors = shouldOnlyPrintWarningsAndErrors;
    }

    @Override
    public void d(Object tag, String msg) {
        if(shouldOnlyPrintWarningsAndErrors)
            return;

        if(tag == null) {
            super.d("null", msg);
            return;
        }

        super.d(calcLogPrefix(tag), msg);
    }

    @Override
    public void w(Object tag, String msg) {
        if(tag == null) {
            super.w("null", msg);
            return;
        }

        super.w(calcLogPrefix(tag), msg);
    }

    @Override
    public void e(Object tag, String msg) {
        if(tag == null) {
            super.e("null", msg);
            return;
        }

        super.e(calcLogPrefix(tag), msg);
    }

    @Override
    public void e(Object tag, String msg, Exception e) {
        if(tag == null) {
            super.e("null", msg);
            return;
        }

        super.e(calcLogPrefix(tag), msg, e);
    }

    private @NotNull String calcLogPrefix(Object obj) {
        return calcSimpleName(obj) + "➤" + calcMethodName() + "()";
    }

    private @NotNull String calcMethodName() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }

    private @NotNull String calcSimpleName(@NotNull Object obj) {
        return obj.getClass().getSimpleName();
    }
}
