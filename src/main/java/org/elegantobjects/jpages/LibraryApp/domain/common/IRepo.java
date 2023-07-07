package org.elegantobjects.jpages.LibraryApp.domain.common;

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
// - Works with the network API & local database to perform CRUD operations, and also performs validation.
// - Can also be used to implement caching.
// The Repo can easily accept fake APIs & Database for testing.
public interface IRepo {} // Marker interface for all `Repo` classes
