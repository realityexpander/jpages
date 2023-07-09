package org.elegantobjects.jpages.LibraryAppTest;

import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.Context;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;
import org.elegantobjects.jpages.LibraryApp.domain.user.User;
import org.junit.Before;
import org.junit.Test;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * UUID2Test - Unit tests for UUID2 class.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */
public class UUID2Test {
    Context ctx;
    UUID2.HashMap<UUID2<Book>, UUID2<User>> uuid2ToEntityMap;
    UUID2<Book> book1;
    UUID2<Book> book2;
    UUID2<User> user01;
    UUID2<User> user02;

    @Before
    @SuppressWarnings("EmptyMethod")
    public void setUp() {
        // no-op
    }

    private void setUpUuid2HashMapTest() {
        ctx = LibraryAppTest.setupDefaultTestContext();

        uuid2ToEntityMap = new UUID2.HashMap<>();
        book1 = new UUID2<>(UUID2.createFakeUUID2(1200, Book.class));
        book2 = new UUID2<>(UUID2.createFakeUUID2(1300, Book.class));
        user01 = new UUID2<>(UUID2.createFakeUUID2(1, User.class));
        user02 = new UUID2<>(UUID2.createFakeUUID2(2, User.class));

        uuid2ToEntityMap.put(book1, user01);
        uuid2ToEntityMap.put(book2, user02);
    }

    @Test
    public void UUID2_deserialized_string_is_Correct() {
        // • ARRANGE
        UUID2<Book> book1200Id = UUID2.createFakeUUID2(1200, Book.class);
        String expectedUUID2Str = "UUID2:Role.Book@00000000-0000-0000-0000-000000001200";

        // • ACT
        String book1200Uuid2Str = book1200Id.toString();

        // • ASSERT
        assertEquals("UUID2 String not serialized correctly",
                book1200Uuid2Str, expectedUUID2Str);
    }

    @Test
    public void Create_new_UUID2_from_UUID2_String_are_Equal() {
        // • ARRANGE
        UUID2<Book> book1200Id = UUID2.createFakeUUID2(1200, Book.class);
        String book1200UUID2Str = book1200Id.toString();

        // • ACT
        @SuppressWarnings("unchecked")
        UUID2<Book> book1200aId = (UUID2<Book>) UUID2.fromUUID2String(book1200UUID2Str);

        // • ASSERT
        assertEquals(book1200Id, book1200aId);
    }

    @Test
    public void Create_new_UUID2_from_another_UUID2_are_Equal() {
        // • ARRANGE
        UUID2<Book> book1200Id = UUID2.createFakeUUID2(1200, Book.class);

        // • ACT
        UUID2<Book> book1200aId = new UUID2<>(book1200Id);

        // • ASSERT
        assertEquals(book1200Id, book1200aId);
    }

    @Test
    public void UUID2_values_with_equal_UUIDs_using_onlyUUIDEquals_are_Equal() {
        // • ARRANGE
        UUID2<Book> book1200Id = UUID2.createFakeUUID2(1200, Book.class);
        UUID2<User> user1200Id = UUID2.createFakeUUID2(1200, User.class);
        UUID2<User> user9999Id = UUID2.createFakeUUID2(9999, User.class);


        // • ACT
        boolean isEqual = book1200Id.isOnlyUUIDEqual(user1200Id);
        boolean isNotEqual = book1200Id.isOnlyUUIDEqual(user9999Id);

        // • ASSERT
        assertTrue(isEqual);
        assertFalse(isNotEqual);
    }

    @Test
    public void Equal_UUID2_values_are_Equal() {
        // • ARRANGE
        UUID2<Book> book9999Id = UUID2.createFakeUUID2(9999, Book.class);
        UUID2<Book> book9999Ida = UUID2.createFakeUUID2(9999, Book.class);

        // • ACT
        assertEquals(book9999Id, book9999Ida);
    }

    @Test
    public void Get_UUID2HashMap_item_is_Success() {
        // • ARRANGE
        setUpUuid2HashMapTest();

        // • ACT
        UUID2<User> user = uuid2ToEntityMap.get(book1);

        // • ASSERT
        ctx.log.d(this, "simple retrieval, user=" + user);
        assertNotNull(user);

    }

    @Test
    public void Get_UUID2HashMap_item_using_new_UUID2_is_Success() {
        // • ARRANGE
        setUpUuid2HashMapTest();
        UUID2<User> user = uuid2ToEntityMap.get(book1);
        UUID2<Book> book1a = UUID2.createFakeUUID2(1200, Book.class);

        // • ACT
        UUID2<User> user2 = uuid2ToEntityMap.get(book1a);

        // • ASSERT
        ctx.log.d(this, "retrieved using new id, user=" + user);
        assertNotNull(user2);
        assertEquals(user2, user);
    }

    @Test
    public void Remove_UUID2HashMap_item_using_new_UUID2_is_Success() {
        // • ARRANGE
        setUpUuid2HashMapTest();
        UUID2<User> user;

        // • ACT
        uuid2ToEntityMap.remove(book1);

        // • ASSERT
        user = uuid2ToEntityMap.get(book1);
        ctx.log.d(this, "after removal, user=" + user);
        assertNull(user);

        // check keySet count
        Set<UUID2<Book>> keySet = uuid2ToEntityMap.keySet();
        assertEquals(1, keySet.size());
    }

    @Test
    public void Put_UUID2HashMap_item_twice_only_upserts_single_item() {
        // • ARRANGE
        setUpUuid2HashMapTest();
        uuid2ToEntityMap.remove(book1);

        // • ACT
        uuid2ToEntityMap.put(book1, user01);
        // put it in again (should replace. not duplicate)
        uuid2ToEntityMap.put(book1, user01);

        // • ASSERT
        // check keySet count
        Set<UUID2<Book>> keySet = uuid2ToEntityMap.keySet();
        assertEquals(2, keySet.size());

        // check values count
        Collection<UUID2<User>> values = uuid2ToEntityMap.values();
        assertEquals(2, values.size());

        // check entrySet count
        Set<Map.Entry<UUID2<Book>, UUID2<User>>> entrySet = uuid2ToEntityMap.entrySet();
        assertEquals(2, entrySet.size());

        // check containsKey
        assertTrue("containsKey(book1) failed", uuid2ToEntityMap.containsKey(book1));
        assertTrue("containsKey(book2) failed", uuid2ToEntityMap.containsKey(book2));
        assertFalse("containsKey(Book 1400) should fail",
                uuid2ToEntityMap.containsKey(UUID2.createFakeUUID2(1400, Book.class))
        );
    }

    @Test
    public void Invalid_UUID2_String_throws_IllegalArgumentException() {
        // • ARRANGE
        String invalidUUID2Str = "UUID2:Role.Book_00000000-0000-0000-0000-000000001200"; // missing `@`

        // • ACT
        try {
            @SuppressWarnings("unchecked")
            UUID2<Book> book1200aId = (UUID2<Book>) UUID2.fromUUID2String(invalidUUID2Str);
            fail("Expected IllegalArgumentException");

            // This is only here to satisfy the compiler warnings.  It should never be reached.
            System.err.printf("SHOULD NEVER SEE THIS - book1200aId=%s", book1200aId);
        } catch (IllegalArgumentException e) {
            // • ASSERT
            assertTrue( true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}