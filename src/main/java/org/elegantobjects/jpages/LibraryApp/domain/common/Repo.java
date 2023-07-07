package org.elegantobjects.jpages.LibraryApp.domain.common;


import org.elegantobjects.jpages.LibraryApp.common.util.log.ILog;

// Since the Repo only accepts/returns Domain.{Domain}Info objects, this lives in the domain layer.
// - Internally, it accesses the Data layer, and does conversions between the data layers and domain.
public class Repo implements IRepo {
    protected final ILog log;  // each Repo needs a Log

    protected Repo(ILog log) {
        this.log = log;
    }

}
