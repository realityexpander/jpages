package org.elegantobjects.jpages.LibraryApp.domain.common.data.info;

import com.google.gson.Gson;
import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.Context;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 <b>{@code Info}</b> is a smart data holder class for transferring data to/from the Domain to/from Database/Api.<br>
 <ul>
   <li><b>{@code TInfo info}</b> - Caches the Role Object's "Info" (data) and defines required operations to mutate
       the 'Info' object.</li>
   <li>The <b>{@code Info}</b> object stores the "business data" for the Domain object & logic to change it.</li>
   <li>It is the "single source of truth" for the Domain object's mutable data.</li>
 </ul>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
**/
public interface Info<TInfo> {
    // Note: Requires a field named `info` of type `AtomicReference<TInfo>` (todo is there a way to enforce this in java?)
    // private final AtomicReference<TInfo> info;

    UUID2<?> id();                        // Return the UUID2 of the Info object.
    TInfo fetchInfo();                    // Fetch data for the Info from server/DB.
    boolean isInfoFetched();              // Return true if Info has been successfully fetched from server/DB.
    Result<TInfo> fetchInfoResult();      // Fetch Result<T> for the Info from server/DB.
    Result<TInfo> updateInfo(TInfo info); // Update Info to server/DB.
    Result<TInfo> refreshInfo();          // Set Info data to `null` and fetches Info from server/DB.
    String fetchInfoFailureReason();      // Performs fetch for Info and returns failure reason, or `null` if successful.
    AtomicReference<TInfo> cachedInfo();  // Return thread-safe Info from cache.

    interface ToInfo<TInfo> {
        UUID2<?> id();             // Returns the UUID2 of the Info object

        @SuppressWarnings("unchecked")
        default TInfo info() {     // Fetches (if necessary) and Returns the Info object
            //noinspection unchecked
            return (TInfo) this;
        }

        @SuppressWarnings("unchecked")
        // **MUST** override, method should return a DEEP copy (& no original references)
        default TInfo toDeepCopyInfo() {
            return ((Info<TInfo>) this).deepCopyInfo();

            // throw new RuntimeException("Info:ToInfo:toDeepCopyInfo(): Must override this method"); // todo Should force override? or use this default behavior?
        }
    }

    static <TToInfo extends ToInfo<?>> @Nullable // implementations of ToInfo<TInfo> interfaces MUST define TInfo field named `info`
    TToInfo createInfoFromJson(
        String json,
        Class<TToInfo> infoClazz, // type of `Info` object to create
        Context context
    ) {
        try {
            TToInfo obj = context.gson.fromJson(json, (Type) infoClazz);
            assert obj != null;
            context.log.d("Info:createInfoFromJson()", "obj = " + obj);

            // Set the UUID2 typeStr to match the Info Class name
            String infoClazzName = UUID2.calcUUID2TypeStr(infoClazz);
            infoClazz.cast(obj)
                    .id()
                    ._setUUID2TypeStr(infoClazzName);

            return obj;
        } catch (Exception e) {
            context.log.d( "Info:createInfoFromJson()", "Failed to createInfoFromJson() for " +
                    "class: " + infoClazz.getName() + ", " +
                    "json: " + json + ", " +
                    "exception: " + e);

            return null;
        }
    }

    // This interface used to enforce all {Domain}Info objects has a `deepCopy()` method.
    // - Just add `implements ToInfo.hasDeepCopyInfo<ToInfo<{InfoClass}>>` to the class
    //   definition, and the toDeepCopyInfo() method will be added.
    interface hasToDeepCopyInfo<TInfo extends ToInfo<?>> {
        @SuppressWarnings("unchecked")
        default TInfo deepCopyInfo() {
            // This default implementation for deepCopyInfo() simply calls the toDeepCopyInfo() implemented in the subclass.
            // This is a workaround for the fact that Java doesn't allow static methods in interfaces.
            return (TInfo) ((TInfo) this).toDeepCopyInfo(); // calls the toDeepCopyInfo() method of the implementing class
        }
    }

    // Performs Atomic update of cachedInfo
    default TInfo updateCachedInfo(final TInfo updatedInfo) {
        return this.cachedInfo().updateAndGet(
            curCachedInfo -> {
                return updatedInfo;
            }
        );
    }

    // Default naive implementation, returns a deep copy of the Info object.
    @SuppressWarnings("unchecked")
    default TInfo deepCopyInfo() {
        Gson gson = new Gson();

        // hacky but works.
        return (TInfo) gson.fromJson(
            gson.toJson(this),
            this.getClass()
        );
    }

    //////////////////////////////
    // Helper methods for Info  //
    //////////////////////////////

    default Result<TInfo> checkJsonInfoIdMatchesThisInfoId(TInfo infoFromJson, Class<?> infoClazz) {
        try {
            // Ensure JSON Info object has an `_id` field
            Class<?> rootInfoClazz = _getRootClazz(infoClazz);
            Object idField = rootInfoClazz.getDeclaredField("_id").get(infoFromJson);
            if(idField == null) {
                return new Result.Failure<>(new Exception("checkJsonInfoIdMatchesThisInfoId(): Info class does not have an _id field"));
            }

            UUID idFromJson = ((UUID2<?>)idField).uuid();

            if (!idFromJson.equals(this.id().uuid())) {
                return new Result.Failure<>(new Exception("checkJsonInfoIdMatchesThisInfoId(): Info _id does not match json _id, " +
                        "info _id: " + this.id() + ", " +
                        "json _id: " + idFromJson));
            }
        } catch (NoSuchFieldException e) {
            return new Result.Failure<>(new Exception("checkJsonInfoIdMatchesThisInfoId(): Info class does not have an id field"));
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(infoFromJson);
    }

    default Class<?> _getRootClazz(Class<?> infoClazz) {
        Class<?> rootClazz = infoClazz;
        while(!rootClazz.getSuperclass().getSimpleName().equals("Object")) {
            rootClazz = rootClazz.getSuperclass();
        }

        return rootClazz;
    }
}
