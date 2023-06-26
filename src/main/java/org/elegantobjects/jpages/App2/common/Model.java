package org.elegantobjects.jpages.App2.common;

import com.google.gson.GsonBuilder;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.network.DTO;
import org.elegantobjects.jpages.App2.data.local.Entity;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.repoData.Domain;
import org.jetbrains.annotations.NotNull;

// "{Model}Info" Data Holders held inside each App Domain Object.
// - Similar to an Entity for a database row or a DTO for a REST API endpoint, these are
//   the objects that are passed around the application.
// - They are the "source of truth" for the Domain objects in the application.
// - {Domain}Info hold the Info state that is on the server/api.
// - {DTO}Info hold the API transfer "dumb" objects and Validation layer for the Domain objects.
// - {Entity}Info hold the Database transfer "dumb" objects. Validation can occur here too, but usually not necessary.
public class Model {
    transient protected UUID2<IUUID2> _id; // Can't make final bc need to set it during JSON deserialization. :(

    protected Model(UUID2<?> id, String uuidTypeStr) {
        this._id = new UUID2<>(id, uuidTypeStr);
    }

    ///////////////////////////////
    // Converters between
    // - Domain.{Domain}Info
    // - Entity.{Domain}Info
    // - DTO.{Domain}Info
    ///////////////////////////////

    public interface ToDomain<TDomainInfo extends Domain> {
        UUID2<?> getDomainInfoId();  // *MUST* override, method should return id of DomainInfo object (used for deserialization)

        @SuppressWarnings("unchecked")
        default TDomainInfo getDomainInfo()
        {  // Return reference to TDomainInfo, used when importing JSON
            return (TDomainInfo) this; // todo test this cast
        }

        default TDomainInfo toDeepCopyDomainInfo() {    // **MUST** override, method should return a DEEP copy (& no original references)
            throw new RuntimeException("DomainInfo:ToDomainInfo:toDeepCopyDomainInfo(): Must override this method");
        }

        // This interface enforces all DomainInfo objects to include a deepCopyDomainInfo() method
        // - Just add "implements ToDomainInfo.deepCopyDomainInfo<ToDomainInfo<Domain>>" to the class
        //   definition, and the deepCopy() method will be added.
        interface hasToDeepCopyDomainInfo<TToInfo extends ToDomain<? extends Domain>> {

            @SuppressWarnings("unchecked")
            default <TDomainInfo extends Domain>
            TDomainInfo deepCopyDomainInfo() // Requires method override, should return a deep copy (no original references)
            {
                // This is a hack to get around the fact that Java doesn't allow you to call a generic method from a generic class
                return (TDomainInfo) ((TToInfo) this).toDeepCopyDomainInfo();
            }
        }
    }
    public interface ToEntity<T extends Entity> {
        T toEntity(); // Should return a deep copy (no original references)
    }
    public interface ToDTO<T extends DTO> {
        T toDTO();    // Should return a deep copy (no original references)
    }

    public String toPrettyJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this); // todo switch over to context version
    }
    public String toPrettyJson(@NotNull Context context) {
        return context.gson.toJson(this);
    }

    public UUID2<?> id() { return _id; }

    // This method is for JSON deserialization purposes & should only be used for such.
    public void _setIdFromImportedJson(UUID2<IUUID2> _id) {
        this._id = _id;
    }

}