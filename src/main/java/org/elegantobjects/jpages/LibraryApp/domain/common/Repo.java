package org.elegantobjects.jpages.LibraryApp.domain.common;


import org.elegantobjects.jpages.LibraryApp.common.util.log.ILog;

/**
 * IRepo is a marker interface for all <b>{@code {DomainInfo}Repo}</b> classes.
 * <br>
 * <b>{@code Repo}</b> is the base class for all {Domain}Info Repository classes.<br>
 * <br>
 * A Repo only accepts/returns <b>{@code {Domain}Info}</b> objects.<br>
 * <br>
 * Repos access the Data layer, and do conversions between the data layer and domain.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */
public class Repo implements IRepo {
    protected final ILog log;  // each Repo needs a Log

    protected Repo(ILog log) {
        this.log = log;
    }

}
