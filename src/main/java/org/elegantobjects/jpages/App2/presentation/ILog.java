package org.elegantobjects.jpages.App2.presentation;

// Simple Logging Operations
public interface ILog {
    void d(Object tag, String msg);
    void w(Object tag, String msg);
    void e(Object tag, String msg);
    void e(Object tag, String msg, Exception e);
}
