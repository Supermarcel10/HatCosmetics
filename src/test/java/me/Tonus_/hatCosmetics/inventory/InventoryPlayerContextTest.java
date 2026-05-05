package me.Tonus_.hatCosmetics.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


class InventoryPlayerContextTest {
    @Test
    void nextPage_incrementsWhenNotAtMax() throws Exception {
        // Arrange
        var context = createContextWithItems(28);

        // Act
        var result = context.nextPage();

        // Assert
        assertTrue(result);
        assertEquals(2, context.getCurrentPage());
    }

    @Test
    void nextPage_doesNotIncrementWhenAtMax() throws Exception {
        // Arrange
        var context = createContextWithItems(28);
        var maxPage = context.getMaxPage();

        context.nextPage();

        // Act
        var result = context.nextPage();

        // Assert
        assertFalse(result);
        assertEquals(maxPage, context.getCurrentPage());
    }

    @Test
    void prevPage_decrementsWhenNotAtFirstPage() throws Exception {
        // Arrange
        var context = createContextWithItems(28);
        context.nextPage();

        // Act
        var result = context.prevPage();

        // Assert
        assertTrue(result);
        assertEquals(1, context.getCurrentPage());
    }

    @Test
    void prevPage_doesNotDecrementWhenAtFirstPage() throws Exception {
        // Arange
        var context = createContextWithItems(28);

        // Act
        var result = context.prevPage();

        // Assert
        assertFalse(result);
        assertEquals(1, context.getCurrentPage());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1",
        "1, 1",
        "9, 1",
        "10, 1",
        "18, 1",
        "19, 1",
        "27, 1",
        "28, 2",
        "36, 2",
        "37, 2",
        "54, 2",
        "55, 3"
    })
    @DisplayName("maxPage is calculated correctly based on item count")
    void maxPage_calculation(int itemCount, int expectedMaxPage) throws Exception {
        // Arrange
        var context = createContextWithItems(itemCount);

        // Act & Assert
        assertEquals(expectedMaxPage, context.getMaxPage());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1, 1",
        "1, 1, 1",
        "9, 1, 1",
        "10, 1, 2",
        "18, 1, 2",
        "19, 1, 3",
    })
    @DisplayName("maxPage is calculated correctly for 1 hat row per page")
    void maxPage_calculationWithOneHatRow(int itemCount, int hatRowsPerPage, int expectedMaxPage) throws Exception {
        // Arrange
        var context = createContextWithItems(itemCount, hatRowsPerPage);

        // Act & Assert
        assertEquals(expectedMaxPage, context.getMaxPage());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 2, 1",
        "18, 2, 1",
        "19, 2, 2",
        "36, 2, 2",
        "37, 2, 3",
    })
    @DisplayName("maxPage is calculated correctly for 2 hat rows per page")
    void maxPage_calculationWithTwoHatRows(int itemCount, int hatRowsPerPage, int expectedMaxPage) throws Exception {
        // Arrange
        var context = createContextWithItems(itemCount, hatRowsPerPage);

        // Act & Assert
        assertEquals(expectedMaxPage, context.getMaxPage());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 4, 1",
        "36, 4, 1",
        "37, 4, 2",
        "72, 4, 2",
        "73, 4, 3",
    })
    @DisplayName("maxPage is calculated correctly for 4 hat rows per page")
    void maxPage_calculationWithFourHatRows(int itemCount, int hatRowsPerPage, int expectedMaxPage) throws Exception {
        // Arrange
        var context = createContextWithItems(itemCount, hatRowsPerPage);

        // Act & Assert
        assertEquals(expectedMaxPage, context.getMaxPage());
    }

    @Test
    void maxPage_itemsHatRowsCalculatesAndDoesNotDefaultToThree() throws Exception {
        // Arrange
        var context = createContextWithItems(19, 2);

        // Act & Assert
        assertEquals(2, context.getMaxPage());
    }

    private InventoryPlayerContext createContextWithItems(int count) {
        return createContextWithItems(count, 3);
    }

    private InventoryPlayerContext createContextWithItems(int count, int hatRowsPerPage) {
        var cosmetics = new HashSet<ItemStack>();

        for (int i = 0; i < count; i++) {
            cosmetics.add(mock(ItemStack.class));
        }

        return new InventoryPlayerContext(cosmetics, hatRowsPerPage);
    }
}
