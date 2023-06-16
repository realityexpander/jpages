package org.elegantobjects.jpages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

import static org.elegantobjects.jpages.Context.ContextType.*;

public final class App2 extends IOException {

    private static final long serialVersionUID = 0x7523L;

    interface Resource {
        Resource define(String name, String value);

        void printTo(Output output) throws IOException;
    }

    interface Output {
        void print(String name, String value) throws IOException;
    }

    private final Session session;

    public App2(Session session) {
        this.session = session;
    }

    static class Session {

        private Resource resource;
        private Map<String, String> params;

        public Session(Resource resource) {
            this.resource = resource;
        }

        String request(String request) {
            String[] lines = request.split("\r\n");

            params = parseRequest(lines);
            parseRequestMethodQueryAndProtocol(lines, params);
            populateResourceWith(params);

            // Make the request & return the response
            try {
                final StringBuilder builder = new StringBuilder();
                final StringBuilderOutput output1 = new StringBuilderOutput(builder);
                final StringOutput output = new StringOutput("");

                // Make the request
                resource.printTo(output);

                //return builder.toString();
                return output.toString();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public String toString() {
            return params.entrySet().stream()
                    .map(entry ->
                            entry.getKey() + "=" + entry.getValue())
                    .reduce("", (a, b) -> a + "\n" + b);
        }

        private Map<String, String> parseRequest(String[] lines) {
            Map<String, String> params = new HashMap<>();

            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    params.put(parts[0].trim(), parts[1].trim());
                }
            }

            return params;
        }

        private void parseRequestMethodQueryAndProtocol(
                String[] lines,
                Map<String, String> params
        ) {
            String[] parts = lines[0].split(" ");
            params.put("X-Method", parts[0]);
            params.put("X-Query", parts[1]);
            params.put("X-Protocol", parts[2]);
        }

        private void populateResourceWith(Map<String, String> params) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                resource.define(entry.getKey(), entry.getValue());
            }
        }
    }

    static class StringBuilderOutput implements Output {
        private StringBuilder builder = new StringBuilder();

        public StringBuilderOutput(StringBuilder builder) {
            this.builder = builder;
        }

        public String toString() {
            return this.builder.toString();
        }

        @Override
        public void print(final String name, final String value) {
            // For first line, add the status
            if (builder.length() == 0) {
                builder.append("HTTP/1.1 200 OK\r\n");
            }

            // If body, add blank line
            if (name.equals("X-Body")) {
                builder.append("\r\n")
                        .append(value);
            } else {
                // add a header
                builder.append(name)
                        .append(": ")
                        .append(value)
                        .append("\r\n");
            }
        }
    }

    static class StringOutput implements Output {
        private String string = "";

        public StringOutput(String string) {
            this.string = string;
        }


        public String toString() {
            return this.string;
        }

        @Override
        public void print(final String name, final String value) {
            // For first line, add the status
            if (string.length() == 0) {
                string += "HTTP/1.1 200 OK\r\n";
            }

            // If body, add blank line
            if (name.equals("X-Body")) {
                string += "\r\n" + value;
            } else {
                // add a header
                string += name + ": " + value + "\r\n";
            }
        }
    }


    // Start the server
    void start(int port) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(1000);

            // Handle a single request
            while (true) {
                try (Socket socket = server.accept()) {
                    if (Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    InputStream input = socket.getInputStream();
                    OutputStream output = socket.getOutputStream();
                    final byte[] buffer = new byte[1024];

                    // Get the request
                    int length = input.read(buffer);
                    String request = new String(Arrays.copyOfRange(buffer, 0, length));

                    // Make the request
                    String response = this.session.request(request);

                    // Send the response
                    output.write(response.getBytes());

                } catch (final SocketTimeoutException ex) {
                    break;
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                } catch (Exception e) {
                    throw e;
                }

            }
        }
    }
}


///////////////////////////////////

class Pair<T, U> {
    private final T first;
    private final U second;

    Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    T getFirst() {
        return first;
    }

    U getSecond() {
        return second;
    }
}

class HttpClient {
    private final String client;

    HttpClient(String client) {
        this.client = client;
    }

    public HttpClient() {
        this.client = "Mozilla/5.0";
    }
}

class URL {
    private final String url;

    URL(String url) {
        this.url = url;
    }
}

class Result<T> {
    static class Success<T> extends Result<T> {
        private final T value;

        Success(T value) {
            this.value = value;
        }

        public T value() {
            return value;
        }

        public String toString() {
            if (value == null)
                return "null";
            return value.toString();
        }
    }

    static class Failure<T> extends Result<T> {
        private final Exception exception;

        Failure(Exception exception) {
            this.exception = exception;
        }

        public Exception exception() {
            return exception;
        }

        public String toString() {
            if (exception == null)
                return "null";
            return exception.getLocalizedMessage();
        }
    }
}

// Simple Data Holder classes for UUID's
abstract class BaseUUID {
    public final UUID uuid;

    BaseUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String toString() {
        return uuid.toString();
    }

    public UUID toUUID() {
        return uuid;
    }

    public boolean equals(Object other) {
        if (other instanceof BookUUID) {
            return ((BookUUID) other).uuid.equals(uuid);
        }
        return false;
    }

    public int hashCode() {
        return uuid.hashCode();
    }

    public static BookUUID fromString(String uuid) {
        return new BookUUID(UUID.fromString(uuid));
    }
}
class BookUUID extends BaseUUID {
    BookUUID(UUID uuid) {
        super(uuid);
    }
}
class LibraryUUID extends BaseUUID {
    LibraryUUID(UUID uuid) {
        super(uuid);
    }
}
class UserUUID extends BaseUUID {
    UserUUID(UUID uuid) {
        super(uuid);
    }
}

interface IDatabase {
    Model.Entity.BookInfo getBookInfo(UUID id);
    Result<Model.Entity.BookInfo> updateBook(Model.Entity.BookInfo bookInfo);
    Result<Model.Entity.BookInfo> addBook(Model.Entity.BookInfo bookInfo);
    Result<Model.Entity.BookInfo> upsertBook(Model.Entity.BookInfo bookInfo);
    Result<Model.Entity.BookInfo> deleteBook(Model.Entity.BookInfo bookInfo);

}

// DB uses Entities
class InMemoryDatabase implements IDatabase {
    private final URL url;
    private final String user;
    private final String password;

    // Simulate a database
    private final HashMap<UUID, Model.Entity.BookInfo> database = new HashMap<>();  // Book id -> book

    InMemoryDatabase(URL url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }
    InMemoryDatabase() {
        this(new URL("memory://hash.map"), "admin", "password");
    }

    @Override
    public Model.Entity.BookInfo getBookInfo(UUID id) {
        // Simulate the request
        return database.get(id);
    }

    @Override
    public Result<Model.Entity.BookInfo> updateBook(Model.Entity.BookInfo bookInfo) {
        // Simulate the request
        if (database.put(bookInfo.id, bookInfo) == null) {
            return new Result.Failure<>(new Exception("DB: Failed to update book"));
        }

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<Model.Entity.BookInfo> addBook(Model.Entity.BookInfo bookInfo) {
        if (database.containsKey(bookInfo.id)) {
            return new Result.Failure<>(new Exception("DB: Book already exists"));
        }

        database.put(bookInfo.id, bookInfo);
        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<Model.Entity.BookInfo> upsertBook(Model.Entity.BookInfo bookInfo) {
        if (database.containsKey(bookInfo.id)) {
            return updateBook(bookInfo);
        } else {
            return addBook(bookInfo);
        }
    }

    @Override
    public Result<Model.Entity.BookInfo> deleteBook(Model.Entity.BookInfo bookInfo) {
        if (database.remove(bookInfo.id) == null) {
            return new Result.Failure<>(new Exception("DB: Failed to delete book"));
        }

        return new Result.Success<>(bookInfo);
    }

    public Map<UUID, Model.Entity.BookInfo> getAllBooks() {
        return new HashMap<>(database);
    }
}

interface IAPI {
    Result<Model.DTO.BookInfo> getBookInfo(UUID id);
    Result<Model.DTO.BookInfo> getBookInfo(String id);
    Result<Model.DTO.BookInfo> addBook(Model.DTO.BookInfo bookInfo);
    Result<Model.DTO.BookInfo> updateBook(Model.DTO.BookInfo bookInfo);
    Result<Model.DTO.BookInfo> upsertBook(Model.DTO.BookInfo bookInfo);
    Result<Model.DTO.BookInfo> deleteBook(Model.DTO.BookInfo bookInfo);
}

// API uses DTOs
class InMemoryAPI implements IAPI {
    private final URL url;
    private final HttpClient client;

    // Simulate an API database
    private final HashMap<UUID, Model.DTO.BookInfo> database = new HashMap<>(); // Book ID -> BookInfo

    InMemoryAPI(URL url, HttpClient client) {
        this.url = url;
        this.client = client;
    }
    InMemoryAPI() {
        this(
            new URL("http://localhost:8080"),
            new HttpClient()
        );
    }

    @Override
    public Result<Model.DTO.BookInfo> getBookInfo(String id) {
        return getBookInfo(UUID.fromString(id));
    }

    @Override
    public Result<Model.DTO.BookInfo> getBookInfo(UUID id) {
        // Simulate the request
        if (!database.containsKey(id)) {
            return new Result.Failure<>(new Exception("API: Book not found"));
        }

        return new Result.Success<>(database.get(id));
    }

    @Override
    public Result<Model.DTO.BookInfo> updateBook(Model.DTO.BookInfo bookInfo) {
        // Simulate the request
        if (database.put(bookInfo.id, bookInfo) == null) {
            return new Result.Failure<>(new Exception("API: Failed to update book"));
        }

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<Model.DTO.BookInfo> addBook(Model.DTO.BookInfo bookInfo) {
        if (database.containsKey(bookInfo.id)) {
            return new Result.Failure<>(new Exception("API: Book already exists"));
        }

        database.put(bookInfo.id, bookInfo);

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<Model.DTO.BookInfo> upsertBook(Model.DTO.BookInfo bookInfo) {
        if (database.containsKey(bookInfo.id)) {
            return updateBook(bookInfo);
        } else {
            return addBook(bookInfo);
        }
    }

    @Override
    public Result<Model.DTO.BookInfo> deleteBook(Model.DTO.BookInfo bookInfo) {
        if (database.remove(bookInfo.id) == null) {
            return new Result.Failure<>(new Exception("API: Failed to delete book"));
        }

        return new Result.Success<>(bookInfo);
    }

    public Map<UUID, Model.DTO.BookInfo> getAllBooks() {
        return new HashMap<>(database);
    }
}

interface IRepo {
    interface Book extends IRepo {
        Result<Model.Domain.BookInfo> fetchBookInfo(UUID id);
        Result<Model.Domain.BookInfo> addBook(Model.Domain.BookInfo bookInfo);
        Result<Model.Domain.BookInfo> updateBook(Model.Domain.BookInfo bookInfo);
        Result<Model.Domain.BookInfo> upsertBook(Model.Domain.BookInfo bookInfo);
    }

    interface User extends IRepo {
        Result<Model.Domain.UserInfo> fetchUserInfo(UUID id);
        Result<Model.Domain.UserInfo> updateUser(Model.Domain.UserInfo userInfo);
        Model.Domain.UserInfo upsertUser(Model.Domain.UserInfo userInfo);
    }

    interface Library extends IRepo {
        Result<Model.Domain.LibraryInfo> fetchLibraryInfo(UUID id);
        Result<Model.Domain.LibraryInfo> updateLibrary(Model.Domain.LibraryInfo libraryInfo);
        Result<Model.Domain.LibraryInfo> upsertLibrary(Model.Domain.LibraryInfo libraryInfo);
    }
}

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
class Repo implements IRepo {
    protected final Log log;

    Repo(Log log) {
        this.log = log;
    }

    static class Book extends Repo implements IRepo.Book {
        private final InMemoryAPI api;
        private final InMemoryDatabase database;

        Book() {
            this(
                new InMemoryAPI(new URL("ram://api.book.com"), new HttpClient()),
                new InMemoryDatabase(new URL("ram://db.book.com"), "user", "password"),
                new Log()
            );
        }

        Book(InMemoryAPI api, InMemoryDatabase database, Log log) {
            super(log);
            this.api = api;
            this.database = database;
        }

        @Override
        public Result<Model.Domain.BookInfo> fetchBookInfo(UUID id) {
            log.d(this,"Repo.Book.fetchBookInfo " + id);

            // Make the request to API
            Result<Model.DTO.BookInfo> bookInfoApiResult = api.getBookInfo(id);
            if (bookInfoApiResult instanceof Result.Failure) {

                // If API fails, try to get from cached DB
                Model.Entity.BookInfo bookInfo = database.getBookInfo(id);
                if (bookInfo == null) {
                    return new Result.Failure<>(new Exception("Book not found"));
                }

                return new Result.Success<>(bookInfo.toDomain());
            }

            // Convert to Domain Model
            Model.Domain.BookInfo bookInfo = ((Result.Success<Model.DTO.BookInfo>) bookInfoApiResult).value().toDomain();

            // Cache to Local DB
            Result<Model.Entity.BookInfo> resultDB = database.updateBook(bookInfo.toEntity());
            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Entity.BookInfo>) resultDB).exception();
                return new Result.Failure<Model.Domain.BookInfo>(exception);
            }

            return new Result.Success<>(bookInfo);
        }

        @Override
        public Result<Model.Domain.BookInfo> updateBook(Model.Domain.BookInfo bookInfo) {
            log.d(this,"Repo.Book - Updating BookInfo: " + bookInfo);

            Result<Model.Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateType.UPDATE);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Model.Domain.BookInfo> addBook(Model.Domain.BookInfo bookInfo) {
            log.d(this,"Repo.Book - Adding book info: " + bookInfo);

            Result<Model.Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateType.ADD);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Model.Domain.BookInfo> upsertBook(Model.Domain.BookInfo bookInfo) {
            log.d(this,"Repo.Book - Upserting book info: " + bookInfo);

            if (database.getBookInfo(bookInfo.id) != null) {
                return updateBook(bookInfo);
            } else {
                return addBook(bookInfo);
            }
        }

        ///////////////////////////////////
        // Private Helper Methods
        ///////////////////////////////////

        private enum UpdateType {
            ADD,
            UPDATE,
            UPSERT,
            DELETE
        }

        private Result<Model.Domain.BookInfo> saveBookToApiAndDB(
                Model.Domain.BookInfo bookInfo,
                UpdateType updateType
        ) {
            log.d(this,"Repo.Book - saveBookToApiAndDB, updateType: " + updateType + ", id: " + bookInfo.id);

            // Make the API request
            Result<Model.DTO.BookInfo> resultApi;
            switch (updateType) {
                case UPDATE:
                    resultApi = api.updateBook(bookInfo.toDTO());
                    break;
                case ADD:
                    resultApi = api.addBook(bookInfo.toDTO());
                    break;
                default:
                    return new Result.Failure<>(new Exception("UpdateType not supported"));
            }

            if (resultApi instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.DTO.BookInfo>) resultApi).exception();
                return new Result.Failure<>(exception);
            }

            // Save to Local DB
            Result<Model.Entity.BookInfo> resultDB;
            switch (updateType) {
                case UPDATE:
                    resultDB = database.updateBook(bookInfo.toEntity());
                    break;
                case ADD:
                    resultDB = database.addBook(bookInfo.toEntity());
                    break;
                default:
                    return new Result.Failure<>(new Exception("UpdateType not supported"));
            }

            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Entity.BookInfo>) resultDB).exception();
                return new Result.Failure<>(exception);
            }

            return new Result.Success<>(bookInfo);
        }


        ///////////////////////////////////
        // Debugging / Testing Methods
        ///////////////////////////////////
        public void populateDB() {
            for (int i = 0; i < 10; i++) {
                database.addBook(
                        new Model.Entity.BookInfo(
                                LibraryApp.createFakeUUID(i),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );
            }
        }

        public void populateAPI() {
            for (int i = 0; i < 10; i++) {
                Result<Model.DTO.BookInfo> result = api.addBook(
                        new Model.DTO.BookInfo(LibraryApp.createFakeUUID(i),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );

                if (result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<Model.DTO.BookInfo>) result).exception();
                    log.d(this,exception.getMessage());
                }
            }
        }

        public void printDB() {
            for (Map.Entry<UUID, Model.Entity.BookInfo> entry : database.getAllBooks().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }

        public void printAPI() {
            for (Map.Entry<UUID, Model.DTO.BookInfo> entry : api.getAllBooks().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    static class User extends Repo implements IRepo.User {
        // Simulate a database on a server somewhere
        private final HashMap<UUID, Model.Domain.UserInfo> database = new HashMap<>(); // User ID -> UserInfo

        User(Log log) {
            super(log);
        }

        @Override
        public Result<Model.Domain.UserInfo> fetchUserInfo(UUID id) {
            log.d(this,"Repo.User - Fetching user info: " + id);

            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        @Override
        public Result<Model.Domain.UserInfo> updateUser(Model.Domain.UserInfo userInfo) {
            log.d(this,"Repo.User - Updating user info: " + userInfo);

            if (database.containsKey(userInfo.id)) {
                database.put(userInfo.id, userInfo);
                return new Result.Success<>(userInfo);
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        @Override
        public Model.Domain.UserInfo upsertUser(Model.Domain.UserInfo userInfo) {
            database.put(userInfo.id, userInfo);
            return userInfo;
        }

    }

    static class Library extends Repo implements IRepo.Library {
        // simulate a database on server
        private final HashMap<UUID, Model.Domain.LibraryInfo> database = new HashMap<>(); // Library id -> LibraryInfo

        Library(Log log) {
            super(log);
        }

        @Override
        public Result<Model.Domain.LibraryInfo> fetchLibraryInfo(UUID id) {
            log.d(this,"Repo.Library - Fetching library info: " + id);

            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Library not found, id: " + id));
        }

//        @Override
//        public Model.Domain.Library libraryInfo(UUID id) {
//            return database.get(id);
//        }

        @Override
        public Result<Model.Domain.LibraryInfo> updateLibrary(Model.Domain.LibraryInfo libraryInfo) {
            log.d(this,"Repo.Library - Updating library info: " + libraryInfo);

            if (database.containsKey(libraryInfo.id)) {
                database.put(libraryInfo.id, libraryInfo);

                return new Result.Success<>(libraryInfo);
            }

            return new Result.Failure<>(new Exception("Library not found"));
        }

        @Override
        public Result<Model.Domain.LibraryInfo> upsertLibrary(Model.Domain.LibraryInfo libraryInfo) {
            log.d(this,"Repo.Library - Upserting library info: " + libraryInfo);

            database.put(libraryInfo.id, libraryInfo);

            return new Result.Success<>(libraryInfo);
        }

        /// Helper methods /// // todo move out of this class?
        public void populateWithFakeBooks(UUID libraryId, int numberOfBooksToCreate) {
            for (int i = 0; i < numberOfBooksToCreate; i++) {
                database.get(libraryId)
                        .bookIdToNumBooksAvailableMap
                        .put(LibraryApp.createFakeUUID(i), 1 /* number on hand */);
            }
        }

    }
}

interface ILog {
    void d(String tag, String msg);
    void e(String tag, String msg);
    void e(String tag, String msg, Exception e);
}

class Log implements ILog {

    public void d(String tag, String msg) {
        System.out.println(tag + ": " + msg);
    }
    public void e(String tag, String msg) {
        System.err.println(tag + ": " + msg);
    }
    public void e(String tag, String msg, Exception e) {
        System.err.println(tag + ": " + msg);
        e.printStackTrace();
    }

    // log.dm(this, "message") will print "ClassName: message"
    public void d(Object obj, String msg) {
        d(obj.getClass().getSimpleName(), msg);
    }
}

interface IContext {
    Repo.Book bookRepo = null;
    Repo.User userRepo = null;
    Repo.Library libraryRepo = null;
    Gson gson = null;
    Log log = null;
}

// Context is a singleton class that holds all the repositories and global objects like Gson
class Context implements IContext {
    // static public Context INSTANCE = null;  // Enforces singleton instance & allows global access, LEAVE for reference

    // Repository Singletons
    private Repo.Book bookRepo = null;
    private Repo.User userRepo = null;
    private Repo.Library libraryRepo = null;

    // Utility Singletons
    protected Gson gson = null;
    public Log log = null;

    public enum ContextType {
        PRODUCTION,
        TEST
    }

    Context(
            Repo.Book bookRepo,
            Repo.User userRepo,
            Repo.Library libraryRepo,
            Gson gson,
            Log log) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.libraryRepo = libraryRepo;
        this.gson = gson;
        this.log = log;
    }

    public static Context setupProductionInstance() {
        return setupInstance(PRODUCTION, null);
    }
    public static Context setupInstance(ContextType contextType, Context context) {
        switch (contextType) {
            case PRODUCTION:
                System.out.println("Context.setupInstance(): passed in Context is null, creating PRODUCTION Context");
                return Context.generateProductionDefaultContext();
            case TEST:
                System.out.println("Context.setupInstance(): using passed in Context");
                return context;
        }

        throw new RuntimeException("Context.setupInstance(): Invalid ContextType");
    }

    // Generate sensible default singletons for the production application
    private static Context generateProductionDefaultContext() {
        Log log = new Log();
        return new Context(
            new Repo.Book(
                new InMemoryAPI(
                        new URL("http://localhost:8080"),
                        new HttpClient("Apache")
                ),
                new InMemoryDatabase(new URL("http://localhost:16078"),
                        "root",
                        "password"
                ),
                log
            ),
            new Repo.User(log),
            new Repo.Library(log),
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

    public Repo.Book bookRepo() {
        return this.bookRepo;
    }
    public Repo.User userRepo() {
        return this.userRepo;
    }
    public Repo.Library libraryRepo() {
        return this.libraryRepo;
    }
}

// These hold the "Info" for each App Domain Object. (like a DTO for a database row)
class Model {
    UUID id;

    static class Domain extends Model {

        static class BookInfo extends Domain implements ToEntity<Entity.BookInfo>, ToDTO<DTO.BookInfo> {
            final String title;
            final String author;
            final String description;

            BookInfo(String id, String title, String author, String description) {
                this(UUID.fromString(id), title, author, description);
            }
            BookInfo(UUID id, String title, String author, String description) {
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }

            public String toString() {
                return "Book " +
                        "(" + this.id + ") " +
                        ": " + this.title + " by " + this.author;
            }

            public DTO.BookInfo toDTO() {
                return new DTO.BookInfo(this.id, this.title, this.author, this.description);
            }

            public Entity.BookInfo toEntity() {
                return new Entity.BookInfo(this.id, this.title, this.author, this.description);
            }
        }

        static class UserInfo extends Domain {
            final String name;
            final String email;
            final ArrayList<UUID> acceptedBooks = new ArrayList<>();

            UserInfo(UUID id, String name, String email) {
                this.id = id;
                this.name = name;
                this.email = email;
            }

            public String toString() {
                return "User: " + this.name + " (" + this.email + "), acceptedBooks: " + this.acceptedBooks;
            }
        }

        static class LibraryInfo extends Domain {
            final private String name;
            final HashMap<UUID, ArrayList<UUID>> userIdToCheckedOutBookMap; // User ID -> Book IDs
            final HashMap<UUID, Integer> bookIdToNumBooksAvailableMap; // Book ID -> Number of books available

            LibraryInfo(UUID id,
                        String name,
                        HashMap<UUID, ArrayList<UUID>> checkoutUserBookMap,
                        HashMap<UUID, Integer> bookIdToNumBooksAvailableMap
            ) {
                this.id = id;
                this.name = name;
                this.userIdToCheckedOutBookMap = checkoutUserBookMap;
                this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
            }
            LibraryInfo(UUID id, String name) {
                this.id = id;
                this.name = name;
                this.userIdToCheckedOutBookMap = new HashMap<>();
                this.bookIdToNumBooksAvailableMap = new HashMap<>();
            }

            public void AddBook(UUID bookId) {
                if (this.bookIdToNumBooksAvailableMap.containsKey(bookId)) {
                    this.bookIdToNumBooksAvailableMap.put(bookId, this.bookIdToNumBooksAvailableMap.get(bookId) + 1);
                } else {
                    this.bookIdToNumBooksAvailableMap.put(bookId, 1);
                }
            }

            public String toString() {
                return this.toPrettyJson();
//                return "Library: " + this.name + " (" + this.id + ")" + "\n" +
//                        "  Available Books: " + this.bookIdToNumBooksAvailableMap + "\n" +
//                        "  Checkout Map: " + this.userIdToCheckedOutBookMap;
            }
        }
    }

    // Data Transfer Objects for API
    static class DTO extends Model {
        static class BookInfo extends DTO implements ToDomain<Domain.BookInfo> {
            final String title;
            final String author;
            final String description;

            BookInfo(UUID id, String title, String author, String description) {
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }

            public String toString() {
                return "Book: " + this.title + " by " + this.author;
            }

            public Domain.BookInfo toDomain() {
                return new Domain.BookInfo(this.id, this.title, this.author, this.description);
            }
        }
    }

    // Entities for the Database
    static class Entity extends Model {
        static class BookInfo extends Entity implements ToDomain<Domain.BookInfo> {
            final String title;
            final String author;
            final String description;

            BookInfo(UUID id, String title, String author, String description) {
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }

            public String toString() {
                return "Book: " + this.title + " by " + this.author;
            }

            public Domain.BookInfo toDomain() {
                return new Domain.BookInfo(this.id.toString(), this.title, this.author, this.description);
            }
        }
    }

    public String toPrettyJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    interface ToDomain<T extends Model.Domain> {
        T toDomain();
    }

    interface ToEntity<T extends Model.Entity> {
        T toEntity();
    }

    interface ToDTO<T extends Model.DTO> {
        T toDTO();
    }
}

// Info - Caches the info and provides methods to fetch and update the info
interface Info<T extends Model.Domain> {
    T fetchInfo();                  // Fetches info for object from server
    boolean isInfoFetched();        // Returns true if info has been fetched from server
    Result<T> fetchInfoResult();    // Fetches Result<T> for info object from server
    Result<T> updateInfo(T info);   // Updates info for object to server
    Result<T> refreshInfo();        // Refreshes info for object from server
    String fetchInfoFailureReason();// Returns reason for failure of last fetchInfo() call, or null if successful
}

abstract class IDomainObject<T extends Model.Domain> implements Info<T> {
    UUID id;
    protected T info;
    protected Result<T> infoResult = null;

    // Singletons
    protected Context context = null;
    private Gson gson = null; // convenience reference to the context's Gson object

    // Class of the info<T> (for GSON serialization)
    @SuppressWarnings("unchecked")
    Class<T> infoClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];

    IDomainObject(T info, Context context) {
        if(context == null) {
            this.context = Context.setupProductionInstance();
            // throw new IllegalArgumentException("Context cannot be null"); // alternative to setting up a production instance
        } else {
            this.context = context;
        }
        // this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation
        this.gson = this.context.gson;
        this.info = info;
        this.id = info.id;
    }
    IDomainObject(UUID id, Context context) {
        this.context = context;
        // this.context = Context.setupINSTANCE(context);  // left for reference for static context instance
        this.gson = this.context.gson;
        this.id = id;
    }
    IDomainObject(String json, Context context) {
        this.context = context;
        // this.context = Context.setupINSTANCE(context);  // left for reference for static context instance
        this.gson = this.context.gson;
        this.info = this.gson.fromJson(json, this.infoClass);
        this.id = this.info.id;
    }
    IDomainObject(Context context) {
        this(UUID.randomUUID(), context);
    }
    IDomainObject(String json) {
        this(json, null);
    }
    IDomainObject(T info) {
        this(info, null);
    }
    IDomainObject(UUID id) { this(id, null);}
    IDomainObject() {
        this(UUID.randomUUID(), null);
    }

    // To be Implemented by subclasses
    public Result<T> fetchInfoResult() {
        return new Result.Failure<>(new Exception("Not Implemented, should be implemented in subclass"));
    }

    // To be Implemented by subclasses
    public Result<T> updateInfo(T info) {
        return new Result.Failure<>(new Exception("Not Implemented, should be implemented in subclass"));
    }


    //////////////////////////////
    // Info<T> interface methods - contains the logic for fetching and updating info to/from server

    public T fetchInfo() {
        if (isInfoFetched()) {
            return this.info;
        }

        Result<T> result = this.fetchInfoResult();
        if (result instanceof Result.Failure) {
            context.log.d(this,"Failed to get info for " +
                    "class: " + this.getClass().getName() + ", " +
                    "id: " + this.id.toString());
            return null;
        }

        this.info = ((Result.Success<T>) result).value();
        return this.info;
    }

    public String fetchInfoFailureReason() {
        if (!isInfoFetched()) {
            if (fetchInfoResult() instanceof Result.Failure) {
                return ((Result.Failure<T>) fetchInfoResult()).exception().getMessage();
            }
        }

        return null; // Returns `null` if the info has been fetched successfully. This makes the call site smaller.
    }

    public boolean isInfoFetched() {
        return this.info != null;
    }

    public Result<T> refreshInfo() {
        context.log.d(this,"Refreshing info for " +
                "class: " + this.getClass().getName() + ", " +
                "id: " + this.id.toString());

        this.info = null;
        return this.fetchInfoResult();
    }
}

abstract class DomainObject<T extends Model.Domain> extends IDomainObject<T> {

    public DomainObject(Context context) {
        super(context);
    }

    public Result<T> updateInfo(T info) {
        this.info = info;
        return new Result.Success<>(info);
    }

    @SuppressWarnings("unchecked")
    public Result<T> updateInfoFromJson(String json) {
        try {
            Class<?> infoClass = this.infoClass;
            Object infoFromJson = this.context.gson.fromJson(json, infoClass);
            assert infoFromJson.getClass() == this.info.getClass();

            this.info = (T) infoFromJson;

            return this.updateInfo(info);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }
    }

    public String toPrettyJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this.fetchInfo());
    }

    public String toJson() {
        return this.context.gson.toJson(this.fetchInfo());
    }

    public String toString() {
        String infoString = this.info == null ? "null" : this.info.toString();
        String nameOfClass = this.getClass().getName();
        return nameOfClass + ": " + this.id.toString() + ", info=" + infoString;
    }
}

class Book extends DomainObject<Model.Domain.BookInfo> {
    private Repo.Book repo = null;

    Book() {
        this(UUID.randomUUID());
    }
    Book(UUID id) {
        this(id, null);
    }
    Book(UUID id, Context context) {
        super(context);
        this.repo = this.context.bookRepo();
        this.id = id;

        context.log.d(this, "Book (" + this.id.toString() + ") created");
    }

    @Override
    public Result<Model.Domain.BookInfo> fetchInfoResult() {
        infoResult = this.repo.fetchBookInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Model.Domain.BookInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<Model.Domain.BookInfo> updateInfo(Model.Domain.BookInfo info) {
        // Update self optimistically
        this.info = info;

        // Update the repo
        Result<Model.Domain.BookInfo> infoResult = this.repo.updateBook(info);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<Model.Domain.BookInfo>) infoResult).value();
        return infoResult;
    }
}

class User extends DomainObject<Model.Domain.UserInfo> {
    private Repo.User repo = null;

    User() {
        this(UUID.randomUUID());
    }
    User(UUID id) {
        this(id, null);
    }
    User(UUID id, Context context) {
        super(context);
        this.repo = this.context.userRepo();
        this.id = id;

        context.log.d(this,"User (" + this.id.toString() + ") created");
    }

    @Override
    public Result<Model.Domain.UserInfo> fetchInfoResult() {
        infoResult = this.repo.fetchUserInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Model.Domain.UserInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public Result<Model.Domain.UserInfo> updateInfo(Model.Domain.UserInfo userInfo) {
        // Update self optimistically
        this.info = userInfo;

        // Update the repo
        Result<Model.Domain.UserInfo> infoResult = this.repo.updateUser(userInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<Model.Domain.UserInfo>) infoResult).value();
        return infoResult;
    }

    public Result<ArrayList<UUID>> receiveBook(@NotNull Book book) {
        context.log.d(this,"User (" + this.id.toString() + ") - receiveBook,  book: " + book.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check user has already accepted book
        if (this.info.acceptedBooks.contains(book.id)) {
            return new Result.Failure<>(new Exception("User has already accepted book"));
        }

        // Accept book
        this.info.acceptedBooks.add(book.id);

        // Update user
        Result<Model.Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.UserInfo>) result).exception());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }

    public Result<ArrayList<UUID>> returnBook(Book book) {
        context.log.d(this,"User (" + this.id.toString() + ") - returnBook,  book: " + book.id.toString() + " to user: " + this.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check user has accepted book
        if (!this.info.acceptedBooks.contains(book.id)) {
            return new Result.Failure<>(new Exception("User has not accepted book"));
        }

        // Remove the Returned book
        this.info.acceptedBooks.remove(book.id);

        // Update UserInfo
        Result<Model.Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.UserInfo>) result).exception());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }

    public Result<ArrayList<UUID>> checkoutBookFromLibrary(
            Book book,
            Library library
    ) {
        context.log.d(this,"User (" + this.id.toString() + ") - checkoutBookFromLibrary, book: " + book.id.toString() + ", library: " + library.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<Book> bookResult = library.checkOutBookToUser(book, this);
        if (bookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) bookResult).exception());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }
}

class Library extends DomainObject<Model.Domain.LibraryInfo> {
    private Repo.Library repo = null;

    Library() {
        this(UUID.randomUUID());
    }
    Library(UUID id) {
        this(id, null);
    }
    Library(Context context) {
        this(UUID.randomUUID(), context);
    }
    Library(UUID id, Context context) {
        super(context);
        this.repo = this.context.libraryRepo();
        this.id = id;

        context.log.d(this,"Library (" + this.id.toString() + ") created");
    }

    @Override
    public Result<Model.Domain.LibraryInfo> fetchInfoResult() {
        infoResult = this.repo.fetchLibraryInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Model.Domain.LibraryInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<Model.Domain.LibraryInfo> updateInfo(Model.Domain.LibraryInfo newInfo) {
        // Update self optimistically
        this.info = newInfo;

        // Update the repo
        Result<Model.Domain.LibraryInfo> infoResult = this.repo.updateLibrary(newInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<Model.Domain.LibraryInfo>) infoResult).value();
        return infoResult;
    }

    public Result<Book> checkOutBookToUser(@NotNull Book book, @NotNull User user) {
        System.out.printf("Library (%s) - checkOutBookToUser, user: %s, book: %s\n", this.id.toString(), user.id.toString(), book.id.toString());

        // Refresh the repo
//        this.refreshInfo();
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + user.id));
        }

        // Check if book exists
        if (!this.info.bookIdToNumBooksAvailableMap.containsKey(book.id)) {
            return new Result.Failure<>(new Exception("Book not found, id: " + book.id));
        }

        // Check if book is available
        if (this.info.bookIdToNumBooksAvailableMap.get(book.id).equals(0)) {
            return new Result.Failure<>(new Exception("Book is not available, id: " + book.id));
        }

        // Check if user has already checked out book
        if (this.info.userIdToCheckedOutBookMap.get(user.id).contains(book.id)) {
            return new Result.Failure<>(new Exception("User has already checked out book, id: " + book.id));
        }

        // Add remove Book from Library and add Book to User
        try {
            this.info.bookIdToNumBooksAvailableMap.put(book.id, this.info.bookIdToNumBooksAvailableMap.get(book.id) - 1); // decrement available books
            this.info.userIdToCheckedOutBookMap.get(user.id).add(book.id);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        // Update the repo
        Result<Model.Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.LibraryInfo>) updateInfoResult).exception());
        }

        // Make User accept Book
        Result<ArrayList<UUID>> acceptedBookIds = user.receiveBook(book);
        if (acceptedBookIds instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID>>) acceptedBookIds).exception());
        }

        return new Result.Success<>(book);
    }

    public Result<Book> checkInBookFromUser(Book book, User user) {
        System.out.printf("Library (%s) - checkInBookFromUser, book %s from user %s\n", this.id, book.id, user.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + user.id));
        }

        // Check if user has not checked out book
        if (!this.info.userIdToCheckedOutBookMap.get(user.id).contains(book.id)) {
            return new Result.Failure<>(new Exception("User has not checked out book, id: " + book.id));
        }

        // Update the Library
        this.info.userIdToCheckedOutBookMap.get(user.id).remove(book.id);
        this.info.bookIdToNumBooksAvailableMap.put(book.id, this.info.bookIdToNumBooksAvailableMap.get(book.id) + 1);

        // Make user return book
        Result<ArrayList<UUID>> result = user.returnBook(book);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID>>) result).exception());
        }

        return new Result.Success<>(book);
    }

    public Result<User> upsertUser(User user) {
        context.log.d(this,"Library (" + this.id + ") - upsertUser id: " + user.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (!isUserKnown(user)) {
            try {
                // Create new user entry
                this.info.userIdToCheckedOutBookMap.put(user.id, new ArrayList<>());
                return new Result.Success<>(user);
            } catch (Exception e) {
                return new Result.Failure<>(e);
            }
        }

        return new Result.Success<>(user);
    }

    public boolean isUnableToFindOrAddUser(User user) {
        System.out.printf("Library (%s) - isUnableToFindOrAddUser %s\n", this.id, user.id);
        if (fetchInfoFailureReason() != null) return true;

        if (isUserKnown(user)) {
            return false;
        }

        // Create a new User entry in the Library
        Result<User> upsertUserResult = upsertUser(user);
        if (upsertUserResult instanceof Result.Failure) {
            return true;
        }

        return false;
    }

    public boolean isBookKnown(Book book) {
        System.out.printf("Library(%s) - hasBook %s\n", this.id, book.id);
        if (fetchInfoFailureReason() != null) return false;

        return this.info.bookIdToNumBooksAvailableMap.containsKey(book.id);
    }

    public boolean isUserKnown(User user) {
        System.out.printf("Library (%s) - isKnownUser %s\n", this.id, user.id);
        if (fetchInfoFailureReason() != null) return false;

        return this.info.userIdToCheckedOutBookMap.containsKey(user.id);
    }

    public boolean isBookAvailable(Book book) {
        System.out.printf("Library (%s) - hasBookAvailable %s\n", this.id, book.id);
        if (fetchInfoFailureReason() != null) return false;

        return this.info.bookIdToNumBooksAvailableMap.containsKey(book.id) &&
                this.info.bookIdToNumBooksAvailableMap.get(book.id) > 0;
    }

    public Result<Book> upsertAvailableBook(Book book, Integer count) {
        System.out.printf("Library (%s) - upsertAvailableBook count: %s, book: %s\n", this.id, count, book);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (count <= 0) {
            return new Result.Failure<>(new Exception("Count must be greater than 0"));
        }

        // Check if book exists
        if (!this.info.bookIdToNumBooksAvailableMap.containsKey(book.id)) {
            // Create new book entry
            this.info.bookIdToNumBooksAvailableMap.put(book.id, count);
        }

        return new Result.Success<>(book);
    }

    public Result<ArrayList<Book>> findBooksCheckedOutByUser(User user) {
        System.out.printf("Library (%s) - findBooksCheckedOutByUser %s\n", this.id, user);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Make sure User is Known
        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + user.id));
        }

        // Generate list of Books checked out by User
        ArrayList<UUID> bookIds = new ArrayList<>(this.info.userIdToCheckedOutBookMap.get(user.id));
        ArrayList<Book> books = new ArrayList<>();
        for (UUID bookId : bookIds) {
            books.add(new Book(bookId, context));
        }

        return new Result.Success<>(books);
    }

    public Result<ArrayList<Pair<UUID, Integer>>> calculateAvailableBookIdToNumberAvailableList() {
        System.out.print("Library (" + this.id + ") - calculateAvailableBooksAndAmountOnHand\n");
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        ArrayList<Pair<UUID, Integer>> availableBooks = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : this.info.bookIdToNumBooksAvailableMap.entrySet()) {
            if (entry.getValue() > 0) {
                availableBooks.add(new Pair<>(entry.getKey(), entry.getValue()));
            }
        }

        return new Result.Success<>(availableBooks);
    }

    public void DumpDB(Context context) {
        context.log.d(this,"\nDumping Library DB:");
        context.log.d(this,this.toPrettyJson());
        context.log.d(this,"\n");
    }
}

class LibraryApp {

    public static void main(final String... args) {

        // Setup App Context Object singletons
        Context productionContext = Context.setupProductionInstance();
        // Context productionContext = Context.setupInstance(TEST, testContext); // for testing

        new LibraryApp(productionContext);
    }

    LibraryApp(Context ctx) {

        //context = Context.setupINSTANCE(context);  // For implementing a static Context. LEAVE for reference

        Populate_And_Poke_Book:
        {
            ctx.log.d(this,"Populating Book DB and API");
            PopulateBookDBandAPI(ctx);

            // Create a book object (it only has an id)
            Book book = new Book(LibraryApp.createFakeUUID(1), ctx);
            ctx.log.d(this,book.fetchInfoResult().toString());

            // Update info for a book
            final Result<Model.Domain.BookInfo> bookInfoResult =
                    book.updateInfo(
                            new Model.Domain.BookInfo(
                                    book.id,
                                    "The Updated Title",
                                    "The Updated Author",
                                    "The Updated Description"
                            ));
            ctx.log.d(this,book.fetchInfoResult().toString());

            // Get the bookInfo (null if not loaded)
            Model.Domain.BookInfo bookInfo3 = book.fetchInfo();
            if (bookInfo3 == null) {
                ctx.log.d(this,"Book Missing --> " +
                        "book id: " + book.id + " >> " +
                        " is null"
                );
            } else {
                ctx.log.d(this,"Book Info --> " +
                        bookInfo3.id + " >> " +
                        bookInfo3.title + ", " +
                        bookInfo3.author
                );
            }

            // Try to get a book id that doesn't exist
            Book book2 = new Book(LibraryApp.createFakeUUID(99), ctx);
            if (book2.fetchInfoResult() instanceof Result.Failure) {
                ctx.log.d(this,"Get Book FAILURE --> " +
                        "book id: " + book2.id + " >> " +
                        ((Result.Failure<Model.Domain.BookInfo>) book2.fetchInfoResult())
                );
            } else {
                ctx.log.d(this,"Book ERxists --> " +
                        ((Result.Success<Model.Domain.BookInfo>) book2.fetchInfoResult()).value()
                );
            }

            DumpBookDBandAPI(ctx);
        }

        Populate_the_library_and_user_DBs:
        {
            ////////////////////////////////////////
            // Setup DB & API simulated resources //
            ////////////////////////////////////////

            // Create & populate a Library in the Library Repo
            final Result<Model.Domain.LibraryInfo> libraryInfo = createFakeLibraryInfoInContextLibraryRepo(1, ctx);
            if (libraryInfo instanceof Result.Failure) {
                ctx.log.d(this,"Create Library FAILURE --> " +
                        ((Result.Failure<Model.Domain.LibraryInfo>) libraryInfo)
                );

                break Populate_the_library_and_user_DBs;
            }
            UUID libraryInfoId = ((Result.Success<Model.Domain.LibraryInfo>) libraryInfo).value().id;
            ctx.log.d(this,"Library Created --> " +
                    ((Result.Success<Model.Domain.LibraryInfo>) libraryInfo).value()
            );

            // Populate the library
            ctx.libraryRepo()
                    .populateWithFakeBooks(libraryInfoId, 10);

            // Create & populate a User in the User Repo
            final Model.Domain.UserInfo userInfo = createFakeUserInfoInContextUserRepo(1, ctx);

            //////////////////////////////////
            // Actual App functionality     //
            //////////////////////////////////

            // Create the App objects
            final User user1 = new User(userInfo.id, ctx);
            final Library library1 = new Library(libraryInfoId, ctx);
            final Book book1 = new Book(createFakeUUID(1), ctx);
            final Book book2 = new Book(createFakeUUID(2), ctx);

            Checkout_2_books_to_a_user:
            {
                ctx.log.d(this,"\nChecking out 2 books to user " + user1.id);

                final Result<Book> result = library1.checkOutBookToUser(book1, user1);
                if (result instanceof Result.Failure) {
                    ctx.log.d(this,"Checked out book FAILURE--> " +
                            ((Result.Failure<Book>) result).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Checked out book SUCCESS --> " +
                            ((Result.Success<Book>) result).value()
                    );
                }

                final Result<Book> result2 = library1.checkOutBookToUser(book2, user1);
                if (result2 instanceof Result.Failure) {
                    ctx.log.d(this,"Checked out book FAILURE--> " +
                            ((Result.Failure<Book>) result2).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Checked out book SUCCESS --> " +
                            ((Result.Success<Book>) result2).value()
                    );
                }

                library1.DumpDB(ctx);
            }

            Get_Available_Books_And_Counts_In_Library:
            {
                ctx.log.d(this,"\nGetting available books and counts in library:");

                final Result<ArrayList<Pair<UUID, Integer>>> availableBookIdCounts =
                        library1.calculateAvailableBookIdToNumberAvailableList();
                if (availableBookIdCounts instanceof Result.Failure) {
                    ctx.log.d(this,"AvailableBookIdCounts FAILURE! --> " +
                            ((Result.Failure<ArrayList<Pair<UUID, Integer>>>) availableBookIdCounts)
                                    .exception().getMessage()
                    );

                    break Get_Available_Books_And_Counts_In_Library;
                }

                // create objects and populate info for available books
                assert availableBookIdCounts instanceof Result.Success;
                final ArrayList<Pair<UUID, Integer>> availableBooks =
                        ((Result.Success<ArrayList<Pair<UUID, Integer>>>) availableBookIdCounts).value();

                // Print out available books
                ctx.log.d(this,"\nAvailable Books in Library:");
                for (Pair<UUID, Integer> bookIdCount : availableBooks) {
                    final Book book3 = new Book(bookIdCount.getFirst(), ctx);

                    final Result<Model.Domain.BookInfo> bookInfoResult = book3.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        ctx.log.d(this,
                                "Book Error: " +
                                        ((Result.Failure<Model.Domain.BookInfo>) bookInfoResult)
                                                .exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,
                                ((Result.Success<Model.Domain.BookInfo>) bookInfoResult).value() +
                                        " >> count: " + bookIdCount.getSecond()
                        );
                    }
                }
                ctx.log.d(this,"Total Available Books (unique UUIDs): " + availableBooks.size());
                ctx.log.d(this,"\n");
            }

            Get_books_checked_out_by_user:
            {
                ctx.log.d(this,"\nGetting books checked out by user " + user1.id);

                final Result<ArrayList<Book>> checkedOutBooksResult = library1.findBooksCheckedOutByUser(user1);
                if (checkedOutBooksResult instanceof Result.Failure) {
                    ctx.log.d(this,"OH NO! --> " +
                            ((Result.Failure<ArrayList<Book>>) checkedOutBooksResult)
                                    .exception().getMessage()
                    );
                }

                assert checkedOutBooksResult instanceof Result.Success;
                ArrayList<Book> checkedOutBooks = ((Result.Success<ArrayList<Book>>) checkedOutBooksResult).value();

                // Print checked out books
                ctx.log.d(this,"\nChecked Out Books for User [" + user1.fetchInfo().name + ", " + user1.id + "]:");
                for (Book book : checkedOutBooks) {
                    final Result<Model.Domain.BookInfo> bookInfoResult = book.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        ctx.log.d(this,
                                "Book Error: " +
                                        ((Result.Failure<Model.Domain.BookInfo>) bookInfoResult)
                                                .exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,
                                ((Result.Success<Model.Domain.BookInfo>) bookInfoResult).value().toString()
                        );
                    }
                }
                System.out.print("\n");
            }

            Check_In_the_Book_from_the_User_to_the_Library:
            {
                ctx.log.d(this,"\nCheck in book " + book1.id + " from user " + user1.id);

                final Result<Book> checkInBookResult = library1.checkInBookFromUser(book1, user1);
                if (checkInBookResult instanceof Result.Failure) {
                    ctx.log.d(this,"Check In book FAILURE --> book id:" +
                            ((Result.Failure<Book>) checkInBookResult).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Returned Book SUCCESS --> book id:" +
                            ((Result.Success<Book>) checkInBookResult).value()
                    );
                }

                library1.DumpDB(ctx);
            }

            // Load Library from Json
            if (false) {
                ctx.log.d(this,"\nLibrary Json:");
                ctx.log.d(this,library1.toPrettyJson());

                Library library2 = new Library(ctx);
                Result<Model.Domain.LibraryInfo> library2Result = library2.updateInfoFromJson(
                        "{\n" +
                                "  \"name\": \"Library 99\",\n" +
                                "  \"checkoutMap\": {\n" +
                                "  },\n" +
                                "  \"availableBooks\": {\n" +
                                "    \"00000000-0000-0000-0000-000000000010\": 1,\n" +
                                "    \"00000000-0000-0000-0000-000000000011\": 1,\n" +
                                "    \"00000000-0000-0000-0000-000000000012\": 0,\n" +
                                "    \"00000000-0000-0000-0000-000000000013\": 1,\n" +
                                "    \"00000000-0000-0000-0000-000000000014\": 1,\n" +
                                "    \"00000000-0000-0000-0000-000000000015\": 1,\n" +
                                "    \"00000000-0000-0000-0000-000000000016\": 1,\n" +
                                "    \"00000000-0000-0000-0000-000000000017\": 1,\n" +
                                "    \"00000000-0000-0000-0000-000000000018\": 1,\n" +
                                "    \"00000000-0000-0000-0000-000000000019\": 1\n" +
                                "  },\n" +
                                "  \"id\": \"00000000-0000-0000-0000-000000000099\"\n" +
                                "}"
                );
                ctx.log.d(this,"\nLibrary2:");
                ctx.log.d(this,library2.toPrettyJson());
            }

            Check_out_Book_via_User:
            {
                final User user2 = new User(createFakeUserInfoInContextUserRepo(2, ctx).id, ctx);
                final Result<Model.Domain.BookInfo> book12Result = addFakeBookInfoInContextBookRepo(12, ctx);

                if (book12Result instanceof Result.Failure) {
                    ctx.log.d(this,"Book Error: " +
                            ((Result.Failure<Model.Domain.BookInfo>) book12Result).exception().getMessage()
                    );
                } else {

                    final UUID book12id = ((Result.Success<Model.Domain.BookInfo>) book12Result).value().id;
                    final Book book12 = new Book(book12id, ctx);

                    ctx.log.d(this,"\nCheck out book " + book12id + " to user " + user2.id);

                    final Result<Book> book12UpsertResult = library1.upsertAvailableBook(book12, 1);
                    if (book12UpsertResult instanceof Result.Failure) {
                        ctx.log.d(this,"Upsert Book Error: " +
                                ((Result.Failure<Book>) book12UpsertResult).exception().getMessage()
                        );
                    }

                    final Result<ArrayList<UUID>> booksAcceptedByUser = user2.checkoutBookFromLibrary(book12, library1);
                    if (booksAcceptedByUser instanceof Result.Failure) {
                        ctx.log.d(this,"Checkout book FAILURE --> " +
                                ((Result.Failure<ArrayList<UUID>>) booksAcceptedByUser).exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,"Checkout Book SUCCESS --> booksAcceptedByUser:" +
                                ((Result.Success<ArrayList<UUID>>) booksAcceptedByUser).value()
                        );
                    }
                }
            }


        }
    }

    //////////////////////////////////////////////////////////////////////
    /////////////////////////// Helper Methods ///////////////////////////

    private void PopulateBookDBandAPI(Context context) {
        // Populate the databases
        context.bookRepo().populateDB();
        context.bookRepo().populateAPI();
    }

    private void DumpBookDBandAPI(Context context) {
        System.out.print("\n");
        context.log.d(this,"DB Dump");
        context.bookRepo().printDB();

        System.out.print("\n");
        context.log.d(this,"API Dump");
        context.bookRepo().printAPI();

        System.out.print("\n");
    }

    private Result<Model.Domain.LibraryInfo> createFakeLibraryInfoInContextLibraryRepo(
        final Integer id,
        Context context
    ) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.libraryRepo()
                .upsertLibrary(
                        new Model.Domain.LibraryInfo(
                                createFakeUUID(someNumber),
                                "Library " + someNumber
                        )
                );
    }

    private Model.Domain.UserInfo createFakeUserInfoInContextUserRepo(
        final Integer id,
        Context context
    ) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.userRepo()
                .upsertUser(new Model.Domain.UserInfo(
                        createFakeUUID(someNumber),
                        "User " + someNumber,
                        "user" + someNumber + "@gmail.com"
                ));
    }

    private Result<Model.Domain.BookInfo> addFakeBookInfoInContextBookRepo(
        final Integer id,
        Context context
    ) {
        final Model.Domain.BookInfo bookInfo = createFakeBookInfo(null, id);
        return context.bookRepo()
                .upsertBook(bookInfo);
    }

    private Model.Domain.BookInfo createFakeBookInfo(String uuidStr, final Integer id) {
        Integer fakeId = id;
        if (fakeId == null) fakeId = 1;

        UUID uuid;
        if (uuidStr == null)
            uuid = createFakeUUID(fakeId);
        else
            uuid = UUID.fromString(uuidStr);

        return new Model.Domain.BookInfo(
                uuid,
                "Book " + fakeId,
                "Author " + fakeId,
                "Description " + fakeId
        );
    }

    public static UUID createFakeUUID(Integer id) {
        if (id == null) id = 1;

        // convert to string and add pad with 11 leading zeros
        final String str = String.format("%011d", id);

        return UUID.fromString("00000000-0000-0000-0000-" + str);
    }
}