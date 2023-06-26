package org.elegantobjects.jpages;


import java.awt.print.Book;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.HashMap;

//import com.google.gson.*;
//import org.elegantobjects.jpages.App2.*;
//import org.jetbrains.annotations.NotNull;
//import static java.lang.String.format;
//import static org.elegantobjects.jpages.BaseUUID.*;
//import static org.elegantobjects.jpages.Context.ContextType.*;
//import static org.elegantobjects.jpages.Model.*;

public final class App1 extends IOException {

    private static final long serialVersionUID = 0x7523L;

    interface Resource {
        Resource define(String name, String value);

        void printTo(Output output) throws IOException;
    }

    interface Output {
        void print(String name, String value) throws IOException;
    }

    private final Session session;

    public App1(Session session) {
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

//class Pair<T, U> {
//    private final T first;
//    private final U second;
//
//    Pair(T first, U second) {
//        this.first = first;
//        this.second = second;
//    }
//
//    T getFirst() {
//        return first;
//    }
//
//    U getSecond() {
//        return second;
//    }
//}

//class HttpClient {
//    private final String client;
//
//    HttpClient(String client) {
//        this.client = client;
//    }
//
//    public HttpClient() {
//        this.client = "Mozilla/5.0";
//    }
//}

//class URL {
//    @SuppressWarnings("FieldCanBeLocal")
//    private final String url;
//
//    URL(String url) {
//        this.url = url;
//    }
//}

//class Result<T> {
//    static class Success<T> extends Result<T> {
//        private final T value;
//
//        Success(T value) {
//            this.value = value;
//        }
//
//        public T value() {
//            return value;
//        }
//
//        public String toString() {
//            if (value == null)
//                return "null";
//            return value.toString();
//        }
//    }
//
//    static class Failure<T> extends Result<T> {
//        private final Exception exception;
//
//        Failure(Exception exception) {
//            this.exception = exception;
//        }
//
//        public Exception exception() {
//            return exception;
//        }
//
//        public String toString() {
//            if (exception == null)
//                return "null";
//            return exception.getLocalizedMessage();
//        }
//    }
//}

//// Marker interface for Domain classes that use UUID2<{Domain}>.
//interface IUUID2 {  // Keep this in global namespace to reduce wordiness at declaration sites (avoiding: UUID2<UUID2.hasUUID2> wordiness)
//    String getUUID2TypeStr();  // usually just the full class name of the Domain object (ie: "org.elegantobjects.jpages.Model$Domain$BookInfo")
//}

//// UUID2 is a type-safe wrapper for UUIDs.
//// - Used to enforce type-specific UUIDs for Objects that expect a specific type of UUID.
//// - Domain objects must be marked with the IUUID2 interface to be used with UUID2.
//// - UUID2 is immutable.
//// - UUID2 is a wrapper for UUID, so it can be used in place of UUID.
//class UUID2<TUUID2 extends IUUID2> implements IUUID2 {
//    private final UUID uuid;
//    private String uuid2TypeStr; // usually just the full class name of the Domain object // not final due to JSON deserialization needs to set this
//
//    UUID2(TUUID2 uuid2, String uuid2TypeStr) {
//        this.uuid = ((UUID2<?>) uuid2).uuid();
//
//        if(uuid2TypeStr != null) {
//            this.uuid2TypeStr = uuid2TypeStr;
//        } else {
//            this.uuid2TypeStr = "UUID"; // Default to untyped UUID
//        }
//    }
//    UUID2(UUID uuid) {
//        this.uuid = uuid;
//        this.uuid2TypeStr = "UUID";  // untyped UUID
//    }
//    UUID2(TUUID2 uuid2) {
//        this(uuid2, uuid2.getUUID2TypeStr());
//    }
//
//    // simple getter
//    public UUID uuid() {return uuid;}
//
//    // return a copy of the UUID
//    public UUID toUUID() {
//        return new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
//    }
//
//    public boolean equals(UUID2<TUUID2> other) {
//        return (other).uuid.equals(uuid);
//    }
//
//    @Override
//    public int hashCode() {
//        return uuid.hashCode();
//    }
//
//    @Override
//    public String getUUID2TypeStr() {
//        return uuid2TypeStr;
//    }
//
//    // Note: Should only be used for importing JSON
//    protected void setUUID2TypeStr(String uuidType) {
//        this.uuid2TypeStr = uuidType;
//    }
//
//    @Override
//    public String toString() {
//        return uuid.toString();
//    }
//
//    public static <TDomainUUID2 extends IUUID2> UUID2<TDomainUUID2> fromString(String uuidStr) {
//        return new UUID2<>(UUID.fromString(uuidStr));
//    }
//
//    public static <TDomainUUID2 extends IUUID2> UUID2<TDomainUUID2> fromUUID(UUID uuid) {
//        return new UUID2<>(uuid);
//    }
//
//    public static <TDomainUUID2 extends IUUID2> UUID2<TDomainUUID2> randomUUID2() {
//        return new UUID2<>(UUID.randomUUID());
//    }
//
//    @SuppressWarnings("unchecked")
//    public static <TDomainUUID2 extends IUUID2> @NotNull UUID2<TDomainUUID2> createFakeUUID2(final Integer id, String className) {
//        Integer nonNullId = id;
//        if (nonNullId == null) nonNullId = 1;
//
//        final String idPaddedWith11LeadingZeroes = format("%011d", nonNullId);
//        final UUID2<TDomainUUID2> uuid2 = fromString("00000000-0000-0000-0000-" + idPaddedWith11LeadingZeroes);
//
//        return new UUID2<>((TDomainUUID2) uuid2, className);
//    }
//    public static <TDomainUUID2 extends IUUID2> @NotNull UUID2<TDomainUUID2> createFakeUUID2(final Integer id) {
//        return createFakeUUID2(id, null);
//    }
//
//    public UUID2<IUUID2> toDomainUUID2() {
//        return new UUID2<>(this, this.uuid2TypeStr);
//    }
//
//    @SuppressWarnings("unchecked")
//    public UUID2<IUUID2> toUUID2() {
//        return (UUID2<IUUID2>) this;
//    }
//
//    // Utility HashMap class for mapping UUID2<T> to Objects.
//    // Wrapper for HashMap where the `key` hash is the common UUID value stored in UUID class.
//    // The problem is that normal HashMap uses `hash()` of UUID<{type}> object itself, which is not
//    // consistent between UUID2<{type}> objects.
//    // This class uses UUID2<T> for the keys, but the hash is the common UUID value stored in UUID class.
//    static class HashMap<TUUID2 extends IUUID2, TEntity> extends java.util.HashMap<UUID, TEntity> {
//        private static final long serialVersionUID = 0x7723L;
//
//        HashMap() {
//            super();
//        }
//        // Creates a database from another database
//        public <T extends TUUID2, U extends TEntity> HashMap(HashMap<UUID2<T>, U> sourceDatabase) {
//            super();
//            this.putAll(sourceDatabase);
//        }
//
//        public TEntity get(String uuid2Str) {
//            return get(UUID.fromString(uuid2Str));
//        }
//        public TEntity get(UUID2<TUUID2> uuid2) {
//            return get(uuid2.uuid);
//        }
//        public TEntity get(UUID uuid) { // allow UUIDs to be used as keys, but not recommended.
//            return super.get(uuid);
//        }
//
//        public TEntity put(String uuid2Str, TEntity value) {
//            return put(UUID2.fromString(uuid2Str), value);
//        }
//        public TEntity put(UUID2<TUUID2> uuid2, TEntity value) {
//            return put(uuid2.uuid, value);
//        }
//        public TEntity put(UUID uuid, TEntity value) { // allow UUIDs to be used as keys, but not recommended.
//            return super.put(uuid, value);
//        }
//
//        public TEntity remove(String uuid2Str) {
//            return remove(UUID.fromString(uuid2Str));
//        }
//        public TEntity remove(UUID2<TUUID2> uuid2) {
//            return remove(uuid2.uuid);
//        }
//        public TEntity remove(UUID uuid) { // allow UUIDs to be used as keys, but not recommended.
//            return super.remove(uuid);
//        }
//
//        public boolean containsKey(String uuid2Str) {
//            return containsKey(UUID.fromString(uuid2Str));
//        }
//        public boolean containsKey(UUID2<TUUID2> uuid2) { return super.containsKey(uuid2.uuid); }
//
//        public Set<TUUID2> keys() throws RuntimeException {
//            Set<UUID> uuidSet = super.keySet();
//            Set<TUUID2> uuid2Set = new HashSet<>();
//
//            // Convert UUIDs to TDomainUUIDs
//            try {
//                for (UUID uuid : uuidSet) {
//                    @SuppressWarnings({"unchecked"})
//                    TUUID2 uuid2 = (TUUID2) UUID2.fromUUID(uuid);
//                    uuid2Set.add(uuid2);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new RuntimeException("HashMap.keys(): Failed to convert UUID to UUID2<TDomainUUID>, uuidSet: " + uuidSet);
//            }
//
//            return uuid2Set;
//        }
//    }
//}

//// DB uses Model.Entities
//interface IDatabase<TUUID2 extends IUUID2, TEntity extends Entity> {
//    Result<TEntity> getEntityInfo(UUID2<TUUID2> id);
//    Result<TEntity> updateEntityInfo(TEntity entityInfo);
//    Result<TEntity> addEntityInfo(TEntity entityInfo);
//    Result<TEntity> upsertEntityInfo(TEntity entityInfo);
//    Result<TEntity> deleteEntityInfo(TEntity entityInfo);
//    Map<UUID2<TUUID2>, TEntity> getAllEntityInfo();
//}
//@SuppressWarnings("FieldCanBeLocal")
//class InMemoryDatabase<TEntity extends Entity, TUUID2 extends IUUID2> implements IDatabase<TUUID2, TEntity> {
//    private final URL url;
//    private final String user;
//    private final String password;
//
//    // Simulate a local database
//    private final UUID2.HashMap<TUUID2, TEntity> database = new UUID2.HashMap<>();
//
//    InMemoryDatabase(URL url, String user, String password) {
//        this.url = url;
//        this.user = user;
//        this.password = password;
//    }
//    InMemoryDatabase() {
//        this(new URL("memory://hash.map"), "admin", "password");
//    }
//
//    @Override
//    public Result<TEntity> getEntityInfo(UUID2<TUUID2> id) {
//        // Simulate the request
//        TEntity infoResult =  database.get(id);
//        if (infoResult == null) {
//            return new Result.Failure<>(new Exception("DB: Failed to get entityInfo, id: " + id));
//        }
//
//        return new Result.Success<>(infoResult);
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public Result<TEntity> updateEntityInfo(TEntity entityInfo) {
//        // Simulate the
//        try {
//            database.put((UUID2<TUUID2>) entityInfo.id(), entityInfo);
//        } catch (Exception e) {
//            return new Result.Failure<>(e);
//        }
//
//        return new Result.Success<>(entityInfo);
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public Result<TEntity> addEntityInfo(TEntity entityInfo) {
//        // Simulate the request
//        if (database.containsKey((UUID2<TUUID2>) entityInfo.id())) {
//            return new Result.Failure<>(new Exception("DB: Entity already exists, entityInfo: " + entityInfo));
//        }
//        if (database.put((UUID2<TUUID2>) entityInfo.id(), entityInfo) == null) {
//            return new Result.Failure<>(new Exception("DB: Failed to add entity, entityInfo: " + entityInfo));
//        }
//
//        return new Result.Success<>(entityInfo);
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public Result<TEntity> upsertEntityInfo(TEntity entityInfo) {
//        if (database.containsKey((UUID2<TUUID2>) entityInfo.id())) {
//            return updateEntityInfo(entityInfo);
//        } else {
//            return addEntityInfo(entityInfo);
//        }
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public Result<TEntity> deleteEntityInfo(TEntity entityInfo) {
//        if (database.remove((UUID2<TUUID2>) entityInfo.id()) == null) {
//            return new Result.Failure<>(new Exception("DB: Failed to delete entityInfo, entityInfo: " + entityInfo));
//        }
//
//        return new Result.Success<>(entityInfo);
//    }
//
//    @Override
//    public Map<UUID2<TUUID2>, TEntity> getAllEntityInfo() {
//
//        Map<UUID2<TUUID2>, TEntity> map = new HashMap<>();
//        for (Map.Entry<UUID, TEntity> entry : database.entrySet()) {
//            map.put(new UUID2<>(entry.getKey()), entry.getValue());
//        }
//
//        return map;
//    }
//}
//class BookInfoDatabase {
//    private final IDatabase<Book, Entity.BookInfo> database;
//
//    BookInfoDatabase(IDatabase<Book, Entity.BookInfo> database) {
//        this.database = database;
//    }
//    BookInfoDatabase() {
//        this(new InMemoryDatabase<>(new URL("memory://db.book.com"), "user", "password"));
//    }
//
//    public Result<Entity.BookInfo> getBookInfo(UUID2<Book> id) {
//        return database.getEntityInfo(id);
//    }
//
//    public Result<Entity.BookInfo> updateBookInfo(Entity.BookInfo bookInfo) {
//        return database.updateEntityInfo(bookInfo);
//    }
//
//    public Result<Entity.BookInfo> addBookInfo(Entity.BookInfo bookInfo) {
//        return database.addEntityInfo(bookInfo);
//    }
//
//    public Result<Entity.BookInfo> upsertBookInfo(Entity.BookInfo bookInfo) {
//        return database.upsertEntityInfo(bookInfo);
//    }
//
//    public Result<Entity.BookInfo> deleteBookInfo(Entity.BookInfo bookInfo) {
//        return database.deleteEntityInfo(bookInfo);
//    }
//
//    public Map<UUID2<Book>, Entity.BookInfo> getAllBookInfos() {  // todo UUID2 keep
//        return database.getAllEntityInfo();
//    }
//}

//// API uses Model.DTOs
//interface IAPI<TUUID2 extends IUUID2, TDTOInfo> {
//    Result<TDTOInfo> getDtoInfo(UUID2<TUUID2> id);
//    Result<TDTOInfo> getDtoInfo(String id);
//    Result<TDTOInfo> addDtoInfo(TDTOInfo dtoInfo);
//    Result<TDTOInfo> updateDtoInfo(TDTOInfo dtoInfo);
//    Result<TDTOInfo> upsertDtoInfo(TDTOInfo dtoInfo);
//    Result<TDTOInfo> deleteDtoInfo(TDTOInfo dtoInfo);
//}
//@SuppressWarnings("FieldCanBeLocal")
//class InMemoryAPI<TUUID2 extends IUUID2, TDTOInfo extends DTO> implements IAPI<TUUID2, TDTOInfo> {
//    private final URL url;
//    private final HttpClient client;
//
//    // Simulate a database accessed via a network API
//    private final UUID2.HashMap<TUUID2, TDTOInfo> database = new UUID2.HashMap<>();
//
//    InMemoryAPI(URL url, HttpClient client) {
//        this.url = url;
//        this.client = client;
//    }
//    InMemoryAPI() {
//        this(
//            new URL("http://localhost:8080"),
//            new HttpClient()
//        );
//    }
//
//    @Override
//    public Result<TDTOInfo> getDtoInfo(String id) {
//        try {
//            @SuppressWarnings("unchecked")
//            UUID2<TUUID2> uuid = (UUID2<TUUID2>) UUID2.fromString(id);
//            return getDtoInfo(uuid);
//        } catch (Exception e) {
//            return new Result.Failure<>(e);
//        }
//    }
//
//    @Override
//    public Result<TDTOInfo> getDtoInfo(UUID2<TUUID2> id) {
//        // Simulate the network request
//        if (!database.containsKey(id)) {
//            return new Result.Failure<>(new Exception("API: DTOInfo not found, id=" + id));
//        }
//
//        return new Result.Success<>(database.get(id));
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public Result<TDTOInfo> updateDtoInfo(TDTOInfo dtoInfo) {
//        try {
//            database.put((UUID2<TUUID2>) dtoInfo.id(), dtoInfo);
//        } catch (Exception e) {
//            return new Result.Failure<>(e);
//        }
//
//        return new Result.Success<>(dtoInfo);
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public Result<TDTOInfo> addDtoInfo(TDTOInfo dtoInfo) {
//        if (database.containsKey((UUID2<TUUID2>) dtoInfo.id())) {
//            return new Result.Failure<>(new Exception("API: DtoInfo already exists, use UPDATE, id=" + dtoInfo.id()));
//        }
//
//        database.put((UUID2<TUUID2>) dtoInfo.id(), dtoInfo);
//
//        return new Result.Success<>(dtoInfo);
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public Result<TDTOInfo> upsertDtoInfo(TDTOInfo dtoInfo) {
//        if (database.containsKey((UUID2<TUUID2>) dtoInfo.id())) {
//            return updateDtoInfo(dtoInfo);
//        } else {
//            return addDtoInfo(dtoInfo);
//        }
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public Result<TDTOInfo> deleteDtoInfo(TDTOInfo dtoInfo) {
//        if (database.remove((UUID2<TUUID2>) dtoInfo.id()) == null) {
//            return new Result.Failure<>(new Exception("API: Failed to delete DtoInfo"));
//        }
//
//        return new Result.Success<>(dtoInfo);
//    }
//
//    public Map<UUID2<TUUID2>, TDTOInfo> getAllDtoInfos() {
//        Map<UUID2<TUUID2>, TDTOInfo> map = new HashMap<>();
//
//        for (Map.Entry<UUID, TDTOInfo> entry : database.entrySet()) {
//            map.put(new UUID2<>(entry.getKey()), entry.getValue());
//        }
//
//        return map;
//    }
//}
//class BookInfoApi {
//    private final InMemoryAPI<Book, DTO.BookInfo> api;
//
//    BookInfoApi() {
//        this(new InMemoryAPI<>(new URL("memory://api.book.com"), new HttpClient()));
//    }
//    BookInfoApi(InMemoryAPI<Book, DTO.BookInfo> api) {
//        this.api = api;
//    }
//
//    // Use Domain-specific language to define the API
//
//    public Result<DTO.BookInfo> getBookInfo(String id) {
//        return api.getDtoInfo(id);
//    }
//    public Result<DTO.BookInfo> getBookInfo(UUID2<Book> id) {
//        return api.getDtoInfo(id);
//    }
//    public Result<DTO.BookInfo> addBookInfo(DTO.BookInfo bookInfo) {
//        return api.addDtoInfo(bookInfo);
//    }
//    public Result<DTO.BookInfo> updateBookInfo(DTO.BookInfo bookInfo) {
//        return api.updateDtoInfo(bookInfo);
//    }
//    public Result<DTO.BookInfo> upsertBookInfo(DTO.BookInfo bookInfo) {
//        return api.upsertDtoInfo(bookInfo);
//    }
//    public Result<DTO.BookInfo> deleteBookInfo(DTO.BookInfo bookInfo) {
//        return api.deleteDtoInfo(bookInfo);
//    }
//
//    public Map<UUID2<Book>, DTO.BookInfo> getAllBookInfos() {
//        return new HashMap<>(api.getAllDtoInfos());
//    }
//}

//// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
//// - works with the network API & local database to perform CRUD operations, and also performs validation.
//// - can also be used to implement caching.
//// The Repo can easily accept fake APIs & Database for testing.
//interface IRepo {
//    interface BookInfo extends IRepo {
//        Result<Domain.BookInfo> fetchBookInfo(UUID2<Book> id);
//        Result<Domain.BookInfo> addBookInfo(Domain.BookInfo bookInfo);
//        Result<Domain.BookInfo> updateBookInfo(Domain.BookInfo bookInfo);
//        Result<Domain.BookInfo> upsertBookInfo(Domain.BookInfo bookInfo);
//    }
//
//    interface UserInfo extends IRepo {
//        Result<Domain.UserInfo> fetchUserInfo(UUID2<User> id);
//        Result<Domain.UserInfo> updateUserInfo(Domain.UserInfo userInfo);
//        Domain.UserInfo upsertUserInfo(Domain.UserInfo userInfo);
//    }
//
//    interface LibraryInfo extends IRepo {
//        Result<Domain.LibraryInfo> fetchLibraryInfo(UUID2<Library> id);
//        Result<Domain.LibraryInfo> updateLibraryInfo(Domain.LibraryInfo libraryInfo);
//        Result<Domain.LibraryInfo> upsertLibraryInfo(Domain.LibraryInfo libraryInfo);
//    }
//}
//class Repo implements IRepo {
//    protected final Log log;
//
//    Repo(Log log) {
//        this.log = log;
//    }
//
//    // Business logic for Book Repo (simple CRUD oerations; converts to/from DTOs/Entities/Domains)
//    static class BookInfo extends Repo implements IRepo.BookInfo {
//        private final BookInfoApi api;
//        private final BookInfoDatabase database;
//
//        BookInfo(BookInfoApi api,
//                 BookInfoDatabase database,
//                 Log log
//        ) {
//            super(log);
//            this.api = api;
//            this.database = database;
//        }
//        BookInfo() { this(new BookInfoApi(), new BookInfoDatabase(), new Log()); }
//
//        @Override
//        public Result<Domain.BookInfo> fetchBookInfo(UUID2<Book> id) {
//            log.d(this,"Repo.BookRepo.fetchBookInfo " + id);
//
//            // Make the request to API
//            Result<DTO.BookInfo> bookInfoApiResult = api.getBookInfo(id);
//            if (bookInfoApiResult instanceof Result.Failure) {
//
//                // If API fails, try to get from cached DB
//                Result<Entity.BookInfo> bookInfoResult = database.getBookInfo(id);
//                if (bookInfoResult instanceof Result.Failure) {
//                    Exception exception = ((Result.Failure<Entity.BookInfo>) bookInfoResult).exception();
//                    return new Result.Failure<Domain.BookInfo>(exception);
//                }
//
//                Entity.BookInfo bookInfo = ((Result.Success<Entity.BookInfo>) bookInfoResult).value();
//                return new Result.Success<>(bookInfo.toDeepCopyDomainInfo());
//            }
//
//            // Convert to Domain Model
//            Domain.BookInfo bookInfo = ((Result.Success<DTO.BookInfo>) bookInfoApiResult)
//                    .value()
//                    .toDeepCopyDomainInfo();
//
//            // Cache to Local DB
//            Result<Entity.BookInfo> resultDB = database.updateBookInfo(bookInfo.toEntity());
//            if (resultDB instanceof Result.Failure) {
//                Exception exception = ((Result.Failure<Entity.BookInfo>) resultDB).exception();
//                return new Result.Failure<>(exception);
//            }
//
//            return new Result.Success<>(bookInfo);
//        }
//
//        @Override
//        public Result<Domain.BookInfo> updateBookInfo(Domain.BookInfo bookInfo) {
//            log.d(this,"Repo.BookRepo - Updating BookInfo: " + bookInfo);
//
//            Result<Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateType.UPDATE);
//            if (bookResult instanceof Result.Failure) {
//                Exception exception = ((Result.Failure<Domain.BookInfo>) bookResult).exception();
//                return new Result.Failure<>(exception);
//            }
//
//            return bookResult;
//        }
//
//        @Override
//        public Result<Domain.BookInfo> addBookInfo(Domain.BookInfo bookInfo) {
//            log.d(this,"Repo.BookRepo - Adding book info: " + bookInfo);
//
//            Result<Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateType.ADD);
//            if (bookResult instanceof Result.Failure) {
//                Exception exception = ((Result.Failure<Domain.BookInfo>) bookResult).exception();
//                return new Result.Failure<>(exception);
//            }
//
//            return bookResult;
//        }
//
//        @Override
//        public Result<Domain.BookInfo> upsertBookInfo(Domain.BookInfo bookInfo) {
//            log.d(this,"Repo.Book - Upserting book id: " + bookInfo.id());
//
//            if (database.getBookInfo(bookInfo.id()) != null) {
//                return updateBookInfo(bookInfo);
//            } else {
//                return addBookInfo(bookInfo);
//            }
//        }
//
//        ///////////////////////////////////
//        // Private Helper Methods
//        ///////////////////////////////////
//
//        private enum UpdateType {
//            ADD,
//            UPDATE,
//            UPSERT,
//            DELETE
//        }
//
//        private Result<Domain.BookInfo> saveBookToApiAndDB(
//                Domain.BookInfo bookInfo,
//                UpdateType updateType
//        ) {
//            log.d(this,"updateType: " + updateType + ", id: " + bookInfo.id());
//
//            // Make the API request
//            Result<DTO.BookInfo> resultApi;
//            switch (updateType) {
//                case UPDATE:
//                    resultApi = api.updateBookInfo(bookInfo.toDTO());
//                    break;
//                case ADD:
//                    resultApi = api.addBookInfo(bookInfo.toDTO());
//                    break;
//                default:
//                    return new Result.Failure<>(new Exception("UpdateType not supported: " + updateType));
//            }
//
//            if (resultApi instanceof Result.Failure) {
//                Exception exception = ((Result.Failure<DTO.BookInfo>) resultApi).exception();
//                return new Result.Failure<>(exception);
//            }
//
//            // Save to Local DB
//            Result<Entity.BookInfo> resultDB;
//            switch (updateType) {
//                case UPDATE:
//                    resultDB = database.updateBookInfo(bookInfo.toEntity());
//                    break;
//                case ADD:
//                    resultDB = database.addBookInfo(bookInfo.toEntity());
//                    break;
//                default:
//                    return new Result.Failure<>(new Exception("UpdateType not supported: " + updateType));
//            }
//
//            if (resultDB instanceof Result.Failure) {
//                Exception exception = ((Result.Failure<Entity.BookInfo>) resultDB).exception();
//                return new Result.Failure<>(exception);
//            }
//
//            return new Result.Success<>(bookInfo);
//        }
//
//
//        /////////////////////////////////////////////////////
//        // Debugging / Testing Methods                     //
//        //  - not part of interface or used in production) //
//        /////////////////////////////////////////////////////
//
//        public void populateDatabaseWithFakeBookInfo() {
//            for (int i = 0; i < 10; i++) {
//                database.addBookInfo(
//                        new Entity.BookInfo(
//                                UUID2.createFakeUUID2(i, Entity.BookInfo.class.getName()),
//                                "Title " + i,
//                                "Author " + i,
//                                "Description " + i)
//                );
//            }
//        }
//
//        public void populateApiWithFakeBookInfo() {
//            for (int i = 0; i < 10; i++) {
//                Result<DTO.BookInfo> result = api.addBookInfo(
//                        new DTO.BookInfo(
//                                UUID2.createFakeUUID2(i, DTO.BookInfo.class.getName()),
//                                "Title " + i,
//                                "Author " + i,
//                                "Description " + i,
//                                "Some extra info from the DTO" + i)
//                );
//
//                if (result instanceof Result.Failure) {
//                    Exception exception = ((Result.Failure<DTO.BookInfo>) result).exception();
//                    log.d(this,exception.getMessage());
//                }
//            }
//        }
//
//        public void printDB() {
//            for (Map.Entry<UUID2<Book>, Entity.BookInfo> entry : database.getAllBookInfos().entrySet()) {
//                log.d(this,entry.getKey() + " = " + entry.getValue());
//            }
//        }
//
//        public void printAPI() {
//            for (Map.Entry<UUID2<Book>, DTO.BookInfo> entry : api.getAllBookInfos().entrySet()) {
//                log.d(this,entry.getKey() + " = " + entry.getValue());
//            }
//        }
//    }
//
//    // Holds User info for all users in the system (simple CRUD operations)
//    static class UserInfo extends Repo implements IRepo.UserInfo {
//        // Simulate a database on a server somewhere
//        private final UUID2.HashMap<User, Domain.UserInfo> database = new UUID2.HashMap<>();
//
//        UserInfo(Log log) {
//            super(log);
//        }
//
//        @Override
//        public Result<Domain.UserInfo> fetchUserInfo(UUID2<User> id) {
//            log.d(this,"Repo.User - Fetching user info: " + id);
//
//            if (database.containsKey(id)) {
//                return new Result.Success<>(database.get(id));
//            }
//
//            return new Result.Failure<>(new Exception("User not found"));
//        }
//
//        @Override
//        public Result<Domain.UserInfo> updateUserInfo(Domain.UserInfo userInfo) {
//            String method = Thread.currentThread().getStackTrace()[2].getMethodName();
//            log.d(this, "Repo.User - " + method + " - Updating user info: " + userInfo);
//
//            if (database.containsKey(userInfo.id())) {
//                database.put(userInfo.id(), userInfo);
//                return new Result.Success<>(userInfo);
//            }
//
//            return new Result.Failure<>(new Exception("User not found, id:" + userInfo.id()));
//        }
//
//        @Override
//        public Domain.UserInfo upsertUserInfo(Domain.UserInfo userInfo) {
//            log.d(this,"Repo.User - Upserting user info: " + userInfo);
//
//            database.put(userInfo.id(), userInfo);
//            return userInfo;
//        }
//
//    }
//
//    // Holds Library info for all the libraries in the system (simple CRUD operations)
//    static class LibraryInfo extends Repo implements IRepo.LibraryInfo {
//        // simulate a database on server (UUID2<Library> is the key)
//        private final UUID2.HashMap<Library, Domain.LibraryInfo> database = new UUID2.HashMap<>();
//
//        LibraryInfo(Log log) {
//            super(log);
//        }
//
//        @Override
//        public Result<Domain.LibraryInfo> fetchLibraryInfo(UUID2<Library> id) {
//            log.d(this,"id: " + id);
//
//            // Simulate a network request
//            if (database.containsKey(id)) {
//                return new Result.Success<>(database.get(id));
//            }
//
//            return new Result.Failure<>(new Exception("Library not found, id: " + id));
//        }
//
//        @Override
//        public Result<Domain.LibraryInfo> updateLibraryInfo(Domain.LibraryInfo libraryInfo) {
//            log.d(this," libraryInfo: " + libraryInfo);
//
//            // Simulate a network request
//            if (database.containsKey(libraryInfo.id)) {
//                database.put(libraryInfo.id, libraryInfo);
//
//                return new Result.Success<>(libraryInfo);
//            }
//
//            return new Result.Failure<>(new Exception("Library not found, id: " + libraryInfo.id));
//        }
//
//        @Override
//        public Result<Domain.LibraryInfo> upsertLibraryInfo(Domain.LibraryInfo libraryInfo) {
//            log.d(this,"libraryInfo: " + libraryInfo);
//
//            database.put(libraryInfo.id, libraryInfo);
//
//            return new Result.Success<>(libraryInfo);
//        }
//
//        ///////////////////////////////////
//        /// Published Helper methods    ///
//        ///////////////////////////////////
//
//        public void populateWithFakeBooks(UUID2<Library> libraryId, int numberOfBooksToCreate) {
//            log.d(this,"libraryId: " + libraryId + ", numberOfBooksToCreate: " + numberOfBooksToCreate);
//            Domain.LibraryInfo library = database.get(libraryId);
//
//            for (int i = 0; i < numberOfBooksToCreate; i++) {
//                Result<UUID2<Book>> result = library.addTestBook(UUID2.createFakeUUID2(i, Book.class.getName()), 1);
//
//                if (result instanceof Result.Failure) {
//                    Exception exception = ((Result.Failure<UUID2<Book>>) result).exception();
//                    log.d(this,exception.getMessage());
//                }
//            }
//        }
//
//    }
//}

//// Simple Logging Operations
//interface ILog {
//    void d(String tag, String msg);
//    void e(String tag, String msg);
//    void e(String tag, String msg, Exception e);
//}
//class Log implements ILog {
//    public void d(String tag, String msg) {
//        System.out.println(tag + ": " + msg);
//    }
//    public void w(String tag, String msg) {
//        System.err.println(tag + ":(WARNING) " + msg);
//    }
//    public void e(String tag, String msg) {
//        System.err.println(tag + ":(ERROR) " + msg);
//    }
//    public void e(String tag, String msg, Exception e) {
//        System.err.println(tag + ":(ERROR) " + msg);
//        e.printStackTrace();
//    }
//
//    // example: log.d(this, "message") will print "ClassName➤MethodName(): message"
//    public void d(Object obj, String msg) {
//        d(obj.getClass().getSimpleName() + "➤" +
//                Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
//                msg
//        );
//    }
//
//    // example: log.w(this, "message") will print "ClassName➤MethodName():(WARNING) message"
//    public void w(Object obj, String msg) {
//        w(obj.getClass().getSimpleName() + "➤" +
//                Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
//                msg
//        );
//    }
//
//    // example: log.e(this, "message") will print "ClassName➤MethodName():(ERROR) message"
//    public void e(Object obj, String msg) {
//        e(obj.getClass().getSimpleName() + "➤" +
//            Thread.currentThread().getStackTrace()[2].getMethodName() + "()",
//            msg
//        );
//    }
//}

//// Context is a singleton class that holds all the repositories and global objects like Gson
//interface IContext {
//    Repo.BookInfo bookInfoRepo = null;
//    Repo.UserInfo userInfoRepo = null;
//    Repo.LibraryInfo libraryInfoRepo = null;
//    Gson gson = null;
//    Log log = null;
//}
//class Context implements IContext {
//    // static public Context INSTANCE = null;  // Enforces singleton instance & allows global access, LEAVE for reference
//
//    // Repository Singletons
//    private final Repo.BookInfo bookInfoRepo;
//    private final Repo.UserInfo userInfoRepo;
//    private final Repo.LibraryInfo libraryInfoRepo;
//
//    // Utility Singletons
//    protected final Gson gson;
//    public final Log log;
//
//    public enum ContextType {
//        PRODUCTION,
//        TEST
//    }
//
//    Context(
//            Repo.BookInfo bookInfoRepo,
//            Repo.UserInfo userInfoRepo,
//            Repo.LibraryInfo libraryInfoRepo,
//            Gson gson,
//            Log log) {
//        this.bookInfoRepo = bookInfoRepo;
//        this.userInfoRepo = userInfoRepo;
//        this.libraryInfoRepo = libraryInfoRepo;
//        this.gson = gson;
//        this.log = log;
//    }
//
//    public static Context setupProductionInstance() {
//        return setupInstance(PRODUCTION, null);
//    }
//    public static Context setupInstance(ContextType contextType, Context context) {
//        switch (contextType) {
//            case PRODUCTION:
//                System.out.println("Context.setupInstance(): passed in Context is null, creating PRODUCTION Context");
//                return Context.generateDefaultProductionContext();
//            case TEST:
//                System.out.println("Context.setupInstance(): using passed in Context");
//                return context;
//        }
//
//        throw new RuntimeException("Context.setupInstance(): Invalid ContextType");
//    }
//
//    // Generate sensible default singletons for the production application
//    private static Context generateDefaultProductionContext() {
//        Log log = new Log();
//        return new Context(
//            new Repo.BookInfo(
//                new BookInfoApi(),
//                new BookInfoDatabase(),
//                log
//            ),
//            new Repo.UserInfo(log),
//            new Repo.LibraryInfo(log),
//            new GsonBuilder().setPrettyPrinting().create(),
//            log
//        );
//    }
//
////    LEAVE for Reference - This is how you would enforce a singleton instance using a static method & variable
////    // If `context` is `null` OR `StaticContext` this returns the default static Context,
////    // otherwise returns the `context` passed in.
////    public static Context setupINSTANCE(Context context) {
////        if (context == null) {
////            if(INSTANCE != null) return INSTANCE;
////
////            System.out.println("Context.getINSTANCE(): passed in Context is null, creating default Context");
////            INSTANCE = new Context();
////            return INSTANCE;  // return default Context (singleton)
////        } else {
////            System.out.println("Context.getINSTANCE(): using passed in Context");
////            INSTANCE = context;  // set the default Context to the one passed in
////            return context;
////        }
////    }
////    public static Context getINSTANCE() {
////        return setupINSTANCE(null);
////    }
//
//    public Repo.BookInfo bookRepo() {
//        return this.bookInfoRepo;
//    }
//    public Repo.UserInfo userRepo() {
//        return this.userInfoRepo;
//    }
//    public Repo.LibraryInfo libraryRepo() {
//        return this.libraryInfoRepo;
//    }
//}

//// Info - Caches the Model Object's "Info" and defines required Info operations.
//// Info object stores the "business data" for the Domain object.
//// It is the "single source of truth" for the Domain object.
//// Domain objects keep a single reference to their Info object, and load/save it to/from the server/DB as needed.
//interface Info<TInfo> {
//    // TInfo info;                        // Requires a field named "info" of type TInfo (is there a way to enforce this in java?
//
//    UUID2<?> id();                        // Returns the UUID2 of the Info object
//    TInfo fetchInfo();                    // Fetches info for object from server/DB
//    boolean isInfoFetched();              // Returns true if info has been fetched from server/DB
//    Result<TInfo> fetchInfoResult();      // Fetches Result<T> for info object from server/DB
//    Result<TInfo> updateInfo(TInfo info); // Updates info for object to server/DB
//    Result<TInfo> refreshInfo();          // Refreshes info for object from server/DB
//    String fetchInfoFailureReason();      // Returns reason for failure of last fetchInfo() call, or null if successful
//
//    @SuppressWarnings("unchecked")
//    default TInfo deepCopyInfo() {                // Returns a deep copy of the Info object
//
//        return (TInfo) new Gson().fromJson(
//                new Gson().toJson(this),
//                this.getClass()
//        );
//    }
//
//    interface ToInfo<TInfo> {
//        UUID2<?> getInfoId();             // Returns the UUID2 of the Info object
//
//        @SuppressWarnings("unchecked")
//        default TInfo getInfo() {         // Returns the Info object
//            //noinspection unchecked
//            return (TInfo) this; // todo test this cast
//        }
//
//        @SuppressWarnings("unchecked")
//        default TInfo toDeepCopyInfo() {    // **MUST** override, method should return a DEEP copy (& no original references)
//            //noinspection unchecked
//            return ((Info<TInfo>) this).deepCopyInfo();  // todo test this cast
//
//            // throw new RuntimeException("Info:ToInfo:toDeepCopyInfo(): Must override this method"); // todo remove this?
//        }
//    }
//
//    public static <
//            TToInfo extends ToInfo<?> // implementations of ToInfo<TInfo> interfaces MUST have TInfo objects
//        > TToInfo createInfoFromJson(
//            String json,
//            Class<TToInfo> infoClazz, // type of `Info` object to create
//            Context context
//    ) {
//        try {
//            TToInfo obj = context.gson.fromJson(json, (Type) infoClazz);
//            assert obj != null;
//            context.log.d("Info:createInfoFromJson()", "obj = " + obj);
//
//            // Set the UUID2 typeStr to match the Info Class name
//            String infoClazzName = infoClazz.getName();
//            infoClazz.cast(obj)
//                    .getInfoId()
//                    .setUUID2TypeStr(infoClazzName);
//
//            return obj;
//        } catch (Exception e) {
//            context.log.d( "IDomainObject:createDomainInfoFromJson()", "Failed to createDomainInfoObjectFromJson() for " +
//                    "class: " + Domain.LibraryInfo.class.getName() + ", " +
//                    "json: " + json + ", " +
//                    "exception: " + e.toString());
//
//            return null;
//        }
//    }
//
//    // This interface is to enforce all DomainInfo objects have a deepCopy() method
//    // - Just add "implements ToInfo.hasDeepCopyInfo<ToInfo<{InfoClass}>>" to the class
//    //   definition, and the toDeepCopyInfo() method will be added.
//    interface hasToDeepCopyInfo<TInfo extends ToInfo<?>> {
//
//        @SuppressWarnings("unchecked")
//        default TInfo deepCopyInfo() {
//            // This is a default implementation for deepCopyInfo() that simply calls the toDeepCopyInfo() method implemented in the subclass
//            // This is a workaround for the fact that Java doesn't allow static methods in interfaces.
//            return (TInfo) ((TInfo) this).toDeepCopyInfo(); // calls the toDeepCopyInfo() method of the implementing class
//        }
//    }
//
//    default Result<TInfo> checkInfoIdMatchesJsonInfoId(TInfo infoFromJson, Class<?> infoClazz) {
//
//        try {
//            // Ensure JSON Info object has an id field
//            Object idField = infoClazz.getDeclaredField("id").get(infoFromJson);
//            if(idField == null) {
//                return new Result.Failure<>(new Exception("checkInfoIdMatchesJsonId(): Info class does not have an id field"));
//            }
//
//            String idStr = idField.toString();
//            UUID infoFromJsonId = UUID.fromString(idStr);
//
//            if (!infoFromJsonId.equals(this.id().uuid())) {
//                return new Result.Failure<>(new Exception("checkInfoIdMatchesJsonId(): Info id does not match json id, " +
//                        "info id: " + this.id() + ", " +
//                        "json id: " + idStr));
//            }
//        } catch (NoSuchFieldException e) {
//            return new Result.Failure<>(new Exception("checkInfoIdMatchesJsonId(): Info class does not have an id field"));
//        } catch (Exception e) {
//            return new Result.Failure<>(e);
//        }
//
//        return new Result.Success<>(infoFromJson);
//    }
//}

//// "{Model}Info" Data Holders held inside each App Domain Object.
//// Similar to an Entity for a database row or a DTO for a REST API, these are the objects that are
//// passed around the application. They are the "source of truth" for the data in the application.
//class Model {
//    transient protected UUID2<IUUID2> _id; // Can't make final bc need to set it during JSON deserialization. :(
//
//    Model(UUID2<IUUID2> id, String uuidTypeStr) {
//        this._id = new UUID2<IUUID2>(id, uuidTypeStr);
//    }
//
//    ///////////////////////////////
//    // Converters between
//    // - Domain.{Domain}Info
//    // - Entity.{Domain}Info
//    // - DTO.{Domain}Info
//    ///////////////////////////////
//
//    interface ToDomainInfo<TDomainInfo extends Domain> {
//        UUID2<?> getDomainInfoId();  // *MUST* override, method should return id of DomainInfo object (used for deserialization)
//
//        @SuppressWarnings("unchecked")
//        default TDomainInfo getDomainInfo()
//        {  // Return reference to TDomainInfo, used when importing JSON
//            return (TDomainInfo) this; // todo test this cast
//        }
//
//        default TDomainInfo toDeepCopyDomainInfo() {    // **MUST** override, method should return a DEEP copy (& no original references)
//            throw new RuntimeException("DomainInfo:ToDomainInfo:toDeepCopyDomainInfo(): Must override this method");
//        }
//
//        // This interface enforces all DomainInfo objects to include a deepCopyDomainInfo() method
//        // - Just add "implements ToDomainInfo.deepCopyDomainInfo<ToDomainInfo<Domain>>" to the class
//        //   definition, and the deepCopy() method will be added.
//        interface hasToDeepCopyDomainInfo<TToInfo extends ToDomainInfo<? extends Domain>> {
//
//            @SuppressWarnings("unchecked")
//            default <TDomainInfo extends Domain>
//            TDomainInfo deepCopyDomainInfo() // Requires method override, should return a deep copy (no original references)
//            {
//                // This is a hack to get around the fact that Java doesn't allow you to call a generic method from a generic class
//                return (TDomainInfo) ((TToInfo) this).toDeepCopyDomainInfo();
//            }
//        }
//    }
//    interface ToEntity<T extends Entity> {
//        T toEntity(); // Should return a deep copy (no original references)
//    }
//    interface ToDTO<T extends DTO> {
//        T toDTO();    // Should return a deep copy (no original references)
//    }
//
//    public String toPrettyJson() {
//        return new GsonBuilder().setPrettyPrinting().create().toJson(this); // todo switch over to context version
//    }
//    public String toPrettyJson(@NotNull Context context) {
//        return context.gson.toJson(this);
//    }
//
//    // These are methods are for JSON deserialization purposes
//    protected UUID2<?> id() { return _id; }
//    protected void _setIdFromImportedJson(UUID2<IUUID2> _id) {
//        this._id = _id;
//    }
//
//    // Domain objects contain the "{Model}Info" and the associated business logic to manipulate it
////    static class Domain extends Model implements Info.hasDeepCopy<ToDomainInfo<Domain>> {
//    static class Domain extends Model {
//
//        // next lines are ugly java boilerplate to allow call to super() with a UUID2
//        Domain(UUID2<?> id, String className) {
//            super(id.toDomainUUID2(), className);
//        }
//        Domain(UUID uuid, String className) {
//            super(new UUID2<IUUID2>(uuid), className);
//        }
//        Domain(String id, String className) {
//            super(UUID2.fromString(id), className);
//        }
//
//        // This is primarily for JSON deserialization purposes
//        @Override
//        public UUID2<?> id() {
//            return super.id();
//        }
//
//        static class BookInfo extends Domain implements
//                ToEntity<Entity.BookInfo>,
//                ToDTO<DTO.BookInfo>,
//                ToDomainInfo<BookInfo>
//        {
//            private final UUID2<Book> id; // note this is a UUID2<Book> not a UUID2<BookInfo>, it is the id of the Book.
//            private final String title;
//            private final String author;
//            private final String description;
//
//            BookInfo(@NotNull
//                 UUID2<Book> id,
//                 String title,
//                 String author,
//                 String description
//            ) {
//                super(id, BookInfo.class.getName());
//                this.title = title;
//                this.author = author;
//                this.description = description;
//                this.id = id;
//            }
//            BookInfo(UUID uuid, String title, String author, String description) {
//                this(new UUID2<Book>(uuid), title, author, description);
//            }
//            BookInfo(String id, String title, String author, String description) {
//                this(UUID.fromString(id), title, author, description);
//            }
//            BookInfo(Domain.BookInfo bookInfo) {
//                // todo validation
//                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
//            }
//            BookInfo(UUID id) {
//                this(id, "", "", "");
//            }
//
//            // Domain Must accept both `DTO.BookInfo` and `Entity.BookInfo` (and convert to Domain.BookInfo)
//            // Domain decides what to include from the DTOs/Entities
//            // todo - should the DTO/Entites decide what to include?
//            BookInfo(DTO.BookInfo bookInfo) {
//                // Converts from DTO to Domain
//                // todo validation here
//                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description); // Domain decides what to include from the DTOs
//            }
//            BookInfo( Entity.BookInfo bookInfo) {
//                // Converts from Entity to Domain
//                // todo validation here
//                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);  // Domain decides what to include from the Entities
//            }
//
//            @Override
//            public UUID2<Book> id() { return this.id; }
//
//            ///////////////////////////////////////////
//            // BookInfo Business Logic Methods       //
//            // - All Info manipulation logic is      //
//            //   done here.                          //
//            ///////////////////////////////////////////
//
//            public BookInfo withTitle(String title) {
//                return new BookInfo(this.id, title, this.author, this.description);
//            }
//
//            public BookInfo withAuthor(String authorName) {
//                return new BookInfo(this.id, this.title, authorName, this.description);
//            }
//
//            public BookInfo withDescription(String description) {
//                return new BookInfo(this.id, this.title, this.author, description);
//            }
//
//            @Override
//            public String toString() {
//                return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description;
//            }
//
//            @Override
//            public DTO.BookInfo toDTO() {
//                return new DTO.BookInfo(this);
//            }
//            @Override
//            public Entity.BookInfo toEntity() {
//                return new Entity.BookInfo(this);
//            }
//
//            /////////////////////////////////
//            // ToInfo implementation //
//            /////////////////////////////////
//
//            @Override
//            public Domain.BookInfo toDeepCopyDomainInfo() {
//                // shallow copy OK here bc its flat
//                return new Domain.BookInfo(this);
//            }
//
//            @Override
//            public UUID2<?> getDomainInfoId() {
//                return this.id;
//            }
//        }
//
//        static class UserInfo extends Domain implements ToDomainInfo<UserInfo> {
//            private final UUID2<User> id;  // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User.
//            private final String name;
//            private final String email;
//            private final ArrayList<UUID2<Book>> acceptedBooks;
//            private final Account account;
//
//            static class Account {
//
//                final AccountStatus accountStatus;
//                final int currentFineAmountPennies;
//                final int maxBooks;             // max books allowed to be checked out
//                final int maxDays;              // max number of days a book can be checked out
//                final int maxRenewals;          // max number of renewals (per book)
//                final int maxRenewalDays;       // max number days for each renewal (per book)
//                final int maxFineAmountPennies; // max dollar amount of all fines allowed before account is suspended
//                final int maxFineDays;          // max number of days to pay fine before account is suspended
//
//                Account(AccountStatus accountStatus,
//                        int currentFineAmountPennies,
//                        int maxBooks,
//                        int maxDays,
//                        int maxRenewals,
//                        int maxRenewalDays,
//                        int maxFineAmountPennies,
//                        int maxFineDays
//                ) {
//                    this.accountStatus = accountStatus;
//                    this.currentFineAmountPennies = currentFineAmountPennies;
//                    this.maxBooks = maxBooks;
//                    this.maxDays = maxDays;
//                    this.maxRenewals = maxRenewals;
//                    this.maxRenewalDays = maxRenewalDays;
//                    this.maxFineAmountPennies = maxFineAmountPennies;
//                    this.maxFineDays = maxFineDays;
//                }
//                Account() {
//                    this.accountStatus = AccountStatus.ACTIVE;
//                    this.currentFineAmountPennies = 0;
//                    maxBooks = 3;
//                    maxDays = 30;
//                    maxRenewals = 1;
//                    maxRenewalDays = 30;
//                    maxFineAmountPennies = 2000;
//                    maxFineDays = 30;
//                }
//
//                enum AccountStatus {
//                    ACTIVE,
//                    INACTIVE,
//                    SUSPENDED,
//                    CLOSED;
//                }
//
//                // Use Builder pattern to create Account
//                static class Builder {
//                    AccountStatus accountStatus;
//                    int maxBooks;
//                    int maxDays;
//                    int maxRenewals;
//                    int maxRenewalDays;
//                    int maxFines;
//                    int maxFineDays;
//                    int maxFineAmount;
//
//                    Builder() {
//                        this.accountStatus = AccountStatus.ACTIVE;
//                    } // default values
//                    Builder(Domain.UserInfo.Account account) {
//                        this.accountStatus = account.accountStatus;
//                        this.maxBooks = account.maxBooks;
//                        this.maxDays = account.maxDays;
//                        this.maxRenewals = account.maxRenewals;
//                        this.maxRenewalDays = account.maxRenewalDays;
//                        this.maxFines = account.maxFineAmountPennies;
//                        this.maxFineDays = account.maxFineDays;
//                        this.maxFineAmount = account.maxFineAmountPennies;
//                    }
//
//                    Builder accountStatus(AccountStatus accountStatus) {
//                        this.accountStatus = accountStatus;
//                        return this;
//                    }
//                    Builder maxBooks(int maxBooks) {
//                        this.maxBooks = maxBooks;
//                        return this;
//                    }
//                    Builder maxDays(int maxDays) {
//                        this.maxDays = maxDays;
//                        return this;
//                    }
//                    Builder maxRenewals(int maxRenewals) {
//                        this.maxRenewals = maxRenewals;
//                        return this;
//                    }
//                    Builder maxRenewalDays(int maxRenewalDays) {
//                        this.maxRenewalDays = maxRenewalDays;
//                        return this;
//                    }
//                    Builder maxFines(int maxFines) {
//                        this.maxFines = maxFines;
//                        return this;
//                    }
//                    Builder maxFineDays(int maxFineDays) {
//                        this.maxFineDays = maxFineDays;
//                        return this;
//                    }
//                    Builder maxFineAmount(int maxFineAmount) {
//                        this.maxFineAmount = maxFineAmount;
//                        return this;
//                    }
//
//                    Account build() {
//                        return new Account(
//                                this.accountStatus,
//                                this.maxBooks,
//                                this.maxDays,
//                                this.maxRenewals,
//                                this.maxRenewalDays,
//                                this.maxFines,
//                                this.maxFineDays,
//                                this.maxFineAmount
//                        );
//                    }
//                }
//            }
//
//            UserInfo(@NotNull
//                     UUID2<User> id,
//                     String name,
//                     String email,
//                     ArrayList<UUID2<Book>> acceptedBooks,
//                     Account account
//            ) {
//                super(id.toDomainUUID2(), UserInfo.class.getName());
//                this.id = id;
//                this.name = name;
//                this.email = email;
//                this.acceptedBooks = acceptedBooks;
//                this.account = account;
//            }
//            UserInfo(Domain.UserInfo userInfo) {
//                this(userInfo.id,
//                    userInfo.name,
//                    userInfo.email,
//                    userInfo.acceptedBooks,
//                    userInfo.account);
//            }
//            UserInfo(UUID uuid, String name, String email, ArrayList<UUID2<Book>> acceptedBooks, Account account) {
//                this(new UUID2<User>(uuid), name, email, acceptedBooks, account);
//            }
//            UserInfo(String id, String name, String email, ArrayList<UUID2<Book>> acceptedBooks, Account account) {
//                this(UUID.fromString(id), name, email, acceptedBooks, account);
//            }
//            UserInfo(UUID2<User> id, String name, String email) {
//                this(id, name, email, new ArrayList<UUID2<Book>>(), new Account());
//            }
//            UserInfo(UUID uuid, String name, String email) {
//                this(new UUID2<User>(uuid), name, email);
//            }
//            UserInfo(String id, String name, String email) {
//                this(UUID.fromString(id), name, email);
//            }
//
//            @Override
//            public UUID2<User> id() {
//                return id;
//            }
//
//            ///////////////////////////////
//            // Published Simple Getters  //
//            ///////////////////////////////
//
//            public String name() {
//                return this.name;
//            }
//            public String email() {
//                return this.email;
//            }
//
//            @Override
//            public String toString() {
//                return "User: " + this.name + " (" + this.email + "), acceptedBooks: " + this.acceptedBooks + ", borrowerStatus: " + this.account;
//            }
//
//            ////////////////////////////////////////
//            // User Info Business Logic Methods   //
//            ////////////////////////////////////////
//
//            public Result<ArrayList<UUID2<Book>>> acceptBook(UUID2<Book> bookId) {
//                if (this.acceptedBooks.contains(bookId)) {
//                    return new Result.Failure<>(new Exception("Book already accepted by user"));
//                }
//
//                try {
//                    this.acceptedBooks.add(bookId);
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(findAllAcceptedBooks());
//            }
//
//            public Result<ArrayList<UUID2<Book>>> unacceptBook(UUID2<Book> bookId) {
//                if (!this.acceptedBooks.contains(bookId)) {
//                    return new Result.Failure<>(new Exception("Book not accepted by user"));
//                }
//
//                try {
//                    this.acceptedBooks.remove(bookId);
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(findAllAcceptedBooks());
//            }
//
//            public ArrayList<UUID2<Book>> findAllAcceptedBooks() {
//                return new ArrayList<UUID2<Book>>(this.acceptedBooks);
//            }
//
//            public boolean isBookAcceptedByUser(UUID2<Book> bookId) {
//                return !this.acceptedBooks.contains(bookId);
//            }
//
//            /////////////////////////////
//            // ToInfo implementation   //
//            /////////////////////////////
//
//            // note: no DB or API for UserInfo (so no .ToEntity() or .ToDTO())
//            @Override
//            public Domain.UserInfo toDeepCopyDomainInfo() {
//                // Note: Must return a deep copy (no original references)
//                Domain.UserInfo domainInfoCopy = new Domain.UserInfo(this);
//
//                // deep copy of acceptedBooks
//                domainInfoCopy.acceptedBooks.clear();
//                for (UUID2<Book> bookId : this.acceptedBooks) {
//                    domainInfoCopy.acceptedBooks.add(new UUID2<Book>(bookId.uuid()));
//                }
//
//                return domainInfoCopy;
//            }
//
//            @Override
//            public UUID2<?> getDomainInfoId() {
//                return this.id;
//            }
//        }
//
//        static class LibraryInfo extends Domain implements ToDomainInfo<LibraryInfo> {
//            final UUID2<Library> id;  // note this is a UUID2<Library> not a UUID2<LibraryInfo>, it is the id of the Library.
//            final String name;
//            final private UUID2.HashMap<User, ArrayList<UUID2<Book>>> userIdToCheckedOutBookIdMap;  // registered users of this library
//            final private UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap;  // books known & available in this library
//
//            LibraryInfo(@NotNull
//                        UUID2<Library> id,
//                        String name,
//                        UUID2.HashMap<User, ArrayList<UUID2<Book>>> checkoutUserBookMap,
//                        UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap
//            ) {
//                super(id, LibraryInfo.class.getName());
//                this.name = name;
//                this.userIdToCheckedOutBookIdMap = checkoutUserBookMap;
//                this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
//                this.id = id;
//            }
//            LibraryInfo(UUID2<Library> id, String name) {
//                this(id, name, new UUID2.HashMap<>(), new UUID2.HashMap<>());
//            }
//            LibraryInfo(Domain.LibraryInfo libraryInfo) {
//                this(libraryInfo.id,
//                    libraryInfo.name,
//                    libraryInfo.userIdToCheckedOutBookIdMap,
//                    libraryInfo.bookIdToNumBooksAvailableMap);
//            }
//            LibraryInfo(UUID uuid, String name) {
//                this(new UUID2<Library>(uuid), name);
//            }
//            LibraryInfo(String id, String name) {
//                this(UUID.fromString(id), name);
//            }
//
//            @Override
//            public String toString() {
//                return this.toPrettyJson();
////                return "Library: " + this.name + " (" + this.id + ")" + "\n" +
////                        "  Available Books: " + this.bookIdToNumBooksAvailableMap + "\n" +
////                        "  Checkout Map: " + this.userIdToCheckedOutBookMap;
//            }
//
//            @Override
//            public UUID2<Library> id() {
//                return id;
//            }
//
//            /////////////////////////////////////////////
//            // Published Domain Business Logic Methods //
//            /////////////////////////////////////////////
//
//            public Result<UUID2<Book>> checkOutBookToUser(UUID2<Book> bookId, UUID2<User> userId) {
//                if(!isBookIdKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known. bookId: " + bookId));
//                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known, userId: " + userId));
//                if(!isBookIdAvailable(bookId)) return new Result.Failure<>(new IllegalArgumentException("book is not available, bookId: " + bookId));
//                if(isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user, bookId: " + bookId + ", userId: " + userId));
//
//                try {
//                    removeBookIdFromInventory(bookId, 1);
//                    addBookIdToUser(bookId, userId);
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(bookId);
//            }
//            public Result<Book> checkOutBookToUser(Book book, User user) {
//                Result<UUID2<Book>> checkedOutUUID2Book = checkOutBookToUser(book.id, user.id);
//
//                if(checkedOutUUID2Book instanceof Result.Failure) {
//                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) checkedOutUUID2Book).exception().getMessage()));
//                }
//
//                return new Result.Success<>(book);
//            }
//
//            public Result<UUID2<Book>> checkInBookFromUser(UUID2<Book> bookId, UUID2<User> userId) {
//                if(!isBookIdKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
//                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
//                if(!isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is not checked out by user"));
//
//                try {
//                    addBookIdToInventory(bookId, 1);
//                    removeBookIdFromUserId(bookId, userId);
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(bookId);
//            }
//            public Result<Book> checkInBookFromUser(Book book, User user) {
//                Result<UUID2<Book>> returnedUUID2Book = checkInBookFromUser(book.id, user.id);
//
//                if(returnedUUID2Book instanceof Result.Failure) {
//                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) returnedUUID2Book).exception().getMessage()));
//                }
//
//                return new Result.Success<>(book);
//            }
//
//            /////////////////////////////////////////
//            // Published Domain Reporting Methods  //
//            /////////////////////////////////////////
//
//            public Result<ArrayList<UUID2<Book>>> findBooksCheckedOutByUserId(UUID2<User> userId) {
//                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
//
//                return new Result.Success<>(userIdToCheckedOutBookIdMap.get(userId));
//            }
//
//
//            public Result<HashMap<UUID2<Book>, Integer>> calculateAvailableBookIdToCountOfAvailableBooksList() {
//                HashMap<UUID2<Book>, Integer> availableBookIdToNumBooksAvailableMap = new HashMap<>();
//
//                for(Book book : this.bookIdToNumBooksAvailableMap.keys()) {
//                    if(isBookIdAvailable(book)) {
//                        int numBooksAvail = this.bookIdToNumBooksAvailableMap.get(book.id);
//                        availableBookIdToNumBooksAvailableMap.put(book.id, numBooksAvail);
//                    }
//                }
//
//                return new Result.Success<>(availableBookIdToNumBooksAvailableMap);
//            }
//
//            /////////////////////////////////
//            // Published Helper Methods    //
//            /////////////////////////////////
//
//            public boolean isBookIdKnown(UUID2<Book> bookId) {
//                return bookIdToNumBooksAvailableMap.containsKey(bookId);
//            }
//            public boolean isBookIdKnown(Book book) {
//                return isBookIdKnown(book.id);
//            }
//
//            public boolean isUserIdKnown(UUID2<User> userId) {
//                return userIdToCheckedOutBookIdMap.containsKey(userId);
//            }
//            public boolean isUserIdKnown(User user) {
//                return isUserIdKnown(user.id);
//            }
//
//            public boolean isBookIdAvailable(UUID2<Book> bookId) {
//                return bookIdToNumBooksAvailableMap.get(bookId) > 0;
//            }
//            public boolean isBookIdAvailable(Book book) {
//                return isBookIdAvailable(book.id);
//            }
//
//            public boolean isBookCurrentlyCheckedOutByUser(UUID2<Book> bookId, UUID2<User> userId) {
//                return userIdToCheckedOutBookIdMap.get(userId.uuid()).contains(bookId);
//            }
//            public boolean isBookCurrentlyCheckedOutByUser(Book book, User user) {
//                return isBookCurrentlyCheckedOutByUser(book.id, user.id);
//            }
//
//            protected Result<UUID2<User>> registerUser(UUID2<User> userId) {
//                return insertUserId(userId);
//            }
//
//            /////////////////////////////////////////
//            // Published Testing Helper Methods    //
//            /////////////////////////////////////////
//
//            protected Result<UUID2<Book>> addTestBook(UUID2<Book> bookId, int quantity) {
//                return addBookIdToInventory(bookId, quantity);
//            }
//
//            protected Result<UUID2<User>> upsertTestUser(UUID2<User> userId) {
//                return upsertUserId(userId);
//            }
//
//            //////////////////////////////
//            // Private Helper Functions //
//            //////////////////////////////
//
//            private Result<UUID2<Book>> addBookIdToInventory(UUID2<Book> bookId, int quantity) {
//                if(quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));
//
//                try {
//                    if (bookIdToNumBooksAvailableMap.containsKey(bookId.uuid())) {
//                        bookIdToNumBooksAvailableMap.put(bookId.uuid(), bookIdToNumBooksAvailableMap.get(bookId.uuid()) + 1);
//                    } else {
//                        bookIdToNumBooksAvailableMap.put(bookId.uuid(), 1);
//                    }
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(bookId);
//            }
//            private Result<Book> addBookToInventory(Book book, int quantity) {
//                Result<UUID2<Book>> addedUUID2Book = addBookIdToInventory(book.id, quantity);
//
//                if(addedUUID2Book instanceof Result.Failure) {
//                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
//                }
//
//                return new Result.Success<>(book);
//            }
//
//            private Result<UUID2<Book>> removeBookIdFromInventory(UUID2<Book> bookId, int quantity) {
//                if(quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));
//
//                try {
//                    if (bookIdToNumBooksAvailableMap.containsKey(bookId.uuid())) {
//                        bookIdToNumBooksAvailableMap.put(bookId.uuid(), bookIdToNumBooksAvailableMap.get(bookId.uuid()) - 1);
//                    } else {
//                        return new Result.Failure<>(new Exception("Book not in inventory"));
//                    }
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(bookId);
//            }
//            private Result<Book> removeBookFromInventory(Book book, int quantity) {
//                Result<UUID2<Book>> removedUUID2Book = removeBookIdFromInventory(book.id, quantity);
//
//                if(removedUUID2Book instanceof Result.Failure) {
//                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
//                }
//
//                return new Result.Success<>(book);
//            }
//
//            private Result<UUID2<Book>> addBookIdToUser(UUID2<Book> bookId, UUID2<User> userId) {
//                if(!isBookIdKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
//                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
//                if(isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user"));
//
//                try {
//                    if(userIdToCheckedOutBookIdMap.containsKey(userId.uuid())) {
//                        userIdToCheckedOutBookIdMap.get(userId).add(bookId);
//                    } else {
//                        //noinspection ArraysAsListWithZeroOrOneArgument
//                        userIdToCheckedOutBookIdMap.put(userId.uuid(), new ArrayList<>(Arrays.asList(bookId)));
//                    }
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(bookId);
//            }
//            private Result<Book> addBookToUser(Book book, User user) {
//                Result<UUID2<Book>> addedUUID2Book = addBookIdToUser(book.id, user.id);
//
//                if(addedUUID2Book instanceof Result.Failure) {
//                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
//                }
//
//                return new Result.Success<>(book);
//            }
//
//            private Result<UUID2<Book>> removeBookIdFromUserId(UUID2<Book> bookId, UUID2<User> userId) {
//                if(!isBookIdKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
//                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
//                if(!isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is not checked out by user"));
//
//                try {
//                    userIdToCheckedOutBookIdMap.get(userId.uuid()).remove(bookId);
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(bookId);
//            }
//            private Result<Book> removeBookFromUser(Book book, User user) {
//                Result<UUID2<Book>> removedUUID2Book = removeBookIdFromUserId(book.id, user.id);
//
//                if(removedUUID2Book instanceof Result.Failure) {
//                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
//                }
//
//                return new Result.Success<>(book);
//            }
//
//            private Result<UUID2<User>> insertUserId(UUID2<User> userId) {
//                if(isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is already known"));
//
//                try {
//                    userIdToCheckedOutBookIdMap.put(userId.uuid(), new ArrayList<>());
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(userId);
//            }
//
//            private Result<UUID2<User>> upsertUserId(UUID2<User> userId) {
//                if(isUserIdKnown(userId)) return new Result.Success<>(userId);
//
//                return insertUserId(userId);
//            }
//
//            private Result<UUID2<User>> removeUserId(UUID2<User> userId) {
//                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
//
//                try {
//                    userIdToCheckedOutBookIdMap.remove(userId.uuid());
//                } catch (Exception e) {
//                    return new Result.Failure<>(e);
//                }
//
//                return new Result.Success<>(userId);
//            }
//
//            /////////////////////////////////
//            // ToInfo implementation //
//            /////////////////////////////////
//
//            // note: currently no DB or API for UserInfo (so no .ToEntity() or .ToDTO())
//            @Override
//            public Domain.LibraryInfo toDeepCopyDomainInfo() {
//                // Note: *MUST* return a deep copy
//                Domain.LibraryInfo libraryInfoDeepCopy = new Domain.LibraryInfo(this.id, this.name);
//
//                // Deep copy the bookIdToNumBooksAvailableMap
//                libraryInfoDeepCopy.bookIdToNumBooksAvailableMap.putAll(this.bookIdToNumBooksAvailableMap);
//
//                // Deep copy the userIdToCheckedOutBookMap
//                for (Map.Entry<UUID, ArrayList<UUID2<Book>>> entry : this.userIdToCheckedOutBookIdMap.entrySet()) {
//                    libraryInfoDeepCopy.userIdToCheckedOutBookIdMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
//                }
//
//                return libraryInfoDeepCopy;
//            }
//
//            @Override
//            public UUID2<?> getDomainInfoId() {
//                return this.id;
//            }
//        }
//    }
//
//    // Data Transfer Objects for APIs
//    // - Simple data holder class for transferring data to/from the Domain from API
//    // - Objects can be created from JSON
//    static class DTO extends Model {
//        public DTO(UUID2<IUUID2> id, String className) {
//            super(id, className);
//        }
//
//        static class BookInfo extends DTO
//            implements
//                ToDomainInfo<Domain.BookInfo>,
//                ToDomainInfo.hasToDeepCopyDomainInfo<Domain.BookInfo>,
//                Info.ToInfo<DTO.BookInfo>,
//                Info.hasToDeepCopyInfo<DTO.BookInfo>
//        {
//            final UUID2<Book> id; // note this is a UUID2<Book> and not a UUID2<BookInfo>
//            final String title;
//            final String author;
//            final String description;
//            final String extraFieldToShowThisIsADTO;
//
//            BookInfo(@NotNull
//                     UUID2<Book> id,
//                     String title,
//                     String author,
//                     String description,
//                     String extraFieldToShowThisIsADTO
//            ) {
//                super(id.toDomainUUID2(), DTO.BookInfo.class.getName());
//                this.id = id;
//                this.title = title;
//                this.author = author;
//                this.description = description;
//
//                if(extraFieldToShowThisIsADTO == null) {
//                    this.extraFieldToShowThisIsADTO = "This is a DTO";
//                } else {
//                    this.extraFieldToShowThisIsADTO = extraFieldToShowThisIsADTO;
//                }
//            }
//            BookInfo(String json, Context context) {
//                this(context.gson.fromJson(json, DTO.BookInfo.class));  // creates a DTO.BookInfo from the JSON
//            }
//
//            // Note: Intentionally DON'T accept `Entity.BookInfo` (to keep DB layer separate from API layer)
//            BookInfo(DTO.BookInfo bookInfo) {
//                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description, bookInfo.extraFieldToShowThisIsADTO);
//            }
//            BookInfo(Domain.BookInfo bookInfo) {
//                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description, "Imported from Domain.BookInfo");
//            }
//            // todo - Is it better to have a constructor that takes in a DTO.BookInfo and throws an exception? Or to not have it at all?
//            // BookInfo(Entity.BookInfo bookInfo) {
//            //     // Never accept Entity.BookInfo to keep the API layer separate from the DB layer
//            //     super(bookInfo.id.toDomainUUID2());
//            //     throw new IllegalArgumentException("DTO.BookInfo should never be created from Entity.BookInfo");
//            // }
//
//            @Override
//            public String toString() {
//                return "Book (" + this.id + ") : " + this.title + " by " + this.author +", " + this.description;
//            }
//
//            ///////////////////////////////////////////
//            // DTOs don't have any business logic    //
//            ///////////////////////////////////////////
//
//            ///////////////////////////////////
//            // ToDomainInfo implementation   //
//            ///////////////////////////////////
//
//            @Override
//            public Domain.BookInfo toDeepCopyDomainInfo() {
//                // note: implement deep copy, if required.
//                return new Domain.BookInfo(
//                    this.id,
//                    this.title,
//                    this.author,
//                    this.description
//                );
//            }
//
//            @Override
//            public UUID2<Book> getDomainInfoId() {
//                return this.id;
//            }
//
//            /////////////////////////////
//            // ToInfo implementation   //
//            /////////////////////////////
//
//            @Override
//            public DTO.BookInfo toDeepCopyInfo() {
//                // note: implement deep copy, if needed.
//                return new DTO.BookInfo(this);
//            }
//
//            @Override
//            public UUID2<Book> getInfoId() {
//                return this.id;
//            }
//        }
//    }
//
//    // Entities for Databases
//    // Simple data holder class for transferring data to/from the Domain from Database
////    static class Entity extends Model implements Info.hasToDeepCopyInfo<DomainInfo<Domain>> {
//    static class Entity extends Model {
//        Entity(UUID2<IUUID2> id, String className) {
//            super(id, className);
//        }
//
//        static class BookInfo extends Entity
//            implements
//                ToDomainInfo<Domain.BookInfo>,
//                ToDomainInfo.hasToDeepCopyDomainInfo<Domain.BookInfo>,
//                Info.ToInfo<Entity.BookInfo>,
//                Info.hasToDeepCopyInfo<Entity.BookInfo>
//        {
//            final UUID2<Book> id;  // note this is a UUID2<Book> and not a UUID2<BookInfo>
//            final String title;
//            final String author;
//            final String description;
//            final String extraFieldToShowThisIsAnEntity = "This is an Entity";
//
//            BookInfo(
//                    @NotNull UUID2<Book> id,
//                    String title,
//                    String author,
//                    String description
//            ) {
//                super(id.toDomainUUID2(), BookInfo.class.getName());
//                this.id = id;
//                this.title = title;
//                this.author = author;
//                this.description = description;
//            }
//
//            // Note: Intentionally DON'T accept `DTO.BookInfo` (to keep DB layer separate from API layer)
//            BookInfo(Entity.BookInfo bookInfo) {
//                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
//            }
//            BookInfo(Domain.BookInfo bookInfo) {
//                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
//            }
//            // todo Is it better to have a constructor that takes in a DTO.BookInfo and throws an exception? Or to not have it at all?
//            // BookInfo(DTO.BookInfo bookInfo) {
//            //     // Never accept DTO.BookInfo to keep the API layer separate from the DB layer
//            //     super(bookInfo.id.toDomainUUID2());
//            //     throw new IllegalArgumentException("Entity.BookInfo should never be created from DTO.BookInfo");
//            // }
//
//            @Override
//            public String toString() {
//                return "Book (" + this.id + ") : " + this.title + " by " + this.author +", " + this.description;
//            }
//
//            ////////////////////////////////////////////
//            // Entities don't have any business logic //
//            ////////////////////////////////////////////
//
//            /////////////////////////////////
//            // ToDomainInfo implementation //
//            /////////////////////////////////
//
//            @Override
//            public Domain.BookInfo toDeepCopyDomainInfo() {
//                // implement deep copy, if needed.
//                return new Domain.BookInfo(this);
//            }
//
//            @Override
//            public UUID2<?> getDomainInfoId() {
//                return this.id;
//            }
//
//            /////////////////////////////
//            // ToInfo implementation   //
//            /////////////////////////////
//
//            @Override
//            public Entity.BookInfo toDeepCopyInfo() {
//                // note: implement deep copy, if needed.
//                return new Entity.BookInfo(this);
//            }
//
//            @Override
//            public UUID2<Book> getInfoId() {
//                return this.id;
//            }
//        }
//    }
//}

///////////////////////////////////////////////////////
//// Domain Role - Common Domain Role Abstract class
//abstract class IRole<TDomainInfo extends Domain>
//    implements
//        Info<TDomainInfo>,
//        IUUID2
//{
//    // Unique ID for this Role
//    // - Matches id of its Info<TDomainInfo> object (to avoid confusion)
//    // - Marked transient so gson will ignore it, as every concrete DomainObject will have a type-specific UUID2.
//    private final UUID2<IUUID2> id;
//
//    protected TDomainInfo info;  // Information object for Info<Domain.{Domain}Info>
//    protected Result<TDomainInfo> infoResult = null;
//
//    // Singletons
//    protected final Context context;
//
//    // Clazz of the Info<TDomain> (for Gson serialization)
//    @SuppressWarnings("unchecked")
//    private final Class<TDomainInfo> infoClazz =
//            (Class<TDomainInfo>) ((ParameterizedType) getClass()
//                .getGenericSuperclass())
//                .getActualTypeArguments()[0];
//
//    private IRole(
//            @NotNull UUID id,
//            TDomainInfo info,
//            @NotNull Context context
//        ) {
//            this.id = UUID2.fromUUID(id); // intentionally NOT validating `id==info.id` bc need to be able to pass in `info` as null.
//            this.info = info;
//            this.context = context;
//        }
//    <TDomainInfo_ extends ToDomainInfo<TDomainInfo>> // All classes implementing ToDomain<> interfaces must have TDomainInfo field
//        IRole(
//            @NotNull String domainInfoJson,
//            Class<TDomainInfo_> classType,
//            Context context
//        ) {
//            this(
//                Objects.requireNonNull(
//                    IRole.createDomainInfoFromJson(domainInfoJson, classType, context)
//                ).getDomainInfo(),
//                context
//            );
//        }
//    <TDomainInfo_ extends TDomainInfo>
//        IRole(
//            @NotNull TDomainInfo_ info,
//            Context context
//        ) {
//            this(info.id().uuid(), info, context);
//        }
//    IRole(
//        @NotNull UUID2<IUUID2> id,
//        Context context
//    ) {
//        this(id.toUUID(), null, context);
//    }
//    IRole(Context context) {
//        this(UUID.randomUUID(), null, context);
//    }
//    // LEAVE for reference, for static Context instance implementation
//    //IDomainObject(String json) {
//    //    this(json, null);
//    //    this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation
//    //}
//    //IDomainObject(T info) {
//    //    this(info, null);
//    //    this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation
//    //}
//    //IDomainObject(UUID id) {
//    //    this(id, null);
//    //    this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation
//    //}
//
//    public UUID2<IUUID2> id() {
//        return this.id;
//    }
//
//    // Todo refactor to use Info.createInfoFromJson interface instead of this method
//    // Creates new `Domain.{Domain}Info` object with id from JSON string of `Domain.{Domain}Info` object
//    // - Implemented as a static method bc it can be called from a constructor.
//    //   (Can't call instance methods from constructor in java.)
//    // - Note: Type definitions are to make sure constrained to Domain subtypes and subclasses.
//    // - ie: The Library domain object has a Domain.LibraryInfo object which requires ToDomain<Domain.LibraryInfo>
//    //   to be implemented.
//    // - Only imports JSON to Domain objects.
//    //   The Domain.Entity and Domain.DTO layer are intentionally restricted to accept only Domain objects.
//    // - todo : Should change to a marker interface instead of a constraining to the ToDomain<TDomain> interface?
//    @SuppressWarnings("unchecked") // for _setIdFromImportedJson() call
//    public static <
//            TDomain extends Domain,  // restrict to Domain subclasses, ie: Domain.BookInfo
//            TDomainInfo extends ToDomainInfo<? extends TDomain>, // implementations of ToInfo<TDomain> interfaces MUST have Info<TDomain> objects
//            TToInfo extends ToInfo<?>
//        > TDomainInfo createDomainInfoFromJson(
//            String json,
//            Class<TDomainInfo> domainInfoClazz, // type of `Domain.TDomainInfo` object to create
//            Context context
//    ) {
//        try {
//            TDomainInfo obj = context.gson.fromJson(json, (Type) domainInfoClazz);
//            context.log.d("IDomainObject:createDomainInfoFromJson()", "obj = " + obj);
//
//            // Set the UUID2 typeStr to match the type of the TDomainInfo object
//            String domainInfoClazzName = domainInfoClazz.getName();
//            domainInfoClazz.cast(obj)
//                    .getDomainInfoId()
//                    .setUUID2TypeStr(domainInfoClazzName);
//
//            // Set Domain "master" id to match id of imported Info, ie: Model._id = Domain.TDomainInfo.id // todo maybe a better way to do this?
//            ((TDomain) obj)._setIdFromImportedJson(
//                new UUID2<>(((TDomain) obj).id(), domainInfoClazzName)
//            );
//
//            return obj;
//        } catch (Exception e) {
//            context.log.d( "IDomainObject:createDomainInfoFromJson()", "Failed to createDomainInfoObjectFromJson() for " +
//                    "class: " + Domain.LibraryInfo.class.getName() + ", " +
//                    "json: " + json + ", " +
//                    "exception: " + e.toString());
//
//            return null;
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public Result<TDomainInfo> updateDomainInfoFromJson(String json) {
//        context.log.d(this,"Updating Domain Info from JSON for " +
//                "class: " + this.getClass().getName() + ", " +
//                "id: " + this.id());
//
//        try {
//            Class<TDomainInfo> domainInfoClazz = this.infoClazz;
//            TDomainInfo infoFromJson = this.context.gson.fromJson(json, domainInfoClazz);
//            assert infoFromJson.getClass() == this.info.getClass();
//
//            Result<TDomainInfo> checkResult = checkInfoIdMatchesJsonInfoId(infoFromJson, domainInfoClazz);
//            if (checkResult instanceof Result.Failure) {
//                return checkResult;
//            }
//
//            // Update the info object with the new info
//            return this.updateInfo(infoFromJson);
//        } catch (JsonSyntaxException e) {
//            return new Result.Failure<>(new Exception("Failed to parse JSON: " + e.getMessage()));
//        } catch (Exception e) {
//            return new Result.Failure<>(e);
//        }
//    }
//
//    public String toJson() {
//        if(!isInfoFetched()) {
//            context.log.w(this,"called on unfetched info for " +
//                    "class: " + this.getClass().getName() + ", " +
//                    "id: " + this.id());
//
//            return "{}";
//        }
//
//        return this.context.gson.toJson(this.fetchInfo());
//    }
//
//    /////////////////////////////////////////////////////
//    // Methods required to be overridden in subclasses //
//    /////////////////////////////////////////////////////
//
//    // Defines how to fetch info from server
//    // - *MUST* be overridden/implemented in subclasses
//    @Override
//    public Result<TDomainInfo> fetchInfoResult() {
//        return new Result.Failure<>(new Exception("Not Implemented, should be implemented in subclass"));
//    }
//
//    // Updates the info object with a new info object
//    // - *MUST* be overridden/implemented in subclasses
//    // - Call super.updateInfo(info) to update the info<TDomainInfo> object
//    //   (caller decides when appropriate, ie: optimistic updates, or after server confirms update)
//    @Override
//    public Result<TDomainInfo> updateInfo(TDomainInfo info) { // **MUST** Override in subclasses
//        this.info = info;
//        return new Result.Success<>(this.info);
//    }
//
//    // NOTE: Should be Implemented by subclasses but not required
//    @Override
//    public String toString() {
//        // default toString() implementation
//        String infoString = this.info == null ? "null" : this.info.toString();
//        String nameOfClass = this.getClass().getName();
//
//        return nameOfClass + ": " + this.id() + ", info=" + infoString;
//    }
//
//    /////////////////////////////////
//    // Info<T> interface methods   //
//    /////////////////////////////////
//
//    public TDomainInfo info() {
//        return this.fetchInfo();
//    }
//
//    // Returns the Info<T> object if it has been fetched, otherwise null.
//    // Used to access the Info object without having to handle the Result<T> object.
//    // NOTE: The Info object is not re-fetched if it has already been fetched.
//    @Override
//    public TDomainInfo fetchInfo() {
//        if (isInfoFetched()) {
//            return this.info;
//        }
//
//        // Attempt to fetch info, since it hasn't been successfully fetched yet.
//        Result<TDomainInfo> result = this.fetchInfoResult();
//        if (result instanceof Result.Failure) {
//            context.log.d(this,"fetchInfoResult() FAILED for " +
//                    "class: " + this.getClass().getName() + ", " +
//                    "id: " + this.id.toString());
//
//            return null;
//        }
//
//        // Fetch was successful, so set info and return it.
//        this.info = ((Result.Success<TDomainInfo>) result).value();
//        return this.info;
//    }
//
//    // Returns reason for failure of last fetchInfo() call, or null if was successful.
//    // - Used as a convenient error guard for methods that require the {Domain}Info to be loaded.
//    // - If Info is not fetched, it attempts to fetch it.
//    // - The "returning null" behavior is to make the call site error handling code smaller.
//    @Override
//    public String fetchInfoFailureReason() {
//        if (!isInfoFetched()) {
//            if (fetchInfoResult() instanceof Result.Failure) {
//                return ((Result.Failure<TDomainInfo>) fetchInfoResult()).exception().getMessage();
//            }
//        }
//
//        return null; // Returns `null` if the info has been fetched successfully. This makes the call site smaller.
//    }
//
//    @Override
//    public boolean isInfoFetched() {
//        return this.info != null;
//    }
//
//    // Forces refresh of Info from server
//    @Override
//    public Result<TDomainInfo> refreshInfo() {
//        context.log.d(this,"Refreshing info for " +
//                "class: " + this.getClass().getName() + ", " +
//                "id: " + this.id.toString());
//
//        this.info = null;
//        return this.fetchInfoResult();
//    }
//
//    /////////////////////////////////
//    // Private helpers             //
//    /////////////////////////////////
//}

////////////////////////////////////////////////
// Application Layer - Domain Objects         //
////////////////////////////////////////////////

//// Book Domain Object - Only interacts with its own repo, Context, and other Domain Objects
//class Book extends IRole<Domain.BookInfo> implements IUUID2 {
//    final UUID2<Book> id;
//    private final Repo.BookInfo repo;
//
//    Book(Domain.BookInfo info, Context context) {
//        super(info, context);
//        this.repo = this.context.bookRepo();
//        this.id = this.info.id();
//
//        context.log.d(this, "Book (" + this.id + ") created from info");
//    }
//    Book(String json, Class<Domain.BookInfo> clazz, Context context) {
//        super(json, clazz, context);
//        this.repo = this.context.bookRepo();
//        this.id = this.info.id();
//    }
//    Book(UUID2<Book> id, Context context) {
//        this(new Domain.BookInfo(id.uuid()), context);
//    }
//    Book(String json, Context context) {
//        this(json, Domain.BookInfo.class, context);
//    }
//    Book(Context context) {
//        this(new Domain.BookInfo(UUID2.randomUUID2().uuid()), context);
//    }
//
//    // LEAVE for reference, for static Context instance implementation
//    // Book(UUID2<Book id) {
//    //     this(id, null);
//    // }
//
//    @Override
//    public Result<Domain.BookInfo> fetchInfoResult() {
//        // context.log.d(this,"Book (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging
//
//        infoResult = this.repo.fetchBookInfo(this.id);
//        if (infoResult instanceof Result.Failure) {
//            return infoResult;
//        }
//
//        this.info = ((Result.Success<Domain.BookInfo>) infoResult).value();
//
//        return infoResult;
//    }
//
//    @Override
//    public Result<Domain.BookInfo> updateInfo(Domain.BookInfo updatedInfo) {
//        // Update self optimistically
//        super.updateInfo(updatedInfo);
//
//        // Update the repo
//        Result<Domain.BookInfo> infoResult = this.repo.updateBookInfo(updatedInfo);
//        if (infoResult instanceof Result.Failure) {
//            return infoResult;
//        }
//
//        // Update self with repo result
//        this.info = ((Result.Success<Domain.BookInfo>) infoResult).value();
//        return infoResult;
//    }
//
//    ///////////////////////////////////////////
//    // Book Domain Business Logic Methods    //
//    ///////////////////////////////////////////
//
//    public Result<Domain.BookInfo> updateAuthor(String authorName) {
//        Domain.BookInfo updatedInfo = this.info.withAuthor(authorName);
//        return this.updateInfo(updatedInfo);
//    }
//
//    public Result<Domain.BookInfo> updateTitle(String title) {
//        Domain.BookInfo updatedInfo = this.info.withTitle(title);
//        return this.updateInfo(updatedInfo);
//    }
//
//    public Result<Domain.BookInfo> updateDescription(String description) {
//        Domain.BookInfo updatedInfo = this.info.withDescription(description);
//        return this.updateInfo(updatedInfo);
//    }
//
//    @Override
//    public String getUUID2TypeStr() {
//        return this.getClass().getName();
//    }
//}

//// User Domain Object - Only interacts with its own Repo, Context, and other Domain Objects
//class User extends IRole<Domain.UserInfo> implements IUUID2 {
//    final UUID2<User> id;
//    private final Repo.UserInfo repo;
//
//    User(Domain.UserInfo info, Context context) {
//        super(info, context);
//        this.repo = context.userRepo();
//        this.id = info.id();
//
//        context.log.d(this,"User (" + this.id.toString() + ") created");
//    }
//    User(UUID2<User> id, Context context) {
//        super(id.toDomainUUID2(), context);
//        this.repo = context.userRepo();
//        this.id = id;
//
//        context.log.d(this,"User (" + this.id.toString() + ") created");
//    }
//    User(String json, Class<Domain.UserInfo> classType, Context context) {
//        super(json, classType, context);
//        this.repo = context.userRepo();
//        this.id = this.info.id();
//
//        context.log.d(this,"User (" + this.id.toString() + ") created");
//    }
//    User(String json, Context context) {
//        this(json, Domain.UserInfo.class, context);
//    }
//    User(Context context) {
//        this(UUID2.randomUUID2(), context);
//    }
//
//    // LEAVE for reference, for static Context instance implementation
//    // User(UserUUID id) {
//    //     this(id, null);
//    // }
//
//    @Override
//    public Result<Domain.UserInfo> fetchInfoResult() {
//        // context.log.d(this,"User (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging
//
//        infoResult = this.repo.fetchUserInfo(this.id);
//        if (infoResult instanceof Result.Failure) {
//            return infoResult;
//        }
//
//        this.info = ((Result.Success<Domain.UserInfo>) infoResult).value();
//        return infoResult;
//    }
//
//    @Override
//    public Result<Domain.UserInfo> updateInfo(Domain.UserInfo updatedUserInfo) {
//        context.log.d(this,"User (" + this.id + "),  userInfo: " + updatedUserInfo);
//
//        // Update self optimistically
//        super.updateInfo(updatedUserInfo);
//
//        // Update the repo
//        Result<Domain.UserInfo> infoResult = this.repo.updateUserInfo(updatedUserInfo);
//        if (infoResult instanceof Result.Failure) {
//            return infoResult;
//        }
//
//        // Update self with Repo result
//        this.info = ((Result.Success<Domain.UserInfo>) infoResult).value();
//        return infoResult;
//    }
//
//    ///////////////////////////////////////////
//    // User Domain Business Logic Methods    //
//    ///////////////////////////////////////////
//
//    public Result<ArrayList<Book>> acceptBook(Book book) {
//        context.log.d(this,"User (" + this.id.toString() + "),  book: " + this.id.toString());
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        Result<ArrayList<UUID2<Book>>> acceptResult = this.info.acceptBook(book.id);
//        if(acceptResult instanceof Result.Failure)
//            return new Result.Failure<>(new Exception("Failed to acceptBook, book: " + book.id.toString()));
//
//        Result<Domain.UserInfo> result = this.updateInfo(this.info);
//        if (result instanceof Result.Failure)
//            return new Result.Failure<>(((Result.Failure<Domain.UserInfo>) result).exception());
//
//        return findAllAcceptedBooks();
//    }
//
//    public Result<ArrayList<Book>> findAllAcceptedBooks() {
//        context.log.d(this,"User (" + this.id.toString() + ")");
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        // Create Book Domain list from the list of Book UUID2s
//        ArrayList<Book> books = new ArrayList<>();
//        for (UUID2<Book> bookId : this.info.findAllAcceptedBooks()) {
//            books.add(new Book(bookId, this.context));
//        }
//
//        return new Result.Success<>(books);
//    }
//
//    public Result<ArrayList<UUID2<Book>>> unacceptBook(Book book) {
//        context.log.d(this,"User (" + this.id.toString() + ") - returnBook,  book: " + book.id.toString() + " to user: " + this.id.toString());
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        Result<ArrayList<UUID2<Book>>> unacceptResult = this.info.unacceptBook(book.id);
//        if(unacceptResult instanceof Result.Failure) {
//            return new Result.Failure<>(new Exception("Failed to unaccept book from User, book: " + book.id.toString()));
//        }
//
//        Result<Domain.UserInfo> result = this.updateInfo(this.info);
//        if (result instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<Domain.UserInfo>) result).exception());
//        }
//
//        return unacceptResult;
//    }
//
//    // Note: *ONLY* the Domain Object can take a Book from one User and give it to another User.
//    public Result<ArrayList<UUID2<Book>>> giveBookToUser(Book book, User receivingUser) {
//        context.log.d(this,"User (" + id + ") - giveBookToUser,  book: " + book.id + ", user: " + this.id);
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        // Check this User has the Book
//        if (!this.info.isBookAcceptedByUser(book.id))
//            return new Result.Failure<>(new Exception("User (" + this.id + ") does not have book (" + book.id + ")"));
//
//        // Add the Book to the receiving User
//        Result<ArrayList<Book>> acceptBookResult = receivingUser.acceptBook(book);
//        if (acceptBookResult instanceof Result.Failure)
//            return new Result.Failure<>(((Result.Failure<ArrayList<Book>>) acceptBookResult).exception());
//
//        // Remove the Book from this User
//        Result<ArrayList<UUID2<Book>>> unacceptBookResult = this.unacceptBook(book);
//        if (unacceptBookResult instanceof Result.Failure)
//            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) unacceptBookResult).exception());
//
//        // Update UserInfo
//        Result<Domain.UserInfo> result = this.updateInfo(this.info);
//        if (result instanceof Result.Failure)
//            return new Result.Failure<>(((Result.Failure<Domain.UserInfo>) result).exception());
//
//        return unacceptBookResult;
//    }
//
//    public Result<UUID2<Book>> checkoutBookFromLibrary(Book book, Library library) {
//        context.log.d(this,"User (" + this.id + "), book: " + this.id + ", library: " + library.id);
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        Result<Book> bookResult = library.checkOutBookToUser(book, this);
//        if (bookResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<Book>) bookResult).exception());
//        }
//
//        return new Result.Success<>(((Result.Success<Book>) bookResult).value().id);
//    }
//
//    @Override
//    public String getUUID2TypeStr() {
//        return this.getClass().getName();
//    }
//}

//// Library Domain Object - *ONLY* interacts with its own Repo, Context, and other Domain Objects
//class Library extends IRole<Domain.LibraryInfo> implements IUUID2 {
//    final UUID2<Library> id;
//    private Repo.LibraryInfo repo = null;
//
//    Library(Domain.LibraryInfo info, Context context) {
//        super(info, context);
//        this.repo = this.context.libraryRepo();
//        this.id = info.id;
//
//        context.log.d(this,"Library (" + this.id + ") created");
//    }
//    Library(UUID2<Library> id, Context context) {
//        super(id.toDomainUUID2(), context);
//        this.repo = this.context.libraryRepo();
//        this.id = id;
//
//        context.log.d(this,"Library (" + this.id + ") created");
//    }
//    Library(String json, Class<Domain.LibraryInfo> classType, Context context) {
//        super(json, classType, context);
//        this.repo = this.context.libraryRepo();
//        this.id = this.info.id;
//    }
//    Library(String json, Context context) { this(json, Domain.LibraryInfo.class, context); }
//    Library(Context context) {
//        super(UUID2.randomUUID2(), context);
//        this.repo = this.context.libraryRepo();
//        this.id = this.info.id;
//    }
//
//    // LEAVE for reference, for static Context instance implementation
//    // Library() {
//    //     this(UUID2.randomUUID());
//    // }
//
//    @Override
//    public Result<Domain.LibraryInfo> fetchInfoResult() {
//        // context.log.d(this,"Library (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging
//
//        infoResult = this.repo.fetchLibraryInfo(this.id);
//        if (infoResult instanceof Result.Failure) {
//            return infoResult;
//        }
//
//        this.info = ((Result.Success<Domain.LibraryInfo>) infoResult).value();
//
//        return infoResult;
//    }
//
//    @Override
//    public Result<Domain.LibraryInfo> updateInfo(Domain.LibraryInfo updatedInfo) {
//        // context.log.d(this,"Library (" + this.id.toString() + ") - updateInfo, newInfo: " + newInfo.toString());  // LEAVE for debugging
//
//        // Update self optimistically
//        super.updateInfo(updatedInfo);
//
//        // Update the Repo
//        Result<Domain.LibraryInfo> infoResult = this.repo.updateLibraryInfo(updatedInfo);
//        if (infoResult instanceof Result.Failure) {
//            return infoResult;
//        }
//
//        // Update self with Repo result
//        super.updateInfo(((Result.Success<Domain.LibraryInfo>) infoResult).value());
//        return infoResult;
//    }
//
//    @Override
//    public String getUUID2TypeStr() {
//        return this.getClass().getName();
//    }
//
//    ///////////////////////////////////////////
//    // Library Domain Business Logic Methods //
//    ///////////////////////////////////////////
//
//    public Result<Book> checkOutBookToUser(Book book, User user) {
//        context.log.d(this, format("Library (%s) - checkOutBookToUser, user: %s, book: %s", this.id, this.id.toString(), this.id.toString()));
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        if (isUnableToFindOrAddUser(user)) {
//            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
//        }
//
//        // Check out Book to User
//        Result<Book> checkOutBookresult = this.info.checkOutBookToUser(book, user);
//        if (checkOutBookresult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<Book>) checkOutBookresult).exception());
//        }
//
//        // User receives Book
//        Result<ArrayList<Book>> receiveBookResult = user.acceptBook(book);
//        if (receiveBookResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<ArrayList<Book>>) receiveBookResult).exception());
//        }
//
//        // Update the Info
//        Result<Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
//        if (updateInfoResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<Domain.LibraryInfo>) updateInfoResult).exception());
//        }
//
//        return new Result.Success<>(book);
//    }
//
//    public Result<Book> checkInBookFromUser(Book book, User user) {
//        context.log.d(this, format("Library (%s) - checkInBookFromUser, book %s from user %s\n", this.id, this.id, this.id));
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        if (isUnableToFindOrAddUser(user)) {
//            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
//        }
//
//        Result<Book> checkInBookResult = this.info.checkInBookFromUser(book, user);
//        if (checkInBookResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<Book>) checkInBookResult).exception());
//        }
//
//        Result<ArrayList<UUID2<Book>>> userReturnedBookResult = user.unacceptBook(book);
//        if (userReturnedBookResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) userReturnedBookResult).exception());
//        }
//
//        // Update the Info
//        Result<Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
//        if (updateInfoResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<Domain.LibraryInfo>) updateInfoResult).exception());
//        }
//
//        return new Result.Success<>(book);
//    }
//
//    // This Library DomainObject enforces the rule: if a User is not known, they are added as a new user.
//    public boolean isUnableToFindOrAddUser(User user) {
//        context.log.d(this, format("Library (%s) user: %s", this.id, this.id));
//        if (fetchInfoFailureReason() != null) return true;
//
//        if (isKnownUser(user)) {
//            return false;
//        }
//
//        // Create a new User entry in the Library
//        Result<UUID2<User>> addRegisteredUserResult = this.info.registerUser(user.id);
//        //noinspection RedundantIfStatement
//        if (addRegisteredUserResult instanceof Result.Failure) {
//            return true;
//        }
//
//        return false;
//    }
//
//    public boolean isKnownBook(Book book) {
//        context.log.d(this, format("Library(%s) Book id: %s\n", this.id, book.id));
//        if (fetchInfoFailureReason() != null) return false;
//
//        return this.info.isBookIdKnown(book);
//    }
//
//    public boolean isKnownUser(User user) {
//        context.log.d(this, format("Library (%s) User id: %s", this.id, user.id));
//        if (fetchInfoFailureReason() != null) return false;
//
//        return this.info.isUserIdKnown(user);
//    }
//
//    public boolean isBookAvailable(Book book) {
//        context.log.d(this, format("Library (%s) Book id: %s\n", this.id, book.id));
//        if (fetchInfoFailureReason() != null) return false;
//
//        return this.info.isBookIdAvailable(book);
//    }
//
//    public Result<ArrayList<Book>> findBooksCheckedOutByUser(User user) {
//        context.log.d(this, format("Library (%s) User id: %s\n", this.id, user));
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        // Make sure User is Known
//        if (isUnableToFindOrAddUser(user)) {
//            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
//        }
//
//        Result<ArrayList<UUID2<Book>>> entriesResult = this.info.findBooksCheckedOutByUserId(user.id);
//        if (entriesResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) entriesResult).exception());
//        }
//
//        // Convert UUID2<Books to Books
//        ArrayList<UUID2<Book>> bookIds = ((Result.Success<ArrayList<UUID2<Book>>>) entriesResult).value();
//        ArrayList<Book> books = new ArrayList<>();
//        for (UUID2<Book> entry : bookIds) {
//            books.add(new Book(entry, context));
//        }
//
//        return new Result.Success<>(books);
//    }
//
//    public Result<HashMap<Book, Integer>> calculateAvailableBookIdToNumberAvailableList() {
//        context.log.d(this, "Library (" + this.id + ")");
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        Result<HashMap<UUID2<Book>, Integer>> entriesResult = this.info.calculateAvailableBookIdToCountOfAvailableBooksList();
//        if (entriesResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<HashMap<UUID2<Book>, Integer>>) entriesResult).exception());
//        }
//
//        // Convert list of UUID2<Book> to list of Book
//        // Note: the BookInfo is not fetched, so the Book only contains the id. This is by design.
//        HashMap<UUID2<Book>, Integer> bookIdToNumberAvailable = ((Result.Success<HashMap<UUID2<Book>, Integer>>) entriesResult).value();
//        HashMap<Book, Integer> bookToNumberAvailable = new HashMap<>();
//        for (Map.Entry<UUID2<Book>, Integer> entry : bookIdToNumberAvailable.entrySet()) {
//            bookToNumberAvailable.put(new Book(entry.getKey(), context), entry.getValue());
//        }
//
//        return new Result.Success<>(bookToNumberAvailable);
//    }
//
//    public Result<Book> addTestBookToLibrary(Book book, Integer count) {
//        context.log.d(this, format("Library (%s) book: %s, count: %s", this.id, book, count));
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        Result<UUID2<Book>> addBookResult =  this.info.addTestBook(book.id, count);
//        if (addBookResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<UUID2<Book>>) addBookResult).exception());
//        }
//
//        // Update the Info
//        Result<Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
//        if (updateInfoResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<Domain.LibraryInfo>) updateInfoResult).exception());
//        }
//
//        return new Result.Success<>(book);
//    }
//
//    public void DumpDB(Context context) {
//        context.log.d(this,"\nDumping Library DB:");
//        context.log.d(this,this.toJson());
//        context.log.d(this,"\n");
//    }
//}

//class LibraryApp {
//
//    public static void main(final String... args) {
//
//        // Setup App Context Object singletons
//        Context productionContext = Context.setupProductionInstance();
//        // Context productionContext = Context.setupInstance(TEST, testContext); // for testing
//
//        new LibraryApp(productionContext);
//    }
//
//    LibraryApp(Context ctx) {
//        //context = Context.setupINSTANCE(context);  // For implementing a static Context. LEAVE for reference
//
//        ctx.log.d(this,"Populating Book DB and API");
//        PopulateFakeBookInfoInContextBookRepoDBandAPI(ctx);
//
//        Populate_And_Poke_Book:
//        if(false) {
//            ctx.log.d(this, "----------------------------------");
//            ctx.log.d(this, "Populate_And_Poke_Book");
//
//            // Create a book object (it only has an id)
//            Book book = new Book(UUID2.createFakeUUID2(1, Book.class.getName()), ctx);
//            ctx.log.d(this,book.fetchInfoResult().toString());
//
//            // Update info for a book
//            final Result<Domain.BookInfo> bookInfoResult =
//                    book.updateInfo(
//                        new Domain.BookInfo(
//                                book.id,
//                                "The Updated Title",
//                                "The Updated Author",
//                                "The Updated Description"
//                        ));
//            ctx.log.d(this,book.fetchInfoResult().toString());
//
//            // Get the bookInfo (null if not loaded)
//            Domain.BookInfo bookInfo3 = book.fetchInfo();
//            if (bookInfo3 == null) {
//                ctx.log.d(this,"Book Missing --> " +
//                        "book id: " + bookInfo3.id() + " >> " +
//                        " is null"
//                );
//            } else {
//                ctx.log.d(this,"Book Info --> " +
//                        bookInfo3.toString()
//                );
//            }
//
//            // Try to get a book id that doesn't exist
//            Book book2 = new Book(UUID2.createFakeUUID2(99, Book.class.getName()), ctx);
//            if (book2.fetchInfoResult() instanceof Result.Failure) {
//                ctx.log.d(this,"Get Book FAILURE --> " +
//                        "book id: " + book2.id + " >> " +
//                        ((Result.Failure<Domain.BookInfo>) book2.fetchInfoResult())
//                );
//            } else {
//                ctx.log.d(this,"Book ERxists --> " +
//                        ((Result.Success<Domain.BookInfo>) book2.fetchInfoResult()).value()
//                );
//            }
//
//            DumpBookDBandAPI(ctx);
//        }
//
//        Populate_the_library_and_user_DBs:
//        {
//            ////////////////////////////////////////
//            // Setup DB & API simulated resources //
//            ////////////////////////////////////////
//
//            // Create & populate a Library in the Library Repo
//            final Result<Domain.LibraryInfo> libraryInfo = createFakeLibraryInfoInContextLibraryRepo(1, ctx);
//            if (libraryInfo instanceof Result.Failure) {
//                ctx.log.d(this,"Create Library FAILURE --> " +
//                        ((Result.Failure<Domain.LibraryInfo>) libraryInfo)
//                );
//
//                break Populate_the_library_and_user_DBs;
//            }
//            UUID2<Library> libraryInfoId = ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().id;
//            ctx.log.d(this,"Library Created --> id: " +
//                ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().id +
//                ", name: "+
//                ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().name
//            );
//
//            // Populate the library
//            ctx.libraryRepo().populateWithFakeBooks(libraryInfoId, 10);
//
//            // Create & populate a User in the User Repo
//            final Domain.UserInfo userInfo = createFakeUserInfoInContextUserRepo(1, ctx);
//
//            //////////////////////////////////
//            // Actual App functionality     //
//            //////////////////////////////////
//
//            // Create the App objects
//            final User user1 = new User(userInfo.id(), ctx);
//            final Library library1 = new Library(libraryInfoId, ctx);
//            final Book book1 = new Book(UUID2.createFakeUUID2(1, Book.class.getName()), ctx);
//            final Book book2 = new Book(UUID2.createFakeUUID2(2, Book.class.getName()), ctx);
//
//            // print the user
//            ctx.log.d(this,"User --> " +
//                    user1.id + ", " +
//                    user1.fetchInfo().toPrettyJson()
//            );
//
//            Checkout_2_books_to_a_user:
//            if (false) {
//                ctx.log.d(this, "----------------------------------");
//                ctx.log.d(this,"Checking out 2 books to user " + user1.id);
//
//                final Result<Book> bookResult = library1.checkOutBookToUser(book1, user1);
//                if (bookResult instanceof Result.Failure) {
//                    ctx.log.d(this,"Checked out book FAILURE--> " +
//                            ((Result.Failure<Book>) bookResult).exception().getMessage()
//                    );
//                } else {
//                    ctx.log.d(this,"Checked out book SUCCESS --> " +
//                            ((Result.Success<Book>) bookResult).value().id
//                    );
//                }
//
//                final Result<Book> bookResult2 = library1.checkOutBookToUser(book2, user1);
//                if (bookResult2 instanceof Result.Failure) {
//                    ctx.log.d(this,"Checked out book FAILURE--> " +
//                            ((Result.Failure<Book>) bookResult2).exception().getMessage()
//                    );
//                } else {
//                    ctx.log.d(this,"Checked out book SUCCESS --> " +
//                            ((Result.Success<Book>) bookResult2).value().id
//                    );
//                }
//
//                // library1.DumpDB(ctx);  // LEAVE for debugging
//            }
//
//            Get_Available_Books_And_Counts_In_Library:
//            if (false) {
//                ctx.log.d(this, "----------------------------------");
//                ctx.log.d(this,"\nGetting available books and counts in library:");
//
//                final Result<HashMap<Book, Integer>> availableBookToNumAvailableResult =
//                        library1.calculateAvailableBookIdToNumberAvailableList();
//                if (availableBookToNumAvailableResult instanceof Result.Failure) {
//                    ctx.log.d(this,"AvailableBookIdCounts FAILURE! --> " +
//                            ((Result.Failure<HashMap<Book, Integer>>) availableBookToNumAvailableResult)
//                                    .exception().getMessage()
//                    );
//
//                    break Get_Available_Books_And_Counts_In_Library;
//                }
//
//                // create objects and populate info for available books
//                assert availableBookToNumAvailableResult instanceof Result.Success;
//                final HashMap<Book, Integer> availableBooks =
//                        ((Result.Success<HashMap<Book, Integer>>) availableBookToNumAvailableResult).value();
//
//                // Print out available books
//                ctx.log.d(this,"\nAvailable Books in Library:");
//                for (Map.Entry<Book, Integer> availableBook : availableBooks.entrySet()) {
//                    final Book book3 = new Book(availableBook.getKey().id, ctx);
//
//                    final Result<Domain.BookInfo> bookInfoResult = book3.fetchInfoResult();
//                    if (bookInfoResult instanceof Result.Failure) {
//                        ctx.log.d(this,
//                                "Book Error: " +
//                                        ((Result.Failure<Domain.BookInfo>) bookInfoResult)
//                                                .exception().getMessage()
//                        );
//                    } else {
//                        ctx.log.d(this,
//                                ((Result.Success<Domain.BookInfo>) bookInfoResult).value() +
//                                        " >> num available: " + availableBook.getValue()
//                        );
//                    }
//                }
//                ctx.log.d(this,"Total Available Books (unique UUIDs): " + availableBooks.size());
//                ctx.log.d(this,"\n");
//            }
//
//            Get_books_checked_out_by_user:
//            if (false) {
//                ctx.log.d(this, "----------------------------------");
//                ctx.log.d(this,"\nGetting books checked out by user " + user1.id);
//
//                final Result<ArrayList<Book>> checkedOutBooksResult = library1.findBooksCheckedOutByUser(user1);
//                if (checkedOutBooksResult instanceof Result.Failure) {
//                    ctx.log.d(this,"OH NO! --> " +
//                            ((Result.Failure<ArrayList<Book>>) checkedOutBooksResult)
//                                    .exception().getMessage()
//                    );
//                }
//
//                assert checkedOutBooksResult instanceof Result.Success;
//                ArrayList<Book> checkedOutBooks = ((Result.Success<ArrayList<Book>>) checkedOutBooksResult).value();
//
//                // Print checked out books
//                ctx.log.d(this,"\nChecked Out Books for User [" + user1.fetchInfo().name() + ", " + user1.id + "]:");
//                for (Book book : checkedOutBooks) {
//                    final Result<Domain.BookInfo> bookInfoResult = book.fetchInfoResult();
//                    if (bookInfoResult instanceof Result.Failure) {
//                        ctx.log.d(this,
//                                "Book Error: " +
//                                        ((Result.Failure<Domain.BookInfo>) bookInfoResult)
//                                                .exception().getMessage()
//                        );
//                    } else {
//                        ctx.log.d(this,
//                                ((Result.Success<Domain.BookInfo>) bookInfoResult).value().toString()
//                        );
//                    }
//                }
//                System.out.print("\n");
//            }
//
//            Check_In_the_Book_from_the_User_to_the_Library:
//            if (false) {
//                ctx.log.d(this, "----------------------------------");
//                ctx.log.d(this,"\nCheck in book " + book1.id + " from user " + user1.id);
//
//                final Result<Book> checkInBookResult = library1.checkInBookFromUser(book1, user1);
//                if (checkInBookResult instanceof Result.Failure) {
//                    ctx.log.d(this,"Check In book FAILURE --> book id:" +
//                            ((Result.Failure<Book>) checkInBookResult).exception().getMessage()
//                    );
//                } else {
//                    ctx.log.d(this,"Returned Book SUCCESS --> book id:" +
//                            ((Result.Success<Book>) checkInBookResult).value()
//                    );
//                }
//
//                library1.DumpDB(ctx);
//            }
//
//            // Load Library from Json
//            if (true) {
//                ctx.log.d(this, "----------------------------------");
//                ctx.log.d(this,"Load Library from Json: ");
//
//                // Library library2 = new Library(ctx); // uses random UUID, will cause expected error due to unknown UUID
//                Library library2 = new Library(UUID2.createFakeUUID2(99), ctx);
//                ctx.log.d(this, library2.toJson());
//
//                String json =
//                        "{\n" +
//                        "  \"name\": \"Ronald Reagan Library\",\n" +
//                        "  \"userIdToCheckedOutBookIdMap\": {\n" +
//                        "    \"00000000-0000-0000-0000-000000000001\": [\n" +
//                        "      {\n" +
//                        "        \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
//                        "        \"uuid2TypeStr\": \"org.elegantobjects.jpages.Model$Domain$BookInfo\"\n" +
//                        "      }\n" +
//                        "    ]\n" +
//                        "  },\n" +
//                        "  \"bookIdToNumBooksAvailableMap\": {\n" +
//                        "    \"00000000-0000-0000-0000-000000000010\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000011\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000012\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000013\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000014\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000015\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000016\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000017\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000018\": 50,\n" +
//                        "    \"00000000-0000-0000-0000-000000000019\": 50\n" +
//                        "  },\n" +
//                        "  \"id\": {\n" +
//                        "    \"uuid\": \"00000000-0000-0000-0000-000000000099\"\n" +
//                        "  }\n" +
//                        "}";
//                if(true) {
//                    Result<Domain.LibraryInfo> library2Result = library2.updateDomainInfoFromJson(json);
//                    if (library2Result instanceof Result.Failure) {
//                        ctx.log.d(this, ((Result.Failure<Domain.LibraryInfo>) library2Result).exception().getMessage());
//                    } else {
//                        ctx.log.d(this, "Results of Library2 json load:");
//                        ctx.log.d(this, library2.toJson());
//                    }
//                }
//
//                try {
//                    Domain.LibraryInfo libraryInfo3 =
//                        Library.createDomainInfoFromJson(
//                                json,
//                                Domain.LibraryInfo.class,
//                                ctx
//                        );
//
//                    Library library3 = new Library(libraryInfo3, ctx);
//                    if(libraryInfo3 == null) {
//                        ctx.log.d(this, "Library3 is null");
//                    } else {
//                        ctx.log.d(this,"Results of Library3 json load:");
//                        ctx.log.d(this,library3.toJson());
//                    }
//                } catch (Exception e) {
//                    ctx.log.d(this, "Exception: " + e.getMessage());
//                }
//            }
//
//            // Load Book from DTO Json
//            if(false) {
//                ctx.log.d(this, "----------------------------------");
//                ctx.log.d(this,"Load Book from DTO Json: ");
//
//                String json =
//                        "{\n" +
//                        "  \"id\": {\n" +
//                        "    \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
//                        "    \"uuid2TypeStr\": \"org.elegantobjects.jpages.Model$DTO$BookInfo\"\n" +
//                        "  },\n" +
//                        "  \"title\": \"The Great Gatsby\",\n" +
//                        "  \"author\": \"F. Scott Fitzgerald\",\n" +
//                        "  \"description\": \"The Great Gatsby is a 1925 novel written by American author F. Scott Fitzgerald that follows a cast of characters living in the fictional towns of West Egg and East Egg on prosperous Long Island in the summer of 1922. The story primarily concerns the young and mysterious millionaire Jay Gatsby and his quixotic passion and obsession with the beautiful former debutante Daisy Buchanan. Considered to be Fitzgerald's magnum opus, The Great Gatsby explores themes of decadence, idealism, resistance to change, social upheaval, and excess, creating a portrait of the Jazz Age or the Roaring Twenties that has been described as a cautionary tale regarding the American Dream.\",\n" +
//                        "  \"extraFieldToShowThisIsADTO\": \"Data from JSON load\"\n" +
//                        "}";
//
//                try {
//                    DTO.BookInfo bookInfo3 = new DTO.BookInfo(json, ctx);
//                    Book book3 = new Book(bookInfo3.toDeepCopyDomainInfo(), ctx);
//
//                    ctx.log.d(this,"Results of Load Book from DTO Json: " + book3.toJson());
//                } catch (Exception e) {
//                    ctx.log.d(this, "Exception: " + e.getMessage());
//                }
//            }
//
//            Check_out_Book_via_User:
//            if (false) {
//                final User user2 = new User(createFakeUserInfoInContextUserRepo(2, ctx), ctx);
//                final Result<Domain.BookInfo> book12Result = addFakeBookInfoInContextBookRepo(12, ctx);
//
//                if (book12Result instanceof Result.Failure) {
//                    ctx.log.d(this,"Book Error: " +
//                            ((Result.Failure<Domain.BookInfo>) book12Result).exception().getMessage()
//                    );
//                } else {
//
//                    final UUID2<Book> book12id = ((Result.Success<Domain.BookInfo>) book12Result).value().id();
//                    final Book book12 = new Book(book12id, ctx);
//
//                    ctx.log.d(this,"\nCheck out book " + book12id + " to user " + user1.id);
//
//                    final Result<Book> book12UpsertResult = library1.addTestBookToLibrary(book12, 1);
//                    if (book12UpsertResult instanceof Result.Failure) {
//                        ctx.log.d(this,"Upsert Book Error: " +
//                                ((Result.Failure<Book>) book12UpsertResult).exception().getMessage()
//                        );
//                    }
//
//                    final Result<UUID2<Book>> checkedOutBookResult = user2.checkoutBookFromLibrary(book12, library1);
//                    if (checkedOutBookResult instanceof Result.Failure) {
//                        ctx.log.d(this,"Checkout book FAILURE --> " +
//                                ((Result.Failure<UUID2<Book>>) checkedOutBookResult).exception().getMessage()
//                        );
//                    } else {
//                        ctx.log.d(this,"Checkout Book SUCCESS --> checkedOutBook:" +
//                                ((Result.Success<UUID2<Book>>) checkedOutBookResult).value()
//                        );
//                    }
//                }
//            }
//        }
//    }
//
//    //////////////////////////////////////////////////////////////////////
//    /////////////////////////// Helper Methods ///////////////////////////
//    //////////////////////////////////////////////////////////////////////
//
//    private void PopulateFakeBookInfoInContextBookRepoDBandAPI(Context context) {
//        context.bookRepo().populateDatabaseWithFakeBookInfo();
//        context.bookRepo().populateApiWithFakeBookInfo();
//    }
//
//    private void DumpBookDBandAPI(Context context) {
//        System.out.print("\n");
//        context.log.d(this,"DB Dump");
//        context.bookRepo().printDB();
//
//        System.out.print("\n");
//        context.log.d(this,"API Dump");
//        context.bookRepo().printAPI();
//
//        System.out.print("\n");
//    }
//
//    private Result<Domain.LibraryInfo> createFakeLibraryInfoInContextLibraryRepo(
//        final Integer id,
//        Context context
//    ) {
//        Integer someNumber = id;
//        if (someNumber == null) someNumber = 1;
//
//        return context.libraryRepo()
//                .upsertLibraryInfo(
//                        new Domain.LibraryInfo(
//                            UUID2.createFakeUUID2(someNumber, Domain.LibraryInfo.class.getName()),
//                            "Library " + someNumber
//                        )
//                );
//    }
//
//    private Domain.UserInfo createFakeUserInfoInContextUserRepo(
//        final Integer id,
//        Context context
//    ) {
//        Integer someNumber = id;
//        if (someNumber == null) someNumber = 1;
//
//        return context.userRepo()
//                .upsertUserInfo(
//                    new Domain.UserInfo(
//                        UUID2.createFakeUUID2(someNumber, Domain.UserInfo.class.getName()),
//                        "User " + someNumber,
//                        "user" + someNumber + "@gmail.com"
//                ));
//    }
//
//    private Result<Domain.BookInfo> addFakeBookInfoInContextBookRepo(
//        final Integer id,
//        Context context
//    ) {
//        final Domain.BookInfo bookInfo = createFakeBookInfo(null, id);
//
//        return context.bookRepo()
//                .upsertBookInfo(bookInfo);
//    }
//
//    private Domain.BookInfo createFakeBookInfo(String uuidStr, final Integer id) {
//        Integer fakeId = id;
//        if (fakeId == null) fakeId = 1;
//
//        UUID2<Book> uuid;
//        if (uuidStr == null)
//            uuid = UUID2.createFakeUUID2(fakeId, Book.class.getName());
//        else
//            uuid = UUID2.fromString(uuidStr);
//
//        return new Domain.BookInfo(
//                uuid,
//                "Book " + fakeId,
//                "Author " + fakeId,
//                "Description " + fakeId
//        );
//    }
//}