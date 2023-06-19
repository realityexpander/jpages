package org.elegantobjects.jpages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.HashMap;

import static java.lang.String.format;
//import static org.elegantobjects.jpages.BaseUUID.*;
import static org.elegantobjects.jpages.Context.ContextType.*;
import static org.elegantobjects.jpages.Model.*;

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
                    // breaks the loop
                    // break; // todo is this a better way to break the loop?
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
    @SuppressWarnings("FieldCanBeLocal")
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


// Marker interface for Domain classes that use UUID2.
interface DomainUUID {} // Keep this in global namespace to reduce clutter at declaration sites

// UUID2 is a type-safe wrapper for UUIDs.
// It is used to prevent passing UUIDs of different types to methods that expect a specific type of UUID.
class UUID2<TDomainUUID extends DomainUUID> implements DomainUUID {
    private UUID uuid;

    UUID2(UUID uuid, TDomainUUID domainUUID) {
        this.uuid = uuid;
    }
    @SuppressWarnings("unchecked")
    UUID2(TDomainUUID domainUUID) {
        this.uuid = domainUUID instanceof UUID2
                    ? ((UUID2<DomainUUID>) domainUUID).uuid()
                    : null;
    }
    UUID2(UUID uuid) {
        this.uuid = uuid;
    }

    // simple getter
    public UUID uuid() {return uuid;}

    public UUID toUUID() {
        return new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public boolean equals(UUID2<DomainUUID> other) {
        return (other).uuid.equals(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    // Should only be used after importing JSON and ID is not set properly (why??? idk)
    protected void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String toString() {
        return uuid.toString();
    }

    public static <T extends DomainUUID> UUID2<T> fromString(String uuidStr) {
        return new UUID2<>(UUID.fromString(uuidStr));
    }

    public static <T extends DomainUUID> UUID2<T> fromUUID(UUID uuid) {
        return new UUID2<>(uuid);
    }

    public static <TDomainUUID extends DomainUUID> UUID2<TDomainUUID> randomUUID2() {
        return new UUID2<>(UUID.randomUUID());
    }

    public static <TDomainUUID extends DomainUUID> @NotNull UUID2<TDomainUUID> createFakeDomainUUID2(final Integer id) {
        Integer idNotNull = id;
        if (idNotNull == null) idNotNull = 1;

        // convert to string and add pad with 11 leading zeros
        final String str = format("%011d", idNotNull);

        return fromString("00000000-0000-0000-0000-" + str);
    }

    public UUID2<DomainUUID> toDomainUUID2() {
        return new UUID2<>(this.uuid);
    }

    // Utility HashMap class for storing UUID2 objects to Entity objects
    // Wrapper for HashMap where the `key` hash is the common UUID value stored in UUID class
    // (Instead of the `hash()` of UUID<{type}> object itself)
    static class HashMap<TDomainUUID extends DomainUUID, TEntity> extends java.util.HashMap<UUID, TEntity> {
        private static final long serialVersionUID = 0x7723L;

        public <T extends DomainUUID, U extends Entity> HashMap(HashMap<T, TEntity> database2) {
            super();
            this.putAll(database2);
        }
//        public <TDomainUUID extends DomainUUID, TEntity> HashMap(HashMap<TDomainUUID, TEntity> database2) { // todo will this work?
//            super();
//            this.putAll(database2);
//        }
        HashMap() {
            super();
        }

        public TEntity get(String baseUUIDStr) {
            return get(UUID.fromString(baseUUIDStr));
        }
        public TEntity get(UUID2<TDomainUUID> uuid) {
            return get(uuid.uuid);
        }
        public TEntity get(UUID uuid) { // allow UUIDs to be used as keys, but not recommended.
            return super.get(uuid);
        }

        public TEntity put(String baseUUIDStr, TEntity value) {
            return put(UUID2.fromString(baseUUIDStr), value);
        }
        public TEntity put(UUID2<TDomainUUID> uuid, TEntity value) {
            return put(uuid.uuid, value);
        }
        public TEntity put(UUID uuid, TEntity value) { // allow UUIDs to be used as keys, but not recommended.
            return super.put(uuid, value);
        }

        public TEntity remove(String baseUUIDStr) {
            return remove(UUID.fromString(baseUUIDStr));
        }
        public TEntity remove(UUID2<TDomainUUID> uuid) {
            return remove(uuid.uuid);
        }
        public TEntity remove(UUID uuid) { // allow UUIDs to be used as keys, but not recommended.
            return super.remove(uuid);
        }

        public boolean containsKey(String baseUUIDStr) {
            return containsKey(UUID.fromString(baseUUIDStr));
        }
        public boolean containsKey(UUID2<TDomainUUID> uuid) { return super.containsKey(uuid.uuid); }

        public Set<TDomainUUID> keys() throws RuntimeException {
            Set<UUID> uuidSet = super.keySet();
            Set<TDomainUUID> domainUUIDSet = new HashSet<>();

            // Convert UUIDs to TDomainUUIDs
            try {
                for (UUID uuid : uuidSet) {
                    @SuppressWarnings({"unchecked"})
                    TDomainUUID baseUUID = (TDomainUUID) UUID2.fromUUID(uuid);
                    domainUUIDSet.add(baseUUID);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.keys(): Failed to convert UUID to UUID2<TDomainUUID>, uuidSet: " + uuidSet);
            }

            return domainUUIDSet;
        }
    }
}

// DB uses Model.Entities
interface IDatabase<TDomainUUID extends DomainUUID, TEntity extends Entity> {
    Result<TEntity> getEntityInfo(UUID2<TDomainUUID> id);
    Result<TEntity> updateEntityInfo(TEntity entityInfo);
    Result<TEntity> addEntityInfo(TEntity entityInfo);
    Result<TEntity> upsertEntityInfo(TEntity entityInfo);
    Result<TEntity> deleteEntityInfo(TEntity entityInfo);
    Map<UUID2<TDomainUUID>, TEntity> getAllEntityInfo(); // todo UUID2 keep
}
@SuppressWarnings("FieldCanBeLocal")
class InMemoryDatabase<TEntity extends Entity, TDomainUUID extends DomainUUID> implements IDatabase<TDomainUUID, TEntity> {
    private final URL url;
    private final String user;
    private final String password;

    // Simulate a database
    private final UUID2.HashMap<TDomainUUID, TEntity> database2 = new UUID2.HashMap<>();

    InMemoryDatabase(URL url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }
    InMemoryDatabase() {
        this(new URL("memory://hash.map"), "admin", "password");
    }

    @Override
    public Result<TEntity> getEntityInfo(UUID2<TDomainUUID> id) {
        // Simulate the request
        TEntity infoResult =  database2.get(id);
        if (infoResult == null) {
            return new Result.Failure<>(new Exception("DB: Failed to get entityInfo, id: " + id));
        }

        return new Result.Success<>(infoResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<TEntity> updateEntityInfo(TEntity entityInfo) {
        // Simulate the request
        if (database2.put((UUID2<TDomainUUID>) entityInfo.id, entityInfo) == null) {
            return new Result.Failure<>(new Exception("DB: Failed to update entityInfo, entityInfo: " + entityInfo));
        }

        return new Result.Success<>(entityInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TEntity> addEntityInfo(TEntity entityInfo) {
        // Simulate the request
        if (database2.containsKey((UUID2<TDomainUUID>) entityInfo.id)) {
            return new Result.Failure<>(new Exception("DB: Entity already exists, entityInfo: " + entityInfo));
        }
        if (database2.put((UUID2<TDomainUUID>) entityInfo.id, entityInfo) == null) {
            return new Result.Failure<>(new Exception("DB: Failed to add entity, entityInfo: " + entityInfo));
        }

        return new Result.Success<>(entityInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TEntity> upsertEntityInfo(TEntity entityInfo) {
        if (database2.containsKey((UUID2<TDomainUUID>) entityInfo.id)) {
            return updateEntityInfo(entityInfo);
        } else {
            return addEntityInfo(entityInfo);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TEntity> deleteEntityInfo(TEntity entityInfo) {
        if (database2.remove((UUID2<TDomainUUID>) entityInfo.id) == null) {
            return new Result.Failure<>(new Exception("DB: Failed to delete entityInfo, entityInfo: " + entityInfo));
        }

        return new Result.Success<>(entityInfo);
    }

    @Override
    public Map<UUID2<TDomainUUID>, TEntity> getAllEntityInfo() {

        Map<UUID2<TDomainUUID>, TEntity> map = new HashMap<>();
        for (Map.Entry<UUID, TEntity> entry : database2.entrySet()) {
            map.put(new UUID2<>(entry.getKey()), entry.getValue());
        }

        return map;
    }
}
class BookDatabase  {
    private final IDatabase<Book, Entity.BookInfo> database;

    BookDatabase(IDatabase<Book, Entity.BookInfo> database) {
        this.database = database;
    }
    BookDatabase() {
        this(new InMemoryDatabase<>(new URL("memory://db.book.com"), "user", "password"));
    }

    public Result<Entity.BookInfo> getBookInfo(UUID2<Book> id) {
        return database.getEntityInfo(id);
    }

    public Result<Entity.BookInfo> updateBookInfo(Entity.BookInfo bookInfo) {
        return database.updateEntityInfo(bookInfo);
    }

    public Result<Entity.BookInfo> addBookInfo(Entity.BookInfo bookInfo) {
        return database.addEntityInfo(bookInfo);
    }

    public Result<Entity.BookInfo> upsertBookInfo(Entity.BookInfo bookInfo) {
        return database.upsertEntityInfo(bookInfo);
    }

    public Result<Entity.BookInfo> deleteBookInfo(Entity.BookInfo bookInfo) {
        return database.deleteEntityInfo(bookInfo);
    }

//    public Map<UUID2<Book>, Model.Entity.BookInfo> getAllBookInfos() {  // todo UUID2 remove
//        return database.getAllEntityInfo();
//    }
    public Map<UUID2<Book>, Entity.BookInfo> getAllBookInfos() {  // todo UUID2 keep
        return database.getAllEntityInfo();
    }
}

// API uses Model.DTOs
interface IAPI<TDomainUUID extends DomainUUID, TEntity extends DTO> {
    Result<TEntity> getDtoInfo(UUID2<TDomainUUID> id);
    Result<TEntity> getDtoInfo(String id);
    Result<TEntity> addDtoInfo(TEntity dtoInfo);
    Result<TEntity> updateDtoInfo(TEntity dtoInfo);
    Result<TEntity> upsertDtoInfo(TEntity dtoInfo);
    Result<TEntity> deleteDtoInfo(TEntity dtoInfo);
}
//class InMemoryAPI<T extends Model.DTO> implements IAPI<T> {
@SuppressWarnings("FieldCanBeLocal")
class InMemoryAPI<TDomainUUID extends DomainUUID, TEntity extends DTO> implements IAPI<TDomainUUID, TEntity> {
    private final URL url;
    private final HttpClient client;

    // Simulate an API database
    private final UUID2.HashMap<TDomainUUID, TEntity> database = new UUID2.HashMap<>(); // todo UUID2 keep

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
    public Result<TEntity> getDtoInfo(String id) {
        try {
            @SuppressWarnings("unchecked")
            UUID2<TDomainUUID> uuid = (UUID2<TDomainUUID>) UUID2.fromString(id);
            return getDtoInfo(uuid);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }
    }

    @Override
    public Result<TEntity> getDtoInfo(UUID2<TDomainUUID> id) {
        // Simulate the network request
        if (!database.containsKey(id)) {
            return new Result.Failure<>(new Exception("API: Entity not found, id=" + id));
        }

        return new Result.Success<>(database.get(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TEntity> updateDtoInfo(TEntity dtoInfo) {
        if (database.put((UUID2<TDomainUUID>) dtoInfo.id, dtoInfo) == null) {
            return new Result.Failure<>(new Exception("API: Failed to update Entity, id=" + dtoInfo.id));
        }

        return new Result.Success<>(dtoInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TEntity> addDtoInfo(TEntity dtoInfo) {
        if (database.containsKey((UUID2<TDomainUUID>) dtoInfo.id)) {
            return new Result.Failure<>(new Exception("API: DtoInfo already exists, use update, id=" + dtoInfo.id));
        }

        database.put((UUID2<TDomainUUID>) dtoInfo.id, dtoInfo);

        return new Result.Success<>(dtoInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TEntity> upsertDtoInfo(TEntity dtoInfo) {
        if (database.containsKey((UUID2<TDomainUUID>) dtoInfo.id)) {
            return updateDtoInfo(dtoInfo);
        } else {
            return addDtoInfo(dtoInfo);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TEntity> deleteDtoInfo(TEntity dtoInfo) {
        if (database.remove((UUID2<TDomainUUID>) dtoInfo.id) == null) {
            return new Result.Failure<>(new Exception("API: Failed to delete DtoInfo"));
        }

        return new Result.Success<>(dtoInfo);
    }

//    public Map<TDomainUUID, TEntity> getAllDtoInfos() {
//
//        Map<TDomainUUID, TEntity> map = new HashMap<>();
//        for (Map.Entry<UUID, TEntity> entry : database.entrySet()) {
//            map.put((TDomainUUID) UUID2.fromUUID(entry.getKey()).toBaseUUID2(),
//                     entry.getValue());
//        }
//
//        return map;
//    }

    public Map<UUID2<TDomainUUID>, TEntity> getAllDtoInfos() {
        Map<UUID2<TDomainUUID>, TEntity> map = new HashMap<>();

        for (Map.Entry<UUID, TEntity> entry : database.entrySet()) {
            map.put(new UUID2<>(entry.getKey()), entry.getValue());
        }

        return map;
    }
}
class BookApi { // Use DSL to define the API (wrapper over in-memory generic API)
    private final InMemoryAPI<Book, DTO.BookInfo> api;

    BookApi() {
        this(new InMemoryAPI<>(new URL("memory://api.book.com"), new HttpClient()));
    }
    BookApi(InMemoryAPI<Book, DTO.BookInfo> api) {
        this.api = api;
    }

    public Result<DTO.BookInfo> getBookInfo(String id) {
        return api.getDtoInfo(id);
    }
    public Result<DTO.BookInfo> getBookInfo(UUID2<Book> id) {
        return api.getDtoInfo(id);
    }

    public Result<DTO.BookInfo> addBookInfo(DTO.BookInfo bookInfo) {
        return api.addDtoInfo(bookInfo);
    }
    public Result<DTO.BookInfo> updateBookInfo(DTO.BookInfo bookInfo) {
        return api.updateDtoInfo(bookInfo);
    }
    public Result<DTO.BookInfo> upsertBookInfo(DTO.BookInfo bookInfo) {
        return api.upsertDtoInfo(bookInfo);
    }
    public Result<DTO.BookInfo> deleteBookInfo(DTO.BookInfo bookInfo) {
        return api.deleteDtoInfo(bookInfo);
    }

    public Map<UUID2<Book>, DTO.BookInfo> getAllBookInfos() {
        return new HashMap<>(api.getAllDtoInfos());
    }
}

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
interface IRepo {
    interface BookRepo extends IRepo {
        Result<Domain.BookInfo> fetchBookInfo(UUID2<Book> id);
        Result<Domain.BookInfo> addBookInfo(Domain.BookInfo bookInfo);
        Result<Domain.BookInfo> updateBookInfo(Domain.BookInfo bookInfo);
        Result<Domain.BookInfo> upsertBookInfo(Domain.BookInfo bookInfo);
    }

    interface UserRepo extends IRepo {
        Result<Domain.UserInfo> fetchUserInfo(UUID2<User> id);
        Result<Domain.UserInfo> updateUserInfo(Domain.UserInfo userInfo);
        Domain.UserInfo upsertUserInfo(Domain.UserInfo userInfo);
    }

    interface LibraryRepo extends IRepo {
        Result<Domain.LibraryInfo> fetchLibraryInfo(UUID2<Library> id);
        Result<Domain.LibraryInfo> updateLibraryInfo(Domain.LibraryInfo libraryInfo);
        Result<Domain.LibraryInfo> upsertLibraryInfo(Domain.LibraryInfo libraryInfo);
    }
}
class Repo implements IRepo {
    protected final Log log;

    Repo(Log log) {
        this.log = log;
    }

    // Business logic for Book Repo (converts to/from DTOs/Entities/Domains)
    static class BookRepo extends Repo implements IRepo.BookRepo {
        private final BookApi api;
        //private final InMemoryDatabase<Model.Entity.BookInfo> database;
        private final BookDatabase database;

        BookRepo() {
            this(
                new BookApi(),
                new BookDatabase(),
                new Log()
            );
        }
        BookRepo(BookApi api,
                 BookDatabase database,
                 Log log
        ) {
            super(log);
            this.api = api;
            this.database = database;
        }

        @Override
        public Result<Domain.BookInfo> fetchBookInfo(UUID2<Book> id) {
            log.d(this,"Repo.BookRepo.fetchBookInfo " + id);

            // Make the request to API
            Result<DTO.BookInfo> bookInfoApiResult = api.getBookInfo(id);
            if (bookInfoApiResult instanceof Result.Failure) {

                // If API fails, try to get from cached DB
                Result<Entity.BookInfo> bookInfoResult = database.getBookInfo(id);
                if (bookInfoResult instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<Entity.BookInfo>) bookInfoResult).exception();
                    return new Result.Failure<Domain.BookInfo>(exception);
                }

                Entity.BookInfo bookInfo = ((Result.Success<Entity.BookInfo>) bookInfoResult).value();
                return new Result.Success<>(bookInfo.toDomain());
            }

            // Convert to Domain Model
            Domain.BookInfo bookInfo = ((Result.Success<DTO.BookInfo>) bookInfoApiResult).value().toDomain();

            // Cache to Local DB
            Result<Entity.BookInfo> resultDB = database.updateBookInfo(bookInfo.toEntity());
            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Entity.BookInfo>) resultDB).exception();
                return new Result.Failure<Domain.BookInfo>(exception);
            }

            return new Result.Success<>(bookInfo);
        }

        @Override
        public Result<Domain.BookInfo> updateBookInfo(Domain.BookInfo bookInfo) {
            log.d(this,"Repo.BookRepo - Updating BookInfo: " + bookInfo);

            Result<Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateType.UPDATE);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Domain.BookInfo> addBookInfo(Domain.BookInfo bookInfo) {
            log.d(this,"Repo.BookRepo - Adding book info: " + bookInfo);

            Result<Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateType.ADD);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Domain.BookInfo> upsertBookInfo(Domain.BookInfo bookInfo) {
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

        private Result<Domain.BookInfo> saveBookToApiAndDB(
                Domain.BookInfo bookInfo,
                UpdateType updateType
        ) {
            log.d(this,"Repo.BookRepo - saveBookToApiAndDB, updateType: " + updateType + ", id: " + bookInfo.id);

            // Make the API request
            Result<DTO.BookInfo> resultApi;
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
                Exception exception = ((Result.Failure<DTO.BookInfo>) resultApi).exception();
                return new Result.Failure<>(exception);
            }

            // Save to Local DB
            Result<Entity.BookInfo> resultDB;
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
                Exception exception = ((Result.Failure<Entity.BookInfo>) resultDB).exception();
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
                        new Entity.BookInfo(
                                UUID2.createFakeDomainUUID2(i),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );
            }
        }

        public void populateApiWithFakeBookInfo() {
            for (int i = 0; i < 10; i++) {
                Result<DTO.BookInfo> result = api.addBookInfo(
                        new DTO.BookInfo(
                                UUID2.createFakeDomainUUID2(i),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );

                if (result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<DTO.BookInfo>) result).exception();
                    log.d(this,exception.getMessage());
                }
            }
        }

        public void printDB() {
            for (Map.Entry<UUID2<Book>, Entity.BookInfo> entry : database.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }

        public void printAPI() {
            for (Map.Entry<UUID2<Book>, DTO.BookInfo> entry : api.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // Holds User info for all users in the system
    static class UserRepo extends Repo implements IRepo.UserRepo {
        // Simulate a database on a server somewhere
        private final UUID2.HashMap<User, Domain.UserInfo> database = new UUID2.HashMap<>();

        UserRepo(Log log) {
            super(log);
        }

        @Override
        public Result<Domain.UserInfo> fetchUserInfo(UUID2<User> id) {
            log.d(this,"Repo.User - Fetching user info: " + id);

            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        @Override
        public Result<Domain.UserInfo> updateUserInfo(Domain.UserInfo userInfo) {
            log.d(this,"Repo.User - Updating user info: " + userInfo);

            if (database.containsKey(userInfo.id)) {
                database.put(userInfo.id, userInfo);
                return new Result.Success<>(userInfo);
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        @Override
        public Domain.UserInfo upsertUserInfo(Domain.UserInfo userInfo) {
            log.d(this,"Repo.User - Upserting user info: " + userInfo);

            database.put(userInfo.id, userInfo);
            return userInfo;
        }

    }

    // Holds Library info for all the libraries in the system
    static class LibraryRepo extends Repo implements IRepo.LibraryRepo {
        // simulate a database on server
        private final UUID2.HashMap<Library, Domain.LibraryInfo> database = new UUID2.HashMap<>();

        LibraryRepo(Log log) {
            super(log);
        }

        @Override
        public Result<Domain.LibraryInfo> fetchLibraryInfo(UUID2<Library> id) {
            log.d(this,"Repo.Library - Fetching library info: " + id);

            // Simulate a network request
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Library not found, id: " + id));
        }

        @Override
        public Result<Domain.LibraryInfo> updateLibraryInfo(Domain.LibraryInfo libraryInfo) {
            log.d(this,"Repo.Library - updateLibrary, libraryInfo id: " + libraryInfo.id);

            // Simulate a network request
            if (database.containsKey(libraryInfo.id)) {
                database.put(libraryInfo.id, libraryInfo);

                return new Result.Success<>(libraryInfo);
            }

            return new Result.Failure<>(new Exception("Library not found, id: " + libraryInfo.id));
        }

        @Override
        public Result<Domain.LibraryInfo> upsertLibraryInfo(Domain.LibraryInfo libraryInfo) {
            log.d(this,"Repo.Library - Upserting library id: " + libraryInfo.id);

            database.put(libraryInfo.id, libraryInfo);

            return new Result.Success<>(libraryInfo);
        }

        ///////////////////////////////////
        /// Helper methods              ///
        ///////////////////////////////////

        // todo move out of this class?
        public void populateWithFakeBooks(UUID2<Library> libraryId, int numberOfBooksToCreate) {
            Domain.LibraryInfo library = database.get(libraryId);

            for (int i = 0; i < numberOfBooksToCreate; i++) {
                Result<UUID2<Book>> result = library.addTestBook(UUID2.createFakeDomainUUID2(i), 1);

                if (result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<UUID2<Book>>) result).exception();
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

// Context is a singleton class that holds all the repositories and global objects like Gson
interface IContext {
    Repo.BookRepo bookRepo = null;
    Repo.UserRepo userRepo = null;
    Repo.LibraryRepo libraryRepo = null;
    Gson gson = null;
    Log log = null;
}
class Context implements IContext {
    // static public Context INSTANCE = null;  // Enforces singleton instance & allows global access, LEAVE for reference

    // Repository Singletons
    private Repo.BookRepo bookRepo = null;
    private Repo.UserRepo userRepo = null;
    private Repo.LibraryRepo libraryRepo = null;

    // Utility Singletons
    protected Gson gson = null;
    public Log log = null;

    public enum ContextType {
        PRODUCTION,
        TEST
    }

    Context(
            Repo.BookRepo bookRepo,
            Repo.UserRepo userRepo,
            Repo.LibraryRepo libraryRepo,
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
            new Repo.BookRepo(
                new BookApi(),
                new BookDatabase(),
                log
            ),
            new Repo.UserRepo(log),
            new Repo.LibraryRepo(log),
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

    public Repo.BookRepo bookRepo() {
        return this.bookRepo;
    }
    public Repo.UserRepo userRepo() {
        return this.userRepo;
    }
    public Repo.LibraryRepo libraryRepo() {
        return this.libraryRepo;
    }
}

// These hold the "{Model}Info" for each App Domain Object. (like a DTO for a database row)
class Model {
    transient protected UUID2<DomainUUID> id;

    Model(UUID2<DomainUUID> id) { // todo UUID2 remove
        this.id = new UUID2<DomainUUID>(id);
    }

    // Used to set the ID of a new object imported from JSON (GSON can't set transient fields)
    public void setDomainUUID(UUID2<DomainUUID> id) {  // todo UUID2 remove
        this.id = new UUID2<DomainUUID>(id);
    }

    // Domain objects contain the "{Model}Info" and the associated business logic to manipulate it
    static class Domain extends Model {
        Domain(UUID2<?> id) {
            super(id.toDomainUUID2());
        }

        static class BookInfo extends Domain implements ToEntity<Entity.BookInfo>, ToDTO<DTO.BookInfo> {
            UUID2<Book> id;
            final String title;
            final String author;
            final String description;

            BookInfo(UUID2<Book> id, String title, String author, String description) {
                super(id);
                this.title = title;
                this.author = author;
                this.description = description;
                this.id = id;
            }
            BookInfo(UUID uuid, String title, String author, String description) {
                this(new UUID2<Book>(uuid), title, author, description);
            }
            BookInfo(String id, String title, String author, String description) {
                this(UUID.fromString(id), title, author, description);
            }
            BookInfo(Domain.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }
            // Must accept both DTO and Entity BookInfo
            BookInfo(DTO.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }
            BookInfo( Entity.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }

            @Override
            public String toString() {
                return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description;
            }

            @Override
            public DTO.BookInfo toDTO() {
                return new DTO.BookInfo(this.id, this.title, this.author, this.description);
            }

            @Override
            public Entity.BookInfo toEntity() {
                return new Entity.BookInfo(this.id, this.title, this.author, this.description);
            }
        }

        static class UserInfo extends Domain implements ToDomain<Domain.UserInfo> {
            // note: no DB or API for UserInfo (so no .ToEntity() or .ToDTO())

            UUID2<User> id;
            final String name;
            final String email;
            final ArrayList<UUID2<Book>> acceptedBooks = new ArrayList<>();

            UserInfo(UUID2<User> id, String name, String email) {
                super(id.toDomainUUID2());
                this.name = name;
                this.email = email;
                this.id = id;
            }
            UserInfo(UserInfo userInfo) {
                this(userInfo.id, userInfo.name, userInfo.email);
            }

            @Override
            public String toString() {
                return "User: " + this.name + " (" + this.email + "), acceptedBooks: " + this.acceptedBooks;
            }

            @Override
            public Domain.UserInfo toDomain() {
                return new Domain.UserInfo(this);
            }
        }

        static class LibraryInfo extends Domain {
            // note: no DB or API for LibraryInfo (so no .ToEntity() or .ToDTO())

            final UUID2<Library> id;
            final String name;
            final private UUID2.HashMap<User, ArrayList<UUID2<Book>>> userIdToCheckedOutBookMap;
            final private UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap;

            LibraryInfo(
                UUID2<Library> id,
                String name,
                UUID2.HashMap<User, ArrayList<UUID2<Book>>> checkoutUserBookMap,
                UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap
            ) {
                super(id);
                this.name = name;
                this.userIdToCheckedOutBookMap = checkoutUserBookMap;
                this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
                this.id = id;
            }
            LibraryInfo(UUID2<Library> id, String name) {
                this(id, name, new UUID2.HashMap<>(), new UUID2.HashMap<>());
            }
            LibraryInfo(UUID2<Library> id, LibraryInfo libraryInfo) {
                this(id,
                        libraryInfo.name,
                        libraryInfo.userIdToCheckedOutBookMap,
                        libraryInfo.bookIdToNumBooksAvailableMap);
            }

            public Result<UUID2<Book>> checkOutBookToUser(UUID2<Book> bookId, UUID2<User> userId) {
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
                Result<UUID2<Book>> checkedOutUUID2Book = checkOutBookToUser(book.id, user.id2);

                if(checkedOutUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) checkedOutUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            public Result<UUID2<Book>> checkInBookFromUser(UUID2<Book> bookId, UUID2<User> userId) {
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
                Result<UUID2<Book>> returnedUUID2Book = checkInBookFromUser(book.id, user.id2);

                if(returnedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) returnedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            public Result<ArrayList<UUID2<Book>>> findBooksCheckedOutByUser(UUID2<User> user) {
                if(!isUserKnown(user)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));

                return new Result.Success<>(userIdToCheckedOutBookMap.get(user));
            }

            public Result<HashMap<UUID2<Book>, Integer>> calculateAvailableBookIdToCountOfAvailableBooksList() {

                HashMap<UUID2<Book>, Integer> availableBookIdToNumBooksAvailableMap = new HashMap<>();
                for(Book book : this.bookIdToNumBooksAvailableMap.keys()) {
                    if(isBookAvailable(book)) {
                        availableBookIdToNumBooksAvailableMap
                                .put(book.id, availableBookIdToNumBooksAvailableMap.get(book.id));
                    }
                }

                return new Result.Success<>(availableBookIdToNumBooksAvailableMap);
            }

            //////////////////////////////
            // Public Helper Methods    //
            //////////////////////////////

            public boolean isBookKnown(UUID2<Book> bookId) {
                return bookIdToNumBooksAvailableMap.containsKey(bookId);
            }
            public boolean isBookKnown(Book book) {
                return isBookKnown(book.id);
            }

            public boolean isUserKnown(UUID2<User> userId) {
                return userIdToCheckedOutBookMap.containsKey(userId);
            }
            public boolean isUserKnown(User user) {
                return isUserKnown(user.id2);
            }

            public boolean isBookAvailable(UUID2<Book> bookId) {
                return bookIdToNumBooksAvailableMap.get(bookId) > 0;
            }
            public boolean isBookAvailable(Book book) {
                return isBookAvailable(book.id);
            }

            public boolean isBookCurrentlyCheckedOutByUser(UUID2<Book> bookId, UUID2<User> userId) {
                return userIdToCheckedOutBookMap.get(userId).contains(bookId);
            }
            public boolean isBookCurrentlyCheckedOutByUser(Book book, User user) {
                return isBookCurrentlyCheckedOutByUser(book.id, user.id2);
            }

            //////////////////////////////
            // Testing Helper Methods   //
            //////////////////////////////

            protected Result<UUID2<Book>> addTestBook(UUID2<Book> bookId, int quantity) {
                return addBookToInventory(bookId, quantity);
            }

            protected Result<UUID2<User>> upsertTestUser(UUID2<User> userId) {
                return upsertUserId(userId);
            }

            //////////////////////////////
            // Private Helper Functions //
            //////////////////////////////

            private Result<UUID2<Book>> addBookToInventory(UUID2<Book> bookId, int quantity) {
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
                Result<UUID2<Book>> addedUUID2Book = addBookToInventory(book.id, quantity);

                if(addedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UUID2<Book>> removeBookFromInventory(UUID2<Book> bookId, int quantity) {
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
                Result<UUID2<Book>> removedUUID2Book = removeBookFromInventory(book.id, quantity);

                if(removedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UUID2<Book>> addBookToUser(UUID2<Book> bookId, UUID2<User> userId) {
                if(!isBookKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
                if(!isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
                if(isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user"));

                try {
                    if(userIdToCheckedOutBookMap.containsKey(userId)) {
                        userIdToCheckedOutBookMap.get(userId).add(bookId);
                    } else {
                        //noinspection ArraysAsListWithZeroOrOneArgument
                        userIdToCheckedOutBookMap.put(userId, new ArrayList<>(Arrays.asList(bookId)));
                    }
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> addBookToUser(Book book, User user) {
                Result<UUID2<Book>> addedUUID2Book = addBookToUser(book.id, user.id2);

                if(addedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UUID2<Book>> removeBookFromUser(UUID2<Book> bookId, UUID2<User> userId) {
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
                Result<UUID2<Book>> removedUUID2Book = removeBookFromUser(book.id, user.id2);

                if(removedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UUID2<User>> insertUserId(UUID2<User> userId) {
                if(isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is already known"));

                try {
                    userIdToCheckedOutBookMap.put(userId, new ArrayList<>());
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(userId);
            }

            private Result<UUID2<User>> upsertUserId(UUID2<User> userId) {
                if(isUserKnown(userId)) return new Result.Success<>(userId);

                return insertUserId(userId);
            }

            private Result<UUID2<User>> removeUserId(UUID2<User> userId) {
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
    // Simple holder class for transferring data to/from the Domain
    static class DTO extends Model {
        public DTO(UUID2<DomainUUID> id) {
            super(id);
        }

        static class BookInfo extends DTO implements ToDomain<Domain.BookInfo> {
            UUID2<Book> id;
            final String title;
            final String author;
            final String description;

            BookInfo(UUID2<Book> id, String title, String author, String description) {
                super(id.toDomainUUID2());
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }
            BookInfo(UUID2<Book> id, BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }
            // Only accept Domain.BookInfo
            BookInfo(Domain.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }

            @Override
            public String toString() {
                return "Book (" + this.id + ") : " + this.title + " by " + this.author +", " + this.description;
            }

            @Override
            public Domain.BookInfo toDomain() {
                return new Domain.BookInfo(this);
            }
        }
    }

    // Entities for Databases
    // Simple holder class for transferring data to/from the Domain
    static class Entity extends Model {
        Entity(UUID2<DomainUUID> id) {
            super(id);
        }

        static class BookInfo extends Entity implements ToDomain<Domain.BookInfo> {
            UUID2<Book> id;
            final String title;
            final String author;
            final String description;

            BookInfo(UUID2<Book> id, String title, String author, String description) {
                super(id.toDomainUUID2());
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }
            BookInfo(BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }
            // Only accept Domain.BookInfo
            BookInfo(Domain.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }

            @Override
            public String toString() {
                return "Book (" + this.id + ") : " + this.title + " by " + this.author +", " + this.description;
            }

            @Override
            public Domain.BookInfo toDomain() {
                return new Domain.BookInfo(this);
            }
        }
    }

    // Converters between Domain, Entity, and DTO
    interface ToDomain<T extends Domain> {
        T toDomain();
    }
    interface ToEntity<T extends Entity> {
        T toEntity();
    }
    interface ToDTO<T extends DTO> {
        T toDTO();
    }

    public String toPrettyJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}

// Info - Caches the Object "Info" and defines required fetch and update methods
interface Info<T extends Domain> {
    T fetchInfo();                  // Fetches info for object from server
    boolean isInfoFetched();        // Returns true if info has been fetched from server
    Result<T> fetchInfoResult();    // Fetches Result<T> for info object from server
    Result<T> updateInfo(T info);   // Updates info for object to server
    Result<T> refreshInfo();        // Refreshes info for object from server
    String fetchInfoFailureReason();// Returns reason for failure of last fetchInfo() call, or null if successful
}

abstract class IDomainObject<T extends Domain> implements Info<T> { // todo why is not ok be within (& DomainUUID)?
    UUID2<DomainUUID> id;
    protected T info;  // Info<T> object for this DomainObject
    protected Result<T> infoResult = null;

    // Singletons
    protected Context context = null;
    private Gson gson = null; // convenience reference to the Context Gson singleton object

    // Class of the info<T> (for GSON serialization)
    @SuppressWarnings("unchecked")
    Class<T> infoClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass())
            .getActualTypeArguments()[0];

    IDomainObject(T info, Context context) {
        if(context == null) throw new IllegalArgumentException("Context cannot be null");
        // this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation

        this.gson = this.context.gson;
        this.info = info;
        this.id = info.id;
    }
    IDomainObject(UUID2<DomainUUID> id, Context context) {
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
        this(UUID2.randomUUID2(), context);
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

    public UUID2<DomainUUID> getId() {
        return this.id;
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

    // Returns the Info<T> object if it has been fetched, otherwise null.
    // Used to access the Info object without having to handle the Result<T> object.
    // NOTE: The Info object is not re-fetched if it has already been fetched.
    public T fetchInfo() {
        if (isInfoFetched()) {
            return this.info;
        }

        // Attempt to fetch info, since it hasn't been successfully fetched yet.
        Result<T> result = this.fetchInfoResult();
        if (result instanceof Result.Failure) {
            context.log.d(this,"Failed to fetchInfoResult() for " +
                    "class: " + this.getClass().getName() + ", " +
                    "id: " + this.id.toString());

            return null;
        }

        this.info = ((Result.Success<T>) result).value();
        return this.info;
    }

    // Returns reason for failure of last fetchInfo() call, or null if was successful.
    // Used as an error guard and if Info is not fetched, it attempts to fetch it.
    // The "returning null" behavior is to make the call site error handling code smaller.
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

    // Forces refresh of Info from server
    public Result<T> refreshInfo() {
        context.log.d(this,"Refreshing info for " +
                "class: " + this.getClass().getName() + ", " +
                "id: " + this.id.toString());

        this.info = null;
        return this.fetchInfoResult();
    }
}
abstract class DomainObject<T extends Domain> extends IDomainObject<T> {

    public DomainObject(T info, Context context) {
        super(info, context);
    }
    public DomainObject(UUID2<DomainUUID> id, Context context) {
        super(id.toDomainUUID2(), context);
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

            Result<T> checkResult = checkInfoIdMatchesJsonId(infoClass, infoFromJson);
            if (checkResult instanceof Result.Failure) {
                return checkResult;
            }

            // Set id - GSON deserialization doesn't set it (???)
            infoFromJson.setDomainUUID(this.id);

            // Update the info object with the new info
            return this.updateInfo(infoFromJson);
        } catch (JsonSyntaxException e) {
            return new Result.Failure<>(new Exception("Failed to parse JSON: " + e.getMessage()));
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

    /////////////////////////////////
    // Private helpers             //
    /////////////////////////////////

    private Result<T> checkInfoIdMatchesJsonId(Class<?> infoClass, T infoFromJson) {

        try {
            Object idField = infoClass.getDeclaredField("id").get(infoFromJson);
            if(idField == null) {
                return new Result.Failure<>(new Exception("checkInfoIdMatchesJsonId(): Info class does not have an id field"));
            }

            String idStr = idField.toString();
            UUID infoFromJsonUUID = UUID.fromString(idStr);

            if (!infoFromJsonUUID.equals(this.id.uuid())) {
                return new Result.Failure<>(new Exception("checkInfoIdMatchesJsonId(): Info id does not match json id, " +
                        "info id: " + this.id.toString() + ", " +
                        "json id: " + idStr));
            }
        } catch (NoSuchFieldException e) {
            return new Result.Failure<>(new Exception("checkInfoIdMatchesJsonId(): Info class does not have an id field"));
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(infoFromJson);
    }
}

// Book Domain Object - Only interacts with its own repo, Context, and other Domain Objects
class Book extends DomainObject<Domain.BookInfo> implements DomainUUID {
    UUID2<Book> id;
    private Repo.BookRepo repo = null;

    Book(Domain.BookInfo info, Context context) {
        super(info, context);
        this.repo = this.context.bookRepo();
        this.id = this.info.id;

        context.log.d(this, "Book (" + this.id.toString() + ") created");
    }
    Book(UUID2<Book> id, Context context) {
        super(id.toDomainUUID2(), context);
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
        super(UUID2.randomUUID2(), context);
        this.repo = this.context.bookRepo();
        this.id = UUID2.fromUUID(super.getId().uuid()); // todo is this necessary? super.getId2() should be the same as this.id2

        context.log.d(this, "Book (" + this.id.toString() + ") created");
    }

    // LEAVE for reference, for static Context instance implementation
    // Book(UUID2<Book id) {
    //     this(id, null);
    // }
    // Book() {
    //     this(UUID2<Book.randomUUID());
    // }

    @Override
    public Result<Domain.BookInfo> fetchInfoResult() {
        // context.log.d(this,"Book (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchBookInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Domain.BookInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<Domain.BookInfo> updateInfo(Domain.BookInfo updatedInfo) {
        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the repo
        Result<Domain.BookInfo> infoResult = this.repo.updateBookInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<Domain.BookInfo>) infoResult).value();
        return infoResult;
    }
}

// User Domain Object - Only interacts with its own Repo, Context, and other Domain Objects
class User extends DomainObject<Domain.UserInfo>  implements DomainUUID {
    UUID2<User> id2;
    private Repo.UserRepo repo = null;

    User(Domain.UserInfo info, Context context) {
        super(info, context);
        this.repo = this.context.userRepo();
        this.id2 = this.info.id;

        context.log.d(this,"User (" + this.id2.toString() + ") created");
    }
    User(UUID2<User> id, Context context) {
        super(id.toDomainUUID2(), context);
        this.repo = this.context.userRepo();
        this.id2 = id;

        context.log.d(this,"User (" + this.id2.toString() + ") created");
    }
    User(String json, Context context) {
        super(json, context);
        this.repo = this.context.userRepo();
        this.id2 = this.info.id;

        context.log.d(this,"User (" + this.id2.toString() + ") created");
    }
    User(Context context) {
        super(UUID2.randomUUID2(), context);
        this.repo = this.context.userRepo();
        this.id2 = this.info.id;

        context.log.d(this,"User (" + this.id2.toString() + ") created");
    }
    // LEAVE for reference, for static Context instance implementation
    // User(UserUUID id) {
    //     this(id, null);
    // }
    // user() {
    //     this(UserUUID.randomUUID());
    // }

    @Override
    public Result<Domain.UserInfo> fetchInfoResult() {
        // context.log.d(this,"User (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchUserInfo(this.id2);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Domain.UserInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public Result<Domain.UserInfo> updateInfo(Domain.UserInfo updatedUserInfo) {
        context.log.d(this,"User (" + this.id2.toString() + ") - updateInfo,  userInfo: " + updatedUserInfo.toString());

        // Update self optimistically
        super.updateInfo(updatedUserInfo);

        // Update the repo
        Result<Domain.UserInfo> infoResult = this.repo.updateUserInfo(updatedUserInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<Domain.UserInfo>) infoResult).value();
        return infoResult;
    }

    public Result<ArrayList<UUID2<Book>>> receiveBook(@NotNull Book book) {
        context.log.d(this,"User (" + this.id2.toString() + ") - receiveBook,  book: " + this.id2.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check user has already accepted book
        if (this.info.acceptedBooks.contains(this.id2)) {
            return new Result.Failure<>(new Exception("User has already accepted book, book: " + book.id.toString()));
        }

        // Accept book
        if(!this.info.acceptedBooks.add(book.id)) {
            return new Result.Failure<>(new Exception("Failed to add book to acceptedBooks, book: " + book.id.toString()));
        }

        // Update user
        Result<Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Domain.UserInfo>) result).exception());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }

    public Result<ArrayList<UUID2<Book>>> returnBook(Book book) {
        context.log.d(this,"User (" + this.id2.toString() + ") - returnBook,  book: " + this.id2.toString() + " to user: " + this.id2.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check user has accepted book
        if (!this.info.acceptedBooks.contains(this.id2)) {
            return new Result.Failure<>(new Exception("User has not accepted book, bookId = " + this.id2.toString()));
        }

        // Remove the Returned book
        this.info.acceptedBooks.remove(this.id2);

        // Update UserInfo
        Result<Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Domain.UserInfo>) result).exception());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }

    public Result<ArrayList<UUID2<Book>>> giveBookToUser(Book book, User receivingUser) {
        context.log.d(this,"User (" + this.id2.toString() + ") - giveBookToUser,  book: " + this.id2.toString() + ", user: " + this.id2.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check user has accepted book
        if (!this.info.acceptedBooks.contains(this.id2)) {
            return new Result.Failure<>(new Exception("User has not accepted book, bookId = " + this.id2.toString()));
        }

        // Remove the Given book
        this.info.acceptedBooks.remove(this.id2);

        // Update UserInfo
        Result<Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Domain.UserInfo>) result).exception());
        }

        // Give book to user
        Result<ArrayList<UUID2<Book>>> result2 = receivingUser.receiveBook(book);
        if (result2 instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) result2).exception());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }

    public Result<ArrayList<UUID2<Book>>> checkoutBookFromLibrary(
    Book book,
    Library library
    ) {
        context.log.d(this,"User (" + this.id2.toString() + ") - checkoutBookFromLibrary, book: " + this.id2.toString() + ", library: " + library.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<Book> bookResult = library.checkOutBookToUser(book, this);
        if (bookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) bookResult).exception());
        }

        return new Result.Success<>(this.info.acceptedBooks);
    }
}

// Library Domain Object - Only interacts with its own repo, Context, and other Domain Objects
class Library extends DomainObject<Domain.LibraryInfo>  implements DomainUUID {
    UUID2<Library> id;
    private Repo.LibraryRepo repo = null;

    Library(Domain.LibraryInfo info, Context context) {
        super(info, context);
        this.repo = this.context.libraryRepo();
        this.id = info.id;

        context.log.d(this,"Library (" + this.id + ") created");
    }
    Library(UUID2<Library> id, Context context) {
        super(id.toDomainUUID2(), context);
        this.repo = this.context.libraryRepo();
        this.id = id;

        context.log.d(this,"Library (" + this.id + ") created");
    }
    Library(String json, Context context) {
        super(json, context);
        this.repo = this.context.libraryRepo();
        this.id = this.info.id;

        context.log.d(this,"Library (" + this.id + ") created");
    }
    Library(Context context) {
        super(UUID2.randomUUID2(), context);
        this.repo = this.context.libraryRepo();
//        this.id = ((UUID2.fromUUID(id.uuid()))); // todo is this needed to get the id2 from the parent class?
    }

    // LEAVE for reference, for static Context instance implementation
    // Library() {
    //     this(LibraryUUID.randomUUID());
    // }

    @Override
    public Result<Domain.LibraryInfo> fetchInfoResult() {
        // context.log.d(this,"Library (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchLibraryInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Domain.LibraryInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<Domain.LibraryInfo> updateInfo(Domain.LibraryInfo updatedInfo) {
        // context.log.d(this,"Library (" + this.id.toString() + ") - updateInfo, newInfo: " + newInfo.toString());  // LEAVE for debugging

        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the Repo
        Result<Domain.LibraryInfo> infoResult = this.repo.updateLibraryInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with Repo result
        super.updateInfo(((Result.Success<Domain.LibraryInfo>) infoResult).value());
        return infoResult;
    }

    ////////////////////////////
    // Library Domain Methods //
    ////////////////////////////

    public Result<Book> checkOutBookToUser(@NotNull Book book, @NotNull User user) {
        context.log.d(this, format("Library (%s) - checkOutBookToUser, user: %s, book: %s", this.id, this.id.toString(), this.id.toString()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
        }

        // Check out Book to User
        Result<Book> checkOutBookresult = this.info.checkOutBookToUser(book, user);
        if (checkOutBookresult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) checkOutBookresult).exception());
        }

        // User receives Book
        Result<ArrayList<UUID2<Book>>> receiveBookResult = user.receiveBook(book);
        if (receiveBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) receiveBookResult).exception());
        }

        // Update the Info
        Result<Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Domain.LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    public Result<Book> checkInBookFromUser(Book book, User user) {
        context.log.d(this, format("Library (%s) - checkInBookFromUser, book %s from user %s\n", this.id, this.id, this.id));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
        }

        Result<Book> checkInBookResult = this.info.checkInBookFromUser(book, user);
        if (checkInBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) checkInBookResult).exception());
        }

        Result<ArrayList<UUID2<Book>>> userReturnedBookResult = user.returnBook(book);
        if (userReturnedBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) userReturnedBookResult).exception());
        }

        // Update the Info
        Result<Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Domain.LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    // DomainObject enforces the rule: if a User is not known, they are added as a new user.
    public boolean isUnableToFindOrAddUser(User user) {
        context.log.d(this, format("Library (%s) - isUnableToFindOrAddUser user: %s", this.id, this.id));
        if (fetchInfoFailureReason() != null) return true;

        if (isKnownUser(user)) {
            return false;
        }

        // Create a new User entry in the Library
        Result<UUID2<User>> upsertUserResult = this.info.upsertTestUser(user.id2);
        //noinspection RedundantIfStatement
        if (upsertUserResult instanceof Result.Failure) {
            return true;
        }

        return false;
    }

    public boolean isKnownBook(Book book) {
        context.log.d(this, format("Library(%s) - hasBook book id: %s\n", this.id, book.id));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookKnown(book);
    }

    public boolean isKnownUser(User user) {
        context.log.d(this, format("Library (%s) - isKnownUser user id: %s", this.id, user.id2));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isUserKnown(user);
    }

    public boolean isBookAvailable(Book book) {
        context.log.d(this, format("Library (%s) - hasBookAvailable book id: %s\n", this.id, book.id));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookAvailable(book);
    }

    public Result<ArrayList<Book>> findBooksCheckedOutByUser(User user) {
        context.log.d(this, format("Library (%s) - findBooksCheckedOutByUser %s\n", this.id, user));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Make sure User is Known
        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
        }

        Result<ArrayList<UUID2<Book>>> entriesResult = this.info.findBooksCheckedOutByUser(user.id2);
        if (entriesResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) entriesResult).exception());
        }

        // Convert UUID2<Books to Books
        ArrayList<UUID2<Book>> bookIds = ((Result.Success<ArrayList<UUID2<Book>>>) entriesResult).value();
        ArrayList<Book> books = new ArrayList<>();
        for (UUID2<Book> entry : bookIds) {
            books.add(new Book(entry, context));
        }

        return new Result.Success<>(books);
    }

    public Result<HashMap<Book, Integer>> calculateAvailableBookIdToNumberAvailableList() {
        context.log.d(this, "Library (" + this.id + ") - calculateAvailableBooksAndAmountOnHand\n");
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<HashMap<UUID2<Book>, Integer>> entriesResult = this.info.calculateAvailableBookIdToCountOfAvailableBooksList();
        if (entriesResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<HashMap<UUID2<Book>, Integer>>) entriesResult).exception());
        }

        // Convert UUID2<Books to Books
        HashMap<UUID2<Book>, Integer> bookIdToNumberAvailable = ((Result.Success<HashMap<UUID2<Book>, Integer>>) entriesResult).value();
        HashMap<Book, Integer> bookToNumberAvailable = new HashMap<>();
        for (Map.Entry<UUID2<Book>, Integer> entry : bookIdToNumberAvailable.entrySet()) {
            bookToNumberAvailable.put(new Book(entry.getKey(), context), entry.getValue());
        }

        return new Result.Success<>(bookToNumberAvailable);
    }

    public Result<Book> addTestBookToLibrary(Book book, Integer count) {
        context.log.d(this, format("Library (%s) - addTestBookToLibrary count: %s, book: %s\n", this.id, count, book));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<UUID2<Book>> addBookResult =  this.info.addTestBook(book.id, count);
        if (addBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<UUID2<Book>>) addBookResult).exception());
        }

        // Update the Info
        Result<Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Domain.LibraryInfo>) updateInfoResult).exception());
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
            ctx.log.d(this, "----------------------------------");
            ctx.log.d(this, "Populate_And_Poke_Book");

            // Create a book object (it only has an id)
            Book book = new Book(UUID2.createFakeDomainUUID2(1), ctx);
            ctx.log.d(this,book.fetchInfoResult().toString());

            // Update info for a book
            final Result<Domain.BookInfo> bookInfoResult =
                    book.updateInfo(
                            new Domain.BookInfo(
                                    book.id,
                                    "The Updated Title",
                                    "The Updated Author",
                                    "The Updated Description"
                            ));
            ctx.log.d(this,book.fetchInfoResult().toString());

            // Get the bookInfo (null if not loaded)
            Domain.BookInfo bookInfo3 = book.fetchInfo();
            if (bookInfo3 == null) {
                ctx.log.d(this,"Book Missing --> " +
                        "book id: " + bookInfo3.id + " >> " +
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
            Book book2 = new Book(UUID2.createFakeDomainUUID2(99), ctx);
            if (book2.fetchInfoResult() instanceof Result.Failure) {
                ctx.log.d(this,"Get Book FAILURE --> " +
                        "book id: " + book2.id + " >> " +
                        ((Result.Failure<Domain.BookInfo>) book2.fetchInfoResult())
                );
            } else {
                ctx.log.d(this,"Book ERxists --> " +
                        ((Result.Success<Domain.BookInfo>) book2.fetchInfoResult()).value()
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
            final Result<Domain.LibraryInfo> libraryInfo = createFakeLibraryInfoInContextLibraryRepo(1, ctx);
            if (libraryInfo instanceof Result.Failure) {
                ctx.log.d(this,"Create Library FAILURE --> " +
                        ((Result.Failure<Domain.LibraryInfo>) libraryInfo)
                );

                break Populate_the_library_and_user_DBs;
            }
            UUID2<Library> libraryInfoId = ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().id;
            ctx.log.d(this,"Library Created --> id: " +
                    ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().id +
                    ", name: "+
                    ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().name
            );

            // Populate the library
            ctx.libraryRepo().populateWithFakeBooks(libraryInfoId, 10);

            // Create & populate a User in the User Repo
            final Domain.UserInfo userInfo = createFakeUserInfoInContextUserRepo(1, ctx);

            //////////////////////////////////
            // Actual App functionality     //
            //////////////////////////////////

            // Create the App objects
            final User user1 = new User(userInfo.id, ctx);
            final Library library1 = new Library(libraryInfoId, ctx);
            final Book book1 = new Book(UUID2.createFakeDomainUUID2(1), ctx);
            final Book book2 = new Book(UUID2.createFakeDomainUUID2(2), ctx);

            Checkout_2_books_to_a_user:
            {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"Checking out 2 books to user " + user1.id2);

                final Result<Book> bookResult = library1.checkOutBookToUser(book1, user1);
                if (bookResult instanceof Result.Failure) {
                    ctx.log.d(this,"Checked out book FAILURE--> " +
                            ((Result.Failure<Book>) bookResult).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Checked out book SUCCESS --> " +
                            ((Result.Success<Book>) bookResult).value().id
                    );
                }

                final Result<Book> bookResult2 = library1.checkOutBookToUser(book2, user1);
                if (bookResult2 instanceof Result.Failure) {
                    ctx.log.d(this,"Checked out book FAILURE--> " +
                            ((Result.Failure<Book>) bookResult2).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Checked out book SUCCESS --> " +
                            ((Result.Success<Book>) bookResult2).value().id
                    );
                }

                // library1.DumpDB(ctx);  // LEAVE for debugging
            }

            Get_Available_Books_And_Counts_In_Library:
            if (false) {
                ctx.log.d(this, "----------------------------------");
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
                final HashMap<Book, Integer> availableBooks =
                        ((Result.Success<HashMap<Book, Integer>>) availableBookToNumAvailableResult).value();

                // Print out available books
                ctx.log.d(this,"\nAvailable Books in Library:");
                for (Map.Entry<Book, Integer> availableBook : availableBooks.entrySet()) {
                    final Book book3 = new Book(availableBook.getKey().id, ctx);

                    final Result<Domain.BookInfo> bookInfoResult = book3.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        ctx.log.d(this,
                                "Book Error: " +
                                        ((Result.Failure<Domain.BookInfo>) bookInfoResult)
                                                .exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,
                                ((Result.Success<Domain.BookInfo>) bookInfoResult).value() +
                                        " >> num available: " + availableBook.getValue()
                        );
                    }
                }
                ctx.log.d(this,"Total Available Books (unique UUIDs): " + availableBooks.size());
                ctx.log.d(this,"\n");
            }

            Get_books_checked_out_by_user:
            if (false) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"\nGetting books checked out by user " + user1.id2);

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
                ctx.log.d(this,"\nChecked Out Books for User [" + user1.fetchInfo().name + ", " + user1.id2 + "]:");
                for (Book book : checkedOutBooks) {
                    final Result<Domain.BookInfo> bookInfoResult = book.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        ctx.log.d(this,
                                "Book Error: " +
                                        ((Result.Failure<Domain.BookInfo>) bookInfoResult)
                                                .exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,
                                ((Result.Success<Domain.BookInfo>) bookInfoResult).value().toString()
                        );
                    }
                }
                System.out.print("\n");
            }

            Check_In_the_Book_from_the_User_to_the_Library:
            if (false) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"\nCheck in book " + book1.id + " from user " + user1.id2);

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
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"Load Library from Json: ");

                // Library library2 = new Library(ctx); // uses random UUID, will cause expected error due to unknown UUID
                Library library2 = new Library(library1.id, ctx);
                ctx.log.d(this, library2.toPrettyJson());
                Result<Domain.LibraryInfo> library2Result = library2.updateInfoFromJson(
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
                    ctx.log.d(this, ((Result.Failure<Domain.LibraryInfo>) library2Result).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Results of Library2 json load:");
                    ctx.log.d(this,library2.toPrettyJson());
                }
            }

            Check_out_Book_via_User:
            if (false) {
                final User user2 = new User(createFakeUserInfoInContextUserRepo(2, ctx), ctx);
                final Result<Domain.BookInfo> book12Result = addFakeBookInfoInContextBookRepo(12, ctx);

                if (book12Result instanceof Result.Failure) {
                    ctx.log.d(this,"Book Error: " +
                            ((Result.Failure<Domain.BookInfo>) book12Result).exception().getMessage()
                    );
                } else {

                    final UUID2<Book> book12id = ((Result.Success<Domain.BookInfo>) book12Result).value().id;
                    final Book book12 = new Book(book12id, ctx);

                    ctx.log.d(this,"\nCheck out book " + book12id + " to user " + user1.id2);

                    final Result<Book> book12UpsertResult = library1.addTestBookToLibrary(book12, 1);
                    if (book12UpsertResult instanceof Result.Failure) {
                        ctx.log.d(this,"Upsert Book Error: " +
                                ((Result.Failure<Book>) book12UpsertResult).exception().getMessage()
                        );
                    }

                    final Result<ArrayList<UUID2<Book>>> booksAcceptedByUser = user2.checkoutBookFromLibrary(book12, library1);
                    if (booksAcceptedByUser instanceof Result.Failure) {
                        ctx.log.d(this,"Checkout book FAILURE --> " +
                                ((Result.Failure<ArrayList<UUID2<Book>>>) booksAcceptedByUser).exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,"Checkout Book SUCCESS --> booksAcceptedByUser:" +
                                ((Result.Success<ArrayList<UUID2<Book>>>) booksAcceptedByUser).value()
                        );
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    /////////////////////////// Helper Methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

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

    private Result<Domain.LibraryInfo> createFakeLibraryInfoInContextLibraryRepo(
        final Integer id,
        Context context
    ) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.libraryRepo()
                .upsertLibraryInfo(
                        new Domain.LibraryInfo(
                            UUID2.createFakeDomainUUID2(someNumber),
                            "Library " + someNumber
                        )
                );
    }

    private Domain.UserInfo createFakeUserInfoInContextUserRepo(
        final Integer id,
        Context context
    ) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.userRepo()
                .upsertUserInfo(new Domain.UserInfo(
                        UUID2.createFakeDomainUUID2(someNumber),
                        "User " + someNumber,
                        "user" + someNumber + "@gmail.com"
                ));
    }

    private Result<Domain.BookInfo> addFakeBookInfoInContextBookRepo(
        final Integer id,
        Context context
    ) {
        final Domain.BookInfo bookInfo = createFakeBookInfo(null, id);
        return context.bookRepo()
                .upsertBookInfo(bookInfo);
    }

    private Domain.BookInfo createFakeBookInfo(String uuidStr, final Integer id) {
        Integer fakeId = id;
        if (fakeId == null) fakeId = 1;

        UUID2<Book> uuid;
        if (uuidStr == null)
            uuid = UUID2.createFakeDomainUUID2(fakeId);
        else
            uuid = UUID2.fromString(uuidStr);

        return new Domain.BookInfo(
                uuid,
                "Book " + fakeId,
                "Author " + fakeId,
                "Description " + fakeId
        );
    }
}