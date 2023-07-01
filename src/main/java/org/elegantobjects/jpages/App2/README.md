# Library App using BOOP

## Design Goals

### Inspiration
- Have a pure domain layer that adheres to Alan Kay and Yegor Bugayenko OO styles
  - Note: BOOP is a design pattern that is inspired by Alan Kay's OO style & lectures, and Yegor Bugayenko's book Elegant Objects.

### Developer Experience
- Architected by layer, and each layer is grouped by feature, which allows convenient and
  easy to understand navigation of the code.
- Built to test from start
- Everything is fake-able (mock-able) and isolated for ease and speed of testing.

## Code Style

Attempting to make the Domain layer code as idiomatic as possible, and to make it easy to read and understand.
Strive to make it look like regular English, and to be able to read it without an IDE and make domain layer use regular java.

### Encapsulation via methods
- No setters
  - Only immutable copies of objects are returned
- No Direct Reference Getters 
  - No references to internal mutable objects (all fields are `final`)
  - Only return copies of information
  - Never reveal internal structures - always return curated copies for a specific purpose
  - Returning/Exposing `Role` objects or `id's` is OK
  - Never return `null`
    - Return an intention revealing object instead, like a `Boolean` or `Result` or `Empty` object.
- Methods that require network or CPU time should be labeled
  - ie: `calculateTotalCost()` is preferred over `getTotalCost()`
  - ie: `fetchInfo()` is preferred over `getInfo()`
- When the getter just returns a simple field, no need to use get.
  - Prefer `id()` over `getId()` for readability
  - ie: `info()` is preferred over `getInfo()`
  - ie: `id()` is preferred over `getId()`

### No Nulls in Domain
- Nulls only allowed to be passed in constructors
- Nulls are checked for in constructors only
- Nulls are not allowed to be returned from methods
  - return "Empty" objects instead
- Use sensible objects that show intent
  - ie: `PrivateLibrary` instead of a `null` `sourceLibrary`
- One exception: "null" is returned from fetches when no error has occurred. 
  - If there is an error, it is returned in a `Result` object.
  - This is for convenience, allows the fetch info and error handling to happen in one line.

### Intention Revealing Error Messages
- Error messages should be human-readable, clearly reveal the problem encountered
- include ids of associated object(s) in message.

### No Global State
- No static/global variables
- No accessing global state
- All dependencies passed in constructors

### All Objects Immutable
- All objects are immutable, except for the `UUID2` `id` and `UUIDType` values for all objects.
  - These are mutable because of how JSON imports work. 
    - The `id` must first be extracted from the JSON data before the new Object is created. 
    - The `UUIDType` is mutable because its not known at object creation time, and must be set after the JSON is parsed.
    - This is a known limitation, and I am unaware of a workaround that doesn't involve a lot of complexity.
- `Role` objects are immutable and communicate or contain other Role objects.
- `Role` objects contain references to their mutable data (Info) which is updated or fetched automatically when the
  role object is updated. 
  - This data is not exposed directly, only through `Role` or `DomainInfo` methods.
- *Yes, things are copied.* 
  - This is easier than dealing with the complexity of mutable objects.

### Constructor Convenience
- No Dependency Injection framework (_ugh..._)
  - All dependencies passed in constructor
- Many different constructors included for many different ways to create objects
- Singletons passed in constructor, held in Context object
- No `null` objects
  - `null` checks on the input to the constructor for special case constructor handling 
  - `Null` is normally intended to generate reasonable default values
- No thrown exceptions except for actual errors that are not expected.
  - ie: `User` object contains a `UserInfo` object, which is updated automatically when `User` is updated
- Constructors have one primary entrypoint, and all other constructors call this one.
  - the only exception is for JSON and Info constructors, since they use special types.

### Anti-inheritance
- Minimal use of inheritance
  - [Model -> Domain -> EntityInfo] for the `Info` objects inside each `Domain` `Role` Object.
  - [IRepo -> Repo -> DomainRepo] for the `Repo` objects
  - [Role -> DomainRole] for the `Role` objects
- Minimal use of Interfaces
  - only where needed for testing via fakes/mocks 
- One abstract class to define the `Role` Object class

### Shallow Hierarchies
- Keep hierarchies as flat as possible, as deep hierarchies are difficult to understand and maintain.
- If reasonable parameterized behavior can be captured in a Role, it is better than 2 or more classes.
  - example: [Library -> PrivateLibrary -> OrphanPrivateLibrary] vs [Library -> PrivateLibrary with `isOrphan` flag]
  - prefer the shallower hierarchy with the `isOrphan` flag. 
  
### No `Static` Methods (with extremely limited exceptions)
- Use of `Static` methods is severely limited to only those that are:
  - pure functions
  - have no side effects
  - used to create objects from JSON, XML, etc.
  - no modification or creation of global state

### Dumb Container Objects for Data Transfer
- Dumb Container objects (DTO, Entity) are immutable and only used to pass data to/from outside domain
  to domain "Role" objects.
    - DTO's and Entities are still useful to maintain separation of concerns and to communicate with
      outside world, and allows independent updating and maintaining of the domain objects.

### Extremely Limit use of `Else` blocks
  - Code for conditions check first and return early if condition is not met.
  - Last return is always "happy path" success return (unless for rare exceptional cases.)
  - Only for specific exceptional rare cases use `else` blocks.
    - Always ask if it can be written in a way that doesnt use `else`. 

### No `Void` Methods
  - All methods return something, even if its just a `Boolean` or `Result` object.

### No `Null` Checks
  - Avoid if any way possible any `null` checks in code

### Explicit Boolean Naming 
- Boolean variables and methods are named explicitly
  - `is{something}`
  - `has{something}`
  - Attempt to avoid using `!` operator
    - Avoid using `isNot{something}` or `hasNot{something}`
  - Attempt to convey intent
    - ie: `isPrivate` is preferred over `isNotPublic`
    - ie: `hasFines` is preferred over `isBalanceOverZero`

### Variable Naming with Explicit Types
  - The emphasis on reading without IDE assistance is important, and explicit type naming helps with this.
  - `{Domain}Id` vs `{Domain}` Types
      - Parameter names are explicit about if they are `{Domain}id` or `{Domain}` objects
    - Id 
      - Appending `Id` to the end of the parameter name is acceptable and encouraged
      - ie: `userId` is preferred over `user`
    - Domain
      - If the object is a `Domain` object, then the name should be `{Domain}` and not `{Domain}Id`
      - ie: `user` is preferred over `userId`
  - `Info` vs `Role` Types
    - Parameter names are explicit about whether they are `Info` or `Role` objects
    - Appending `Info` to the end of the parameter name is acceptable and encouraged
    - Using the plain `{Domain}` name is preferred if the object is a `Role` object

### Result Object for Errors & Exceptions
- Use of `Result` object to return success or failure
  - encapsulate the error message and `Exception`.
  - instead of throwing an `Exception`, return a `Result` object with the error message and `Exception`.

### Avoid C++/Java Design Pattern Hacks
- No factory patterns
  - Just use constructors. 
- No builder patterns
  - Create a new object modified copy from the old object, and return the new object. 
  - No need for a builder. 
  - Use `.with{someField}(...)` method to update `someField` field.
- No fluent interfaces
  - Use `.with{someField}(...)` to update `someField` field. 
- This architecture will update the info for the domain object automatically when the Role object Info field is updated.
  - No need for a separate `update()` method, in most cases.

### Use Early Return
- Multiple early returns for ease of error handling 
  - unhappy path errors return immediately
- one success return at the end

### Single Responsibility of Role
- BOOP makes clear separation of concerns easy and understandable.
- Each Role has a single responsibility, and only handles that responsibility, and delegates all other responsibilities
  to other Roles.
- Each Role has a many methods to handle its responsibility, and return encapsulated data to other Roles.
- No direct access to any other Role's data, all data is encapsulated and only accessed through methods.

### Guard Clauses
- Guard clauses are used to check for errors and return early if error is found.
- Basic data validation

## Architecture

- ### Data
  - `Model`
    - `DomainInfo`
      - `AccountInfo` - Handles library account details for the `User`, like fines, status, max books, etc. (NO BOOKS TRACKED HERE)
      - `Bookinfo`    - Handles book details, like title, author, source library, etc.
      - `UserInfo`    - Handles user details, like name, email, books held, can give books to other users, etc.
      - `LibraryInfo` - Handles library details, lists of books on hand, users registered to it, checking books in and out,
    
    - `Repo` - Handles persistence of `Info` referenced by `Domain` objects
      - `BookInfoRepo` - Handles persistence of `BookInfo` objects
      - `AccountInfoRepo` - Handles persistence of `AccountInfo` objects
      - `UserInfoRepo` - Handles persistence of `UserInfo` objects
      - `LibraryInfoRepo` - Handles persistence of `LibraryInfo` objects
    - `EntityInfo`
      - `BookInfoEntity` - Handles transfer of `BookInfo` objects to database
    - `DTOInfo`
      - `BookInfoDTO`  - Handles transfer of `BookInfo` objects to API
- ### Role
  - `User` - Handles `User` actions, like `giveBookToUser`, `checkoutBook`, `checkinBook`, etc.
    - `User` contains an `Account` object, which contains an `AccountInfo` object.  
    - `Account` - Handles `Account` actions, like paying fines, checking status, etc.
  - `Library` - Handles `Library` actions, like `checkoutBook`, `checkinBook`, `isKnownBook` etc.
  - `Book` - Handles `Book` actions like changing `title`, `author`, `description`, `sourceLibrary`, etc.
      
## Design decisions

Some decisions have been made capricious and mandatory to see how much domain complexity can be modeled using BOOP.

### Books & Libraries 
- Possession of a `Book` are primarily by the `User` and partially tracked by the `Library`. 
  - This is because the `User` is the one who checks out the `Book`, and the `User` is the one who returns the `Book`. 
  - The `Library` is only the intermediary between the `User` and the `Book`.
- `Books` are not tracked by the `Account`, because the `Account` is only for the `User`.
  - The `User` is the one who checks out the `Book`, and the `User` is the one who returns the `Book`.
- `Book` possession is partially tracked by the lending `Library`, because the `Library` is only the intermediary between the
  `User` and the `Book`. `Libraries` track which `BookId` was checked out by which `UserId`.
- `Users` can only check out books from Libraries that they are registered to.
- `Users` hold the books that they have checked out, and can give them to another `User`.
- If a `User` gives a `Book` to another `User`, the `Book`'s `checkout` is transferred to the other `User`, 
  only if the other `User` is registered & `Account` is in `Good Standing` with the `Library` that the `Book` was checked out from.

### Private Libraries & Books with Orphan Private Libraries
- A `PrivateLibrary` is a sub-type of `Library` that is used to track `Books` that are not associated with any System `Library`.
- A `Book` can exist without being associated with any system `Library`. 
  - These `Books` have a `PrivateLibrary` for their `sourceLibrary`.
- `Users` can `checkout` `Books` from `PrivateLibraries` just like any other `Library`.
- `Orphan` `PrivateLibraries` are automatically created when a `Book` is created without any associated existing `Library`. 
  - The `id` for an `Orphan` `PrivateLibrary` is generated at the time of the creation of the `Book`.
  - `Orphan` `PrivateLibraries` can only have one `Book` checked in, and it has to be the same `BookId` that it was created with.
  - #### Why?
    - We allow this as a part of the design, because we want to allow the creation of `Books` without having to 
      create a `Library` first. 
    - This is an arbitrary decision to see if I could stretch how the system works, and is not a 
      real world use case. 
    - In a normal Library system, all `Books` would be associated with a `Library` at the time of creation.