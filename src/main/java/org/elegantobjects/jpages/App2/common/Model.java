package org.elegantobjects.jpages.App2.common;

import com.google.gson.GsonBuilder;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.common.network.DTOInfo;
import org.elegantobjects.jpages.App2.data.common.local.EntityInfo;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.common.DomainInfo;
import org.jetbrains.annotations.NotNull;

/**
 {@code {Model}Info} Data "Holders" kept inside each Role Domain Object.<br>
 <br>
 <b>Domain Info Classes</b><br>
 - Similar to an Entity for a database row or a DTO for a REST API endpoint, these objects are the
   the objects contain the "data" or {@code Info} that is accessed by the {@code Role} object.<br>
 - They are the "source of truth" for the Domain object's "data" in the application.<br>
 - {@code {Domain}}Info hold the {@code Role Info} that resides elsewhere, usually on a local-server/db/api,
   but the {@code Role} does not know where or care where the data comes from, it only knows the format.<br>
 <br>
 <b>DTO/Entity Info Classes</b><br>
 - {@code {DTO}Info} hold the API transfer "dumb" objects that transport info to/from their service/api/db.<br>
 - {@code {Entity}Info} hold the Database transfer "dumb" objects that transport info to/from their service/api/db.<br>
 - Validation occurs in the Domain layer, when an DTO/Entity is converted to a DomainInfo object.<br>
 **/
public class Model {
    public UUID2<?> _id; // Can't make final due to need to set it during JSON deserialization. :(
                         // Also can't make it private due to Gson's need to access it during deserialization. :(
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
    public void _setIdFromImportedJson(UUID2<IUUID2> id) {
        this._id = id;
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
//        UUID2<?> domainInfoId();  // *MUST* override, method should return id of DomainInfo object (used for deserialization)
        UUID2<?> id();  // *MUST* override, method should return id of DomainInfo object (used for deserialization)


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
