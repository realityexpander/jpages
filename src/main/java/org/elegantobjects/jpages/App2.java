package org.elegantobjects.jpages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

            for(String line: lines) {
                String[] parts = line.split(":");
                if(parts.length == 2) {
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
            for(Map.Entry<String, String> entry : params.entrySet()) {
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
            if(builder.length() == 0) {
                builder.append("HTTP/1.1 200 OK\r\n");
            }

            // If body, add blank line
            if(name.equals("X-Body")) {
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
            if(string.length() == 0) {
                string += "HTTP/1.1 200 OK\r\n";
            }

            // If body, add blank line
            if(name.equals("X-Body")) {
                string += "\r\n" + value;
            } else {
                // add a header
                string += name + ": " + value + "\r\n";
            }
        }
    }


    // Start the server
    void start(int port) throws IOException {
        try(ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(1000);

            // Handle a single request
            while(true) {
                try(Socket socket = server.accept()) {
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

class HttpClient {
    private final String client;

    HttpClient(String client) {
        this.client = client;
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
            return exception.getLocalizedMessage();
        }
    }
}

interface IDB {
    Entity.Book getBook(UUID id);
    Result<Entity.Book> updateBook(Entity.Book bookInfo);
}

// DB uses Entities
class DB implements IDB {
    private final URL url;
    private final String user;
    private final String password;

    // Simulate a database
    private final HashMap<UUID, Entity.Book> database = new HashMap<>();

    DB(URL url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Entity.Book getBook(UUID id) {
        // Simulate the request
        return database.get(id);
    }

    public Result<Entity.Book> updateBook(Entity.Book bookInfo) {
        // Simulate the request
        if(database.put(bookInfo.id, bookInfo) == null) {
            return new Result.Failure<>(new Exception("Failed to update book"));
        }

        return new Result.Success<Entity.Book>(bookInfo);
    }

    public void addBook(Entity.Book book) {
        database.put(book.id, book);
    }

    public Map<UUID, Entity.Book> getAllBooks() {
        return new HashMap<>(database);
    }
}

interface IAPI {
    Result<DTO.Book> getBook(UUID id);
    Result<DTO.Book> getBook(String id);
    Result<DTO.Book> updateBook(DTO.Book bookInfo);
    Result<DTO.Book> addBook(DTO.Book book);
}

// API uses DTOs
class API implements IAPI {
    private final URL url;
    private final HttpClient client;

    // Simulate an API database
    private final HashMap<String, DTO.Book> database = new HashMap<>();

    API(URL url, HttpClient client) {
        this.url = url;
        this.client = client;
    }

    @Override
    public Result<DTO.Book> getBook(UUID id) {
        return getBook(id.toString());
    }

    @Override
    public Result<DTO.Book> getBook(String id) {
        // Simulate the request
        if(!database.containsKey(id)) {
            return new Result.Failure<>(new Exception("Book not found"));
        }

        return new Result.Success<>(database.get(id));
    }

    @Override
    public Result<DTO.Book> updateBook(DTO.Book bookInfo) {
        // Simulate the request
        if(database.put(bookInfo.id, bookInfo) == null) {
            return new Result.Failure<>(new Exception("Failed to update book"));
        }

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<DTO.Book> addBook(DTO.Book book) {
        if(database.containsKey(book.id)) {
            return new Result.Failure<>(new Exception("Book already exists"));
        }

        database.put(book.id, book);

        return new Result.Success<>(book);
    }

    public Map<String, DTO.Book> getAllBooks() {
        return new HashMap<>(database);
    }
}

interface IRepo {

    interface Book {
        Result<Domain.Book> getBookInfo(UUID id);
        Result<Domain.Book> updateBookInfo(Domain.Book bookInfo);
    }
}

// Repo uses Domain Models, and internally converts to/from DTOs/Entities
class Repo {
    static class Book implements IRepo.Book {
        private final API api;
        private final DB database;

        Book(API api, DB database) {
            this.api = api;
            this.database = database;
        }

        public Result<Domain.Book> getBookInfo(UUID id) {
            // Make the request
            Result<DTO.Book> bookInfo = api.getBook(id);
            if(bookInfo instanceof Result.Failure) {
                Exception exception = ((Result.Failure<DTO.Book>) bookInfo).getException();
                return new Result.Failure<Domain.Book>(exception);
            }

            // Convert to Domain Model
            Domain.Book book = ((Result.Success<DTO.Book>) bookInfo).getValue().toDomain();

            // Save to Local DB
            Result<Entity.Book> resultDB = database.updateBook(book.toEntity());
            if(resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Entity.Book>) resultDB).getException();
                return new Result.Failure<Domain.Book>(exception);
            }

            return new Result.Success<Domain.Book>(book);
        }

        public Result<Domain.Book> updateBookInfo(Domain.Book bookInfo) {
            // Make the request
            Result<DTO.Book> resultApi = api.updateBook(bookInfo.toDTO());
            if(resultApi instanceof Result.Failure) {
                Exception exception = ((Result.Failure<DTO.Book>) resultApi).getException();
                return new Result.Failure<Domain.Book>(exception);
            }

            // Convert to Domain Model
            Domain.Book book = ((Result.Success<DTO.Book>) resultApi).getValue().toDomain();

            // Save to Local DB
            Result<Entity.Book> resultDB = database.updateBook(book.toEntity());
            if(resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Entity.Book>) resultDB).getException();
                return new Result.Failure<Domain.Book>(exception);
            }

            return new Result.Success<Domain.Book>(book);
        }

        public void populateDB() {
            for(int i = 0; i < 10; i++) {
                database.addBook(
                    new Entity.Book(UUID.fromString(
                        "00000000-0000-0000-0000-00000000000" + i),
                    "Title " + i,
                    "Author " + i,
                    "Description " + i)
                );
            }
        }

        public void populateAPI() {
            for(int i = 0; i < 10; i++) {
                Result<DTO.Book> result = api.addBook(
                        new DTO.Book("00000000-0000-0000-0000-00000000000" + i,
                        "Title " + i,
                        "Author " + i,
                        "Description " + i)
                    );

                if(result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<DTO.Book>) result).getException();
                    System.out.println(exception.getMessage());
                }
            }
        }

        public void printDB() {
            for (Map.Entry<UUID, Entity.Book> entry : database.getAllBooks().entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }

        public void printAPI() {
            for (Map.Entry<String, DTO.Book> entry : api.getAllBooks().entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }
}

interface IContext {
    Repo.Book getBookRepo();
    void setBookRepo(Repo.Book bookRepo);
}

class Context implements IContext {
    static Context INSTANCE = new Context(null);  // Default Context
    private Repo.Book bookRepo = null;

    Context(
        Repo.Book bookRepo
    ) {
        this.bookRepo = bookRepo;
    }

    public void setBookRepo(Repo.Book bookRepo) {
        if (bookRepo != null) this.bookRepo = bookRepo;
    }

    public Repo.Book getBookRepo() {
        return this.bookRepo;
    }
}

// Domain Models
class Domain {
    static class Book extends Domain implements ToEntity<Entity.Book>, ToDTO<DTO.Book> {
        final UUID id;
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
}

// Data Transfer Objects for API
class DTO {
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
class Entity {
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

interface ToDomain<T extends Domain> {
    T toDomain();
}
interface ToEntity<T extends Entity> {
    T toEntity();
}
interface ToDTO<T extends DTO> {
    T toDTO();
}

class Book {
    final UUID id;
    private Domain.Book info = null;
    private Repo.Book repo = Context.INSTANCE.getBookRepo();

    Book(UUID id, Repo.Book repo) {
        if (repo != null) this.repo = repo;

        this.id = id;
    }
    Book(UUID id) {
        this(id, null);
    }
    Book() {
        this(UUID.randomUUID());
    }

    public Result<Domain.Book> getInfo() {
        if (this.info != null) return new Result.Success<>(this.info);

        Result<Domain.Book> result = this.repo.getBookInfo(this.id);
        if(result instanceof Result.Failure) {
            return result;
        }

        this.info = ((Result.Success<Domain.Book>) result).getValue();
        return result;
    }

    public void updateInfo(Domain.Book info) throws Exception {
        // optimistic update
        this.info = info;

        // Update the repo
        Result<Domain.Book> result = this.repo.updateBookInfo(info);

        if(result instanceof Result.Failure) {
            Exception exception = ((Result.Failure<Domain.Book>) result).getException();
            throw exception;
        }

        // Update the resulting info
        this.info = ((Result.Success<Domain.Book>) result).getValue();

        return;
    }

    public void refreshInfo() {
        this.info = null;
        this.getInfo();
    }

    public String toString() {
        return "Book: " + this.info.title + " by " + this.info.author;
    }
}

class XApp2 {
    private Context context = Context.INSTANCE;
    private IRepo repo = new Repo.Book(
        new API(
            new URL("http://localhost:8080"),
            new HttpClient("Apache")
        ),
        new DB(new URL("http://localhost:16078"),
                "root",
                "password"
        )
    );

    XApp2(Context context) {
        if(context != null) {
            // Passed in context
            this.context = context;
        } else {
            // Setup the context
            this.context.setBookRepo((Repo.Book) this.repo);
        }

        // Populate the databases
        this.context.getBookRepo().populateDB();
        this.context.getBookRepo().populateAPI();

        // Get the book info
        Book book = new Book(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        System.out.println(book.getInfo().toString());

        // Update info for a book
        try {
            book.updateInfo(new Domain.Book(
                "00000000-0000-0000-0000-000000000001",
                "The Updated Title",
                "The Updated Author",
                "The Updated Description"
            ));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Get the book info
        System.out.println(book.getInfo().toString());

        // Try to get a book id that doesn't exist
        Book book2 = new Book(UUID.fromString("00000000-0000-0000-0000-000000000099"));
        if(book2.getInfo() instanceof Result.Failure) {
            System.out.println("OH NO! --> " +
                    "book id: " + book2.id + " >> " +
                ((Result.Failure<Domain.Book>) book2.getInfo())
            );
        } else {
            System.out.println("YAY! --> " +
                ((Result.Success<Domain.Book>) book2.getInfo()).getValue()
            );
        }

        System.out.printf("\n");
        System.out.println("DB");
        this.context.getBookRepo().printDB();

        System.out.printf("\n");
        System.out.println("API");
        this.context.getBookRepo().printAPI();
    }

    public static void main(final String... args) {
        new XApp2(null);
    }
}