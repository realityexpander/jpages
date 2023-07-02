 package org.elegantobjects.jpages.App2.common.util.uuid2;

import org.jetbrains.annotations.Contract;
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
    private String uuid2TypeStr; // usually just the full class name of the Domain object // not final due to JSON deserialization needs to set this

    public UUID2(TUUID2 uuid2, String uuid2TypeStr) {
        this.uuid = ((UUID2<?>) uuid2).uuid();

        if(uuid2TypeStr != null) {
            this.uuid2TypeStr = getNormalizedUuid2TypeString(uuid2TypeStr);
        } else {
            this.uuid2TypeStr = "UUID"; // Default to untyped UUID
        }
    }
    public UUID2(TUUID2 uuid2, Class<?> clazz) {
        this(uuid2, UUID2.getUUID2TypeStr(clazz));
    }
    public UUID2(UUID uuid) {
        this.uuid = uuid;
        this.uuid2TypeStr = "UUID";  // untyped UUID
    }
    public UUID2(TUUID2 uuid2) {
        this(uuid2, uuid2.getUUID2TypeStr());
    }
    @SuppressWarnings("unchecked")
    public UUID2(UUID2<?> uuid2) {
        this((TUUID2) uuid2, uuid2.getUUID2TypeStr());
    }
    @SuppressWarnings("unchecked")
    public UUID2(UUID uuid, Class<?> clazz) {
        this((TUUID2) UUID2.fromUUID(uuid), clazz);
    }

    // simple getter
    public UUID uuid() {return uuid;}

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

    @Override
    public String toString() {
        return  "<" + getLast3SegmentsOfTypeStrPath(uuid2TypeStr) + ">" + uuid;
    }

    public UUID2<IUUID2> toDomainUUID2() {
        return new UUID2<>(this, this.getUUID2TypeStr());
    }

    public UUID2<?> toUUID2() {
        return this;
    }

    public static <TDomainUUID2 extends IUUID2>
    UUID2<TDomainUUID2> randomUUID2() {
        return new UUID2<>(UUID.randomUUID());
    }
    public static <TDomainUUID2 extends IUUID2>
    UUID2<TDomainUUID2> randomUUID2(Class<TDomainUUID2> clazz) {
        return new UUID2<>(UUID.randomUUID(), clazz);
    }

    public static @NotNull
    String getUUID2TypeStr(@NotNull Class<?> clazz) {

        // Climbs the class hierarchy for the clazz, ie: `Model.{Domain}.{Entity}Info`
        String modelClassPathStr = clazz.getSuperclass().getSuperclass().toString();
        String domainClassPathStr = clazz.getSuperclass().toString();
        String entityClassPathStr = clazz.getName();

        String model = UUID2.getLastSegmentOfTypeStrPath(modelClassPathStr);
        String domain = UUID2.getLastSegmentOfTypeStrPath(domainClassPathStr);
        String entity = UUID2.getLastSegmentOfTypeStrPath(entityClassPathStr);

        return model + "." + domain + "." + entity;
    }

    @Override
    public String getUUID2TypeStr() {
        return uuid2TypeStr;
    }

    // Note: Should only be used when importing JSON
    public void _setUUID2TypeStr(String uuid2TypeStr) {
        this.uuid2TypeStr = getNormalizedUuid2TypeString(uuid2TypeStr);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
    public boolean equals(@NotNull UUID2<TUUID2> other) {
        return (other).uuid.equals(uuid);
    }

    //////////////////////////////////////////////////
    // Methods for creating fake UUID's for testing //
    //////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private static <TDomainUUID2 extends IUUID2> @NotNull
    UUID2<TDomainUUID2> createFakeUUID2(final Integer id, String clazzPathStr) {
        Integer nonNullId = id;
        if (nonNullId == null) nonNullId = 0; // default value

        final String idPaddedWith11LeadingZeroes = format("%011d", nonNullId);
        final UUID2<IUUID2> uuid2 = fromString("00000000-0000-0000-0000-" + idPaddedWith11LeadingZeroes);

        return new UUID2<>((TDomainUUID2) uuid2, clazzPathStr);
    }
    public static <TDomainUUID2 extends IUUID2> @NotNull
    UUID2<TDomainUUID2> createFakeUUID2(final Integer id, Class<?> clazz) {
        return createFakeUUID2(id, getUUID2TypeStr(clazz));
    }
    public static <TDomainUUID2 extends IUUID2> @NotNull
    UUID2<TDomainUUID2> createFakeUUID2(final Integer id) {
        return createFakeUUID2(id, IUUID2.class);
    }


    // Utility HashMap class for mapping UUID2<T> to Objects.
    // Wrapper for HashMap where the `key` hash is the common UUID value stored in UUID class.
    //  - The problem is that normal HashMap uses `hashCode()` of UUID<{type}> object itself, which is not
    //    consistent between UUID2<{type}> objects.
    //  - This class uses UUID2<T> for the keys, but the hashCode() used is from the common UUID value
    //    stored in UUID2 class.
    public static class HashMap<TUUID2 extends UUID2<?>, TEntity> {
        private static final long serialVersionUID = 0x2743L;

        // We have use 2 HashMaps because the .hashCode() of UUID2<T> is not consistent between UUID2<T> objects.
        // - The hash of UUID2<T> objects includes the "type" of the UUID2<T> object, which is not consistent between
        //   UUID2<T> objects.
        // - The .hashCode() of UUID's is consistent between UUID objects of the same value.
        private final java.util.HashMap<TUUID2, UUID> uuid2ToUuidMap = new java.util.HashMap<>();
        private final java.util.HashMap<Integer, TEntity> hashCodeToEntityMap = new java.util.HashMap<>();

        public HashMap() {}

        // Creates a database from another database
        public HashMap(UUID2.HashMap<TUUID2, TEntity> sourceDatabase) {
            this.putAll(sourceDatabase);
        }

        public TEntity get(@NotNull UUID2<?> uuid2) {
            return hashCodeToEntityMap.get(uuid2.uuid().hashCode());
        }

        public TEntity put(@NotNull TUUID2 uuid2, TEntity value) {
            uuid2ToUuidMap.put(uuid2, uuid2.uuid());
            return hashCodeToEntityMap.put(uuid2.uuid().hashCode(), value);
        }

        public ArrayList<TEntity> putAll(@NotNull HashMap<TUUID2, TEntity> sourceDatabase) {
            ArrayList<TEntity> entities = new ArrayList<>();

            for (Map.Entry<TUUID2, UUID> entry : sourceDatabase.uuid2ToUuidMap.entrySet()) {
                TUUID2 uuid2 = entry.getKey();
                UUID uuid = entry.getValue();

                TEntity entity = sourceDatabase.hashCodeToEntityMap.get(uuid.hashCode());

                uuid2ToUuidMap.put(uuid2, uuid);
                hashCodeToEntityMap.put(uuid.hashCode(), entity);

                entities.add(entity);
            }

            return entities;
        }

        public TEntity remove(@NotNull TUUID2 uuid2) {
            uuid2ToUuidMap.remove(uuid2);
            return hashCodeToEntityMap.remove(uuid2.uuid().hashCode());
        }

        public boolean containsKey(TUUID2 uuid2) {
            UUID uuid = uuid2ToUuidMap.get(uuid2);

            return uuid != null && hashCodeToEntityMap.containsKey(uuid.hashCode());
        }

        public boolean containsValue(TEntity entity) {
            return hashCodeToEntityMap.containsValue(entity);
        }

        public Set<TUUID2> keySet() throws RuntimeException {
            Set<TUUID2> uuid2Set = new HashSet<>();

            try {
                for (TUUID2 uuid2 : uuid2ToUuidMap.keySet()) {
                    //noinspection UseBulkOperation
                    uuid2Set.add(uuid2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.keys(): Failed to convert UUID to UUID2<TDomainUUID>, uuidSet: " + hashCodeToEntityMap.keySet());
            }

            return uuid2Set;
        }

        public Set<Map.Entry<TUUID2, TEntity>> entrySet() throws RuntimeException {
            Set<Map.Entry<TUUID2, TEntity>> uuid2Set = new HashSet<>();

            try {
                for (Map.Entry<TUUID2, UUID> entry : uuid2ToUuidMap.entrySet()) {
                    TUUID2 uuid2 = entry.getKey();
                    UUID uuid = entry.getValue();

                    TEntity entity = hashCodeToEntityMap.get(uuid.hashCode());

                    uuid2Set.add(new AbstractMap.SimpleEntry<>(uuid2, entity));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.keys(): Failed to convert UUID to UUID2<TDomainUUID>, uuidSet: " + hashCodeToEntityMap.keySet());
            }

            return uuid2Set;
        }

        public Set<TEntity> values() throws RuntimeException {
            Set<TEntity> entitySet = new HashSet<>();

            try {
                for (Integer hashCode : hashCodeToEntityMap.keySet()) {
                    TEntity entity = hashCodeToEntityMap.get(hashCode);

                    entitySet.add(entity);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.keys(): Failed to convert UUID to UUID2<TDomainUUID>, uuidSet: " + hashCodeToEntityMap.keySet());
            }

            return entitySet;
        }
    }


    ////////////////////////////
    ///// Private helpers //////
    ////////////////////////////

    private String getNormalizedUuid2TypeString(String uuid2TypeStr) {
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
    private String getLast3SegmentsOfTypeStrPath(String uuid2TypeStr) {
        String[] segments = uuid2TypeStr.split("\\.");
        if(segments.length < 3) {
            return uuid2TypeStr;
        }

        return segments[segments.length - 3] + "." +
                segments[segments.length - 2] + "." +
                segments[segments.length - 1];
    }

    private static String getLastSegmentOfTypeStrPath(String classPath) {
        String[] segments = classPath.split("\\.");
        if(segments.length == 1) {
            return classPath;
        }

        return segments[segments.length - 1];
    }
}