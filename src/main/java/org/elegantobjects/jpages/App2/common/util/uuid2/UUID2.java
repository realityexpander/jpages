 package org.elegantobjects.jpages.App2.common.util.uuid2;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.String.format;

// UUID2 is a type-safe wrapper for UUIDs.
// - Used to enforce type-specific UUIDs for Objects that expect a specific type of UUID.
// - Domain objects must be marked with the IUUID2 interface to be used with UUID2.
// - UUID2 is immutable.
// - UUID2 is a wrapper for UUID, so it can be used in place of UUID.
// - IUUID2 is a marker interface for Domain objects that can be used with UUID2.
public class UUID2<TUUID2 extends IUUID2> implements IUUID2 {
    private final UUID uuid;
    private String _uuid2Type; // usually just the last 3 path segments of class name of the Domain object
                               // NOT final due to JSON deserialization needs to set it. :(

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

    ////////////////////////////////
    // Published Getters          //
    ////////////////////////////////

    public
    UUID uuid() {return uuid;}

    public @Override
    String uuid2TypeStr() {
        return _uuid2Type;
    }

    public @Override
    int hashCode() {
        return uuid.hashCode();
    }

    public
    boolean equals(@NotNull UUID2<TUUID2> other) {
        return (other).uuid.equals(uuid);
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
    UUID2<IUUID2> fromString(String uuidStr) {
        return new UUID2<>(UUID.fromString(uuidStr));
    }

    public @Override
    String toString() {
        return  "<" + getLast3SegmentsOfTypeStrPath(_uuid2Type) + ">" + uuid;
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

    public static @NotNull
    String calcUUID2TypeStr(@NotNull Class<?> clazz) {

        // Climbs the class hierarchy for the clazz, ie: `Model.{Domain}.{Entity}Info`
        String modelClassPathStr = clazz.getSuperclass().getSuperclass().toString();
        String domainClassPathStr = clazz.getSuperclass().toString();
        String entityClassPathStr = clazz.getName();

        String model = UUID2.getLastSegmentOfTypeStrPath(modelClassPathStr);
        String domain = UUID2.getLastSegmentOfTypeStrPath(domainClassPathStr);
        String entity = UUID2.getLastSegmentOfTypeStrPath(entityClassPathStr);

        return model + "." + domain + "." + entity;
    }

    // Note: Should only be used when importing JSON
    @SuppressWarnings("UnusedReturnValue")
    public
    boolean _setUUID2TypeStr(String uuid2TypeStr) {
        this._uuid2Type = getNormalizedUuid2TypeString(uuid2TypeStr);
        return true; // always return `true` instead of a `void` return type
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
        final UUID2<IUUID2> uuid2 = fromString("00000000-0000-0000-0000-" + idPaddedWith11LeadingZeroes);

        return new UUID2<>((TDomainUUID2) uuid2, clazzPathStr);
    }


    /**
     Utility HashMap class for mapping UUID2<T> to Objects.<br>
     <br>
     Wrapper for HashMap where the `key` hash is the common UUID value stored in UUID class.
     <ol>
      <li>
        The problem is that java `HashMap` uses `hashCode()` of `UUID2<T>` object itself, which is _not_
        consistent between `UUID2<T>` objects of the same value.
      </li>
      - This class uses `UUID2<T>` for the keys, but the `hashCode()` used is from the common `UUID` value
        stored in `UUID2` class.
     </ol>
    **/
    public static class HashMap<TUUID2 extends UUID2<?>, TEntity> {
        private static final long serialVersionUID = 0x2743L;

        // We have use 2 HashMaps because the .hashCode() of UUID2<T> is not consistent between UUID2<T> objects.
        // - The hash of UUID2<T> objects includes the "type" of the UUID2<T> object, which is not consistent between
        //   UUID2<T> objects.
        // - The .hashCode() of UUID's is consistent between UUID objects of the same value.
        private final java.util.HashMap<TUUID2, TEntity> uuid2ToEntityMap = new java.util.HashMap<>(); // keeps the mapping of UUID2<T> to TEntity
        transient private final java.util.HashMap<UUID, TEntity> _uuidToEntityMap = new java.util.HashMap<>();

        public HashMap() {}

        // Creates a copy of another UUID2.HashMap
        public HashMap(UUID2.HashMap<TUUID2, TEntity> sourceDatabase) {
            this.putAll(sourceDatabase);
        }
        public HashMap(java.util.HashMap<TUUID2, TEntity> sourceDatabase) {

            // Copy the sourceDatabase into this HashMap
            for (Map.Entry<TUUID2, TEntity> entry : sourceDatabase.entrySet()) {
                TUUID2 uuid2 = entry.getKey();
                TEntity entity = entry.getValue();
                this.put(uuid2, entity);
            }
        }

        @Override
        public String toString() {
            return uuid2ToEntityMap.toString();
        }

        public TEntity get(@NotNull TUUID2 uuid2) {
            return _uuidToEntityMap.get(uuid2.uuid());
        }

        public TEntity put(@NotNull TUUID2 uuid2, TEntity value) {
            uuid2ToEntityMap.put(uuid2, value);
            return _uuidToEntityMap.put(uuid2.uuid(), value);
        }

        public ArrayList<TEntity> putAll(@NotNull UUID2.HashMap<TUUID2, TEntity> sourceDatabase) {
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

        public TEntity remove(@NotNull TUUID2 uuid2) {
            uuid2ToEntityMap.remove(uuid2);
            return _uuidToEntityMap.remove(uuid2.uuid());
        }

        public boolean containsKey(@NotNull TUUID2 uuid2) {
            return _uuidToEntityMap.containsKey(uuid2.uuid());
        }

        public boolean containsValue(TEntity entity) {
            return _uuidToEntityMap.containsValue(entity);
        }

        public Set<TUUID2> keySet() throws RuntimeException {
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

        public Set<Map.Entry<TUUID2, TEntity>> entrySet() throws RuntimeException {
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

        public ArrayList<TEntity> values() throws RuntimeException {
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
    private
    String getLast3SegmentsOfTypeStrPath(@NotNull String uuid2TypeStr) {
        String[] segments = uuid2TypeStr.split("\\.");
        if(segments.length < 3) {
            return uuid2TypeStr;
        }

        return segments[segments.length - 3] + "." +
                segments[segments.length - 2] + "." +
                segments[segments.length - 1];
    }

    private static
    String getLastSegmentOfTypeStrPath(@NotNull String classPath) {
        String[] segments = classPath.split("\\.");
        if(segments.length == 1) {
            return classPath;
        }

        return segments[segments.length - 1];
    }
}