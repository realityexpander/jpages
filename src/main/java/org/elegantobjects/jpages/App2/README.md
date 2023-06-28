## Design Goals
- No setters
- No getters
- No static variables
- immutable objects
- All dependencies passed in constructor
- No accessing global state
- Singletons passed in constructor held in Context object
- No Dependency Injection framework
- Dumb Container objects (DTO, Entity) are immutable and only used to pass data to/from outside domain
  to domain "Role" objects.
  - DTO's and Entities are still useful to maintain separation of concerns and to communicate with
    outside world, and allows independent updating and maintaining of the domain objects. 
- Role objects are immutable and communicate or contain other Role objects
- Role objects contain references to mutable data, which is updated or fetched automatically
  - ie: `User` object contains a `UserInfo` object, which is updated automatically when `User` is updated
- Constructors have one primary entrypoint, and all other constructors call this one.
  - the only exception is for JSON and Info constructors, since they use special types. 

- Use of Static methods is severely limited to only those that are:
  - pure functions
  - no side effects
  - no access or creation of global state
  - used to create objects from JSON, XML, etc.

- All objects are immutable, with the exception of the UUID and UUID Types. These are mutable because
  of how JSON imports work. The UUID is used as a key to find the object in the Context, and the
  UUIDType is used to create the UUID object. The UUIDType is mutable bc its not known at creation time.

- built to test from start

- No nulls
- No exceptions
- Minimal null checks at boundaries

- No factory pattern
- No builder pattern
- No fluent interface

- yes on early return for error handling
- one success return at the end


- clear separation of concerns