package me.Tonus_.hatCosmetics.utility.file;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@AllArgsConstructor
public class DirectoryCompressor {
    private static final int BUFFER_CAPACITY = 8192;

    public void compressDirectory(Path sourceDir, @NotNull Path outputArchivePath, int compressionLevel) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputArchivePath.toFile()); BufferedOutputStream bos = new BufferedOutputStream(fos); ZipOutputStream zos = new ZipOutputStream(bos)) {

            zos.setLevel(compressionLevel);
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                    Path relativePath = sourceDir.relativize(file);
                    ZipEntry zipEntry = new ZipEntry(relativePath.toString());
                    zos.putNextEntry(zipEntry);

                    try (FileChannel sourceChannel = FileChannel.open(file, StandardOpenOption.READ)) {
                        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_CAPACITY);
                        byte[] data = new byte[buffer.capacity()];
                        while (sourceChannel.read(buffer) > 0) {
                            buffer.flip();
                            int limit = buffer.limit();
                            buffer.get(data, 0, limit);
                            zos.write(data, 0, limit);
                            buffer.clear();
                        }
                    }

                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
                    Path relativePath = sourceDir.relativize(dir);
                    if (!relativePath.toString().isEmpty()) {
                        ZipEntry zipEntry = new ZipEntry(relativePath + "/");
                        zos.putNextEntry(zipEntry);
                        zos.closeEntry();
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
