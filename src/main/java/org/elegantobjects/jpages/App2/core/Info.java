package org.elegantobjects.jpages.App2.core;

import com.google.gson.Gson;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.Domain;

import java.lang.reflect.Type;
import java.util.UUID;

// Info - Caches the Model Object's "Info" and defines required Info operations.
// Info object stores the "business data" for the Domain object.
// It is the "single source of truth" for the Domain object.
// Domain objects keep a single reference to their Info object, and load/save it to/from the server/DB as needed.
public interface Info<TInfo> {
    // TInfo info;                        // Requires a field named "info" of type TInfo (is there a way to enforce this in java?

    UUID2<?> id();                        // Returns the UUID2 of the Info object
    TInfo fetchInfo();                    // Fetches info for object from server/DB
    boolean isInfoFetched();              // Returns true if info has been fetched from server/DB
    Result<TInfo> fetchInfoResult();      // Fetches Result<T> for info object from server/DB
    Result<TInfo> updateInfo(TInfo info); // Updates info for object to server/DB
    Result<TInfo> refreshInfo();          // Refreshes info for object from server/DB
    String fetchInfoFailureReason();      // Returns reason for failure of last fetchInfo() call, or null if successful

    @SuppressWarnings("unchecked")
    default TInfo deepCopyInfo() {                // Returns a deep copy of the Info object

        return (TInfo) new Gson().fromJson(
                new Gson().toJson(this),
                this.getClass()
        );
    }

    interface ToInfo<TInfo> {
        UUID2<?> getInfoId();             // Returns the UUID2 of the Info object

        @SuppressWarnings("unchecked")
        default TInfo getInfo() {         // Returns the Info object
            //noinspection unchecked
            return (TInfo) this; // todo test this cast
        }

        @SuppressWarnings("unchecked")
        default TInfo toDeepCopyInfo() {    // **MUST** override, method should return a DEEP copy (& no original references)
            //noinspection unchecked
            return ((Info<TInfo>) this).deepCopyInfo();  // todo test this cast

            // throw new RuntimeException("Info:ToInfo:toDeepCopyInfo(): Must override this method"); // todo remove this?
        }
    }

    public static <
            TToInfo extends ToInfo<?> // implementations of ToInfo<TInfo> interfaces MUST have TInfo objects
            > TToInfo createInfoFromJson(
            String json,
            Class<TToInfo> infoClazz, // type of `Info` object to create
            Context context
    ) {
        try {
            TToInfo obj = context.gson.fromJson(json, (Type) infoClazz);
            assert obj != null;
            context.log.d("Info:createInfoFromJson()", "obj = " + obj);

            // Set the UUID2 typeStr to match the Info Class name
            String infoClazzName = infoClazz.getName();
            infoClazz.cast(obj)
                    .getInfoId()
                    ._setUUID2TypeStr(infoClazzName);

            return obj;
        } catch (Exception e) {
            context.log.d( "IDomainObject:createDomainInfoFromJson()", "Failed to createDomainInfoObjectFromJson() for " +
                    "class: " + Domain.LibraryInfo.class.getName() + ", " +
                    "json: " + json + ", " +
                    "exception: " + e.toString());

            return null;
        }
    }

    // This interface is to enforce all DomainInfo objects have a deepCopy() method
    // - Just add "implements ToInfo.hasDeepCopyInfo<ToInfo<{InfoClass}>>" to the class
    //   definition, and the toDeepCopyInfo() method will be added.
    interface hasToDeepCopyInfo<TInfo extends ToInfo<?>> {

        @SuppressWarnings("unchecked")
        default TInfo deepCopyInfo() {
            // This is a default implementation for deepCopyInfo() that simply calls the toDeepCopyInfo() method implemented in the subclass
            // This is a workaround for the fact that Java doesn't allow static methods in interfaces.
            return (TInfo) ((TInfo) this).toDeepCopyInfo(); // calls the toDeepCopyInfo() method of the implementing class
        }
    }

    default Result<TInfo> checkInfoIdMatchesJsonInfoId(TInfo infoFromJson, Class<?> infoClazz) {

        try {
            // Ensure JSON Info object has an id field
            Object idField = infoClazz.getDeclaredField("id").get(infoFromJson);
            if(idField == null) {
                return new Result.Failure<>(new Exception("checkInfoIdMatchesJsonId(): Info class does not have an id field"));
            }

            String idStr = idField.toString();
            UUID infoFromJsonId = UUID.fromString(idStr);

            if (!infoFromJsonId.equals(this.id().uuid())) {
                return new Result.Failure<>(new Exception("checkInfoIdMatchesJsonId(): Info id does not match json id, " +
                        "info id: " + this.id() + ", " +
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
