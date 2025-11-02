package me.Tonus_.hatCosmetics.utility.string;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.Map;


@RequiredArgsConstructor
public class StringFormatter implements IStringFormatter {
    private static final boolean STOP_ON_FIRST = true;

    private final Logger logger;

    public @NotNull String format(@NotNull String format, @Nullable String formatArg) {
        if (formatArg == null) return format;

        var result = new StringBuilder(format.length());
        var start = 0;
        var openBrace = format.indexOf('{', start);

        while (openBrace != -1) {
            var closeBrace = format.indexOf('}', openBrace);
            if (closeBrace == -1) break;

            result.append(format, start, openBrace);
            result.append(formatArg);
            start = closeBrace + 1;
            openBrace = format.indexOf('{', start);

            // TODO: Potentially make separate method for formatAll if needed.
            if (STOP_ON_FIRST) break;
        }

        result.append(format, start, format.length());

        return result.toString();
    }

    public @NotNull String format(@NotNull String format, @NotNull Map<String, String> formatArgs) {
        var result = new StringBuilder(format.length());
        var placeholder = new StringBuilder();
        var inPlaceholder = false;

        for (var i = 0; i < format.length(); ++i) {
            var c = format.charAt(i);

            if (c == '{') {
                inPlaceholder = true;
                placeholder.setLength(0);
            } else if (c == '}' && inPlaceholder) {
                inPlaceholder = false;
                var key = placeholder.toString();
                if (formatArgs.containsKey(key)) {
                    result.append(formatArgs.get(key));
                } else {
                    logger.warn("Value for placeholder \"{}\" is not defined in the template: {}", placeholder, format);
                    result.append('{').append(key).append('}');
                }
            } else if (inPlaceholder) {
                placeholder.append(c);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
