package me.Tonus_.hatCosmetics.utility.jar;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;


public interface IJarAccessor {
    JarFile open() throws IOException;

    InputStream getResource(String path) throws IOException;
}
