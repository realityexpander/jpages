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
import java.util.HashMap;

import static java.lang.String.format;
import static org.elegantobjects.jpages.BaseUUID.*;
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

// Simple Data Holder classes for Type Safe UUID's
class BaseUUID {
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

    public boolean equals(BaseUUID other) {
        return (other).uuid.equals(uuid);
    }

    public int hashCode() {
        return uuid.hashCode();
    }

    public static BaseUUID fromString(String uuid) {
        return new BaseUUID(UUID.fromString(uuid));
    }

    public static BaseUUID fromUUID(UUID uuid) {
        return new BaseUUID(uuid);
    }

    public static BaseUUID randomUUID() {
        return new BaseUUID(UUID.randomUUID());
    }

    public static BaseUUID createFakeBaseUUID(Integer id) {
        if (id == null) id = 1;

        // convert to string and add pad with 11 leading zeros
        final String str = format("%011d", id);

        return fromString("00000000-0000-0000-0000-" + str);
    }

    static class BookUUID extends BaseUUID {
        BookUUID(UUID uuid) { super(uuid); }

        public static BookUUID fromString(String uuid) {
            return new BookUUID(UUID.fromString(uuid));
        }

        public static BookUUID fromUUID(UUID uuid) {
            return new BookUUID(uuid);
        }

        public static BookUUID randomUUID() {
            return new BookUUID(UUID.randomUUID());
        }

        public static BookUUID fromBaseUUID(BaseUUID uuid) {
            return new BookUUID(uuid.uuid);
        }

        public static BookUUID createFakeBookUUID(Integer id) {
            return fromBaseUUID(createFakeBaseUUID(id));
        }
    }

    static class LibraryUUID extends BaseUUID {
        LibraryUUID(UUID uuid) {
            super(uuid);
        }

        public static LibraryUUID fromString(String uuid) {
            return new LibraryUUID(UUID.fromString(uuid));
        }

        public static LibraryUUID fromUUID(UUID uuid) {
            return new LibraryUUID(uuid);
        }

        public static LibraryUUID fromBaseUUID(BaseUUID uuid) {
            return new LibraryUUID(uuid.uuid);
        }

        public static LibraryUUID randomUUID() {
            return new LibraryUUID(UUID.randomUUID());
        }

        public static LibraryUUID createFakeLibraryUUID(Integer id) {
            return fromBaseUUID(createFakeBaseUUID(id));
        }
    }

    static class UserUUID extends BaseUUID {
        UserUUID(UUID uuid) {
            super(uuid);
        }

        public static UserUUID fromString(String uuid) {
            return new UserUUID(UUID.fromString(uuid));
        }

        public static UserUUID fromUUID(UUID uuid) {
            return new UserUUID(uuid);
        }

        public static UserUUID fromBaseUUID(BaseUUID uuid) {
            return new UserUUID(uuid.uuid);
        }

        public static UserUUID randomUUID() {
            return new UserUUID(UUID.randomUUID());
        }

        public static UserUUID createFakeUserUUID(Integer id) {
            return fromBaseUUID(createFakeBaseUUID(id));
        }
    }

    // Facade for HashMap but `key` hash is the common UUID value stored in BaseUUID class (not the hash of BaseUUID object itself)
    static class HashMap<T extends BaseUUID, U> extends java.util.HashMap<UUID, U> {
        private static final long serialVersionUID = 0x7723L;

        public U get(String baseUUIDStr) {
            return get(UUID.fromString(baseUUIDStr));
        }
        public U get(BaseUUID baseUUID) {
            return super.get(baseUUID.uuid);
        }

        public U put(String baseUUIDStr, U value) {
            return put(UUID.fromString(baseUUIDStr), value);
        }
        public U put(BaseUUID baseUUID, U value) {
            return super.put(baseUUID.uuid, value);
        }

        public U remove(String baseUUIDStr) {
            return remove(UUID.fromString(baseUUIDStr));
        }
        public U remove(BaseUUID baseUUID) {
            return super.remove(baseUUID.uuid);
        }

        public boolean containsKey(String baseUUIDStr) {
            return containsKey(UUID.fromString(baseUUIDStr));
        }
        public boolean containsKey(BaseUUID baseUUID) {
            return super.containsKey(baseUUID.uuid);
        }

        public Set<T> keys() throws RuntimeException {
            Set<UUID> uuidSet = super.keySet();
            Set<T> baseUUIDSet = new HashSet<>();

            // Convert UUIDs to BaseUUIDs
            try {
                for (UUID uuid : uuidSet) {
                    @SuppressWarnings("unchecked")
                    T baseUUID = (T) BaseUUID.fromUUID(uuid);
                    baseUUIDSet.add(baseUUID);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.keys(): Failed to convert UUIDs to BaseUUIDs, uuidSet: " + uuidSet);
            }

            return baseUUIDSet;
        }
    }
}

//// Facade for HashMap but `key` hash is the common UUID value stored in BaseUUID class (not the hash of BaseUUID object itself)
//class HashMap<T extends BaseUUID, U> extends HashMap<UUID, U> {
//    private static final long serialVersionUID = 0x7723L;
//
//    public U get(String baseUUIDStr) {
//        return get(UUID.fromString(baseUUIDStr));
//    }
//    public U get(BaseUUID baseUUID) {
//        return super.get(baseUUID.uuid);
//    }
//
//    public U put(String baseUUIDStr, U value) {
//        return put(UUID.fromString(baseUUIDStr), value);
//    }
//    public U put(BaseUUID baseUUID, U value) {
//        return super.put(baseUUID.uuid, value);
//    }
//
//    public U remove(String baseUUIDStr) {
//        return remove(UUID.fromString(baseUUIDStr));
//    }
//    public U remove(BaseUUID baseUUID) {
//        return super.remove(baseUUID.uuid);
//    }
//
//    public boolean containsKey(String baseUUIDStr) {
//        return containsKey(UUID.fromString(baseUUIDStr));
//    }
//    public boolean containsKey(BaseUUID baseUUID) {
//        return super.containsKey(baseUUID.uuid);
//    }
//
//    public Set<T> keys() throws RuntimeException {
//        Set<UUID> uuidSet = super.keySet();
//        Set<T> baseUUIDSet = new HashSet<>();
//
//        // Convert UUIDs to BaseUUIDs
//        try {
//            for (UUID uuid : uuidSet) {
//                @SuppressWarnings("unchecked")
//                T baseUUID = (T) BaseUUID.fromUUID(uuid);
//                baseUUIDSet.add(baseUUID);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("HashMap.keys(): Failed to convert UUIDs to BaseUUIDs, uuidSet: " + uuidSet);
//        }
//
//        return baseUUIDSet;
//    }
//}

// DB uses Model.Entities
interface IDatabase<T extends Model.Entity> {
    T getEntityInfo(BaseUUID id);
    Result<T> updateEntityInfo(T entityInfo);
    Result<T> addEntityInfo(T entityInfo);
    Result<T> upsertEntityInfo(T entityInfo);
    Result<T> deleteEntityInfo(T entityInfo);
    Map<BaseUUID, T> getAllEntityInfo();
}
class InMemoryDatabase<T extends Model.Entity> implements IDatabase<T> {
    private final URL url;
    private final String user;
    private final String password;

    // Simulate a database
    private final HashMap<BaseUUID, T> database = new HashMap<>();

    InMemoryDatabase(URL url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }
    InMemoryDatabase() {
        this(new URL("memory://hash.map"), "admin", "password");
    }

    @Override
    public T getEntityInfo(BaseUUID id) {
        // Simulate the request
        return database.get(id);
    }

    @Override
    public Result<T> updateEntityInfo(T entityInfo) {
        // Simulate the request
        if (database.put(entityInfo.id, entityInfo) == null) {
            return new Result.Failure<>(new Exception("DB: Failed to update book"));
        }

        return new Result.Success<>(entityInfo);
    }

    @Override
    public Result<T> addEntityInfo(T entityInfo) {
        if (database.containsKey(entityInfo.id)) {
            return new Result.Failure<>(new Exception("DB: Book already exists"));
        }

        database.put(entityInfo.id, entityInfo);
        return new Result.Success<>(entityInfo);
    }

    @Override
    public Result<T> upsertEntityInfo(T entityInfo) {
        if (database.containsKey(entityInfo.id)) {
            return updateEntityInfo(entityInfo);
        } else {
            return addEntityInfo(entityInfo);
        }
    }

    @Override
    public Result<T> deleteEntityInfo(T entityInfo) {
        if (database.remove(entityInfo.id) == null) {
            return new Result.Failure<>(new Exception("DB: Failed to delete book"));
        }

        return new Result.Success<>(entityInfo);
    }

    @Override
    public Map<BaseUUID, T> getAllEntityInfo() {
        return new HashMap<>(database);
    }
}
class BookDatabase  {
    private final IDatabase<Model.Entity.BookInfo> database;

    BookDatabase(IDatabase<Model.Entity.BookInfo> database) {
        this.database = database;
    }
    BookDatabase() {
        this(new InMemoryDatabase<>(new URL("memory://db.book.com"), "user", "password"));
    }

    public Model.Entity.BookInfo getBookInfo(BookUUID id) {
        return database.getEntityInfo(id);
    }

    public Result<Model.Entity.BookInfo> updateBookInfo(Model.Entity.BookInfo bookInfo) {
        return database.updateEntityInfo(bookInfo);
    }

    public Result<Model.Entity.BookInfo> addBookInfo(Model.Entity.BookInfo bookInfo) {
        return database.addEntityInfo(bookInfo);
    }

    public Result<Model.Entity.BookInfo> upsertBookInfo(Model.Entity.BookInfo bookInfo) {
        return database.upsertEntityInfo(bookInfo);
    }

    public Result<Model.Entity.BookInfo> deleteBookInfo(Model.Entity.BookInfo bookInfo) {
        return database.deleteEntityInfo(bookInfo);
    }

    public Map<BaseUUID, Model.Entity.BookInfo> getAllBookInfos() {
        return database.getAllEntityInfo();
    }
}

// API uses Model.DTOs
interface IAPI<T extends Model.DTO> {
    Result<T> getDtoInfo(BaseUUID id);
    Result<T> getDtoInfo(String id);
    Result<T> addDtoInfo(T dtoInfo);
    Result<T> updateDtoInfo(T dtoInfo);
    Result<T> upsertDtoInfo(T dtoInfo);
    Result<T> deleteDtoInfo(T dtoInfo);
}
class InMemoryAPI<T extends Model.DTO> implements IAPI<T> {
    private final URL url;
    private final HttpClient client;

    // Simulate an API database
    private final HashMap<BaseUUID, T> database = new HashMap<>();

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
    public Result<T> getDtoInfo(String id) {
        return getDtoInfo(fromString(id));
    }

    @Override
    public Result<T> getDtoInfo(BaseUUID id) {
        // Simulate the network request
        if (!database.containsKey(id)) {
            return new Result.Failure<>(new Exception("API: Book not found"));
        }

        return new Result.Success<>(database.get(id));
    }

    @Override
    public Result<T> updateDtoInfo(T dtoInfo) {
        // Simulate the network request
        if (database.put(dtoInfo.id, dtoInfo) == null) {
            return new Result.Failure<>(new Exception("API: Failed to update book"));
        }

        return new Result.Success<>(dtoInfo);
    }

    @Override
    public Result<T> addDtoInfo(T dtoInfo) {
        if (database.containsKey(dtoInfo.id)) {
            return new Result.Failure<>(new Exception("API: DtoInfo already exists, use update, id=" + dtoInfo.id));
        }

        database.put(dtoInfo.id, dtoInfo);

        return new Result.Success<>(dtoInfo);
    }

    @Override
    public Result<T> upsertDtoInfo(T dtoInfo) {
        if (database.containsKey(dtoInfo.id)) {
            return updateDtoInfo(dtoInfo);
        } else {
            return addDtoInfo(dtoInfo);
        }
    }

    @Override
    public Result<T> deleteDtoInfo(T dtoInfo) {
        if (database.remove(dtoInfo.id) == null) {
            return new Result.Failure<>(new Exception("API: Failed to delete DtoInfo"));
        }

        return new Result.Success<>(dtoInfo);
    }

    public Map<BaseUUID, T> getAllDtoInfos() {
        return new HashMap<>(database);
    }
}
class BookApi { // Use DSL to define the API (wrapper over in-memory generic API)
    private final InMemoryAPI<Model.DTO.BookInfo> api;

    BookApi() {
        this(new InMemoryAPI<>(new URL("memory://api.book.com"), new HttpClient()));
    }
    BookApi(InMemoryAPI<Model.DTO.BookInfo> api) {
        this.api = api;
    }

    public Result<Model.DTO.BookInfo> getBookInfo(String id) {
        return api.getDtoInfo(fromString(id));
    }
    public Result<Model.DTO.BookInfo> getBookInfo(BookUUID id) {
        return api.getDtoInfo(id);
    }

    public Result<Model.DTO.BookInfo> addBookInfo(Model.DTO.BookInfo bookInfo) {
        return api.addDtoInfo(bookInfo);
    }
    public Result<Model.DTO.BookInfo> updateBookInfo(Model.DTO.BookInfo bookInfo) {
        return api.updateDtoInfo(bookInfo);
    }
    public Result<Model.DTO.BookInfo> upsertBookInfo(Model.DTO.BookInfo bookInfo) {
        return api.upsertDtoInfo(bookInfo);
    }
    public Result<Model.DTO.BookInfo> deleteBookInfo(Model.DTO.BookInfo bookInfo) {
        return api.deleteDtoInfo(bookInfo);
    }

    public Map<BaseUUID, Model.DTO.BookInfo> getAllBookInfos() {
        return new HashMap<>(api.getAllDtoInfos());
    }
}

interface IRepo {
    interface Book extends IRepo {
        Result<Model.Domain.BookInfo> fetchBookInfo(BookUUID id);
        Result<Model.Domain.BookInfo> addBookInfo(Model.Domain.BookInfo bookInfo);
        Result<Model.Domain.BookInfo> updateBookInfo(Model.Domain.BookInfo bookInfo);
        Result<Model.Domain.BookInfo> upsertBookInfo(Model.Domain.BookInfo bookInfo);
    }

    interface User extends IRepo {
        Result<Model.Domain.UserInfo> fetchUserInfo(UserUUID id);
        Result<Model.Domain.UserInfo> updateUserInfo(Model.Domain.UserInfo userInfo);
        Model.Domain.UserInfo upsertUserInfo(Model.Domain.UserInfo userInfo);
    }

    interface Library extends IRepo {
        Result<Model.Domain.LibraryInfo> fetchLibraryInfo(LibraryUUID id);
        Result<Model.Domain.LibraryInfo> updateLibraryInfo(Model.Domain.LibraryInfo libraryInfo);
        Result<Model.Domain.LibraryInfo> upsertLibraryInfo(Model.Domain.LibraryInfo libraryInfo);
    }
}

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
class Repo implements IRepo {
    protected final Log log;

    Repo(Log log) {
        this.log = log;
    }

    // Holds all known Books in the system
    static class Book extends Repo implements IRepo.Book {
        private final BookApi api;
        //private final InMemoryDatabase<Model.Entity.BookInfo> database;
        private final BookDatabase database;

        Book() {
            this(
                new BookApi(),
                new BookDatabase(),
                new Log()
            );
        }
        Book(BookApi api,
                BookDatabase database,
             Log log
        ) {
            super(log);
            this.api = api;
            this.database = database;
        }

        @Override
        public Result<Model.Domain.BookInfo> fetchBookInfo(BookUUID id) {
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
            Result<Model.Entity.BookInfo> resultDB = database.updateBookInfo(bookInfo.toEntity());
            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Entity.BookInfo>) resultDB).exception();
                return new Result.Failure<Model.Domain.BookInfo>(exception);
            }

            return new Result.Success<>(bookInfo);
        }

        @Override
        public Result<Model.Domain.BookInfo> updateBookInfo(Model.Domain.BookInfo bookInfo) {
            log.d(this,"Repo.Book - Updating BookInfo: " + bookInfo);

            Result<Model.Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateType.UPDATE);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Model.Domain.BookInfo> addBookInfo(Model.Domain.BookInfo bookInfo) {
            log.d(this,"Repo.Book - Adding book info: " + bookInfo);

            Result<Model.Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateType.ADD);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Model.Domain.BookInfo> upsertBookInfo(Model.Domain.BookInfo bookInfo) {
            log.d(this,"Repo.Book - Upserting book id: " + bookInfo.id);

            if (database.getBookInfo(bookInfo.id) != null) {
                return updateBookInfo(bookInfo);
            } else {
                return addBookInfo(bookInfo);
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
                    resultApi = api.updateBookInfo(bookInfo.toDTO());// updateBook(bookInfo.toDTO());
                    break;
                case ADD:
                    resultApi = api.addBookInfo(bookInfo.toDTO());
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
                    resultDB = database.updateBookInfo(bookInfo.toEntity());
                    break;
                case ADD:
                    resultDB = database.addBookInfo(bookInfo.toEntity());
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
        public void populateDatabaseWithFakeBookInfo() {
            for (int i = 0; i < 10; i++) {
                database.addBookInfo(
                        new Model.Entity.BookInfo(
                                BookUUID.createFakeBookUUID(i),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );
            }
        }

        public void populateApiWithFakeBookInfo() {
            for (int i = 0; i < 10; i++) {
                Result<Model.DTO.BookInfo> result = api.addBookInfo(
                        new Model.DTO.BookInfo(
                                BookUUID.createFakeBookUUID(i),
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
            for (Map.Entry<BaseUUID, Model.Entity.BookInfo> entry : database.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }

        public void printAPI() {
            for (Map.Entry<BaseUUID, Model.DTO.BookInfo> entry : api.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // Holds User info for all users in the system
    static class User extends Repo implements IRepo.User {
        // Simulate a database on a server somewhere
        private final HashMap<UserUUID, Model.Domain.UserInfo> database = new HashMap<>();

        User(Log log) {
            super(log);
        }

        @Override
        public Result<Model.Domain.UserInfo> fetchUserInfo(UserUUID id) {
            log.d(this,"Repo.User - Fetching user info: " + id);

            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        @Override
        public Result<Model.Domain.UserInfo> updateUserInfo(Model.Domain.UserInfo userInfo) {
            log.d(this,"Repo.User - Updating user info: " + userInfo);

            if (database.containsKey(userInfo.id)) {
                database.put(userInfo.id, userInfo);
                return new Result.Success<>(userInfo);
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        @Override
        public Model.Domain.UserInfo upsertUserInfo(Model.Domain.UserInfo userInfo) {
            database.put(userInfo.id, userInfo);
            return userInfo;
        }

    }

    // Holds Library info for all the libraries in the system
    static class Library extends Repo implements IRepo.Library {
        // simulate a database on server
        private final HashMap<LibraryUUID, Model.Domain.LibraryInfo> database = new HashMap<>();

        Library(Log log) {
            super(log);
        }

        @Override
        public Result<Model.Domain.LibraryInfo> fetchLibraryInfo(LibraryUUID id) {
            log.d(this,"Repo.Library - Fetching library info: " + id);

            // Simulate a network request
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Library not found, id: " + id));
        }

        // Leave for now - not sure if we need this
//        @Override
//        public Model.Domain.LibraryInfo libraryInfo(UUID id) {
//            return database.get(id);
//        }

        @Override
        public Result<Model.Domain.LibraryInfo> updateLibraryInfo(Model.Domain.LibraryInfo libraryInfo) {
            log.d(this,"Repo.Library - updateLibrary, libraryInfo id: " + libraryInfo.id);

            // Simulate a network request
            if (database.containsKey(libraryInfo.id)) {
                database.put(libraryInfo.id, libraryInfo);

                return new Result.Success<>(libraryInfo);
            }

            return new Result.Failure<>(new Exception("Library not found"));
        }

        @Override
        public Result<Model.Domain.LibraryInfo> upsertLibraryInfo(Model.Domain.LibraryInfo libraryInfo) {
            log.d(this,"Repo.Library - Upserting library id: " + libraryInfo.id);

            database.put(libraryInfo.id, libraryInfo);

            return new Result.Success<>(libraryInfo);
        }

        ///////////////////////////////////
        /// Helper methods              ///
        ///////////////////////////////////

        // todo move out of this class?
        public void populateWithFakeBooks(LibraryUUID libraryId, int numberOfBooksToCreate) {
            Model.Domain.LibraryInfo library = database.get(libraryId);

            for (int i = 0; i < numberOfBooksToCreate; i++) {
                Result<BookUUID> result = library.addTestBook(BookUUID.createFakeBookUUID(i), 1);

                if (result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<BookUUID>) result).exception();
                    log.d(this,exception.getMessage());
                }
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

    // log.d(this, "message") will print "ClassName: message"
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
                new BookApi(),
                new BookDatabase(),
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

// These hold the "{Model}Info" for each App Domain Object. (like a DTO for a database row)
class Model {
    transient BaseUUID id;

    Model(BaseUUID id) {
        this.id = id;
    }

    static class Domain extends Model {

        Domain(BaseUUID id) {
            super(id);
        }

        static class BookInfo extends Domain implements ToEntity<Entity.BookInfo>, ToDTO<DTO.BookInfo> {
            transient BookUUID id;
            final String title;
            final String author;
            final String description;

            BookInfo(String id, String title, String author, String description) {
                this(BookUUID.fromString(id), title, author, description);
            }
            BookInfo(BookUUID id, String title, String author, String description) {
                super(id);
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
            transient final UserUUID id;
            final String name;
            final String email;
            final ArrayList<BookUUID> acceptedBooks = new ArrayList<>();

            UserInfo(UserUUID id, String name, String email) {
                super(id);
                this.id = id;
                this.name = name;
                this.email = email;
            }

            public String toString() {
                return "User: " + this.name + " (" + this.email + "), acceptedBooks: " + this.acceptedBooks;
            }
        }

        static class LibraryInfo extends Domain {
            final LibraryUUID id;  // transient because we don't want to serialize this
            final private String name;
            final private BaseUUID.HashMap<UserUUID, ArrayList<BookUUID>> userIdToCheckedOutBookMap;
            final private BaseUUID.HashMap<BookUUID, Integer> bookIdToNumBooksAvailableMap;

            LibraryInfo(LibraryUUID id,
                        String name,
                        BaseUUID.HashMap<UserUUID, ArrayList<BookUUID>> checkoutUserBookMap,
                        BaseUUID.HashMap<BookUUID, Integer> bookIdToNumBooksAvailableMap
            ) {
                super(id);
                this.id = id;
                this.name = name;
                this.userIdToCheckedOutBookMap = checkoutUserBookMap;
                this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
            }
            LibraryInfo(LibraryUUID id, String name) {
                super(id);
                this.id = id;
                this.name = name;
                this.userIdToCheckedOutBookMap = new BaseUUID.HashMap<>();
                this.bookIdToNumBooksAvailableMap = new BaseUUID.HashMap<>();
            }

            public Result<BookUUID> checkOutBookToUser(BookUUID bookId, UserUUID userId) {
                if(!isBookKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known. bookId: " + bookId));
                if(!isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known, userId: " + userId));
                if(!isBookAvailable(bookId)) return new Result.Failure<>(new IllegalArgumentException("book is not available, bookId: " + bookId));
                if(isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user, bookId: " + bookId + ", userId: " + userId));

                try {
                    removeBookFromInventory(bookId, 1);
                    addBookToUser(bookId, userId);
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            public Result<Book> checkOutBookToUser(Book book, User user) {
                Result<BookUUID> checkedOutBookUUID = checkOutBookToUser(new BookUUID(book.id.toUUID()), new UserUUID(user.id.toUUID()));

                if(checkedOutBookUUID instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<BookUUID>) checkedOutBookUUID).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            public Result<BookUUID> checkInBookFromUser(BookUUID bookId, UserUUID userId) {
                if(!isBookKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
                if(!isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
                if(!isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is not checked out by user"));

                try {
                    addBookToInventory(bookId, 1);
                    removeBookFromUser(bookId, userId);
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            public Result<Book> checkInBookFromUser(Book book, User user) {
                Result<BookUUID> returnedBookUUID = checkInBookFromUser(book.id, user.id);

                if(returnedBookUUID instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<BookUUID>) returnedBookUUID).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            public Result<ArrayList<BookUUID>> findBooksCheckedOutByUser(UserUUID user) {
                if(!isUserKnown(user)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));

                return new Result.Success<>(userIdToCheckedOutBookMap.get(user));
            }

            public Result<HashMap<BookUUID, Integer>> calculateAvailableBookIdToCountOfAvailableBooksList() {

                HashMap<BookUUID, Integer> availableBookIdToNumBooksAvailableMap = new HashMap<>();
                for(BookUUID bookId : this.bookIdToNumBooksAvailableMap.keys()) {
                    if(isBookAvailable(bookId)) {
                        availableBookIdToNumBooksAvailableMap.put(bookId, availableBookIdToNumBooksAvailableMap.get(bookId));
                    }
                }

                return new Result.Success<>(availableBookIdToNumBooksAvailableMap);
            }

            //////////////////////////////
            // Public Helper Methods    //
            //////////////////////////////

            public boolean isBookKnown(BookUUID bookId) {
                return bookIdToNumBooksAvailableMap.containsKey(bookId);
            }
            public boolean isBookKnown(Book book) {
                return isBookKnown(new BookUUID(book.id.toUUID()));
            }

            public boolean isUserKnown(UserUUID userId) {
                return userIdToCheckedOutBookMap.containsKey(userId);
            }
            public boolean isUserKnown(User user) {
                return isUserKnown(new UserUUID(user.id.toUUID()));
            }

            public boolean isBookAvailable(BookUUID bookId) {
                return bookIdToNumBooksAvailableMap.get(bookId) > 0;
            }
            public boolean isBookAvailable(Book book) {
                return isBookAvailable(new BookUUID(book.id.toUUID()));
            }

            public boolean isBookCurrentlyCheckedOutByUser(BookUUID bookId, UserUUID userId) {
                return userIdToCheckedOutBookMap.get(userId).contains(bookId);
            }
            public boolean isBookCurrentlyCheckedOutByUser(Book book, User user) {
                return isBookCurrentlyCheckedOutByUser(new BookUUID(book.id.toUUID()), new UserUUID(user.id.toUUID()));
            }

            //////////////////////////////
            // Testing Helper Methods   //
            //////////////////////////////

            protected Result<BookUUID> addTestBook(BookUUID bookId, int quantity) {
                return addBookToInventory(bookId, quantity);
            }

            protected Result<UserUUID> upsertTestUser(UserUUID userId) {
                return upsertUser(userId);
            }

            //////////////////////////////
            // Private Helper Functions //
            //////////////////////////////

            private Result<BookUUID> addBookToInventory(BookUUID bookId, int quantity) {
                if(quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));

                try {
                    if (bookIdToNumBooksAvailableMap.containsKey(bookId)) {
                        bookIdToNumBooksAvailableMap.put(bookId, bookIdToNumBooksAvailableMap.get(bookId) + 1);
                    } else {
                        bookIdToNumBooksAvailableMap.put(bookId, 1);
                    }
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> addBookToInventory(Book book, int quantity) {
                Result<BookUUID> addedBookUUID = addBookToInventory(new BookUUID(book.id.toUUID()), quantity);

                if(addedBookUUID instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<BookUUID>) addedBookUUID).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<BookUUID> removeBookFromInventory(BookUUID bookId, int quantity) {
                if(quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));

                try {
                    if (bookIdToNumBooksAvailableMap.containsKey(bookId)) {
                        bookIdToNumBooksAvailableMap.put(bookId, bookIdToNumBooksAvailableMap.get(bookId) - 1);
                    } else {
                        return new Result.Failure<>(new Exception("Book not in inventory"));
                    }
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> removeBookFromInventory(Book book, int quantity) {
                Result<BookUUID> removedBookUUID = removeBookFromInventory(new BookUUID(book.id.toUUID()), quantity);

                if(removedBookUUID instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<BookUUID>) removedBookUUID).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<BookUUID> addBookToUser(BookUUID bookId, UserUUID userId) {
                if(!isBookKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
                if(!isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
                if(isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user"));

                try {
                    if(userIdToCheckedOutBookMap.containsKey(userId)) {
                        userIdToCheckedOutBookMap.get(userId).add(bookId);
                    } else {
                        userIdToCheckedOutBookMap.put(userId, new ArrayList<>(Arrays.asList(bookId)));
                    }
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> addBookToUser(Book book, User user) {
                Result<BookUUID> addedBookUUID = addBookToUser(new BookUUID(book.id.toUUID()), new UserUUID(user.id.toUUID()));

                if(addedBookUUID instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<BookUUID>) addedBookUUID).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<BookUUID> removeBookFromUser(BookUUID bookId, UserUUID userId) {
                if(!isBookKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
                if(!isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
                if(!isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is not checked out by user"));

                try {
                    userIdToCheckedOutBookMap.get(userId).remove(bookId);
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> removeBookFromUser(Book book, User user) {
                Result<BookUUID> removedBookUUID = removeBookFromUser(new BookUUID(book.id.toUUID()), new UserUUID(user.id.toUUID()));

                if(removedBookUUID instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<BookUUID>) removedBookUUID).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UserUUID> insertUser(UserUUID userId) {
                if(isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is already known"));

                try {
                    userIdToCheckedOutBookMap.put(userId, new ArrayList<>());
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(userId);
            }

            private Result<UserUUID> upsertUser(UserUUID userId) {
                if(isUserKnown(userId)) return new Result.Success<>(userId);

                return insertUser(userId);
            }

            private Result<UserUUID> removeUser(UserUUID userId) {
                if(!isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));

                try {
                    userIdToCheckedOutBookMap.remove(userId);
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(userId);
            }

            public String toString() {
                return this.toPrettyJson();
//                return "Library: " + this.name + " (" + this.id + ")" + "\n" +
//                        "  Available Books: " + this.bookIdToNumBooksAvailableMap + "\n" +
//                        "  Checkout Map: " + this.userIdToCheckedOutBookMap;
            }
        }
    }

    // Data Transfer Objects for APIs
    static class DTO extends Model {
        public DTO(BaseUUID id) {
            super(id);
        }

        static class BookInfo extends DTO implements ToDomain<Domain.BookInfo> {
            BookUUID id;
            final String title;
            final String author;
            final String description;

            BookInfo(BookUUID id, String title, String author, String description) {
                super(id);
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

    // Entities for Databases
    static class Entity extends Model {
        Entity(BaseUUID id) {
            super(id);
        }

        static class BookInfo extends Entity implements ToDomain<Domain.BookInfo> {
            final BookUUID id;
            final String title;
            final String author;
            final String description;

            BookInfo(BookUUID id, String title, String author, String description) {
                super(id);
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

    // Converters between Domain, Entity, and DTO
    interface ToDomain<T extends Model.Domain> {
        T toDomain();
    }
    interface ToEntity<T extends Model.Entity> {
        T toEntity();
    }
    interface ToDTO<T extends Model.DTO> {
        T toDTO();
    }

    public String toPrettyJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}

// Info - Caches the Object "Info" and defines required fetch and update methods
interface Info<T extends Model.Domain> {
    T fetchInfo();                  // Fetches info for object from server
    boolean isInfoFetched();        // Returns true if info has been fetched from server
    Result<T> fetchInfoResult();    // Fetches Result<T> for info object from server
    Result<T> updateInfo(T info);   // Updates info for object to server
    Result<T> refreshInfo();        // Refreshes info for object from server
    String fetchInfoFailureReason();// Returns reason for failure of last fetchInfo() call, or null if successful
}

abstract class IDomainObject<T extends Model.Domain> implements Info<T> {
    BaseUUID id;
    protected T info;
    protected Result<T> infoResult = null;

    // Singletons
    protected Context context = null;
    private Gson gson = null; // convenience reference to the Context Gson singleton object

    // Class of the info<T> (for GSON serialization)
    @SuppressWarnings("unchecked")
    Class<T> infoClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];

    IDomainObject(T info, Context context) {
        if(context == null) throw new IllegalArgumentException("Context cannot be null");
        // this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation

        this.gson = this.context.gson;
        this.info = info;
        this.id = info.id;
    }
    IDomainObject(BaseUUID id, Context context) {
        if(context == null) throw new IllegalArgumentException("Context cannot be null");
        // this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation

        this.context = context;
        this.gson = this.context.gson;
        this.id = id;

    }
    IDomainObject(String json, Context context) {
        if(context == null) throw new IllegalArgumentException("Context cannot be null");
        // this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation

        this.context = context;
        this.gson = this.context.gson;
        this.info = this.gson.fromJson(json, this.infoClass);
        this.id = this.info.id;
    }
    IDomainObject(Context context) {
        this(fromString(UUID.randomUUID().toString()), context);
    }

    // LEAVE for reference, for static Context instance implementation
    //IDomainObject(String json) {
    //    this(json, null);
    //}
    //IDomainObject(T info) {
    //    this(info, null);
    //}
    //IDomainObject(BaseUUID id) { this(id, null);}
    //IDomainObject() {
    //    this(randomUUID(), null);
    //}

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

    public DomainObject(T info, Context context) {
        super(info, context);
    }
    public DomainObject(BaseUUID id, Context context) {
        super(id, context);
    }
    public DomainObject(String json, Context context) {
        super(json, context);
    }

    // Defines how to fetch info from server - Should be overridden/implemented in subclasses
    public Result<T> updateInfo(T info) {
        this.info = info;
        return new Result.Success<>(info);
    }

    @SuppressWarnings("unchecked")
    public Result<T> updateInfoFromJson(String json) {
        context.log.d(this,"Updating info from JSON for " +
                "class: " + this.getClass().getName() + ", " +
                "id: " + this.id.toString());

        try {
            Class<?> infoClass = this.infoClass;
            T infoFromJson = (T) this.context.gson.fromJson(json, infoClass);
            assert infoFromJson.getClass() == this.info.getClass();

            // Update the info object with the new info
            this.info = infoFromJson;

            if(!this.info.id.equals(this.id)) {
                return new Result.Failure<>(new Exception("ID mismatch when updating info from JSON: json.Id=" + this.info.id + " != object.id" + this.id.toString()));
            }

            // cast the info object to the correct type and set the id
            this.info = (T) infoClass.cast(this.info);
            this.info.id = this.id;

            return this.updateInfo(this.info);
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
    BookUUID id;
    private Repo.Book repo = null;

    Book(Model.Domain.BookInfo info, Context context) {
        super(info, context);
        this.repo = this.context.bookRepo();
        this.id = this.info.id;

        context.log.d(this, "Book (" + this.id.toString() + ") created");
    }
    Book(BookUUID id, Context context) {
        super(id, context);
        this.repo = this.context.bookRepo();
        this.id = id;

        context.log.d(this, "Book (" + this.id.toString() + ") created");
    }
    Book(String json, Context context) {
        super(json, context);
        this.repo = this.context.bookRepo();
        this.id = this.info.id;

        context.log.d(this, "Book (" + this.id.toString() + ") created");
    }
    Book(Context context) {
        super(BookUUID.randomUUID(), context);
        this.repo = this.context.bookRepo();
        this.id = this.info.id;

        context.log.d(this, "Book (" + this.id.toString() + ") created");
    }

    // LEAVE for reference, for static Context instance implementation
    // Book(BookUUID id) {
    //     this(id, null);
    // }
    // Book() {
    //     this(BookUUID.randomUUID());
    // }

    @Override
    public Result<Model.Domain.BookInfo> fetchInfoResult() {
        // context.log.d(this,"Book (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchBookInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Model.Domain.BookInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<Model.Domain.BookInfo> updateInfo(Model.Domain.BookInfo updatedInfo) {
        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the repo
        Result<Model.Domain.BookInfo> infoResult = this.repo.updateBookInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<Model.Domain.BookInfo>) infoResult).value();
        return infoResult;
    }
}

class User extends DomainObject<Model.Domain.UserInfo> {
    UserUUID id = null;
    private Repo.User repo = null;

    User(UserUUID id, Context context) {
        super(id, context);
        this.repo = this.context.userRepo();
        this.id = id;

        context.log.d(this,"User (" + this.id.toString() + ") created");
    }
    User(String json, Context context) {
        super(json, context);
        this.repo = this.context.userRepo();
        this.id = this.info.id;

        context.log.d(this,"User (" + this.id.toString() + ") created");
    }
    User(Context context) {
        super(UserUUID.randomUUID(), context);
        this.repo = this.context.userRepo();
        this.id = this.info.id;

        context.log.d(this,"User (" + this.id.toString() + ") created");
    }
    // LEAVE for reference, for static Context instance implementation
    // User(UserUUID id) {
    //     this(id, null);
    // }
    // user() {
    //     this(UserUUID.randomUUID());
    // }

    @Override
    public Result<Model.Domain.UserInfo> fetchInfoResult() {
        // context.log.d(this,"User (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchUserInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Model.Domain.UserInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public Result<Model.Domain.UserInfo> updateInfo(Model.Domain.UserInfo updatedUserInfo) {
        context.log.d(this,"User (" + this.id.toString() + ") - updateInfo,  userInfo: " + updatedUserInfo.toString());

        // Update self optimistically
        super.updateInfo(updatedUserInfo);

        // Update the repo
        Result<Model.Domain.UserInfo> infoResult = this.repo.updateUserInfo(updatedUserInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<Model.Domain.UserInfo>) infoResult).value();
        return infoResult;
    }

    public Result<ArrayList<BookUUID>> receiveBook(@NotNull Book book) {
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

    public Result<ArrayList<BookUUID>> returnBook(Book book) {
        context.log.d(this,"User (" + this.id.toString() + ") - returnBook,  book: " + book.id.toString() + " to user: " + this.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check user has accepted book
        if (!this.info.acceptedBooks.contains(book.id)) {
            return new Result.Failure<>(new Exception("User has not accepted book, bookId = " + book.id.toString()));
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

    public Result<ArrayList<BookUUID>> giveBookToUser(Book book, User receivingUser) {
        context.log.d(this,"User (" + this.id.toString() + ") - giveBookToUser,  book: " + book.id.toString() + ", user: " + receivingUser.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check user has accepted book
        if (!this.info.acceptedBooks.contains(book.id)) {
            return new Result.Failure<>(new Exception("User has not accepted book, bookId = " + book.id.toString()));
        }

        // Remove the Given book
        this.info.acceptedBooks.remove(book.id);

        // Update UserInfo
        Result<Model.Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.UserInfo>) result).exception());
        }

        // Give book to user
        Result<ArrayList<BookUUID>> result2 = receivingUser.receiveBook(book);
        if (result2 instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<BookUUID>>) result2).exception());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }

    public Result<ArrayList<BookUUID>> checkoutBookFromLibrary(
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
    LibraryUUID id = null;
    private Repo.Library repo = null;

    Library(Model.Domain.LibraryInfo info, Context context) {
        super(info, context);
        this.repo = this.context.libraryRepo();
        this.id = info.id;

        context.log.d(this,"Library (" + this.id.toString() + ") created");
    }
    Library(LibraryUUID id, Context context) {
        super(id, context);
        this.repo = this.context.libraryRepo();
        this.id = id;

        context.log.d(this,"Library (" + this.id.toString() + ") created");
    }
    Library(String json, Context context) {
        super(json, context);
        this.repo = this.context.libraryRepo();
        this.id = this.info.id;

        context.log.d(this,"Library (" + this.id.toString() + ") created");
    }
    Library(Context context) {
        this(LibraryUUID.randomUUID(), context);
    }

    // LEAVE for reference, for static Context instance implementation
    // Library() {
    //     this(LibraryUUID.randomUUID());
    // }

    @Override
    public Result<Model.Domain.LibraryInfo> fetchInfoResult() {
        // context.log.d(this,"Library (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchLibraryInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Model.Domain.LibraryInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<Model.Domain.LibraryInfo> updateInfo(Model.Domain.LibraryInfo updatedInfo) {
        // context.log.d(this,"Library (" + this.id.toString() + ") - updateInfo, newInfo: " + newInfo.toString());  // LEAVE for debugging

        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the Repo
        Result<Model.Domain.LibraryInfo> infoResult = this.repo.updateLibraryInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with Repo result
        this.info = ((Result.Success<Model.Domain.LibraryInfo>) infoResult).value();
        return infoResult;
    }

    ////////////////////////////
    // Library Domain Methods //
    ////////////////////////////

    public Result<Book> checkOutBookToUser(@NotNull Book book, @NotNull User user) {
        context.log.d(this, format("Library (%s) - checkOutBookToUser, user: %s, book: %s", this.id.toString(), user.id.toString(), book.id.toString()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + user.id));
        }

        // Check out Book to User
        Result<Book> checkOutBookresult = this.info.checkOutBookToUser(book, user);
        if (checkOutBookresult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) checkOutBookresult).exception());
        }

        // User receives Book
        Result<ArrayList<BookUUID>> receiveBookResult = user.receiveBook(book);
        if (receiveBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<BookUUID>>) receiveBookResult).exception());
        }

        // Update the Info
        Result<Model.Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    public Result<Book> checkInBookFromUser(Book book, User user) {
        context.log.d(this, format("Library (%s) - checkInBookFromUser, book %s from user %s\n", this.id, book.id, user.id));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + user.id));
        }

        Result<Book> checkInBookResult = this.info.checkInBookFromUser(book, user);
        if (checkInBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) checkInBookResult).exception());
        }

        Result<ArrayList<BookUUID>> userReturnedBookResult = user.returnBook(book);
        if (userReturnedBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<BookUUID>>) userReturnedBookResult).exception());
        }

        // Update the Info
        Result<Model.Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    // Domain enforces the rule: if a user is not known, they are added as a new user.
    public boolean isUnableToFindOrAddUser(User user) {
        context.log.d(this, format("Library (%s) - isUnableToFindOrAddUser user: %s", this.id, user.id));
        if (fetchInfoFailureReason() != null) return true;

        if (isKnownUser(user)) {
            return false;
        }

        // Create a new User entry in the Library
        Result<UserUUID> upsertUserResult = this.info.upsertTestUser(user.id);
        if (upsertUserResult instanceof Result.Failure) {
            return true;
        }

        return false;
    }

    public boolean isKnownBook(Book book) {
        context.log.d(this, format("Library(%s) - hasBook %s\n", this.id, book.id));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookKnown(book);
    }

    public boolean isKnownUser(User user) {
        context.log.d(this, format("Library (%s) - isKnownUser %s", this.id, user.id));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isUserKnown(user);
    }

    public boolean isBookAvailable(Book book) {
        context.log.d(this, format("Library (%s) - hasBookAvailable %s\n", this.id, book.id));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookAvailable(book);
    }

    public Result<ArrayList<Book>> findBooksCheckedOutByUser(User user) {
        context.log.d(this, format("Library (%s) - findBooksCheckedOutByUser %s\n", this.id, user));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Make sure User is Known
        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + user.id));
        }

        Result<ArrayList<BookUUID>> entriesResult = this.info.findBooksCheckedOutByUser(user.id);
        if (entriesResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<BookUUID>>) entriesResult).exception());
        }

        // Convert BookUUIDs to Books
        ArrayList<BookUUID> bookIds = ((Result.Success<ArrayList<BookUUID>>) entriesResult).value();
        ArrayList<Book> books = new ArrayList<>();
        for (BookUUID entry : bookIds) {
            books.add(new Book(entry, context));
        }

        return new Result.Success<>(books);
    }

    public Result<HashMap<Book, Integer>> calculateAvailableBookIdToNumberAvailableList() {
        context.log.d(this, "Library (" + this.id + ") - calculateAvailableBooksAndAmountOnHand\n");
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<HashMap<BookUUID, Integer>> entriesResult = this.info.calculateAvailableBookIdToCountOfAvailableBooksList();
        if (entriesResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<HashMap<BookUUID, Integer>>) entriesResult).exception());
        }

        // Convert BookUUIDs to Books
        HashMap<BookUUID, Integer> bookIdToNumberAvailable = ((Result.Success<HashMap<BookUUID, Integer>>) entriesResult).value();
        HashMap<Book, Integer> bookToNumberAvailable = new HashMap<>();
        for (Map.Entry<BookUUID, Integer> entry : bookIdToNumberAvailable.entrySet()) {
            bookToNumberAvailable.put(new Book(entry.getKey(), context), entry.getValue());
        }

        return new Result.Success<>(bookToNumberAvailable);
    }

    public Result<Book> addTestBookToLibrary(Book book, Integer count) {
        context.log.d(this, format("Library (%s) - addTestBookToLibrary count: %s, book: %s\n", this.id, count, book));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<BookUUID> addBookResult =  this.info.addTestBook(book.id, count);
        if (addBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<BookUUID>) addBookResult).exception());
        }

        // Update the Info
        Result<Model.Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
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

        ctx.log.d(this,"Populating Book DB and API");
        PopulateFakeBookInfoInContextBookRepoDBandAPI(ctx);

        Populate_And_Poke_Book:
        if(false){
            // Create a book object (it only has an id)
            Book book = new Book(BookUUID.fromBaseUUID(createFakeBaseUUID(1)), ctx);
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
            Book book2 = new Book(BookUUID.fromBaseUUID(createFakeBaseUUID(99)), ctx);
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
            LibraryUUID libraryInfoId = ((Result.Success<Model.Domain.LibraryInfo>) libraryInfo).value().id;
            ctx.log.d(this,"Library Created --> id: " +
                    ((Result.Success<Model.Domain.LibraryInfo>) libraryInfo).value().id
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
            final Book book1 = new Book(BookUUID.fromBaseUUID(createFakeBaseUUID(1)), ctx);
            final Book book2 = new Book(BookUUID.fromBaseUUID(createFakeBaseUUID(2)), ctx);

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
                            ((Result.Success<Book>) result).value().id
                    );
                }

                final Result<Book> result2 = library1.checkOutBookToUser(book2, user1);
                if (result2 instanceof Result.Failure) {
                    ctx.log.d(this,"Checked out book FAILURE--> " +
                            ((Result.Failure<Book>) result2).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Checked out book SUCCESS --> " +
                            ((Result.Success<Book>) result2).value().id
                    );
                }

//                library1.DumpDB(ctx);
            }

            Get_Available_Books_And_Counts_In_Library:
            if (false) {
                ctx.log.d(this,"\nGetting available books and counts in library:");

                final Result<HashMap<Book, Integer>> availableBookToNumAvailableResult =
                        library1.calculateAvailableBookIdToNumberAvailableList();
                if (availableBookToNumAvailableResult instanceof Result.Failure) {
                    ctx.log.d(this,"AvailableBookIdCounts FAILURE! --> " +
                            ((Result.Failure<HashMap<Book, Integer>>) availableBookToNumAvailableResult)
                                    .exception().getMessage()
                    );

                    break Get_Available_Books_And_Counts_In_Library;
                }

                // create objects and populate info for available books
                assert availableBookToNumAvailableResult instanceof Result.Success;
                final HashMap<Book, Integer> availableBooks = ((Result.Success<HashMap<Book, Integer>>) availableBookToNumAvailableResult).value();

                // Print out available books
                ctx.log.d(this,"\nAvailable Books in Library:");
                for (Map.Entry<Book, Integer> availableBook : availableBooks.entrySet()) {
                    final Book book3 = new Book(availableBook.getKey().id, ctx);

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
                                        " >> num available: " + availableBook.getValue()
                        );
                    }
                }
                ctx.log.d(this,"Total Available Books (unique UUIDs): " + availableBooks.size());
                ctx.log.d(this,"\n");
            }

            Get_books_checked_out_by_user:
            if (false) {
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
            if (false) {
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
            if (true) {
                ctx.log.d(this,"Load Library from Json: ");

//                Library library2 = new Library(ctx); // creates using a random UUID, but no info. Must load from json.
                Library library2 = new Library(library1.id, ctx);
                ctx.log.d(this, library2.toPrettyJson());
                Result<Model.Domain.LibraryInfo> library2Result = library2.updateInfoFromJson(
                        "{\n" +
                        "  \"name\": \"Ronald Reagan Library\",\n" +
                        "  \"userIdToCheckedOutBookMap\": {\n" +
                        "    \"00000000-0000-0000-0000-000000000001\": [\n" +
                        "      {\n" +
                        "        \"uuid\": \"00000000-0000-0000-0000-000000000002\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"bookIdToNumBooksAvailableMap\": {\n" +
                        "    \"00000000-0000-0000-0000-000000000010\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000011\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000012\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000013\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000014\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000015\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000016\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000017\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000018\": 50,\n" +
                        "    \"00000000-0000-0000-0000-000000000019\": 50\n" +
                        "  },\n" +
                        "  \"id\": {\n" +
                        "    \"uuid\": \"00000000-0000-0000-0000-000000000001\"\n" +
                        "  }\n" +
                        "}"
                );
                if(library2Result instanceof Result.Failure) {
                    ctx.log.d(this,"Library2 Load Error: " +
                            ((Result.Failure<Model.Domain.LibraryInfo>) library2Result).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Library2 Load Success: " +
                            ((Result.Success<Model.Domain.LibraryInfo>) library2Result).value()
                    );

                    ctx.log.d(this,"\nResults of Library2 json load:");
                    ctx.log.d(this,library2.toPrettyJson());
                }
            }

            Check_out_Book_via_User:
            if (false) {
                final User user2 = new User(createFakeUserInfoInContextUserRepo(2, ctx).id, ctx);
                final Result<Model.Domain.BookInfo> book12Result = addFakeBookInfoInContextBookRepo(12, ctx);

                if (book12Result instanceof Result.Failure) {
                    ctx.log.d(this,"Book Error: " +
                            ((Result.Failure<Model.Domain.BookInfo>) book12Result).exception().getMessage()
                    );
                } else {

                    final BookUUID book12id = ((Result.Success<Model.Domain.BookInfo>) book12Result).value().id;
                    final Book book12 = new Book(book12id, ctx);

                    ctx.log.d(this,"\nCheck out book " + book12id + " to user " + user2.id);

                    final Result<Book> book12UpsertResult = library1.addTestBookToLibrary(book12, 1);
                    if (book12UpsertResult instanceof Result.Failure) {
                        ctx.log.d(this,"Upsert Book Error: " +
                                ((Result.Failure<Book>) book12UpsertResult).exception().getMessage()
                        );
                    }

                    final Result<ArrayList<BookUUID>> booksAcceptedByUser = user2.checkoutBookFromLibrary(book12, library1);
                    if (booksAcceptedByUser instanceof Result.Failure) {
                        ctx.log.d(this,"Checkout book FAILURE --> " +
                                ((Result.Failure<ArrayList<BookUUID>>) booksAcceptedByUser).exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,"Checkout Book SUCCESS --> booksAcceptedByUser:" +
                                ((Result.Success<ArrayList<BookUUID>>) booksAcceptedByUser).value()
                        );
                    }
                }
            }


        }
    }

    //////////////////////////////////////////////////////////////////////
    /////////////////////////// Helper Methods ///////////////////////////

    private void PopulateFakeBookInfoInContextBookRepoDBandAPI(Context context) {
        context.bookRepo().populateDatabaseWithFakeBookInfo();
        context.bookRepo().populateApiWithFakeBookInfo();
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
                .upsertLibraryInfo(
                        new Model.Domain.LibraryInfo(
                            LibraryUUID.fromBaseUUID(createFakeBaseUUID(someNumber)),
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
                .upsertUserInfo(new Model.Domain.UserInfo(
                        UserUUID.fromBaseUUID(createFakeBaseUUID(someNumber)),
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
                .upsertBookInfo(bookInfo);
    }

    private Model.Domain.BookInfo createFakeBookInfo(String uuidStr, final Integer id) {
        Integer fakeId = id;
        if (fakeId == null) fakeId = 1;

        BookUUID uuid;
        if (uuidStr == null)
            uuid = BookUUID.createFakeBookUUID(fakeId);
        else
            uuid = (BookUUID) fromString(uuidStr);

        return new Model.Domain.BookInfo(
                uuid,
                "Book " + fakeId,
                "Author " + fakeId,
                "Description " + fakeId
        );
    }
}