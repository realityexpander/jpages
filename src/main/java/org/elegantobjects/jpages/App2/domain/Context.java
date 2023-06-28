package org.elegantobjects.jpages.App2.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.elegantobjects.jpages.App2.data.book.network.BookInfoApi;
import org.elegantobjects.jpages.App2.data.book.local.BookInfoDatabase;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.log.Log;
import org.elegantobjects.jpages.App2.domain.account.AccountInfoRepo;
import org.elegantobjects.jpages.App2.domain.common.IContext;
import org.elegantobjects.jpages.App2.domain.book.BookInfoRepo;
import org.elegantobjects.jpages.App2.domain.library.LibraryInfoRepo;
import org.elegantobjects.jpages.App2.domain.user.UserInfoRepo;

import static org.elegantobjects.jpages.App2.domain.Context.ContextType.PRODUCTION;

public class Context implements IContext {
    // static public Context INSTANCE = null;  // Enforces singleton instance & allows global access, LEAVE for reference

    // Repository Singletons
    private final BookInfoRepo bookInfoRepo;
    private final UserInfoRepo userInfoRepo;
    private final LibraryInfoRepo libraryInfoRepo;
    private final AccountInfoRepo accountInfoRepo;

    // Utility Singletons
    public final Gson gson;
    public final ILog log;

    public enum ContextType {
        PRODUCTION,
        TEST
    }

    Context(
            BookInfoRepo bookInfoRepo,
            UserInfoRepo userInfoRepo,
            LibraryInfoRepo libraryInfoRepo,
            AccountInfoRepo accountInfoRepo,
            Gson gson,
            ILog log
    ) {
        this.bookInfoRepo = bookInfoRepo;
        this.userInfoRepo = userInfoRepo;
        this.libraryInfoRepo = libraryInfoRepo;
        this.accountInfoRepo = accountInfoRepo;
        this.log = log;
        this.gson = gson;
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
            new BookInfoRepo(
                new BookInfoApi(),
                new BookInfoDatabase(),
                log
            ),
            new UserInfoRepo(log),
            new LibraryInfoRepo(log),
            new AccountInfoRepo(log),
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

    public BookInfoRepo bookInfoRepo() {
        return this.bookInfoRepo;
    }
    public UserInfoRepo userInfoRepo() {
        return this.userInfoRepo;
    }
    public LibraryInfoRepo libraryInfoRepo() {
        return this.libraryInfoRepo;
    }
    public AccountInfoRepo accountInfoRepo() {
        return this.accountInfoRepo;
    }
}