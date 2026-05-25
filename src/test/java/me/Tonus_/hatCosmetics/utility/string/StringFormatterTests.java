package me.Tonus_.hatCosmetics.utility.string;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class StringFormatterTests {
    private final Logger logger = mock();
    private final IStringFormatter sut = new StringFormatter(logger);

    @ParameterizedTest
    @MethodSource
    void format_substAndArgMatrix_shouldProvideCorrectOutput(@NotNull FormatTestCase formatTestCase) {
        // Act
        var result = sut.format(formatTestCase.input, formatTestCase.formatArg);

        // Assert
        assertEquals(formatTestCase.expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource
    void formatMultiArgs_substAndArgsMatrix_shouldProvideCorrectOutput(@NotNull FormatTestCaseMulti formatTestCase) {
        // Act
        var result = sut.format(formatTestCase.input, formatTestCase.formatArgs);

        // Assert
        assertEquals(formatTestCase.expectedResult, result);
        verify(logger, times(formatTestCase.warningTimes)).warn(anyString(), any(StringBuilder.class), anyString());
    }

    record FormatTestCase(String input, String formatArg, String expectedResult) {}
    record FormatTestCaseMulti(String input, Map<String, String> formatArgs, String expectedResult, int warningTimes) {}

    static @NotNull Stream<Arguments> format_substAndArgMatrix_shouldProvideCorrectOutput() {
        return Stream.of(
                Arguments.of(new FormatTestCase("Hello!", null, "Hello!")),
                Arguments.of(new FormatTestCase("Hello {}!", null, "Hello {}!")),
                Arguments.of(new FormatTestCase("Hello {}!", "World", "Hello World!")),
                Arguments.of(new FormatTestCase("Hello {}! Today is {}", "World", "Hello World! Today is {}"))
        );
    }

    static @NotNull Stream<Arguments> formatMultiArgs_substAndArgsMatrix_shouldProvideCorrectOutput() {
        return Stream.of(
                Arguments.of(new FormatTestCaseMulti("Hello!", Map.of(), "Hello!", 0)),
                Arguments.of(new FormatTestCaseMulti("Hello {}!", Map.of(), "Hello {}!", 1)),
                Arguments.of(new FormatTestCaseMulti("Hello {name}!", Map.of("name", "World"), "Hello World!", 0)),
                Arguments.of(new FormatTestCaseMulti("Hello {name}! Today is {}", Map.of("name", "World"), "Hello World! Today is {}", 1)),
                Arguments.of(new FormatTestCaseMulti("Hello {name}! Today is {day}", Map.of("name", "World"), "Hello World! Today is {day}", 1)),
                Arguments.of(new FormatTestCaseMulti("Hello {name}! Today is {name}", Map.of("name", "World"), "Hello World! Today is World", 0)),
                Arguments.of(new FormatTestCaseMulti("Hello {name}! Today is {day}", Map.of("name", "World", "day", "Monday"), "Hello World! Today is Monday", 0))
        );
    }
}
