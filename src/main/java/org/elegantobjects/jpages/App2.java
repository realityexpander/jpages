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

        public T getValue() {
            return value;
        }

        public String toString() {
            if(value == null)
                return "null";
            return value.toString();
        }
    }

    static class Failure<T> extends Result<T> {
        private final Exception exception;

        Failure(Exception exception) {
            this.exception = exception;
        }

        public Exception getException() {
            return exception;
        }

        public String toString() {
            if(exception == null)
                return "null";
            return exception.getLocalizedMessage();
        }
    }
}

interface IDB {
    Model.Entity.Book getBook(UUID id);

    Result<Model.Entity.Book> updateBook(Model.Entity.Book bookInfo);
}

// DB uses Entities
class DB implements IDB {
    private final URL url;
    private final String user;
    private final String password;

    // Simulate a database
    private final HashMap<UUID, Model.Entity.Book> database = new HashMap<>();

    DB(URL url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Model.Entity.Book getBook(UUID id) {
        // Simulate the request
        return database.get(id);
    }

    public Result<Model.Entity.Book> updateBook(Model.Entity.Book bookInfo) {
        // Simulate the request
        if (database.put(bookInfo.id, bookInfo) == null) {
            return new Result.Failure<>(new Exception("Failed to update book"));
        }

        return new Result.Success<Model.Entity.Book>(bookInfo);
    }

    public void addBook(Model.Entity.Book book) {
        database.put(book.id, book);
    }

    public Map<UUID, Model.Entity.Book> getAllBooks() {
        return new HashMap<>(database);
    }
}

interface IAPI {
    Result<Model.DTO.Book> getBook(UUID id);

    Result<Model.DTO.Book> getBook(String id);

    Result<Model.DTO.Book> updateBook(Model.DTO.Book bookInfo);

    Result<Model.DTO.Book> addBook(Model.DTO.Book book);
}

// API uses DTOs
class API implements IAPI {
    private final URL url;
    private final HttpClient client;

    // Simulate an API database
    private final HashMap<String, Model.DTO.Book> database = new HashMap<>();

    API(URL url, HttpClient client) {
        this.url = url;
        this.client = client;
    }

    @Override
    public Result<Model.DTO.Book> getBook(UUID id) {
        return getBook(id.toString());
    }

    @Override
    public Result<Model.DTO.Book> getBook(String id) {
        // Simulate the request
        if (!database.containsKey(id)) {
            return new Result.Failure<>(new Exception("Book not found"));
        }

        return new Result.Success<>(database.get(id));
    }

    @Override
    public Result<Model.DTO.Book> updateBook(Model.DTO.Book bookInfo) {
        // Simulate the request
        if (database.put(bookInfo.id, bookInfo) == null) {
            return new Result.Failure<>(new Exception("Failed to update book"));
        }

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<Model.DTO.Book> addBook(Model.DTO.Book book) {
        if (database.containsKey(book.id)) {
            return new Result.Failure<>(new Exception("Book already exists"));
        }

        database.put(book.id, book);

        return new Result.Success<>(book);
    }

    public Map<String, Model.DTO.Book> getAllBooks() {
        return new HashMap<>(database);
    }
}

interface IRepo {

    interface Book extends IRepo {
        Result<Model.Domain.Book> getBookInfoResult(UUID id);

        Result<Model.Domain.Book> updateBookInfo(Model.Domain.Book bookInfo);
    }

    interface User extends IRepo {
        Result<Model.Domain.User> getUserInfoResult(UUID id);

        Model.Domain.User getUserInfo(UUID id);

        Result<Model.Domain.User> updateUserInfo(Model.Domain.User userInfo);

        Model.Domain.User createUser(Model.Domain.User user);
    }

    interface Library extends IRepo {
        Result<Model.Domain.Library> getLibraryInfoResult(UUID id);

        Model.Domain.Library getLibraryInfo(UUID id);

        Result<Model.Domain.Library> updateLibraryInfo(Model.Domain.Library libraryInfo);

        Model.Domain.Library createLibrary(Model.Domain.Library library);
    }
}

// Repo uses Domain Models, and internally converts to/from DTOs/Entities/Domains
class Repo implements IRepo {
    static class Book implements IRepo.Book {
        private final API api;
        private final DB database;

        Book(API api, DB database) {
            this.api = api;
            this.database = database;
        }
        Book() {
            this(
                new API(new URL("https://api.book.com"), new HttpClient()),
                new DB(new URL("https://db.book.com"), "user", "password")
            );
        }

        @Override
        public Result<Model.Domain.Book> getBookInfoResult(UUID id) {
            // Make the request to API
            Result<Model.DTO.Book> bookInfo = api.getBook(id);
            if (bookInfo instanceof Result.Failure) {
//                Exception exception = ((Result.Failure<Model.DTO.Book>) bookInfo).getException();
//                return new Result.Failure<Model.Domain.Book>(exception);

                // Try to get from cached DB
                Model.Entity.Book book = database.getBook(id);
                if (book == null) {
                    return new Result.Failure<>(new Exception("Book not found"));
                }

                return new Result.Success<>(book.toDomain());
            }

            // Convert to Domain Model
            Model.Domain.Book book = ((Result.Success<Model.DTO.Book>) bookInfo).getValue().toDomain();

            // Cache to Local DB
            Result<Model.Entity.Book> resultDB = database.updateBook(book.toEntity());
            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Entity.Book>) resultDB).getException();
                return new Result.Failure<Model.Domain.Book>(exception);
            }

            return new Result.Success<Model.Domain.Book>(book);
        }

        @Override
        public Result<Model.Domain.Book> updateBookInfo(Model.Domain.Book bookInfo) {
            System.out.println("Updating book info: " + bookInfo);

            // Make the request
            Result<Model.DTO.Book> resultApi = api.updateBook(bookInfo.toDTO());
            if (resultApi instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.DTO.Book>) resultApi).getException();
                return new Result.Failure<Model.Domain.Book>(exception);
            }

            // Convert to Domain Model
            Model.Domain.Book book = ((Result.Success<Model.DTO.Book>) resultApi).getValue().toDomain();

            // Save to Local DB
            Result<Model.Entity.Book> resultDB = database.updateBook(book.toEntity());
            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Entity.Book>) resultDB).getException();
                return new Result.Failure<Model.Domain.Book>(exception);
            }

            return new Result.Success<Model.Domain.Book>(book);
        }

        public void populateDB() {
            for (int i = 0; i < 10; i++) {
                database.addBook(
                        new Model.Entity.Book(UUID.fromString(
                                "00000000-0000-0000-0000-00000000000" + i),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );
            }
        }

        public void populateAPI() {
            for (int i = 0; i < 10; i++) {
                Result<Model.DTO.Book> result = api.addBook(
                        new Model.DTO.Book("00000000-0000-0000-0000-00000000000" + i,
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );

                if (result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<Model.DTO.Book>) result).getException();
                    System.out.println(exception.getMessage());
                }
            }
        }

        public void printDB() {
            for (Map.Entry<UUID, Model.Entity.Book> entry : database.getAllBooks().entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }

        public void printAPI() {
            for (Map.Entry<String, Model.DTO.Book> entry : api.getAllBooks().entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    static class User implements IRepo.User {
        private final HashMap<UUID, Model.Domain.User> database = new HashMap<>(); // Simulate a database on server

        @Override
        public Result<Model.Domain.User> getUserInfoResult(UUID id) {
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        @Override
        public Model.Domain.User getUserInfo(UUID id) {
            return database.get(id);
        }

        @Override
        public Result<Model.Domain.User> updateUserInfo(Model.Domain.User userInfo) {
            if (database.containsKey(userInfo.id)) {
                database.put(userInfo.id, userInfo);
                return new Result.Success<>(userInfo);
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        ;

        @Override
        public Model.Domain.User createUser(Model.Domain.User user) {
            database.put(user.id, user);
            return user;
        }

    }

    static class Library implements IRepo.Library {
        private final HashMap<UUID, Model.Domain.Library> database = new HashMap<>(); // simulates a database on server

        @Override
        public Result<Model.Domain.Library> getLibraryInfoResult(UUID id) {
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Library not found"));
        }

        @Override
        public Model.Domain.Library getLibraryInfo(UUID id) {
            return database.get(id);
        }

        @Override
        public Result<Model.Domain.Library> updateLibraryInfo(Model.Domain.Library libraryInfo) {
            if (database.containsKey(libraryInfo.id)) {
                database.put(libraryInfo.id, libraryInfo);
                return new Result.Success<>(libraryInfo);
            }

            return new Result.Failure<>(new Exception("Library not found"));
        }

        ;

        @Override
        public Model.Domain.Library createLibrary(Model.Domain.Library libraryInfo) {
            database.put(libraryInfo.id, libraryInfo);
            return libraryInfo;
        }

        public void populateLibraryWithRandomBooks(UUID libraryId, int numberOfBooks) {
            for (int i = 0; i < numberOfBooks; i++) {
                database.get(libraryId).availableBooks
                        .put(UUID.fromString("00000000-0000-0000-0000-00000000000" + i),
                                1 /* number on hand */);
            }
        }

    }
}

interface IContext {
    Repo.Book bookRepo();
    Repo.User userRepo();
    Repo.Library libraryRepo();

    void setBookRepo(Repo.Book bookRepo);
    void setUserRepo(Repo.User userRepo);
    void setLibraryRepo(Repo.Library libraryRepo);
}

// Context is a singleton class that holds all the repositories and global objects like Gson
class Context implements IContext {
    static Context INSTANCE = new Context();  // Create Default static Context

    // Repositories
    private Repo.Book bookRepo = null;
    private Repo.User userRepo = null;
    private Repo.Library libraryRepo = null;

    // Utility Singletons
    protected Gson gson = null;

    Context(
            Repo.Book bookRepo,
            Repo.User userRepo,
            Repo.Library libraryRepo,
            Gson gson
    ) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.libraryRepo = libraryRepo;
        this.gson = gson;
    }
    Context() {
        this(
            new Repo.Book(),
            new Repo.User(),
            new Repo.Library(),
            new Gson()
        );
    }

    // Returns the default static Context if null OR StaticContext is passed in
    public static Context getContextInstance(Context context) {
        if(context == null || context instanceof StaticContext)
            return Context.INSTANCE;
        else
            return context;
    }

    public void setBookRepo(Repo.Book bookRepo) {
        if (bookRepo != null) this.bookRepo = bookRepo;
    }

    public Repo.Book bookRepo() {
        return this.bookRepo;
    }

    public void setUserRepo(Repo.User userRepo) {
        if (userRepo != null) this.userRepo = userRepo;
    }

    public Repo.User userRepo() {
        return this.userRepo;
    }

    public void setLibraryRepo(Repo.Library libraryRepo) {
        if (libraryRepo != null) this.libraryRepo = libraryRepo;
    }

    public Repo.Library libraryRepo() {
        return this.libraryRepo;
    }
}
class StaticContext extends Context {
    StaticContext() {
        super(); // Create the default static context
    }
}

class Model {

    static class Domain extends Model {
        UUID id;

        static class Book extends Domain implements ToEntity<Entity.Book>, ToDTO<DTO.Book> {
            final String title;
            final String author;
            final String description;

            Book(String id, String title, String author, String description) {
                this.id = UUID.fromString(id);
                this.title = title;
                this.author = author;
                this.description = description;
            }

            Book(UUID id, String title, String author, String description) {
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }

            public String toString() {
                return "Book: " + this.title + " by " + this.author;
            }

            public DTO.Book toDTO() {
                return new DTO.Book(this.id.toString(), this.title, this.author, this.description);
            }

            public Entity.Book toEntity() {
                return new Entity.Book(this.id, this.title, this.author, this.description);
            }
        }

        static class User extends Domain {
            final String name;
            final String email;
            final ArrayList<UUID> acceptedBooks = new ArrayList<>();

            User(UUID id, String name, String email) {
                this.id = id;
                this.name = name;
                this.email = email;
            }

            public String toString() {
                return "User: " + this.name + " (" + this.email + ")";
            }
        }

        static class Library extends Domain {
            final String name;
            final HashMap<UUID, ArrayList<UUID>> checkoutMap; // User ID -> Books
            final HashMap<UUID, Integer> availableBooks; // Book ID -> Number of books available

            Library(UUID id,
                    String name,
                    HashMap<UUID, ArrayList<UUID>> checkoutUserBookMap,
                    HashMap<UUID, Integer> availableBooks
            ) {
                this.id = id;
                this.name = name;
                this.checkoutMap = checkoutUserBookMap;
                this.availableBooks = availableBooks;
            }

            Library(UUID id, String name) {
                this.id = id;
                this.name = name;
                this.checkoutMap = new HashMap<>();
                this.availableBooks = new HashMap<>();
            }

            public String toString() {
                return "Library: " + this.name + " (" + this.id + ")" + "\n" +
                        "Available Books: " + this.availableBooks + "\n" +
                        "Checkout Map: " + this.checkoutMap;
            }
        }
    }

    // Data Transfer Objects for API
    static class DTO extends Model
    {
        static class Book extends DTO implements ToDomain<Domain.Book> {
            final String id;
            final String title;
            final String author;
            final String description;

            Book(String id, String title, String author, String description) {
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }

            public String toString() {
                return "Book: " + this.title + " by " + this.author;
            }

            public Domain.Book toDomain() {
                return new Domain.Book(this.id, this.title, this.author, this.description);
            }
        }
    }

    // Entities for the Database
    static class Entity extends Model {
        static class Book extends Entity implements ToDomain<Domain.Book> {
            final UUID id;
            final String title;
            final String author;
            final String description;

            Book(UUID id, String title, String author, String description) {
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }

            public String toString() {
                return "Book: " + this.title + " by " + this.author;
            }

            public Domain.Book toDomain() {
                return new Domain.Book(this.id.toString(), this.title, this.author, this.description);
            }
        }
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
    T fetchInfo();                  // Fetches the info for the object from the server
    Result<T> fetchInfoResult();    // Fetches the Result<T> for the info object from the server
    boolean isInfoFetched();        // Returns true if the info has been fetched
    Result<T> updateInfo(T info);   // Updates the info for the object on the server
    Result<T> refreshInfo();        // Refreshes the info for the object from the server
}

abstract class IDomainObject<T extends Model.Domain> implements Info<T> {
    UUID id;
    protected T info;
    protected Context context = null;
    private Gson gson = null;

    // Class of the info<T> (for GSON deserialization)
    @SuppressWarnings("unchecked")
    Class<T> infoClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];


    IDomainObject(T info, Context context) {
        this.context = Context.getContextInstance(context);
        this.gson = this.context.gson;
        this.info = info;
        this.id = info.id;
    }
    IDomainObject(UUID id, Context context) {
        this.context = Context.getContextInstance(context);
        this.gson = this.context.gson;
        this.id = id;
    }
    IDomainObject(String json, Context context) {
        this.context = Context.getContextInstance(context);
        this.gson = this.context.gson;
        this.info = this.gson.fromJson(json, this.infoClass);
        this.id = this.info.id;
    }
    IDomainObject(Context context) {
        this(UUID.randomUUID(), context);
    }
    IDomainObject(String json) {
        this(json, new StaticContext());
    }
    IDomainObject(T info) {
        this(info, new StaticContext());
    }
    IDomainObject(UUID id) {
        this(id, new StaticContext());
    }
    IDomainObject() { this(UUID.randomUUID(), null); }

    public boolean isInfoFetched() {
        return this.info != null;
    }
    public T fetchInfo() { return null; }
    public Result<T> fetchInfoResult() { return null; };
    public Result<T> updateInfo(T info) { return null; };
    public Result<T> refreshInfo() {
        this.info = null;
        return this.fetchInfoResult();
    }

    protected String getFetchInfoResultFailureReason() {
        if (!isInfoFetched()) {
            if (fetchInfoResult() instanceof Result.Failure) {
                return ((Result.Failure<T>) fetchInfoResult()).getException().getMessage();
            }
        }
        return null;
    }
}

abstract class DomainObject<T extends Model.Domain> extends IDomainObject<T> {

    public DomainObject(Context context) {
        super(context);
    }


    public T fetchInfo() {
        if (isInfoFetched()) { return this.info; }

        Result<T> result = this.fetchInfoResult();
        if (result instanceof Result.Failure) {
            System.out.println("Failed to get info for "+
                    "class: " + this.getClass().getName() + ", " +
                    "id: " + this.id.toString());
            return null;
        }

        this.info = ((Result.Success<T>) result).getValue();
        return this.info;
    }

    public Result<T> updateInfo(T info) {
        this.info = info;
        return new Result.Success<>(info);
    }

    public String toJson() {
        return this.context.gson.toJson(this.fetchInfo());
    }

    public String toJsonPretty() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this.fetchInfo());
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

    public String toString() {
        String infoString = this.info == null ? "null" : this.info.toString();
        String nameOfClass = this.getClass().getName();
        return nameOfClass + ": " + this.id.toString() + ", info=" + infoString;
    }
}

class Book extends DomainObject<Model.Domain.Book> {
    private Model.Domain.Book info = null;
    private Result<Model.Domain.Book> infoResult = null;
    private Repo.Book repo = null;

    Book(UUID id, Context context) {
        super(context);
        this.repo = this.context.bookRepo();
        this.id = id;

        System.out.println("Book created, id: " + this.id.toString());
    }
    Book(UUID id) {
        this(id, new StaticContext());
    }
    Book() { this(UUID.randomUUID()); }

    @Override
    public Result<Model.Domain.Book> fetchInfoResult() {
        Result<Model.Domain.Book> result = this.repo.getBookInfoResult(this.id);
        infoResult = result;
        if (result instanceof Result.Failure) {
            return result;
        }

        this.info = ((Result.Success<Model.Domain.Book>) result).getValue();
        return result;
    }

    @Override
    public Result<Model.Domain.Book> updateInfo(Model.Domain.Book info) {
        // Update self optimistically
        this.info = info;

        // Update the repo
        Result<Model.Domain.Book> result = this.repo.updateBookInfo(info);
        if (result instanceof Result.Failure) {
            return result;
        }

        // Update self with repo result
        this.info = ((Result.Success<Model.Domain.Book>) result).getValue();
        return result;
    }
}

class User extends DomainObject<Model.Domain.User> {
    private Model.Domain.User info = null;
    private Result<Model.Domain.User> infoResult = null;
    private Repo.User repo = null;

    User(UUID id, Context context) {
        super(context);
        this.repo = this.context.userRepo();
        this.id = id;

        System.out.println("User created, id: " + this.id.toString());
    }
    User(UUID id) {
        this(id, null);
    }
    User() {
        this(UUID.randomUUID());
    }

    @Override
    public Result<Model.Domain.User> fetchInfoResult() {
        Result<Model.Domain.User> result = this.repo.getUserInfoResult(this.id);
        infoResult = result;
        if (result instanceof Result.Failure) {
            return result;
        }

        this.info = ((Result.Success<Model.Domain.User>) result).getValue();
        return result;
    }

    @Override
    public Result<Model.Domain.User> updateInfo(Model.Domain.User info) {
        // Update self optimistically
        this.info = info;

        // Update the repo
        Result<Model.Domain.User> result = this.repo.updateUserInfo(info);
        if (result instanceof Result.Failure) {
            return result;
        }

        // Update self with repo result
        this.info = ((Result.Success<Model.Domain.User>) result).getValue();
        return result;
    }

    public Result<ArrayList<UUID>> acceptBook(@NotNull Book book) {
        System.out.println("User acceptBook,  book: " + book.id.toString() + " to user: " + this.id.toString());

        fetchInfo();
        if (infoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.User>) infoResult).getException ());
        }

        // Check user has not already accepted book
        if (this.info.acceptedBooks.contains(book.id)) {
            return new Result.Failure<>(new Exception("User has already accepted book"));
        }

        // Accept book
        this.info.acceptedBooks.add(book.id);

        // Update user
        Result<Model.Domain.User> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.User>) result).getException ());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }

    public Result<ArrayList<UUID>> returnBook(Book book) {
        System.out.println("Returning book");
        if (!isInfoFetched()) return new Result.Failure<>(new Exception("User info not found"));

        // check if book is valid
        if (book == null) {
            return new Result.Failure<>(new Exception("Book is null"));
        }

        // Check user has accepted book
        if (!this.info.acceptedBooks.contains(book.id)) {
            return new Result.Failure<>(new Exception("User has not accepted book"));
        }

        // Remove the Returned book
        this.info.acceptedBooks.remove(book.id);

        // Update user
        Result<Model.Domain.User> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.User>) result).getException ());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }
}

class Library extends DomainObject<Model.Domain.Library> {
    private Model.Domain.Library info = null;
    private Result<Model.Domain.Library> infoResult = null;
    private Repo.Library repo = null;

    Library(UUID id, Context context) {
        super(context);
        this.repo = this.context.libraryRepo();
        this.id = id;

        System.out.println("Library created, id: " + id);
    }
    Library(UUID id) {
        this(id, null);
    }
    Library() {
        this(UUID.randomUUID());
    }

    @Override
    public Result<Model.Domain.Library> fetchInfoResult() {
        Result<Model.Domain.Library> result = this.repo.getLibraryInfoResult(this.id);
        infoResult = result;
        if (result instanceof Result.Failure) {
            return result;
        }

        this.info = ((Result.Success<Model.Domain.Library>) result).getValue();
        return result;
    }

    @Override
    public Result<Model.Domain.Library> updateInfo(Model.Domain.Library newInfo) {
        // Update self optimistically
        this.info = newInfo;

        // Update the repo
        Result<Model.Domain.Library> result = this.repo.updateLibraryInfo(newInfo);
        if (result instanceof Result.Failure) {
            return result;
        }

        // Update self with repo result
        this.info = ((Result.Success<Model.Domain.Library>) result).getValue();
        return result;
    }

    public Result<UUID> checkOutBookToUser(@NotNull Book book, @NotNull User user) {
        System.out.printf("Library checkOutBookToUser, book %s to user %s\n", book.id, user.id);

        Result<Void> refreshResult = createUserIfMissing(user.id);
        if (refreshResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Void>) refreshResult).getException());
        }

        // Check if book exists
        if (!this.info.availableBooks.containsKey(book.id)) {
            return new Result.Failure<>(new Exception("Book not found, id: " + book.id));
        }

        // Check if book is available
        if (this.info.availableBooks.get(book.id).equals(0)) {
            return new Result.Failure<>(new Exception("Book is not available, id: " + book.id));
        }

        // Check if user has already checked out book
        if (this.info.checkoutMap.get(user.id).contains(book.id)) {
            return new Result.Failure<>(new Exception("User has already checked out book, id: " + book.id));
        }

        // Add Book to User and Remove Book from Library
        this.info.checkoutMap.get(user.id).add(book.id);
        this.info.availableBooks.put(book.id, this.info.availableBooks.get(book.id) - 1); // decrement available books

        // Update the repo
        Result<Model.Domain.Library> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.Library>) result).getException());
        }

        // Make user accept book
        Result<ArrayList<UUID>> result2 = user.acceptBook(book);
        if (result2 instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID>>) result2).getException());
        }

        return new Result.Success<>(book.id);
    }

    public Result<UUID> returnBookFromUser(Book book, User user) {
        System.out.printf("Returning book %s from user %s\n", book.id, user.id);
        if (this.info == null) {
            if (this.fetchInfo() == null) {
                return new Result.Failure<>(new Exception("Library info not found"));
            }
        }

        createUserIfMissing(user.id);

        // Check if user has not checked out book
        if (!this.info.checkoutMap.get(user.id).contains(book.id)) {
            return new Result.Failure<>(new Exception("User has not checked out book, id: " + book.id));
        }

        // Update the Library
        this.info.checkoutMap.get(user.id).remove(book.id);
        this.info.availableBooks.put(book.id, this.info.availableBooks.get(book.id) + 1);

        // Make user return book
        Result<ArrayList<UUID>> result = user.returnBook(book);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID>>) result).getException());
        }

        return new Result.Success<>(book.id);
    }

    private Result<Void> createUserIfMissing(UUID userId) {
        if(refreshInfo() instanceof Result.Failure) {
            return new Result.Failure<>(new Exception(((Result.Failure<Model.Domain.Library>) refreshInfo()).getException()));
        }

        // Check if user exists
        if (!this.info.checkoutMap.containsKey(userId)) {
            // Create new user entry
            this.info.checkoutMap.put(userId, new ArrayList<>());
        }

        return new Result.Success<>(null);
    }

    public Result<ArrayList<UUID>> getBooksCheckedOutByUser(UUID userId) {
        System.out.printf("Getting books checked out by user %s\n", userId);
        if (!isInfoFetched()) return new Result.Failure<>(new Exception("Library info not found"));

        createUserIfMissing(userId);

        return new Result.Success<>(this.info.checkoutMap.get(userId));
    }

    public Result<ArrayList<Pair<UUID, Integer>>> calculateAvailableBooksAndAmountOnHand() {
        System.out.print("Getting available books\n");

        String reason = getFetchInfoResultFailureReason();
        if(reason != null) {
            return new Result.Failure<>(new Exception(reason));
        }

        ArrayList<Pair<UUID, Integer>> availableBooks = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : this.info.availableBooks.entrySet()) {
            if (entry.getValue() > 0) {
                availableBooks.add(new Pair<>(entry.getKey(), entry.getValue()));
            }
        }

        return new Result.Success<>(availableBooks);
    }

//    @Override
//    protected String getFetchInfoResultFailureReason() {
//        if (!isInfoFetched()) {
//            if (fetchInfoResult() instanceof Result.Failure) {
//                return ((Result.Failure<Model.Domain.Library>) fetchInfoResult()).getException().getMessage();
//            }
//        }
//        return null;
//    }

    public void DumpDB() {
        System.out.println("\nDumping Library DB:");
        System.out.println(this.toJsonPretty());
        System.out.println("\n");
    }
}

class XApp2 {
    // Setup App Singletons for Context
    private Context context = Context.INSTANCE;
    private final Repo.Book bookRepo = new Repo.Book(
            new API(
                    new URL("http://localhost:8080"),
                    new HttpClient("Apache")
            ),
            new DB(new URL("http://localhost:16078"),
                    "root",
                    "password"
            )
    );
    private final Repo.Library libraryRepo = new Repo.Library();
    private final Repo.User userRepo = new Repo.User();

    XApp2(Context context) {
        // Setup App Context Object
        if (context != null) {
            // Passed in context
            this.context = context;
        } else {
            this.context.setBookRepo(this.bookRepo);
            this.context.setLibraryRepo(this.libraryRepo);
            this.context.setUserRepo(this.userRepo);
        }

        Populate_And_Poke_Book:
        {
            System.out.println("Populating Book DB and API");
            PopulateBookDBandAPI();

            // Create a book object (it only has an id)
            Book book = new Book(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            System.out.println(book.fetchInfoResult().toString());

            // Update info for a book
            final Result<Model.Domain.Book> bookInfoResult =
                    book.updateInfo(
                            new Model.Domain.Book(
                                    "00000000-0000-0000-0000-000000000001",
                                    "The Updated Title",
                                    "The Updated Author",
                                    "The Updated Description"
                            ));
            System.out.println(bookInfoResult.toString());

            // Get the book infoResult
            System.out.println(book.fetchInfoResult().toString());

            // Get the bookInfo (null if not loaded)
            Model.Domain.Book book3 = book.fetchInfo();
            if (book3 == null) {
                System.out.println("Book Missing --> " +
                        "book id: " + book.id + " >> " +
                        " is null"
                );
            } else {
                System.out.println("Book Info --> " +
                        book3.id + " >> " +
                        book3.title + ", " +
                        book3.author
                );
            }

            // Try to get a book id that doesn't exist
            Book book2 = new Book(UUID.fromString("00000000-0000-0000-0000-000000000099"));
            if (book2.fetchInfoResult() instanceof Result.Failure) {
                System.out.println("Get Book FAILURE --> " +
                        "book id: " + book2.id + " >> " +
                        ((Result.Failure<Model.Domain.Book>) book2.fetchInfoResult())
                );
            } else {
                System.out.println("Book ERxists --> " +
                        ((Result.Success<Model.Domain.Book>) book2.fetchInfoResult()).getValue()
                );
            }

            DumpBookDBandAPI();
        }

        Populate_the_library_and_user_DBs:
        {
            // Create & populate a fake library in the library repo
            final Model.Domain.Library libraryInfo = createFakeLibraryInfoInRepo();
            this.context.libraryRepo()
                    .populateLibraryWithRandomBooks(libraryInfo.id, 10);

            // Create & populate a fake user in the user repo
            final Model.Domain.User userInfo = createFakeUserInfoInRepo();

            // Create "hollow" bookInfo for the user to use to check out. These only contain an id.
            final Model.Domain.Book bookInfo = createFakeBookInfo(null, null);
            final Model.Domain.Book bookInfo2 = createFakeBookInfo("00000000-0000-0000-0000-000000000002", 2);

            //////////////////////////////////
            // Actual App functionality     //
            //////////////////////////////////

            // Create the App objects
            final User user = new User(userInfo.id);
            final Library library = new Library(libraryInfo.id);
            final Book book = new Book(bookInfo.id);
            final Book book1 = new Book(bookInfo2.id);

            Checkout_a_book_to_the_user:
            {
                System.out.println("\nChecking out books to user " + user.id + "\n");

                final Result<UUID> result = library.checkOutBookToUser(book, user);
                if (result instanceof Result.Failure) {
                    System.out.println("Checked out book FAILURE--> " +
                            ((Result.Failure<UUID>) result).getException().getMessage()
                    );
                } else {
                    System.out.println("Checked out book SUCCESS --> " +
                            ((Result.Success<UUID>) result).getValue()
                    );
                }

                final Result<UUID> result2 = library.checkOutBookToUser(book1, user);
                if (result2 instanceof Result.Failure) {
                    System.out.println("Checked out book FAILURE--> " +
                            ((Result.Failure<UUID>) result2).getException().getMessage()
                    );
                } else {
                    System.out.println("Checked out book SUCCESS --> " +
                            ((Result.Success<UUID>) result2).getValue()
                    );
                }

                library.DumpDB();
            }

            Get_Available_Books_And_Counts_In_Library: {
                System.out.println("\nGetting available books and counts in library:");

                final Result<ArrayList<Pair<UUID, Integer>>> availableBookIdCounts =
                        library.calculateAvailableBooksAndAmountOnHand();
                if (availableBookIdCounts instanceof Result.Failure) {
                    System.out.println("AvailableBookIdCounts FAILURE! --> " +
                            ((Result.Failure<ArrayList<Pair<UUID, Integer>>>) availableBookIdCounts)
                                    .getException().getMessage()
                    );

                    break Get_Available_Books_And_Counts_In_Library;
                }

                // create objects and populate info for available books
                assert availableBookIdCounts instanceof Result.Success;
                final ArrayList<Pair<UUID, Integer>> availableBooks =
                        ((Result.Success<ArrayList<Pair<UUID, Integer>>>) availableBookIdCounts).getValue();

                // Print out available books
                System.out.println("\nAvailable Books in Library:");
                for (Pair<UUID, Integer> bookIdCount : availableBooks) {
                    final Book book2 = new Book(bookIdCount.getFirst());

                    final Result<Model.Domain.Book> bookInfoResult = book2.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        System.out.println(
                                "Book Error:" +
                                ((Result.Failure<Model.Domain.Book>) bookInfoResult)
                                        .getException().getMessage()
                        );
                    } else {
                        System.out.println(
                                ((Result.Success<Model.Domain.Book>) bookInfoResult).getValue() +
                                " >> count: " + bookIdCount.getSecond()
                        );
                    }
                }
            }

            Get_books_checked_out_by_user:
            {
                final Result<ArrayList<UUID>> checkedOutBookIds = library.getBooksCheckedOutByUser(user.id);
                if (checkedOutBookIds instanceof Result.Failure) {
                    System.out.println("OH NO! --> " +
                        ((Result.Failure<ArrayList<UUID>>) checkedOutBookIds)
                            .getException().getMessage()
                    );
                }

                // create book objects and populate info for checked out books
                assert checkedOutBookIds instanceof Result.Success;
                final ArrayList<UUID> checkedOutBooks =
                        ((Result.Success<ArrayList<UUID>>) checkedOutBookIds).getValue();

                // Print checked out books
                System.out.println("\nChecked Out Books from User [" + user.fetchInfo().name + "]:");
                for (UUID bookId : checkedOutBooks) {
                    final Book book2 = new Book(bookId);

                    final Result<Model.Domain.Book> bookInfoResult = book2.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        System.out.println(
                                "Book Error:" +
                                        ((Result.Failure<Model.Domain.Book>) bookInfoResult)
                                                .getException().getMessage()
                        );
                    } else {
                        System.out.println(
                                ((Result.Success<Model.Domain.Book>) bookInfoResult).getValue()
                        );
                    }
                }
                System.out.print("\n");
            }

            Return_the_book_from_the_user_to_the_library:
            {
                final Result<UUID> result2 = library.returnBookFromUser(book, user);
                if (result2 instanceof Result.Failure) {
                    System.out.println("Returned book FAILURE --> " +
                            ((Result.Failure<UUID>) result2).getException().getMessage()
                    );
                } else {
                    System.out.println("Returned Book SUCCESS --> " +
                            ((Result.Success<UUID>) result2).getValue()
                    );
                }

                library.DumpDB();
            }

            // Dump Json
            {
                System.out.println("\nLibrary Json:");
                System.out.println(library.toJsonPretty());

                Library library2 = new Library();
                library2.updateInfoFromJson(
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
                System.out.println("\nLibrary2:");
                //System.out.println(library2.toJsonPretty());

            }
        }
    }

    private Model.Domain.Library createFakeLibraryInfoInRepo() {
        return this.context.libraryRepo()
                .createLibrary(new Model.Domain.Library(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        "Library 1"
                ));
    }

    private void PopulateBookDBandAPI() {
        // Populate the databases
        this.context.bookRepo().populateDB();
        this.context.bookRepo().populateAPI();
    }

    private void DumpBookDBandAPI() {
        System.out.print("\n");
        System.out.println("DB Dump");
        this.context.bookRepo().printDB();

        System.out.print("\n");
        System.out.println("API Dump");
        this.context.bookRepo().printAPI();

        System.out.print("\n");
    }

    private Model.Domain.User createFakeUserInfoInRepo() {
        return this.context.userRepo()
                .createUser(new Model.Domain.User(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        "User 1",
                        "user1@test.com"
                ));
    }

    private Model.Domain.Book createFakeBookInfo(String id, Integer someNumber) {
        if(id == null) id = "00000000-0000-0000-0000-000000000001";
        if(someNumber == null) someNumber = 1;

        return new Model.Domain.Book(
                UUID.fromString(id),
                "Book " + someNumber,
                "Author " + someNumber,
                "Description " + someNumber
        );
    }

    public static void main(final String... args) {
        new XApp2(null);
    }
}