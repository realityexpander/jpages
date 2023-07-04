 package org.elegantobjects.jpages.App2.common.util.uuid2;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

import static java.lang.String.format;

/**
 UUID2 is a type-safe wrapper for UUIDs.<br>
 <ul>
 <li> {@code UUID2} is a wrapper for a UUID, so it can be used in place of a UUID.</li>
 <li> Benefits:<br>
    <ul>
        <li>Useful to enforce type-constrained UUIDs for Objects that expect specific types of UUIDs.</li>
        <li>Allows for easier debugging and exposing UUID types in Json payloads.</li>
        <li>Includes type-safe {@code HashMap} for {@code UUID2}s.</li>
        <li>{@code UUID2} is immutable.</li>
    </ul>
 <li> {@code IUUID2} is a marker interface for Domain objects that can be used with UUID2.</li>
 <li> Domain objects must be marked with the {@code IUUID2} marker interface to be used with {@code UUID2}.</li>
 <li> {@code UUID2Type} is the Class <b>Inheritance</b> path, <b>NOT</b> the Class Path (ie: Package path). <br>
      {@code Model.Domain.BookInfo} <-- "Class Inheritance path"<br>
      <i>instead of:</i> <br>
      {@code org.elegantobjects.jpages.App2.domain.book.BookInfo} <-- "Class Path"<br>
 </li>
 <li><i>Design note: The java "Class Path" changes if the package of a Class changes, so UUID2 uses the Class Inheritance Path instead.</i></li>
</ul>
**/
public class UUID2<TUUID2 extends IUUID2> implements IUUID2 {
    private final UUID uuid;
    private String _uuid2Type; // Class Inheritance Path of the object the UUID refers to. '.' separated.
                               // NOT final due to need for it to be set for creating objects via JSON deserialization. :( // todo - is there a way around this? Maybe reflection?

    public
    UUID2(TUUID2 uuid2, String uuid2TypeStr) {
        this.uuid = ((UUID2<?>) uuid2).uuid();

        if(uuid2TypeStr != null) {
            this._uuid2Type = getNormalizedUuid2TypeString(uuid2TypeStr);
        } else {
            this._uuid2Type = "UUID"; // Default to untyped UUID
        }
    }
    public
    UUID2(TUUID2 uuid2, Class<?> clazz) {
        this(uuid2, UUID2.calcUUID2TypeStr(clazz));
    }
    public
    UUID2(UUID uuid) {
        this.uuid = uuid;
        this._uuid2Type = "UUID";  // untyped UUID
    }
    public
    UUID2(TUUID2 uuid2) {
        this(uuid2, uuid2.uuid2TypeStr());
    }
    @SuppressWarnings("unchecked")
    public
    UUID2(@NotNull UUID2<?> uuid2) {
        this((TUUID2) uuid2, uuid2.uuid2TypeStr());
    }
    @SuppressWarnings("unchecked")
    public
    UUID2(UUID uuid, Class<?> clazz) {
        this((TUUID2) UUID2.fromUUID(uuid), clazz);
    }

    public static UUID2<?> fromUUID2String(@NotNull String uuid2FormattedString) throws IllegalArgumentException, ClassNotFoundException {
        // format example:
        // UUID2:Object.Role.User@00000000-0000-0000-0000-000000000001
        // ^-- Always prefixed with `UUID2`
        //      ^-- `:` divides between Prefix and Type
        //       ^-- UUID2Type
        //                       ^-- `@` divides the Type block and Value

        String[] segments = uuid2FormattedString.split("@");
        if(segments.length != 2) {
            throw new IllegalArgumentException("Invalid UUID2 formatted string, invalid number of segments: " + uuid2FormattedString);
        }

        String[] typeSegments = segments[0].split(":");
        if(!typeSegments[0].equals("UUID2")) {
            throw new IllegalArgumentException("Invalid UUID2 formatted string, no `UUID2` prefix: " + uuid2FormattedString);
        }

        String uuid2TypeStr = typeSegments[1];  // ie: Object.Role.User
        String uuidStr = segments[1];           // ie: 00000000-0000-0000-0000-000000000001

        return new UUID2<>(UUID2.fromUuidString(uuidStr), uuid2TypeStr);
    }

    ////////////////////////////////
    // Published Getters          //
    ////////////////////////////////

    public
    UUID uuid() { return uuid; }

    public @Override
    String uuid2TypeStr() {
        return _uuid2Type;
    }

    public @Override
    int hashCode() {
        return this.uuid().hashCode();
    }

    public
    boolean equals(@NotNull UUID2<TUUID2> other) {
        return (other).uuid().equals(uuid());
    }

    boolean isMatchingUUID2Type(@NotNull UUID2<?> checkUUID2) {
        return this.uuid2TypeStr().equals(checkUUID2.uuid2TypeStr());
    }

    static boolean isMatchingUUID2Type(String firstUuid2Str, String secondUuid2Str) {
        if(firstUuid2Str == null) return false;  // note: null checks are acceptable for static methods.
        if(secondUuid2Str == null) return false;

        try {
            UUID2<?> firstUUID2 = UUID2.fromUUID2String(firstUuid2Str);
            UUID2<?> secondUUID2 = UUID2.fromUUID2String(secondUuid2Str);

            return firstUUID2.isMatchingUUID2Type(secondUUID2);
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Unable to find class for UUID2: " + firstUuid2Str);
            e.printStackTrace();
            return false;
        }
    }

    ////////////////////////////////
    // Converters                 //
    ////////////////////////////////

    public static @NotNull
    UUID2<?> fromUUID2(UUID2<?> id, Class<?> clazz) {
        return new UUID2<>(id, clazz);
    }

    public static @NotNull
    UUID2<IUUID2> fromUUID(UUID uuid) {
        return new UUID2<>(uuid);
    }

    public
    UUID toUUID() {
        return new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public static @NotNull
    UUID2<IUUID2> fromUuidString(String uuidStr) {
        return new UUID2<>(UUID.fromString(uuidStr));
    }

    public @Override
    String toString() {
        return  "UUID2:" + getClassInheritancePathStr(_uuid2Type) + "@" + uuid;
    }

    public
    UUID2<IUUID2> toDomainUUID2() {
        return new UUID2<>(this, this.uuid2TypeStr());
    }

    public
    UUID2<?> toUUID2() {
        return this;
    }

    ////////////////////////////////
    // Generators                 //
    ////////////////////////////////

    public static <TDomainUUID2 extends IUUID2>
    UUID2<TDomainUUID2> randomUUID2() {
        return new UUID2<>(UUID.randomUUID());
    }

    public static <TDomainUUID2 extends IUUID2>
    UUID2<TDomainUUID2> randomUUID2(Class<TDomainUUID2> clazz) {
        return new UUID2<>(UUID.randomUUID(), clazz);
    }

    //////////////////////////////////////////////////
    // Methods for creating fake UUID's for testing //
    //////////////////////////////////////////////////

    public static <TDomainUUID2 extends IUUID2> @NotNull
    UUID2<TDomainUUID2> createFakeUUID2(final Integer id, Class<?> clazz) {
        return _createFakeUUID2(id, calcUUID2TypeStr(clazz));
    }
    public static <TDomainUUID2 extends IUUID2> @NotNull
    UUID2<TDomainUUID2> createFakeUUID2(final Integer id) {
        return createFakeUUID2(id, IUUID2.class);
    }
    @SuppressWarnings("unchecked")
    private static <TDomainUUID2 extends IUUID2> @NotNull
    UUID2<TDomainUUID2> _createFakeUUID2(final Integer id, String clazzPathStr) {
        Integer nonNullId = id;
        if (nonNullId == null) nonNullId = 0; // default value

        final String idPaddedWith11LeadingZeroes = format("%011d", nonNullId);
        final UUID2<IUUID2> uuid2 = fromUuidString("00000000-0000-0000-0000-" + idPaddedWith11LeadingZeroes);

        return new UUID2<>((TDomainUUID2) uuid2, clazzPathStr);
    }

    /////////////////
    // UUID2 Type  //
    /////////////////

    public static @NotNull
    String calcUUID2TypeStr(@NotNull Class<?> clazz) {

        // Get all names of superClasses for this clazz.
        // - Climbs the Class Inheritance hierarchy for the clazz
        // - ie: `Model.{Domain}.{Entity}Info`
        // - *NOT* the Class path (org.elegantobjects.jpages.App2.domain.{entity}.{entity}info`
        List<String> superClassNames = new ArrayList<>();
        Class<?> curClazz = clazz;
        while(!curClazz.getSimpleName().equals("Object")) {
            superClassNames.add(curClazz.toString());
            curClazz = curClazz.getSuperclass();
        }

        // Build Class Inheritance path from each Class name
        StringBuilder uuid2TypeStr = new StringBuilder();
        for(int i = superClassNames.size() - 1; i >= 0; i--) {
            uuid2TypeStr.append(
                    UUID2.getLastSegmentOfTypeStrPath(superClassNames.get(i))
            );
            if(i != 0) {
                uuid2TypeStr.append(".");
            }
        }

        return uuid2TypeStr.toString();
    }

    // Note: Should only be used when importing JSON
    @SuppressWarnings("UnusedReturnValue")
    public
    boolean _setUUID2TypeStr(String uuid2TypeStr) {
        this._uuid2Type = getNormalizedUuid2TypeString(uuid2TypeStr);
        return true; // always return `true` instead of a `void` return type
    }

    ////////////////////
    // UUID2 HashMap  //
    ////////////////////

    /**
     Utility {@code HashMap} class for mapping {@code UUID2<TUUID2>} to {@code TEntity} Objects.<br>
     <br>
     This class is a wrapper for {@code java.util.HashMap} where the {@code key} hash value used is
     the hash of the {@code UUID} value s of  {@code UUID2<TUUID2>}'s embedded {@code UUID} object.
     <ul>
      <li>
        <b>Problem:</b> The {@code java.util.HashMap} class uses the {@code hashCode()} of the {@code UUID2<TUUID2>}
          object itself, which is <b><i>not</i></b> consistent between {@code UUID2<TUUID2>} objects
          with the {@code UUID} same value.
     </li>
     <li>
        <b>Solution:</b> {@code UUID2.HashMap} uses the {@code hashCode()} from the embedded {@code UUID} object and:
        <ol>
          <li>The {@code hashCode()} is calculated from the {@code UUID}.</li>
          <li>The {@code UUID hashCode()} is consistent between {@code UUID2<TUUID2>} objects
              with the same UUID value.</li>
        </ol>
     </li>
     </ul>
     * @param  <TUUID2>  the type of the class that implements the IUUID2 interface, ie: {@code Book} or {@code Account}
     * @param  <TEntity> the type of the object to be stored.
    **/
    public static class HashMap<TUUID2 extends UUID2<?>, TEntity> {
        private static final long serialVersionUID = 0x2743L;

        // We have use 2 HashMaps because the .hashCode() of UUID2<T> is not consistent between UUID2<T> objects.
        // - The hash of UUID2<T> objects includes the "type" of the UUID2<T> object, which is not consistent between
        //   UUID2<T> objects.
        // - The .hashCode() of UUID's is consistent between UUID objects of the same value.
        private final java.util.HashMap<TUUID2, TEntity> uuid2ToEntityMap = new java.util.HashMap<>(); // keeps the mapping of UUID2<T> to TEntity
        transient private final java.util.HashMap<UUID, TEntity> _uuidToEntityMap = new java.util.HashMap<>();

        public
        HashMap() {}

        // Creates a copy of another UUID2.HashMap
        public
        HashMap(UUID2.HashMap<TUUID2, TEntity> sourceDatabase) {
            this.putAll(sourceDatabase);
        }
        public
        HashMap(java.util.HashMap<TUUID2, TEntity> sourceDatabase) {

            // Copy the sourceDatabase into this HashMap
            for (Map.Entry<TUUID2, TEntity> entry : sourceDatabase.entrySet()) {
                TUUID2 uuid2 = entry.getKey();
                TEntity entity = entry.getValue();
                this.put(uuid2, entity);
            }
        }

        @Override
        public
        String toString() {
            return uuid2ToEntityMap.toString();
        }

        public
        TEntity get(@NotNull TUUID2 uuid2) {
            return _uuidToEntityMap.get(uuid2.uuid());
        }

        @SuppressWarnings("unchecked")
        public
        TEntity put(@NotNull UUID2<?> uuid2, TEntity value) {
            removeEntryByUUID(uuid2.uuid());

            uuid2ToEntityMap.put((TUUID2) uuid2, value);
            return _uuidToEntityMap.put(uuid2.uuid(), value);
        }

        private
        void removeEntryByUUID(UUID uuid) {
            // This is to prevent duplicate UUID2 entries that have the same internal UUID value.
            //   This is due to the hashCode() of the UUID2 is the entire object, not the internal UUID's hashCode.
            // So even though the UUID2 internal UUID is the same UUID value, the java Hash implementation doesn't use that,
            //   and we must work around it by doing a linear search here for each removal.
            // Maybe there is a way around this, but i've already tried overloading the hashCode() method for the
            //   UUID2, but it seems to be ignored(?).
            // Maybe there is an alternate Map collection I could use?

            // iterate thru uuid2ToEntityMap and remove element that matches UUID
            for (Map.Entry<TUUID2, TEntity> entry : uuid2ToEntityMap.entrySet()) {
                TUUID2 uuid2 = entry.getKey();
                TEntity entity = entry.getValue();

                if (uuid2.uuid().equals(uuid)) {
                    uuid2ToEntityMap.remove(uuid2);
                    _uuidToEntityMap.remove(uuid);
                    break;
                }
            }
        }

        public
        ArrayList<TEntity> putAll(@NotNull UUID2.HashMap<TUUID2, TEntity> sourceDatabase) {
            ArrayList<TEntity> entities = new ArrayList<>();

            for (Map.Entry<TUUID2, TEntity> entry : sourceDatabase.uuid2ToEntityMap.entrySet()) {
                TUUID2 uuid2 = entry.getKey();
                TEntity entity = entry.getValue();

                this.uuid2ToEntityMap.put(uuid2, entity);
                this._uuidToEntityMap.put(uuid2.uuid(), entity);

                entities.add(entity);
            }


            return entities;
        }

        public
        TEntity remove(@NotNull TUUID2 uuid2) {
            uuid2ToEntityMap.remove(uuid2);
            return _uuidToEntityMap.remove(uuid2.uuid());
        }

        public
        boolean containsKey(@NotNull TUUID2 uuid2) {
            return _uuidToEntityMap.containsKey(uuid2.uuid());
        }

        public
        boolean containsValue(TEntity entity) {
            return _uuidToEntityMap.containsValue(entity);
        }

        public
        Set<TUUID2> keySet() throws RuntimeException {
            Set<TUUID2> uuid2Set = new HashSet<>();

            try {
                for (TUUID2 uuid2 : uuid2ToEntityMap.keySet()) {
                    //noinspection UseBulkOperation
                    uuid2Set.add(uuid2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.keySet(): Failed to convert UUID to UUID2<TDomainUUID>, uuid2ToEntityMap: " + uuid2ToEntityMap.keySet());
            }

            return uuid2Set;
        }

        public
        Set<Map.Entry<TUUID2, TEntity>> entrySet() throws RuntimeException {
            Set<Map.Entry<TUUID2, TEntity>> uuid2Set = new HashSet<>();

            try {
                for (Map.Entry<TUUID2, TEntity> entry : uuid2ToEntityMap.entrySet()) {
                    TUUID2 uuid2 = entry.getKey();
                    UUID uuid = uuid2.uuid();

                    TEntity entity = _uuidToEntityMap.get(uuid2.uuid());

                    uuid2Set.add(new AbstractMap.SimpleEntry<>(uuid2, entity));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.entrySet(): Failed to convert UUID to UUID2<TDomainUUID>, uuid2ToEntityMap: " + uuid2ToEntityMap.keySet());
            }

            return uuid2Set;
        }

        public
        ArrayList<TEntity> values() throws RuntimeException {
            ArrayList<TEntity> entityValues = new ArrayList<>();

            try {
                for (Map.Entry<UUID, TEntity> entry : _uuidToEntityMap.entrySet()) {
                    TEntity entity = entry.getValue();

                    entityValues.add(entity);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.values(): Failed to convert UUID to UUID2<TDomainUUID>, uuid2ToEntityMap: " + uuid2ToEntityMap.keySet());
            }

            return entityValues;
        }
    }

    ////////////////////////////
    ///// Private helpers //////
    ////////////////////////////

    private @NotNull
    String getNormalizedUuid2TypeString(String uuid2TypeStr) {
        if(uuid2TypeStr == null) {
            return "UUID"; // unspecified-type
        }

        // Change any '$' in path of `uuid2TypeStr` into a '.'
        // - For some(?) reason Java returns delimiter `$` with: Model.Domain.BookInfo.class.getName();
        //   And returns returns `.` with: this.getClass().getName();
        StringBuilder normalizedTypeStr = new StringBuilder();
        for(int i = 0; i < uuid2TypeStr.length(); i++) {
            if(uuid2TypeStr.charAt(i) == '$') {
                normalizedTypeStr.append('.');
            } else {
                normalizedTypeStr.append(uuid2TypeStr.charAt(i));
            }
        }

        return normalizedTypeStr.toString();
    }
    private static
    String getClassInheritancePathStr(@NotNull String uuid2TypeStr) {
        String[] segments = uuid2TypeStr.split("\\.");
        if(segments.length <= 1) {
            return uuid2TypeStr;
        }

        // Make a string of up to 4 last segments (stopping at null) of the uuid2TypeStr with a period between them
        int numSegments = segments.length;
        StringBuilder pathBuilder = new StringBuilder();
        int maxSegments = Math.min(numSegments, 4);
        for(int i = 0; i < maxSegments; i++) {
            pathBuilder.append(segments[numSegments - maxSegments + i]);
            if(i < maxSegments - 1) {
                pathBuilder.append(".");
            }
        }

        return pathBuilder.toString();
    }

    private static
    String getLastSegmentOfTypeStrPath(@NotNull String classPath) {
        String[] segments = classPath.split("\\.");
        if(segments.length == 1) {
            return classPath;
        }

        return segments[segments.length - 1];
    }

    public static class Uuid2HashMapJsonDeserializer implements JsonDeserializer<UUID2.HashMap<?,?>> {
        // Note: Deserializes all JSON Numbers to Longs for all UUID2.HashMap Entity Number values.
        // - for consistent number deserialization bc GSON defaults to Doubles.

        @Override
        public UUID2.HashMap<?,?> deserialize(
                @NotNull
                JsonElement jsonElement,
                Type type,
                JsonDeserializationContext jsonDeserializationContext
        ) throws JsonParseException {
            UUID2.HashMap<?, ?> uuid2HashMapFromJson = new Gson().fromJson(jsonElement.getAsJsonObject(), UUID2.HashMap.class);

            HashMap<? extends UUID2<?>, Object> uuid2ToUuidMap = new HashMap<>();
            try {
                JsonObject uuid2ToUuidMapJsonObj = null;
                if (jsonElement.getAsJsonObject().get("uuid2ToEntityMap") != null) {
                    uuid2ToUuidMapJsonObj = jsonElement.getAsJsonObject()
                        .get("uuid2ToEntityMap")
                        .getAsJsonObject();
                }

                // Rebuild the UUID2<?> to Entity map
                for (Map.Entry<?, ?> entry : uuid2HashMapFromJson.uuid2ToEntityMap.entrySet()) {
                    UUID2<?> uuid2Key = UUID2.fromUUID2String(entry.getKey().toString());
                    Object entity = entry.getValue();

                    if (entity == null) {
                        throw new RuntimeException("Uuid2HashMapGsonDeserializer.deserialize(): entity is null, uuid2Key=" + uuid2Key);
                    }

                    // Convert any Numbers to Longs
                    if (entity instanceof Number) {
                        entity = ((Number) entity).longValue();

                    }

                    uuid2ToUuidMap.put(uuid2Key, entity);
                }

            } catch (IllegalArgumentException | ClassNotFoundException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException(e);
            }

            return uuid2ToUuidMap;
        }
    }
}