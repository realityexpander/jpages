package org.elegantobjects.jpages.App2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.elegantobjects.jpages.App2.Context.ContextType.PRODUCTION;

// Context is a singleton class that holds all the repositories and global objects like Gson
interface IContext {
    Repo.BookInfo bookInfoRepo = null;
    Repo.UserInfo userInfoRepo = null;
    Repo.LibraryInfo libraryInfoRepo = null;
    Gson gson = null;
    ILog log = null;
}
public class Context implements IContext {
    // static public Context INSTANCE = null;  // Enforces singleton instance & allows global access, LEAVE for reference

    // Repository Singletons
    private final Repo.BookInfo bookInfoRepo;
    private final Repo.UserInfo userInfoRepo;
    private final Repo.LibraryInfo libraryInfoRepo;

    // Utility Singletons
    protected final Gson gson;
    public final ILog log;

    public enum ContextType {
        PRODUCTION,
        TEST
    }

    Context(
            Repo.BookInfo bookInfoRepo,
            Repo.UserInfo userInfoRepo,
            Repo.LibraryInfo libraryInfoRepo,
            Gson gson,
            ILog log) {
        this.bookInfoRepo = bookInfoRepo;
        this.userInfoRepo = userInfoRepo;
        this.libraryInfoRepo = libraryInfoRepo;
        this.gson = gson;
        this.log = log;
    }

    public static Context setupProductionInstance() {
        return setupInstance(PRODUCTION, null);
    }
    public static Context setupInstance(Context.ContextType contextType, Context context) {
        switch (contextType) {
            case PRODUCTION:
                System.out.println("Context.setupInstance(): passed in Context is null, creating PRODUCTION Context");
                return Context.generateDefaultProductionContext();
            case TEST:
                System.out.println("Context.setupInstance(): using passed in Context");
                return context;
        }

        throw new RuntimeException("Context.setupInstance(): Invalid ContextType");
    }

    // Generate sensible default singletons for the production application
    private static Context generateDefaultProductionContext() {
        ILog log = new Log();
        return new Context(
                new Repo.BookInfo(
                        new BookInfoApi(),
                        new BookInfoDatabase(),
                        log
                ),
                new Repo.UserInfo(log),
                new Repo.LibraryInfo(log),
                new GsonBuilder().setPrettyPrinting().create(),
                log
        );
    }

//    LEAVE for Reference - This is how you would enforce a singleton instance using a static method & variable
//    // If `context` is `null` OR `StaticContext` this returns the default static Context,
//    // otherwise returns the `context` passed in.
//    public static Context setupINSTANCE(Context context) {
//        if (context == null) {
//            if(INSTANCE != null) return INSTANCE;
//
//            System.out.println("Context.getINSTANCE(): passed in Context is null, creating default Context");
//            INSTANCE = new Context();
//            return INSTANCE;  // return default Context (singleton)
//        } else {
//            System.out.println("Context.getINSTANCE(): using passed in Context");
//            INSTANCE = context;  // set the default Context to the one passed in
//            return context;
//        }
//    }
//    public static Context getINSTANCE() {
//        return setupINSTANCE(null);
//    }

    public Repo.BookInfo bookRepo() {
        return this.bookInfoRepo;
    }
    public Repo.UserInfo userRepo() {
        return this.userInfoRepo;
    }
    public Repo.LibraryInfo libraryRepo() {
        return this.libraryInfoRepo;
    }
}