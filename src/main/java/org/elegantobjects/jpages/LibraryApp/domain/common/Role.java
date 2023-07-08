package org.elegantobjects.jpages.LibraryApp.domain.common;

import com.google.gson.JsonSyntaxException;
import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.LibraryApp.data.common.Info;
import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Role - Common Domain Role Abstract class<br>
<ul>
 <li>Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains</li>
 <li>Works with the network API & local database to perform CRUD operations, and also performs validation.</li>
 <li>Can also be used to implement caching.</li>
</ul>
  The Repo can easily accept fake APIs & Database for testing.
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public abstract class Role<TDomainInfo extends DomainInfo>
    implements
        Info<TDomainInfo>,
        IUUID2
{
    private final UUID2<?> id;  // the UUID of the Domain object matches the UUID of the Info object
                                // It is final and private in the Domain layer.

    protected TDomainInfo info;  // Information object for Info<Domain.{Domain}Info>
    protected Result<TDomainInfo> infoResult = null;

    // Singletons
    protected final Context context;  // All roles have access the Context singleton

    // Class of the Info<TDomain> info object (for Gson serialization)
    // - also JAVA REFLECTION IS UGLY!!
    @SuppressWarnings("unchecked")
    private final Class<TDomainInfo> infoClazz =
        getClass().getGenericSuperclass() instanceof ParameterizedType
            ? (Class<TDomainInfo>) ((ParameterizedType) getClass() // 1. Get clazz for "Info" from this class... ⬇︎
                .getGenericSuperclass())
                .getActualTypeArguments()[0]
            : (Class<TDomainInfo>) (
                    (ParameterizedType) (
                        (Class<?>) (
                            this.getClass()
                                .getGenericSuperclass()            // 2. ⬆︎ ...or from the superClass generic type.
                        )
                    ).getGenericSuperclass()
              ).getActualTypeArguments()[0];

    private
    Role(
        @NotNull UUID id,
        @Nullable TDomainInfo info,
        @NotNull Context context
    ) {
        this.id = UUID2.fromUUID(id); // Note: NOT validating "id==info.id" due to need to pass in `info` as `null`
        this.info = info;
        this.context = context;
    }
    private
    Role(
        @NotNull UUID2<?> id,
        @Nullable TDomainInfo info,
        @NotNull Context context
    ) {
        this.id = id; // intentionally NOT validating `id==info.id` bc need to be able to pass in `info` as null.
        this.info = info;
        this.context = context;
    }
    protected <TDomainInfo_ extends Model.ToDomainInfo<TDomainInfo>> // All classes implementing ToDomain<> interfaces must have TDomainInfo field
    Role(
        @NotNull String domainInfoJson,
        @NotNull Class<TDomainInfo_> classType,
        @NotNull Context context
    ) {
        this(
            Objects.requireNonNull(
                Role.createInfoFromJson(domainInfoJson, classType, context)
            ).getDomainInfo(),
            context
        );
    }
    protected <TDomainInfo_ extends TDomainInfo>
    Role(
        @NotNull TDomainInfo_ info,
        @NotNull Context context
    ) {
        this(info.id(), info, context);
    }
    protected
    Role(
        @NotNull UUID2<?> id,
        @NotNull Context context
    ) {
        this(id, null, context);
    }
    Role(Context context) {
        this(UUID.randomUUID(), null, context);
    }
    // LEAVE FOR REFERENCE, for static Context instance implementation
    // Role(String json) {
    //    this(json, null);
    //    this.context = Context.setupInstance(context);  // LEAVE for reference, for static Context instance implementation
    // }

    /////////////////////
    // Simple getters  //
    /////////////////////

    public UUID2<?> id() {
        return this.id;
    }

    ///////////////////////////
    // Static Constructors   //
    ///////////////////////////

    // Creates new `Domain.{Domain}Info` object with id from JSON string of `Domain.{Domain}Info` object
    // - Implemented as a static method bc it can be called from a constructor.
    //   (Can't call instance methods from constructor in java.)
    // - Note: Type definitions are to make sure constrained to Domain subtypes and subclasses.
    // - ie: The Library domain object has a Domain.LibraryInfo object which requires ToDomain<Domain.LibraryInfo>
    //   to be implemented.
    // - Only imports JSON to Domain objects.
    //   The Domain.Entity and Domain.DTO layer are intentionally restricted to accept only Domain objects.
    // - todo : Should change to a marker interface instead of a constraining to the ToDomain<TDomain> interface?
    @SuppressWarnings("unchecked") // for _setIdFromImportedJson() call
    public static <
            TDomain extends DomainInfo,  // restrict to Domain subclasses, ie: Domain.BookInfo
            TDomainInfo extends Model.ToDomainInfo<? extends TDomain> // implementations of ToInfo<TDomain> interfaces MUST have Info<TDomain> objects
        > TDomainInfo createInfoFromJson(
            String json,
            @NotNull Class<TDomainInfo> domainInfoClazz, // type of `Domain.TDomainInfo` object to create
            @NotNull Context context
    ) {
        if(json == null) return null;

        try {
            TDomainInfo obj = context.gson.fromJson(json, (Type) domainInfoClazz);
            context.log.d("Role:createInfoFromJson()", "obj = " + obj);

            // Set UUID2Type to match the type of TDomainInfo object
            String domainInfoClazzName = UUID2.calcUUID2TypeStr(domainInfoClazz);
            domainInfoClazz.cast(obj)
                    .id()
                    ._setUUID2TypeStr(domainInfoClazzName);

            // Set `id` to match `id` of the Info
            ((TDomain) obj)._setIdFromImportedJson(
                new UUID2<>(((TDomain) obj).id(), domainInfoClazzName)
            );

            return obj;
        } catch (Exception e) {
            context.log.d( "Role:createInfoFromJson()", "Failed to createInfoFromJson() for " +
                    "class: " + domainInfoClazz.getName() + ", " +
                    "json: " + json + ", " +
                    "exception: " + e);

            return null;
        }
    }

    public Result<TDomainInfo> updateInfoFromJson(@NotNull String json) {
        context.log.d(this,"Updating Info from JSON for " +
                "class: " + this.getClass().getName() + ", " +
                "id: " + this.id());

        try {
            Class<TDomainInfo> domainInfoClazz = this.infoClazz;
            TDomainInfo infoFromJson = this.context.gson.fromJson(json, domainInfoClazz);

            Result<TDomainInfo> checkResult = checkJsonInfoIdMatchesThisInfoId(infoFromJson, domainInfoClazz);
            if (checkResult instanceof Result.Failure) {
                return checkResult;
            }

            // Set Domain "Model" id to match id of imported Info // todo maybe use just one id?
            infoFromJson._setIdFromImportedJson(
                new UUID2<>(infoFromJson.id(), UUID2.calcUUID2TypeStr(domainInfoClazz))
            );

            // Update the info object with the new info
            return this.updateInfo(infoFromJson);
        } catch (JsonSyntaxException e) {
            return new Result.Failure<>(new Exception("Failed to parse JSON: " + e.getMessage()));
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }
    }

    public String toJson() {
        if(!isInfoFetched()) {
            context.log.w(this,"called on un-fetched info for " +
                    "class: " + this.getClass().getName() + ", " +
                    "id: " + this.id());

            return "{}";
        }

        return this.context.gson.toJson(this.fetchInfo());
    }

    /////////////////////////////////////////////////////
    // Methods REQUIRED to be overridden in subclasses //
    /////////////////////////////////////////////////////

    // Defines how to fetch info from server
    // - REQUIRED - *MUST* be overridden/implemented in subclasses
    @Override
    public Result<TDomainInfo> fetchInfoResult() {
        return new Result.Failure<>(new Exception("Not Implemented, should be implemented in subclass"));
    }

    // Updates the info object with a new info object
    // - REQUIRED - *MUST* be overridden/implemented in subclasses
    // - Call super.updateInfo(info) to update the info<TDomainInfo> object
    //   (caller decides when appropriate, ie: optimistic updates, or after server confirms update)
    @Override
    public Result<TDomainInfo> updateInfo(@NotNull TDomainInfo info) { // **MUST** Override in subclasses
        this.info = info;
        return new Result.Success<>(this.info);
    }

    // NOTE: Should be Implemented by subclasses but not required
    @Override
    public String toString() {
        // default toString() implementation
        String infoString = this.info == null ? "null" : this.info.toString();
        String nameOfClass = this.getClass().getName();

        return nameOfClass + ": " + this.id() + ", info=" + infoString;
    }

    /////////////////////////////////
    // Info<T> interface methods   //
    /////////////////////////////////

    public TDomainInfo info() {
        return this.fetchInfo();
    }

    // Returns the Info<T> object if it has been fetched, otherwise null.
    // Used to access the Info object without having to handle the Result<T> object.
    // NOTE: The Info object is not re-fetched if it has already been fetched.
    @Override
    public TDomainInfo fetchInfo() {
        if (isInfoFetched()) {
            return this.info;
        }

        // Attempt to fetch info, since it hasn't been successfully fetched yet.
        Result<TDomainInfo> result = this.fetchInfoResult();
        if (result instanceof Result.Failure) {
            context.log.d(this,"fetchInfoResult() FAILED for " +
                    "class: " + this.getClass().getName() + ", " +
                    "id: " + this.id.toString());

            return null;
        }

        // Fetch was successful, so set info and return it.
        this.info = ((Result.Success<TDomainInfo>) result).value();
        return this.info;
    }

    // Returns reason for failure of last fetchInfo() call, or null if was successful.
    // - Used as a convenient error guard for methods that require the {Domain}Info to be loaded.
    // - If Info is not fetched, it attempts to fetch it.
    // - The "returning null" behavior is to make the call site error handling code smaller.
    @Override
    public String fetchInfoFailureReason() {
        if (!isInfoFetched()) {
            if (fetchInfoResult() instanceof Result.Failure) {
                return ((Result.Failure<TDomainInfo>) fetchInfoResult()).exception().getMessage();
            }
        }

        return null; // Returns `null` if the info has been fetched successfully. This makes the call site smaller.
    }

    @Override
    public boolean isInfoFetched() {
        return this.info != null;
    }

    // Forces refresh of Info from server
    @Override
    public Result<TDomainInfo> refreshInfo() {
        context.log.d(this,"Refreshing info for " +
                "class: " + this.getClass().getName() + ", " +
                "id: " + this.id.toString());

        // Exception to the no-null rule. This is only when forcing a re-fetch of the info.
        // todo should we leave the data stale?
        this.info = null;

        return this.fetchInfoResult();
    }
}