# Library App using BOOP Style

## Design Goals

### Inspiration

- Have a pure domain layer that adheres to Alan Kay's and Yegor Bugayenko's OO styles
  - BOOP stands for "Back-to Object Oriented Programming" or "Bugayenko Object Oriented Programming"
  - Writing code that is easy to change & comprehend quickly using English prose.
  - Back to original OO conceptual basics approach for Java coding style in the Domain layer for Role objects.
  - Built to have any Role object be easily separated into an independently horizontally scalable. (ie: microservice)

- BOOP is a design pattern that is inspired by:
  - Alan Kay's OO style & lectures, HyperCard, the ideas behind Smalltalk.
  - Yegor Bugayenko's lecture series on OOP and book Elegant Objects.
  - David West, PhD's book "Object Thinking"

### Contents

- [Developer Experience is Paramount](#developer-experience-is-paramount)
- [Avoiding Ugly COP Paradigms](#avoiding-ugly-cop-paradigms)

#### Code Style & Rules

- [Code Style](#code-style)
- [Encapsulation of Data via Intention-named methods](#encapsulation-of-data-via-intention-named-methods)
- [No `null` in Domain](#no-null-in-domain)
- [Intention Revealing Error Messages](#intention-revealing-error-messages)
- [No Shared Global State](#no-shared-global-state)
- [No Dependency Injection Framework](#no-dependency-injection-framework)
- [Constructor Convenience](#constructor-convenience)
- [Anti-inheritance](#anti-inheritance)
- [Shallow Hierarchies](#shallow-hierarchies)
- [No Static Methods](#no-static-methods)
- [Dumb Container Objects for Data Transfer Only to/from Domain](#dumb-container-objects-for-data-transfer-only-tofrom-domain)
- [Extremely Limit use of `Else` blocks](#extremely-limit-use-of-else-blocks)
- [No `Void` Methods](#no-void-methods)
- [No `Null` Checks](#no-null-checks)
- [Synchronous Code](#synchronous-code)
- [Encourage Explicit Boolean Naming](#encourage-explicit-boolean-naming)
- [Encourage Variable Naming with Explicit Types](#encourage-variable-naming-with-explicit-types)
- [Use Result Object for Errors & Exceptions](#use-result-object-for-errors--exceptions)
- [Avoid C++/Java Design Pattern Hacks](#avoid-cjava-design-pattern-hacks)
- [Prefer Use of Early Return](#prefer-use-of-early-return)
- [Single Responsibility of Role](#single-responsibility-of-role)
- [Reverse-scope-naming Style](#reverse-scope-naming-style)
- [Naming of "Inverse" methods](#naming-of-inverse-methods) 
- [Explicit Naming of "Transfer" methods](#explicit-naming-of-transfer-methods)
- [Explicit Naming of "Find" methods](#explicit-naming-of-find-methods)
- [Explicit Naming of "Maps" and "Lists"](#explicit-naming-of-maps-and-lists)
- [Guard Clauses](#guard-clauses)
- [Domain Role Object can create other Domain Role Objects](#domain-role-object-can-create-other-domain-role-objects)
- [Minimal Annotations](#minimal-annotations)
- [Acceptable Acronyms, Prefixes, and Suffixes](#acceptable-acronyms-prefixes-and-suffixes)

#### Sample Use-case Library Application Implementation Details

- [Library Application Details](#architecture)
- [Arbitrary Domain Design decisions](#arbitrary-domain-design-decisions)
- [Some Arbitrary Rules](#some-arbitrary-rules)
- [Flat Hierarchies](#flat-hierarchies)
- [More Arbitrary Rules for: Domain User, Book, Library, Account](#more-arbitrary-domain-user-book-library-account-rules)
- [Private Libraries & Books with Orphan Private Libraries](#private-libraries--books-with-orphan-private-libraries)

### Developer Experience is Paramount

- Write code in a way that is oriented to the reader (not computer), as code is read 100x more than it is written,
  and computers really don't care what the code looks like.
- The developer experience is paramount, and should be the primary focus of the design.
- Architected by layer, and each layer is grouped by feature
- Allows convenient and easy to comprehend navigation of the code.
  - One downside, the hierarchy is separated into different folders, so it's not obvious what the hierarchy is.
  - To remedy this, documentation about the data hierarchy should exist near the code (maybe a README). 
- Built to test from start to finish, with no external dependencies.
- Everything is fake-able (mock-able) and isolated for ease and speed of testing.
<br>

#### Avoiding Ugly COP Paradigms

Class Oriented Programming (COP) is a style of programming that seems to be primarily focused 
around continuing to use old procedural/imperative styles leftover from C and C++, but with
Class "wrappers" instead of just data structures, files and functions (procedures) like in C.
Much of what most people call OOP is actually just COP, and is not actually OOP at all. It's important
to know the difference, because the two styles are very different, and have very different
advantages and disadvantages.

- BOOP seeks to entirely <b>avoid</b> the COP (Class Oriented Programming) paradigms & idioms, such as:<br>
  - Using Classes as dumb data containers, with no methods or minimal methods.
  - Using Classes as name space for static methods, with no associated data.
  - Using static methods to modify objects data directly.
  - Using static methods and static variables to avoid having to create objects.
  - Exposing internal data structures and mutable objects.
  - Allowing `null` to be returned from methods.
  - `Null` checks everywhere.
  - Allowing multiple shared access to static global variables/state.
  - Lots of inheritance, and deep inheritance hierarchies.
  - Lots of "Design Patterns" to solve problems that should not exist in the first place.
  - Factories, Builders, AbstractFactoryFactories and other hacky "creational" patterns.

## Code Style

- Prevent <b>"Whats this for?"</b> and <b>"What does that do?"</b> questions by using explicit 
  intention-revealing names, pedantically for everything.
- Prefer verbosity of descriptions to brevity of code. Should always be conveying intent as dense as possible 
  but still readable English.
- Risk pedantic naming over brevity of code. Strive to convey meaning as densely as possible, 
  but not at the expense of clarity.
  - You may think you know what a variable/method is for, but the next person may not.
  - Yes, this risks job security, but it also makes it easier to change code as you keep.
  - If you think someone will be confused by something, take extra time choosing names and add the minimum amount 
    of comments to explain <b>WHY?</b> This is also the value of pair programming, someone else can ask you why 
    you did something. Instead of just telling them, that's a place to find better names, or refactor the code to 
    make it more clear, or add a "why?" answer comment.
- Even this short guide repeats ideas, to make it easier to understand what is important and what is not.
- Strive to make code read like regular English as possible, and to be able to understand it without 
  using IDE tools (like cursor-hover to find var types).
    extending the code base. We risk improving the developer experience for our own sake.
- Limit language to plain-old Java 8
- Strive to write Domain layer code in plain-old idiomatic Java as much as possible, and read like English prose.
- A person who doesn't code should be able to look at a method or variable and know what it does/means.
- Some of these ideas are contradictory, and those are the ones that require more thought and consideration for the situation.

### Encapsulation of Data via Intention-named methods

  - Set and Get methods are not used, instead methods are named for their intention.
  - Problem: The English word `set` and `get` are _extremely_ generic 
    - Hundreds of definitions, each with many subtle different meanings, based on context.
    - `set` and `get` do not reveal the underlying intention of the method. 
    - It's a very convenient shorthand for the code-writer and always confusing for the code-reader.<br> 
      Requires investigation into what is actually going on, specifically network, CPU or disk access.
- No setters
  - All changes must be made via intention-named methods.
  - Only immutable copies of objects are returned, for read-only purposes.
- No Direct Reference Getters 
  - No references to internal mutable objects (all fields are `final`)
  - Only return copies of information.
  - Never reveal internal structures - always return _curated_ copies for a specific intended purpose.
  - Returning/Exposing `Role` objects or `id's` is OK.
    - `Role` objects are immutable, and contain references to their mutable `Info` data.
    - `id` is immutable, and is used to fetch Info objects from their respective Repositories.
  - Should never return `null`
    - Return an intention-revealing object instead
    - Prefer `Result` or `Empty` object instead of `null`
    - `boolean` is acceptable, over `null`.
- Methods that require network, disk access or CPU time should be labeled with that intent.
  - Prefer `calculateTotalCost()` over `getTotalCost()`
  - Prefer `fetchInfo()` over `getInfo()`
  - Prefer `findUserIdOfCheckedOutBook` over `getCheckedOutBookUserId()`
- When the method is a simple data-accessor that just returns a simple field or object, no need to use prefix `get`.
  - Prefer `id()` over `getId()`
  - Prefer  `info()` over `getInfo()`
  - Prefer  `sourceLibrary()` over `getSourceLibrary()`

### No `null` in Domain

- `null` only allowed to be passed in constructors
  - used to indicate <i>"use a reasonable default value for this parameter"</i>
- `null` is checked for in constructors only, usually to create a reasonable default value.
- `null` is not allowed to be returned from methods in the Domain.
  - Prefer to `return` "Empty" or `Result` objects instead of `null`.
- Use intention-named objects that indicate the reason for a `null` case.
  - ie: `PrivateLibrary` instead of a `null` for an "unknown" system Library object.
  - This can often become awkward to describe in English, so the use of `null` must always be questioned.

- <b>Important Exception:</b>
  - `null` is returned from `fetchInfo()` when no error has occurred. 
  - If there is an error, it is returned in a `Result` object.
  - This is for convenience: 
    - It allows the `fetchInfo()` and error handling to be a single line.
  - `null` still used outside of Domain, but prefer to limit its use in general.

### Intention Revealing Error Messages

- Error messages should be human-readable, clearly reveal the issue encountered.
- include `id` of associated object(s) in message or useful data for the issue.
- This is to prevent guessing and hunting what the cause of the issue may be.
- The unhappy path is the more complex path, so doing this helps reduce its complexity.

### No Shared Global State

- No shared mutable state
- No static/global variables
- No global accessing state of App (except via passed-in Context object)

### No Dependency Injection Framework

- All dependencies passed in constructors
- Singleton objects reside in the Context object, and are passed in constructors.

### All Objects Immutable
- All objects are immutable, except for the referred `Info` objects.
  - Important exception:
    - The `UUID2` `id` and `UUIDType` values for all objects are kept mutable due to limitations of java:
    - These are mutable because of how JSON imports work. 
      - The `id` must first be extracted from the JSON data before the new Object is created. 
      - The `UUIDType` is mutable because it is not known at object creation time, and must be set after the JSON is parsed.
      - This is a known limitation, and I am unaware of a workaround that doesn't involve a lot of complexity.
      - We keep the id private, and only expose it via `id()` method.
      - The setter function is public but noted with a `_` prefix to indicate its special case.
- `Role` objects are immutable and communicate or contain other Role objects.
- `Role` objects contain references to their mutable data (Info) which is updated or fetched automatically when the
  role object is updated. 
  - This data is not exposed directly, only through `Role` or `DomainInfo` methods.
- *Yes, things are copied.* 
  - This is easier than dealing with the complexity of mutable objects.
  - It is easier than working with threads.
  - Optimizations can be made later if needed, the architecture is designed to easily allow for this.

### Constructor Convenience

- All dependencies passed in constructor
  - No Dependency Injection framework (_anyone want to google a thermosiphon?..._)
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

- Minimal & shallow use of inheritance
  - <code>Model ➤➤ {Domain} ➤➤ {Entity}{Domain}Info</code> for the `Info` objects inside each `Domain` Object.
    - ie: <code>Model.DTOInfo.DTOBookInfo</code>
    - ie: <code>Model.DomainInfo.BookInfo</code> 
      - note:`BookInfo` is <i>not</i> a `DomainBookInfo` because the Domain is the core and more plain java-like, 
        so we use a simplest name for it.
  - <code>IRepo ➤➤ Repo ➤➤ {Domain}Repo</code> for the `Repo` objects
    - ie: <code>Repo.BookInfoRepo</code> 
  - <code>Role ➤➤ {DomainRole}</code> for the `Role` objects
    - ie: <code>Role.Book</code>
- Minimal use of Interfaces
  - only where needed for testing via fakes/mocks 
- One abstract class to define the `Role` Object class
- Be very wary of any attempts to `generic-ify` using `abstract class`. 
  - Prefer extending concrete classes or Duplicating code at least 3 times
  - Or find yourself having to make the same change in multiple places too often
  - Prefer comprehension over "lets make this generic, but add special cases"
- Model.Domain.Entity class should be 3 levels MAX, unless a very unusual case. 
  - You should be able to keep it as flat as possible
    - use packages to put the objects in the appropriate places, usually together with the feature.
    - Keep the Class Inheritance simple, and allow the package arrangement can be complex.

### Shallow Hierarchies

- Keep hierarchies as flat as possible, because deep hierarchies are difficult to understand and change.
- If reasonable parameterized behavior can be captured in a `Role`, it is preferred over creating 2 or more classes.
  - example:
    - [Library ➤➤ PrivateLibrary with `isOrphan` flag] &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    ⬅︎ shallow is preferred
    - vs
    - [Library ➤➤ PrivateLibrary ➤➤ OrphanPrivateLibrary]
  - prefer the shallower hierarchy with the `isOrphan` flag.
  
### No `Static` Methods

- Use of `Static` methods is severely limited to only those that are:
  - Pure functions ie: have no side effects that change data outside the function.
  - Used to create objects from JSON, XML, another object, the network, etc.
  - No modification or creation of global state
  - No modification of any state outside the function

### Dumb-Container-Objects for Data Transfer Only to/from Domain

- Dumb Container objects (`InfoDTO`, `InfoEntity`) are immutable and only used to pass data to/from outside domain
  to domain `Role` objects.
- Note: DTOs and Entities are still useful to maintain separation of concerns and to communicate with
  world outside domain, and allows independent changing and versioning of the domain/DTO/Entity objects.

### Extremely Limit use of `Else` blocks

  - Code for conditions check first and return early if condition is not met.
  - Last return is always "happy path" success return (unless for rare exceptional cases.)
  - Only for specific exceptional rare cases use `else` blocks.
    - Always ask if it can be written in a way that doesn't use `else`. 

### No `Void` Methods

  - All methods return something, even if it's just a `Boolean` or `Result` object.

### No `Null` Checks

  - Avoid if any way possible any `null` checks in code

### Synchronous Code

  - Keep code as synchronous as possible, or looking synchronous.
  - If callbacks are needed, they should be wrapped to look synchronous.

### Encourage Explicit Boolean Naming

- Boolean variables and methods are named explicitly
  - `is{something}`
  - `has{something}`
  - `should{something}` - use sparingly for parameters, consider using `enum` instead.
  - Attempt to avoid using `!` operator
    - OK to use `isNot{something}` over the `!` operator if it makes the code more readable.
    - Avoid blindly creating `isNot{something}` or `hasNot{something}` just to oppose each positive 
      case, only create a "negative case" method when it is needed, not just automatically.
  - Attempt to convey intent
    - ie: `isPrivate` is preferred over `isNotPublic`
    - ie: `hasFines` is preferred over `isBalanceOverZero`

### Encourage Variable Naming with Explicit Types

  - Slight nod to Hungarian Notation, it is still useful for readability in limited cases.
  - The emphasis on reading without IDE assistance is important, and explicit type naming helps with this.
  - `{Domain}Id` vs `{Domain}` Types
      - Parameter names are explicit about if they are `{Domain}id` or `{Domain}` objects
    - `Id` 
      - Appending `Id` to the end of a UUID2<> type variable name is acceptable and encouraged
      - ie: `userId` is preferred over `user` or  plain `id`
    - Domain
      - If the object is a `Domain` object, then the name should be `{Domain}` and not `{Domain}Id`
      - ie: `user` is preferred over `userId` in this case.
  - `Info` vs `Role` Types
    - Parameter names are explicit about whether they are `Info` or `Role` objects
    - Appending `Info` to the end of the parameter name is acceptable and encouraged
      - ie: `userInfo` is preferred over `user` or plain `info`
    - Using the plain `{Domain}` name is preferred if the object is a domain `Role` object
      - ie: `user` is preferred over `userInfo` in this case.

### Use Result Object for Errors & Exceptions

- Use of `Result` object to return success or failure
  - Encapsulate the error message in an `Exception` object.
  - Use instead of throwing an `Exception`, return a `Result` object with the error message and `Exception`.
- Avoid returning null or raw values
  - Use `Result` object to return success or failure
  - Encapsulate the error message in an `Exception` object.
  - Use instead of throwing an `Exception`, return a `Result` object with the error message and `Exception`.

### Avoid C++/Java Design Pattern Hacks

- Java has inherited many bad ideas from C, C++ and other languages. 
- Many of the ideas were so bad that a common set of workarounds were created and passed around the community 
  (or discovered independently). These eventually became "industry standard" which slowly turned into "best practices."
- These were then catalogued in many books, sold in lectures and conferences, made into clever repeatable acronyms,
  and eventually became a "gospel holy truth" and assumed "just the way it's done", even though it was often taught
  without any explanation of why it was done that way, or proof any of it works optimally, or even works at all.
- Turns out many of the patterns were after-thought workarounds to fundamental language design flaws, directly
  inherited from C++ and C (and other languages) that never were resolved properly much less questioned.
- We know this now because recent language versions have remediated <i>some</i> of these issues, and other languages 
  like Kotlin illustrate how to address these flaws in a more sane, comprehensible and maintainable manner. 
- Combined with BOOP, many of the popular design patterns just don't make sense and add unnecessary 
  complexity and confusion. <i>But it did pay a lot of presenters and authors bills for a long time!</i>

#### Examples

- No Factory patterns
  - Just use constructors. 
- No Builder patterns
  - Create a new object modified copy from the old object, and return the new object. 
  - No need for a builder. 
  - Use `.with{someField}(updatedValue)` method to update `someField` member field.
- No Fluent interfaces
  - Use `.with{someField}(updatedValue)` to update each `someField` member field.
- Interestingly, limited use of the hated `Singleton` and `Repository` patterns do actually fit well with BOOP 
  and are encouraged. <i>Even a broken clock is right twice a day!</i>

### Prefer Use of Early Return

- Multiple early `returns` for ease of error handling 
  - Unhappy path errors `return` immediately
- One success `return` at the end is preferred.
- Note: multiple Success `returns` are acceptable, but discouraged. 
  - Maybe break up into 2 functions?

### Single Responsibility of Role

- BOOP makes clear separation of concerns easy and understandable.
- Each Role has a single responsibility, and only handles that responsibility, and delegates all other responsibilities
  to other Roles.
- Each Role has a many methods to handle its responsibility, and return encapsulated intention-revealing data to 
  other Roles.
- No direct access to any other Role's data, all data is encapsulated and only accessed through methods.
- All Role Info is returned as copies, never direct references.
  - This makes it possible to have a Role change independently of other Role objects. By defining communication
    protocols via methods.
- This architecture will update the info for the domain object automatically when the Role object Info field is updated.
- No need for a separate `update()` method, in most cases.

### Reverse-scope-naming Style

- Starts with the most specific adjective to more general adjectives, and ends with the name of the actual concrete type.
- Domain objects are the plainest named.
  - ie: `User` and `Account`
- Subtypes are always given an adjective name that differentiates it
  - ie: `Library` and `PrivateLibary`
- If it's a generic item, adding a descriptor is encouraged.
  - ie: `accountStatus` is preferred over `status`
  - ie: `currentFine` is preferred over `fine`
- Concrete item is at the end of the name
  - ie: `maxAcceptedPennies` instead of `maxPenniesAccepted`
    - we want to refer to the  `Pennies` not `Accepted`s (whatever those are!)
  - ie: `accountAuditLog` vs `log`
    - We know it's a Log. 
    - What kind of log? An Audit log. 
    - What kind of audit log? An Account Audit Log. 
- It is acceptable and preferred to chain more precise adjectives in the name first and 
  move to more general adjectives.
  - ie: `OrphanPrivateLibrary` is preferred over `Orphan`
  - ie: `updatedAccountStatus` is preferred over `updated` or `status`

### Naming of "Inverse" methods

- Prefer using same verb and a short modifier than to use two different verbs for inverse/opposite methods.
- ie: Prefer `CheckIn` and `CheckOut` to `Borrow` and `Return`
- ie: Prefer `Register` and `UnRegister` to `register` and `delete` (or `remove`)
- ie: Prefer `Suspend` and `UnSuspend` to `suspend` and `reinstate`
- ie: Prefer `Activate` and `DeActivate` to `activate` and `suspend` (or `disable`)
  - Exceptions:
  - For CRUD operations, it is acceptable to use standard opposite terms: `create`, `add`, `insert`, `delete`
  - `Close` and `Open` are preferred over `Open` and `UnOpen` (unless the domain specifies it)
  - `Push` and `Pop` are preferred over `Push` and `UnPush` (unless the domain specifies it)

### Explicit Naming of "Transfer" methods


- Use of `From` and `To` encouraged, to show explicit intent.
  - ie: `checkOutBookToUser` is preferred over `checkOut` or `checkOutBook`
  - ie: `transferBookSourceLibraryToThisLibrary` is preferred over `transferBook`
    - yes, it's wordier, but leaves no doubt as to what is going on. 
- Use of `By` if there is an authorization, or a delegate.
  - ie: `activateAccountByStaff` is preferred over `staffActivateAccount`
  - ie: `findAllCheckedOutBooksByUserId` is preferred over `findAllUserIdCheckedOutBooks`
    - even though both convey the same meaning, one is easier to comprehend in English.

### Explicit Naming of "Find" methods


- Use of `Of` is encouraged
 - ie: `findUserIdOfCheckedOutBook` instead of `findCheckedOutBookUserId`
 - even though both convey the same meaning, one is easier to read in English.

### Explicit Naming of "Maps" and "Lists"

- List the `from` type and the `to` type in the name of the map.
- It is preferred to use `To` between the `from` and `to` types.
- It is preferred to add `Map` or `List` at the end of the variable names.
- ie: `acceptedBookIdToSourceLibraryIdMap` is preferred over `acceptedBooks`
- ie: `timeStampToAccountAuditLogItemMap` is preferred over `auditLog`
- This makes the JSON data easy to read and understand out of context.
- `List` can refer to Arrays or a "single column" data.
- `Map` can refer to any Map or "two column lookup" data.

### Guard Clauses

- Guard clauses are used to check for errors and return early if error is found.
- Basic data validation

### Domain Role Object can create other Domain Role Objects

- This is acceptable for Domain objects!
  - They all can instantiate themselves & others.
  - They all can be instantiated by others.
  - Their `Info` gets pulled in from their Repository on demand with a call to `info()`.
  - `Role` objects are essentially smart pointers to their `Info` objects & other `Role` objects.

### Minimal Annotations

- Annotations are used sparingly, and only for the most important things, like @NotNull, @Override, @Suppress, etc.

### Acceptable Acronyms, Prefixes, and Suffixes

<table>
  <tr>
    <th>Acronym</th>
    <th>Preferred Use</th>
  </tr>
  <tr>
    <td> 
      <code>ctx</code>
    </td>
    <td>
      Acceptable to use in place of <code>context</code>.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Id</code>
    </td>
    <td>
      for <code>Id</code>'s
    </td>
  </tr>
  <tr>
    <td> 
      <code>Info</code>
    </td>
    <td>
      For Classes that contain the Info for the <code>Role</code> Class internal information.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Repo</code>
    </td>
    <td>
      For Repository Classes.
    </td>
  </tr>
  <tr>
    <td> 
      <code>DTO</code>
    </td>
    <td>
      Prefix for "Data Transfer Object" classes.
    </td>
  </tr>
  <tr>
    <td> 
      <code>num</code>
    </td>
    <td>
      Prefix for counts or amounts, or other integer intentions that are sum-like.
    </td>
  </tr>
  <tr>
    <td> 
      <code>max</code> & <code>min</code>
    </td>
    <td>
      Prefix for limits on ranges.
    </td>
  </tr>
  <tr>
    <td> 
      <code>cur</code>
    </td>
    <td>
      Prefix for <code>current</code> is gray area.<br>
      • Prefer spelling out unless too pedantic for a local context.<br>
      • Indicates the current value for the object.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Amt</code> & <code>Amnt</code>
    </td>
    <td>
      These are in a gray area.<br>
      • Prefer spelling it out, ie: <code>Amount</code>.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Ct</code> & <code>Cnt</code>
    </td>
    <td>
      These are too vague and one is mildly rude in English.<br>
      • Prefer spelling it out, ie: <code>Count</code>.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Kind</code>
    </td>
    <td>
      Use in <code>enums</code> instead of the word <code>Type</code> which is 
      reserved specifically for the clazz <code>Class&lt;?&gt;</code> types.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Str</code>
    </td>
    <td>
      • Append to a string variable name that represents a specific type.<br>
      • ie: <code>UUID2TypeStr</code>> is preferred over <code>UUID2Type</code>.<br>
      <br>
      <i>Reasoning:</i> Because casual reading of the type name <code>UUID2Type</code> 
      could be easily misunderstood for a <code>"clazz"</code> of 
      <code>Class&lt;UUID2&lt?&gt&gt</code> in plain reading of the name in code. Without the 
      <code>Str</code> at the end, you would need to take an extra step to look up the actual type.
    </td>
  </tr>
</table>

# Library Application Details

You can find the sample Library App in the App2 folder, along with some tests to show functionality.


## Architecture

- ### Data
  - `Model`
    - `DomainInfo`
      - `AccountInfo` - Handles library account details for the `User`, like fines, status, max books, etc. 
        - Note: NO BOOKS TRACKED HERE, only account details and limits for a `User` for all Libraries in the system.
      - `BookInfo`    - Handles book details, like title, author, source library, etc.
      - `UserInfo`    - Handles user details, like name, email, books held, can give books to other users, etc.
      - `LibraryInfo` - Handles library details, lists of books on hand, users registered to it, checking books in and out,
    
    - `Repo` - Handles persistence of `Info` referenced by `Domain` objects
      - `BookInfoRepo` - Handles persistence of `BookInfo` objects
      - `AccountInfoRepo` - Handles persistence of `AccountInfo` objects
      - `UserInfoRepo` - Handles persistence of `UserInfo` objects
      - `LibraryInfoRepo` - Handles persistence of `LibraryInfo` objects
    - `EntityInfo`
      - `BookInfoEntity` - Handles transfer of `BookInfo` objects to database 
      - currently only one `Entity` in the system, to be expanded later to 
        include `AccountInfoEntity`, `UserInfoEntity`, `LibraryInfoEntity`
    - `DTOInfo`
      - `BookInfoDTO`  - Handles transfer of `BookInfo` objects to API
      - currently only one `DTO` in the system, to be expanded later to 
        include `AccountInfoDTO`, `UserInfoDTO`, `LibraryInfoDTO`
      
- ### Role
  - `User` - Handles `User` actions, like `giveBookToUser()`, `checkOutBook()`, `checkInBook()`, etc.
    - `User` contains an `Account` object, which contains an `AccountInfo` object.  
    - `Account` - Handles `Account` actions, like paying fines, checking account status, checking limits etc.
  - `Library` - Handles `Library` actions, like `checkoutBook`, `checkinBook`, `isKnownBook` etc.
  - `Book` - Handles `Book` actions like changing `title`, `author`, `description`, `sourceLibrary`, etc.

## Arbitrary Domain Design decisions

### _"BOOP is made for modeling the capriciousness of the Real World..."_ 

Some decisions have been made capriciously and with intentionally irrationally haphazard in order 
to see how much inherent domain complexity can be modeled using BOOP. This is not a criticism of BOOP, but 
rather a test of its flexibility and power.

#### Some Arbitrary Rules:

- Like the fact that `Users` of the system can have `PrivateLibrary`, and can give `Books` to other `Users`.
- Or a `User` can create their own book and add it to their library first, then to a public library.
- Or a `User` can "find" a book, and add it to their `PrivateLibrary`, and then give it to a public library. 
- So some books need to respect system Account rules, and others don't.
- A normal library system would likely _not_ be set up like this, but would also have other arbitrary rules that would
  need to be modeled with minimal additional code complexity.
- Things could be set up differently to simplify the Domain
  - like not allow `Users` to have `PrivateLibrary`, or not allow `Users` to give `Books` to other `Users`.
  - But that would be less fun, and less of a test of BOOP's flexibility and power.

#### Flat Hierarchies

- In order to keep the hierarchies flat and adhere to other design considerations, some decisions break
  the strict BOOP paradigm and opt for a functional or procedural approach internal to Role objects.
  - the further away from the `Domain` core layer, the more functional/procedural the code becomes. 
- `Role` objects themselves adhere strictly to BOOP when interacting in the `Domain` layer.

### More Arbitrary Domain User, Book, Library, Account Rules

- Possession of a given `Book` is primarily tracked by the `User` and partially tracked by the `Library`. 
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

- A `PrivateLibrary` is a subtype of `Library` that is used to track `Books` that are not associated with any System `Library`.
- A `Book` can exist without being associated with any system `Library`. 
  - These `Books` have a `PrivateLibrary` for their `sourceLibrary`.
- `Users` can `checkout` `Books` from `PrivateLibraries` just like any other `Library`.
- `Orphan` `PrivateLibraries` are automatically created when a `Book` is created without any associated existing `Library`. 
  - The `id` for an `Orphan` `PrivateLibrary` is set at the time of the creation of the `Book` to be the same `id` as the `Book`.
  - `Orphan` `PrivateLibraries` can only have one `Book` checked in, and it has to be the same `BookId` that it was created with.
- #### Why?
  - We allow this as a part of the design, because we want to allow the creation of `Books` without having to 
    create a `Library` first. 
  - This is an arbitrary decision to see if I could stretch how the system works, and is not a 
    real world use case. 
  - In a well-conceived "normal" Library system, all `Books` would be associated with a particular 
    system `Library` at the time of creation.

### My Java Complaints
- Type system... for a language that is dealing with types, it sure does forget types a lot... design flaw!
- Wow, the verbosity is outlandish and quite pedantic and very irregular syntax defining using generic types
  for class or functions.