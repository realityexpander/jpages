package org.elegantobjects.jpages.LibraryApp.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.elegantobjects.jpages.LibraryApp.common.util.log.Log;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.network.BookInfoApi;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.local.BookInfoDatabase;
import org.elegantobjects.jpages.LibraryApp.common.util.log.ILog;
import org.elegantobjects.jpages.LibraryApp.domain.account.data.AccountInfoRepo;
import org.elegantobjects.jpages.LibraryApp.domain.common.IContext;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.BookInfoRepo;
import org.elegantobjects.jpages.LibraryApp.domain.library.data.LibraryInfoRepo;
import org.elegantobjects.jpages.LibraryApp.domain.user.data.UserInfoRepo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.elegantobjects.jpages.LibraryApp.domain.Context.ContextKind.PRODUCTION;

/**
 * Context is a singleton class that holds all the repositories and utility classes.<br>
 * <br>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

@SuppressWarnings("CommentedOutCode")
public class Context implements IContext {
    // static public Context INSTANCE = null;  // LEAVE for reference - Enforces singleton instance & allows global access

    // Repository Singletons
    private final BookInfoRepo bookInfoRepo;
    private final UserInfoRepo userInfoRepo;
    private final LibraryInfoRepo libraryInfoRepo;
    private final AccountInfoRepo accountInfoRepo;

    // Utility Singletons
    public final Gson gson;
    public final ILog log;

    public
    Context(
        @NotNull BookInfoRepo bookInfoRepo,
        @NotNull UserInfoRepo userInfoRepo,
        @NotNull LibraryInfoRepo libraryInfoRepo,
        @NotNull AccountInfoRepo accountInfoRepo,
        @NotNull Gson gson,
        @NotNull ILog log
    ) {
        this.bookInfoRepo = bookInfoRepo;
        this.userInfoRepo = userInfoRepo;
        this.libraryInfoRepo = libraryInfoRepo;
        this.accountInfoRepo = accountInfoRepo;
        this.log = log;
        this.gson = gson;
    }

    //////////////////////////////
    // Static Constructors      //
    //////////////////////////////

    public enum ContextKind {
        PRODUCTION,
        TEST
    }

    public static
    Context setupProductionInstance(@Nullable ILog log) {
        if (log == null)
            return setupInstance(PRODUCTION, new Log(), null);
        else
            return setupInstance(PRODUCTION, log, null);
    }
    public static
    Context setupInstance(
        @NotNull Context.ContextKind contextKind,
        @NotNull ILog log,
        @Nullable Context context
    ) {
        switch (contextKind) {
            case PRODUCTION:
                return Context.generateDefaultProductionContext(log);
                // LEAVE FOR REFERENCE: System.out.println("Context.setupInstance(): passed in Context is null, creating PRODUCTION Context");
            case TEST:
                System.out.println("Context.setupInstance(): contextType=TEST, using passed in Context");
                return context;
        }

        throw new RuntimeException("Context.setupInstance(): Invalid ContextType");
    }

    // Generate sensible default singletons for the PRODUCTION application
    private static
    Context generateDefaultProductionContext(@NotNull ILog log) {
        return new Context(
            new BookInfoRepo(
                new BookInfoApi(),
                new BookInfoDatabase(),
                log
            ),
            new UserInfoRepo(log),
            new LibraryInfoRepo(log),
            new AccountInfoRepo(log),
            new GsonBuilder()
                .registerTypeAdapter(UUID2.HashMap.class, new UUID2.Uuid2HashMapJsonDeserializer())
                .setPrettyPrinting()
                .create(),
            log
        );
    }

//    LEAVE FOR REFERENCE - This is how you would enforce a singleton instance using a static method & variable
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