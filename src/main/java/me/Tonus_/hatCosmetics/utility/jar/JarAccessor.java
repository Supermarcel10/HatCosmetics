package me.Tonus_.hatCosmetics.utility.jar;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class JarAccessor implements IJarAccessor {
    private final Class<?> pluginClass;

    @Override
    public JarFile open() throws IOException {
        var codeSource = pluginClass.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new IOException("Not running from a JAR file.");
        }

        return new JarFile(codeSource.getLocation().getPath());
    }

    @Override
    public InputStream getResource(String path) throws IOException {
        return pluginClass.getResourceAsStream(path);
    }
}
