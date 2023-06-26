package org.elegantobjects.jpages.App2.core.log;

import org.elegantobjects.jpages.App2.core.log.ILog;

public class Log implements ILog {
    private void d(String tag, String msg) {
        System.out.println(tag + ": " + msg);
    }
    private void w(String tag, String msg) {
        System.err.println(tag + ":(WARNING) " + msg);
    }
    private void e(String tag, String msg) {
        System.err.println(tag + ":(ERROR) " + msg);
    }

    public void e(Object obj, String msg, Exception e) {
        if(obj == null) {
            d("null", msg);
            return;
        }

        // Collect stacktrace to a comma delimited string
        StringBuilder stacktrace = new StringBuilder();
        for(StackTraceElement ste : e.getStackTrace()) {
            stacktrace.append(ste.toString()).append(", ");
        }

        e(obj.getClass().getSimpleName() + "➤" +
            Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
            msg + " - " + e.getMessage() + " - " + stacktrace
        );
        e.printStackTrace(); // LEAVE for debugging
    }

    // example: log.d(this, "message") will print "ClassName➤MethodName(): message"
    public void d(Object obj, String msg) {
        if(obj == null) {
            d("null", msg);
            return;
        }

        d(obj.getClass().getSimpleName() + "➤" +
            Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
            msg
        );
    }

    // example: log.w(this, "message") will print "ClassName➤MethodName():(WARNING) message"
    public void w(Object obj, String msg) {
        if(obj == null) {
            w("null", msg);
            return;
        }

        w(obj.getClass().getSimpleName() + "➤" +
            Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
            msg
        );
    }

    // example: log.e(this, "message") will print "ClassName➤MethodName():(ERROR) message"
    public void e(Object obj, String msg) {
        if(obj == null) {
            e("null", msg);
            return;
        }

        e(obj.getClass().getSimpleName() + "➤" +
            Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
            msg
        );
    }
}
