package me.Tonus_.hatCosmetics.messages;

import me.Tonus_.hatCosmetics.message.ColorParser;
import me.Tonus_.hatCosmetics.message.IColorParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ColorParserTests {
    private final IColorParser sut = new ColorParser();

    @Test
    void parse_whenNothingToParse_returnsUnchanged() {
        // Arrange
        var input = "&PARSE_ME";
        var expectedResult = "&PARSE_ME";

        // Act
        var result = sut.parse(input);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void parse_whenSingleParsableElement_returnsParsedString() {
        // Arrange
        var input = "&9PARSE_ME";
        var expectedResult = "§9PARSE_ME";

        // Act
        var result = sut.parse(input);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void parse_whenMultipleParsableElements_returnsParsedString() {
        // Arrange
        var input = "&9&oPARSE_ME";
        var expectedResult = "§9§oPARSE_ME";

        // Act
        var result = sut.parse(input);

        // Assert
        assertEquals(expectedResult, result);
    }
}
