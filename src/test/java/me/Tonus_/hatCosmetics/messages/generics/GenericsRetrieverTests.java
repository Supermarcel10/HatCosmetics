package me.Tonus_.hatCosmetics.messages.generics;

import me.Tonus_.hatCosmetics.message.IColorParser;
import me.Tonus_.hatCosmetics.message.generics.GenericsRetriever;
import me.Tonus_.hatCosmetics.message.generics.IGenericsRetriever;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;


public class GenericsRetrieverTests {
    private final Logger logger = mock();
    private final IColorParser colorParser = mock();
    private final IGenericsRetriever sut = new GenericsRetriever(logger, colorParser);

    @Test
    void getGeneric_whenGenericNotPresent_shouldReturnNull() {
        // Arrange
        var generic = "DOES_NOT_EXIST";

        // Act
        var result = sut.getGeneric(generic);

        // Assert
        assertNull(result);
        verify(logger).info(anyString(), anyInt());
        verify(logger, atMostOnce()).error(anyString(), eq(generic));
    }

    @Test
    void getGeneric_whenGenericPresent_shouldReturnValue() {
        // Arrange
        doReturn("SOME_VALUE").when(colorParser).parse(anyString());

        var generic = "prefix";
        var expectedResult = "SOME_VALUE";

        // Act
        var result = sut.getGeneric(generic);

        // Assert
        assertEquals(expectedResult, result);
        verify(logger).info(anyString(), anyInt());
        verify(logger, never()).error(anyString(), anyInt());
    }
}
