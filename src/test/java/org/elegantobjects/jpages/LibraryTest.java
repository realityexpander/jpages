package org.elegantobjects.jpages;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;

public class LibraryTest {

    Library library = new Library();
    User user = new User();

    @Before
    public void setUp() throws Exception {
        library = new Library(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        library.fetchInfo();

        user = new User(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    }

    @Test
    public void checkoutBookToUser() {
        // Arrange
        Book book = new Book(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // Act
        library.checkoutBookToUser(book, user);

        // Assert
        Result<ArrayList<UUID>> numBooksCheckedOut = library.findBooksCheckedOutByUser(user.id);
        assert(numBooksCheckedOut instanceof Result.Success);
        assertEquals(1, ((Result.Success<ArrayList<UUID>>) numBooksCheckedOut).value().size());
    }

    @Test
    public void returnBookFromUser() {
    }

    @Test
    public void findBooksCheckedOutByUser() {
    }

    @Test
    public void calculateAvailableBooksAndAmountOnHand() {
    }
}