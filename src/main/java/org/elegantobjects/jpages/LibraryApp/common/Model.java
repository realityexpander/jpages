package org.elegantobjects.jpages.LibraryApp.common;

import com.google.gson.GsonBuilder;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.data.common.network.DTOInfo;
import org.elegantobjects.jpages.LibraryApp.data.common.local.EntityInfo;
import org.elegantobjects.jpages.LibraryApp.domain.Context;
import org.elegantobjects.jpages.LibraryApp.domain.common.DomainInfo;
import org.jetbrains.annotations.NotNull;

/**
 <b>Model - Top of data "Info" hierarchy</b><br>
 <br>
 Handles data "Info" conversion of {@code DomainInfo} <i>to/from</i> {@code DTOInfo/EntityInfo}.<br>
 <br>
 <b>Domain Info Classes</b><br>
 <ul>
   <li>The {@code {Domain}Info} "Data Holder" class is kept inside each Domain {@code Role} Object.</li>
   <li>Similar to an Entity for a database row or a DTO for a REST API endpoint, these objects
   contain the "data" or {@code Info} that is accessed by the {@code Role} object.</li>
   <li>These are the "source of truth" for the Domain object's "information" in the application.</li>
   <li>{@code {Domain}Info} hold the {@code Role Info} that resides *elsewhere*, usually on a server/db/api.<br>
       The {@code Role} does not know (or care) where the data comes from, it only knows the "data shapes"
       that it accepts.</li>
 </ul>
 <br>
 <b>DTO/Entity Info Classes</b><br>
 <ul>
   <li>{@code {DTOInfo}Info} hold the API transfer "dumb" objects that transport info to/from their service/api/db.</li>
   <li>{@code {EntityInfo}Info} hold the Database transfer "dumb" objects that transport info to/from their service/db.</li>
   <li>Minimal validation occurs in the Domain layer, when an DTOInfo/EntityInfo object is converted into a DomainInfo object.</li>
 </ul>
 **/
public class Model {
    public UUID2<?> _id; // Can't make final due to need to set it during JSON deserialization. 🫤
                         // Also can't make it private due to Gson's need to access it during deserialization. 🫤
                         // todo Is there a better way to do this? (maybe another JSON library?)

    protected
    Model(UUID2<?> id) {
        this._id = new UUID2<>(id);
    }

    ////////////////////////
    // Simple getters     //
    ////////////////////////

    public UUID2<?> id() { return _id; }

    // EXCEPTIONAL CASE:
    // - This method is for JSON deserialization purposes & should only be used for such.
    // - It is not intended to be used for any other purpose.
    // - todo Is there a better way to do this?
    public void _setIdFromImportedJson(UUID2<IUUID2> _id) {
        this._id = _id;
    }

    public String toPrettyJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
    public String toPrettyJson(@NotNull Context context) {
        return context.gson.toJson(this);
    }

    ///////////////////////////
    // Converters between    //
    // - Domain.{Domain}Info //
    // - Entity.{Domain}Info //
    // - DTO.{Domain}Info    //
    ///////////////////////////

    public interface ToDomainInfo<TDomainInfo extends DomainInfo> {

        // *MUST* override
        // - Overridden method should return `id` with the correct type of UUID2 for the domain
        //   ie: `UUID2<User>` for the `User`, `UUID2<UserInfo>` for the UserInfo, etc.
        UUID2<?> id();

        default @SuppressWarnings("unchecked")
        TDomainInfo getDomainInfo()
        {  // Return reference to TDomainInfo, used when importing JSON
            return (TDomainInfo) this;
        }

        default TDomainInfo toDeepCopyDomainInfo() {    // **MUST** override, method should return a DEEP copy (& no original references)
            throw new RuntimeException("DomainInfo:ToDomainInfo:toDeepCopyDomainInfo(): Must override this method");
        }

        // This interface enforces all DomainInfo objects to include a deepCopyDomainInfo() method
        // - Just add "implements ToDomainInfo.deepCopyDomainInfo<ToDomainInfo<Domain>>" to the class
        //   definition, and the deepCopy() method will be added.
        interface hasToDeepCopyDomainInfo<TToInfo extends ToDomainInfo<? extends DomainInfo>> {


            default @SuppressWarnings("unchecked") <TDomainInfo extends DomainInfo>
            TDomainInfo deepCopyDomainInfo() // Requires method override, should return a deep copy (no original references)
            {
                // This method is a lazy convenience, and should really be overridden in each class.
                // This is a hack to get around the fact that Java doesn't allow you to call a generic method from a generic class
                return (TDomainInfo) ((TToInfo) this).toDeepCopyDomainInfo();
            }
        }
    }
    public interface ToEntityInfo<T extends EntityInfo> {
        T toInfoEntity(); // Should return a deep copy (no original references)
    }
    public interface ToDTOInfo<T extends DTOInfo> {
        T toInfoDTO();    // Should return a deep copy (no original references)
    }

}
