package org.elegantobjects.jpages.App2.domain.common;


import org.elegantobjects.jpages.App2.common.util.log.ILog;

// Since the Repo only accepts/returns Domain.{Domain}Info objects, this lives in the domain layer.
// - Internally, it accesses the data layer, and does conversions between the layers.
public class Repo implements IRepo {
    protected final ILog log;

    protected Repo(ILog log) {
        this.log = log;
    }

}
