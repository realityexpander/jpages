 package org.elegantobjects.jpages.App2.common.util.uuid2;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;

// UUID2 is a type-safe wrapper for UUIDs.
// - Used to enforce type-specific UUIDs for Objects that expect a specific type of UUID.
// - Domain objects must be marked with the IUUID2 interface to be used with UUID2.
// - UUID2 is immutable.
// - UUID2 is a wrapper for UUID, so it can be used in place of UUID.
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

    public static UUID2<?> fromUUID2(UUID2<?> id, Class<?> clazz) {
        return new UUID2<>(id, clazz);
    }

    @Override
    public String toString() {
        return  "<" + getLast3SegmentsOfTypeStrPath(uuid2TypeStr) + ">" + uuid;
    }

    // simple getter
    public UUID uuid() {return uuid;}

    public static <TDomainUUID2 extends IUUID2>
    UUID2<TDomainUUID2> fromUUID(UUID uuid) {
        return new UUID2<>(uuid);
    }

    public static <TDomainUUID2 extends IUUID2>
    UUID2<TDomainUUID2> fromString(String uuidStr) {
        return new UUID2<>(UUID.fromString(uuidStr));
    }

    // return a copy of the UUID
    public UUID toUUID() {
        return new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
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

    public static String getUUID2TypeStr(Class<?> clazz) {

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
    public boolean equals(UUID2<TUUID2> other) {
        return (other).uuid.equals(uuid);
    }

    //////////////////////////////////////////////////
    // Methods for creating fake UUID's for testing //
    //////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private static <TDomainUUID2 extends IUUID2>
    @NotNull UUID2<TDomainUUID2> createFakeUUID2(final Integer id, String clazzPathStr) {
        Integer nonNullId = id;
        if (nonNullId == null) nonNullId = 0; // default value

        final String idPaddedWith11LeadingZeroes = format("%011d", nonNullId);
        final UUID2<TDomainUUID2> uuid2 = fromString("00000000-0000-0000-0000-" + idPaddedWith11LeadingZeroes);

        return new UUID2<>((TDomainUUID2) uuid2, clazzPathStr);
    }
    public static <TDomainUUID2 extends IUUID2>
    @NotNull UUID2<TDomainUUID2> createFakeUUID2(final Integer id, Class<?> clazz) {
        return createFakeUUID2(id, getUUID2TypeStr(clazz));
    }
    public static <TDomainUUID2 extends IUUID2>
    @NotNull UUID2<TDomainUUID2> createFakeUUID2(final Integer id) {
        return createFakeUUID2(id, IUUID2.class);
    }

    // Utility HashMap class for mapping UUID2<T> to Objects.
    // Wrapper for HashMap where the `key` hash is the common UUID value stored in UUID class.
    // The problem is that normal HashMap uses `hash()` of UUID<{type}> object itself, which is not
    // consistent between UUID2<{type}> objects.
    // This class uses UUID2<T> for the keys, but the hash is the common UUID value stored in UUID class.
    // todo allow UUID2<?> as the key
    public static class HashMap<TUUID2 extends IUUID2, TEntity> extends java.util.HashMap<UUID, TEntity> {
        private static final long serialVersionUID = 0x7723L;
//        transient private Class<?> keyClazz;

//        public HashMap(Class<?> keyClazz) {
        public HashMap() {
            super();
//            this.keyClazz = keyClazz;
        }

        // Creates a database from another database
        public <TKey extends TUUID2, TValue extends TEntity>
//            HashMap(UUID2.HashMap<UUID2<TKey>, TValue> sourceDatabase, Class<TUUID2> keyClazz) {
            HashMap(UUID2.HashMap<UUID2<TKey>, TValue> sourceDatabase) {
            super();
//            this.keyClazz = keyClazz;
            this.putAll(sourceDatabase);
        }

        public TEntity get(String uuidStr) {
            return get(UUID.fromString(uuidStr));
        }
        public TEntity get(@NotNull UUID2<TUUID2> uuid2) {
            return get(uuid2.uuid);
        }
        public TEntity get(UUID uuid) { // allow UUIDs to be used as keys, but not recommended.
            return super.get(uuid);
        }

        public TEntity put(String uuidStr, TEntity value) {
            return put(UUID2.fromString(uuidStr), value);
        }
        public TEntity put(@NotNull UUID2<TUUID2> uuid2, TEntity value) {
            return put(uuid2.uuid, value);
        }
        public TEntity put(UUID uuid, TEntity value) { // allow UUIDs to be used as keys, but not recommended.
            return super.put(uuid, value);
        }

        public TEntity remove(String uuidStr) {
            return remove(UUID.fromString(uuidStr));
        }
        public TEntity remove(@NotNull UUID2<TUUID2> uuid2) {
            return remove(uuid2.uuid);
        }
        public TEntity remove(UUID uuid) { // allow UUIDs to be used as keys, but not recommended.
            return super.remove(uuid);
        }

        public boolean containsKey(String uuid2Str) {
            return containsKey(UUID.fromString(uuid2Str));
        }
        public boolean containsKey(UUID2<TUUID2> uuid2) { return super.containsKey(uuid2.uuid); }

        public <TKey extends TUUID2>
        Set<UUID2<TKey>> keys() throws RuntimeException {
            Set<UUID> uuidSet = super.keySet();
            Set<UUID2<TKey>> uuid2Set = new HashSet<>();

            // Convert UUIDs to TDomainUUIDs
            try {
                for (UUID uuid : uuidSet) {
                    @SuppressWarnings({"unchecked"})
//                    UUID2<TUUID2> uuid2 = (UUID2<TUUID2>) UUID2.fromUUID(uuid);
                    UUID2<TKey> uuid2 = UUID2.fromUUID(uuid);
//                    UUID2<TKey> uuid2 = (UUID2<TKey>) UUID2.fromUUID(uuid).toUUID2();
//                    uuid2._setUUID2TypeStr(UUID2.getUUID2TypeStr(keyClazz));

                    uuid2Set.add(uuid2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.keys(): Failed to convert UUID to UUID2<TDomainUUID>, uuidSet: " + uuidSet);
            }

            return uuid2Set;
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