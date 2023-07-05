package org.elegantobjects.jpages.App2.common.util.log;

// Logs to the system console
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

    // example: log.d(this, "message") will print "ClassName➤MethodName(): message"
    public void d(Object tag, String msg) {
        if(tag == null) {
            d("null", msg);
            return;
        }
        if(tag instanceof String) {
            d((String) tag, msg);
            return;
        }

        d(tag.getClass().getSimpleName() + "➤" +
                        Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
                msg
        );
    }

    public void e(Object tag, String msg, Exception e) {
        if(tag == null) {
            e("null", msg);
            return;
        }
        if(tag instanceof String) {
            e((String) tag, msg);
            return;
        }

        // Collect stacktrace to a comma delimited string
        StringBuilder stacktrace = new StringBuilder();
        for(StackTraceElement ste : e.getStackTrace()) {
            stacktrace.append(ste.toString()).append(", ");
        }

        e(tag.getClass().getSimpleName() + "➤" +
            Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
            msg + " - " + e.getMessage() + " - " + stacktrace
        );
        e.printStackTrace(); // LEAVE for debugging
    }

    // example: log.w(this, "message") will print "ClassName➤MethodName():(WARNING) message"
    public void w(Object tag, String msg) {
        if(tag == null) {
            w("null", msg);
            return;
        }
        if(tag instanceof String) {
            w((String) tag, msg);
            return;
        }

        w(tag.getClass().getSimpleName() + "➤" +
            Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
            msg
        );
    }

    // example: log.e(this, "message") will print "ClassName➤MethodName():(ERROR) message"
    public void e(Object tag, String msg) {
        if(tag == null) {
            e("null", msg);
            return;
        }
        if(tag instanceof String) {
            e((String) tag, msg);
            return;
        }

        e(tag.getClass().getSimpleName() + "➤" +
            Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
            msg
        );
    }
}
