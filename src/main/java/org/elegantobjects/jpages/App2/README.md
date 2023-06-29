# Library App using BOOP

## Design Goals

### Inspiration
- Have a pure domain layer that adheres to Alan Kay and Yegor Bugayenko OO styles

### Developer Experience
- Architected by layer, and each layer is grouped by feature, which allows convenenient and
  easy to understand navigation of the code.
- Built to test from start

### Encapsulation
- No setters
- No getters
- No Dependency Injection framework

### Global State
- No static/global variables
- No accessing global state
- All dependencies passed in constructors

### Immutability
- all objects are immutable
- Role objects are immutable and communicate or contain other Role objects
- Role objects contain references to mutable data, which is updated or fetched automatically
- All objects are immutable, with the exception of the UUID and UUID Types. These are mutable because
  of how JSON imports work. The UUID is used as a key to find the object in the Context, and the
  UUIDType is used to create the UUID object. The UUIDType is mutable bc its not known at creation time.

### Constructor Convenience
- Many different constructors included for many different ways to create objects
- Singletons passed in constructor, held in Context object
- No null objects
  - No null checks, except for null checks on the input to the constructor
- No thrown exceptions except for actual errors that are not expected.
  - ie: `User` object contains a `UserInfo` object, which is updated automatically when `User` is updated
- Constructors have one primary entrypoint, and all other constructors call this one.
  - the only exception is for JSON and Info constructors, since they use special types.

### Anti-inheritance
- Minimal use of inheritance
  - [Model -> Domain -> EntityInfo] for the Info objects inside each Domain Role Object.
  - [IRepo -> Repo -> DomainRepo] for the Repo objects
  - [Role -> DomainRole] for the Role objects
- Minimal use of Interfaces
- One abstract class to define the Role Object class
  
### Static Methods
- Use of Static methods is severely limited to only those that are:
  - pure functions
  - no side effects
  - no access or creation of global state
  - used to create objects from JSON, XML, etc.

### Dumb Container Objects
- Dumb Container objects (DTO, Entity) are immutable and only used to pass data to/from outside domain
  to domain "Role" objects.
    - DTO's and Entities are still useful to maintain separation of concerns and to communicate with
      outside world, and allows independent updating and maintaining of the domain objects.

### Extremely Limit use of If/Else
  - Only for specific exceptional cases.
  - Normally check for conditions and return early if condition is not met.
  - Last return is always "happy path" success return (unless for rare exceptional cases.)

### Result Object for Errors & Exceptions
- Use of Result object to return success or failure, encapsulating the error message and exception.

### Design Patterns avoided
- No factory pattern
- No builder pattern
- No fluent interface

### Early Return
- yes on early return for error handling
- one success return at the end

### Single Responsibility of Role
- BOOP makes clear separation of concerns easy.



## Architecture
  - Model
    - Domain
      - Account - Handles library account details for the user, like fines, status, max books, etc. (NO BOOKS TRACKED HERE)
      - Book    - Handles book details, like title, author, source library, etc.
      - User    - Handles user details, like name, email, books held, can give books to other users, etc.
      - Library - Handles library details, lists of books on hand, users registered to it, checking books in and out,
    
    - Repo - Handles persistence of Info referenced by Domain objects
      - BookInfoRepo - Handles persistence of BookInfo objects
      - AccountInfoRepo - Handles persistence of AccountInfo objects
      - UserInfoRepo - Handles persistence of UserInfo objects
      - LibraryInfoRepo - Handles persistence of LibraryInfo objects
    - Entity
      - BookInfoEntity - Handles transfer of BookInfo objects to database
    - DTO
      - BookInfoDTO  - Handles transfer of BookInfo objects to 