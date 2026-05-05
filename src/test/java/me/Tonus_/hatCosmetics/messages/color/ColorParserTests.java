package me.Tonus_.hatCosmetics.messages.color;

import me.Tonus_.hatCosmetics.message.color.ColorParser;
import me.Tonus_.hatCosmetics.message.color.IColorParser;
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

    @Test
    void parse_whenBlackColorCode_returnsParsedString() {
        // Arrange
        var input = "&0PARSE_ME";
        var expectedResult = "§0PARSE_ME";

        // Act
        var result = sut.parse(input);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void parse_whenWhiteColorCodeLowercase_returnsParsedString() {
        // Arrange
        var input = "&fPARSE_ME";
        var expectedResult = "§fPARSE_ME";

        // Act
        var result = sut.parse(input);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void parse_whenWhiteColorCodeUppercase_returnsParsedString() {
        // Arrange
        var input = "&FPARSE_ME";
        var expectedResult = "§FPARSE_ME";

        // Act
        var result = sut.parse(input);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void parse_whenMixedColorCodes_returnsParsedString() {
        // Arrange
        var input = "&0Hello &fWorld";
        var expectedResult = "§0Hello §fWorld";

        // Act
        var result = sut.parse(input);

        // Assert
        assertEquals(expectedResult, result);
    }
}
