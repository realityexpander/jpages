package org.elegantobjects.jpages.App2;

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

    UUID2(TUUID2 uuid2, String uuid2TypeStr) {
        this.uuid = ((UUID2<?>) uuid2).uuid();

        if(uuid2TypeStr != null) {
            this.uuid2TypeStr = uuid2TypeStr;
        } else {
            this.uuid2TypeStr = "UUID"; // Default to untyped UUID
        }
    }
    UUID2(UUID uuid) {
        this.uuid = uuid;
        this.uuid2TypeStr = "UUID";  // untyped UUID
    }
    UUID2(TUUID2 uuid2) {
        this(uuid2, uuid2.getUUID2TypeStr());
    }

    // simple getter
    public UUID uuid() {return uuid;}

    // return a copy of the UUID
    public UUID toUUID() {
        return new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public boolean equals(UUID2<TUUID2> other) {
        return (other).uuid.equals(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String getUUID2TypeStr() {
        return uuid2TypeStr;
    }

    // Note: Should only be used for importing JSON
    protected void setUUID2TypeStr(String uuidType) {
        this.uuid2TypeStr = uuidType;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    public static <TDomainUUID2 extends IUUID2> UUID2<TDomainUUID2> fromString(String uuidStr) {
        return new UUID2<>(UUID.fromString(uuidStr));
    }

    public static <TDomainUUID2 extends IUUID2> UUID2<TDomainUUID2> fromUUID(UUID uuid) {
        return new UUID2<>(uuid);
    }

    public static <TDomainUUID2 extends IUUID2> UUID2<TDomainUUID2> randomUUID2() {
        return new UUID2<>(UUID.randomUUID());
    }

    @SuppressWarnings("unchecked")
    public static <TDomainUUID2 extends IUUID2> @NotNull UUID2<TDomainUUID2> createFakeUUID2(final Integer id, String className) {
        Integer nonNullId = id;
        if (nonNullId == null) nonNullId = 1;

        final String idPaddedWith11LeadingZeroes = format("%011d", nonNullId);
        final UUID2<TDomainUUID2> uuid2 = fromString("00000000-0000-0000-0000-" + idPaddedWith11LeadingZeroes);

        return new UUID2<>((TDomainUUID2) uuid2, className);
    }
    public static <TDomainUUID2 extends IUUID2> @NotNull UUID2<TDomainUUID2> createFakeUUID2(final Integer id) {
        return createFakeUUID2(id, null);
    }

    public UUID2<IUUID2> toDomainUUID2() {
        return new UUID2<>(this, this.getUUID2TypeStr()); //uuid2TypeStr); // todo make sure this works
    }

    @SuppressWarnings("unchecked")
    public UUID2<IUUID2> toUUID2() {
        return (UUID2<IUUID2>) this;
    }

    // Utility HashMap class for mapping UUID2<T> to Objects.
    // Wrapper for HashMap where the `key` hash is the common UUID value stored in UUID class.
    // The problem is that normal HashMap uses `hash()` of UUID<{type}> object itself, which is not
    // consistent between UUID2<{type}> objects.
    // This class uses UUID2<T> for the keys, but the hash is the common UUID value stored in UUID class.
    static class HashMap<TUUID2 extends IUUID2, TEntity> extends java.util.HashMap<UUID, TEntity> {
        private static final long serialVersionUID = 0x7723L;

        HashMap() {
            super();
        }
        // Creates a database from another database
        public <T extends TUUID2, U extends TEntity> HashMap(UUID2.HashMap<UUID2<T>, U> sourceDatabase) {
            super();
            this.putAll(sourceDatabase);
        }

        public TEntity get(String uuid2Str) {
            return get(UUID.fromString(uuid2Str));
        }
        public TEntity get(UUID2<TUUID2> uuid2) {
            return get(uuid2.uuid);
        }
        public TEntity get(UUID uuid) { // allow UUIDs to be used as keys, but not recommended.
            return super.get(uuid);
        }

        public TEntity put(String uuid2Str, TEntity value) {
            return put(UUID2.fromString(uuid2Str), value);
        }
        public TEntity put(UUID2<TUUID2> uuid2, TEntity value) {
            return put(uuid2.uuid, value);
        }
        public TEntity put(UUID uuid, TEntity value) { // allow UUIDs to be used as keys, but not recommended.
            return super.put(uuid, value);
        }

        public TEntity remove(String uuid2Str) {
            return remove(UUID.fromString(uuid2Str));
        }
        public TEntity remove(UUID2<TUUID2> uuid2) {
            return remove(uuid2.uuid);
        }
        public TEntity remove(UUID uuid) { // allow UUIDs to be used as keys, but not recommended.
            return super.remove(uuid);
        }

        public boolean containsKey(String uuid2Str) {
            return containsKey(UUID.fromString(uuid2Str));
        }
        public boolean containsKey(UUID2<TUUID2> uuid2) { return super.containsKey(uuid2.uuid); }

        public Set<TUUID2> keys() throws RuntimeException {
            Set<UUID> uuidSet = super.keySet();
            Set<TUUID2> uuid2Set = new HashSet<>();

            // Convert UUIDs to TDomainUUIDs
            try {
                for (UUID uuid : uuidSet) {
                    @SuppressWarnings({"unchecked"})
                    TUUID2 uuid2 = (TUUID2) UUID2.fromUUID(uuid);
                    uuid2Set.add(uuid2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("HashMap.keys(): Failed to convert UUID to UUID2<TDomainUUID>, uuidSet: " + uuidSet);
            }

            return uuid2Set;
        }
    }
}