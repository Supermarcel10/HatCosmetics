package me.Tonus_.hatCosmetics.utility.file;

import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;


@AllArgsConstructor
public class DirectoryCopier {
	private Plugin plugin;

	public void copyDirectory(@NotNull File source, @NotNull File target){
		copyDirectory(source.toPath(), target.toPath());
	}

	public void copyDirectory(Path source, Path target) {
		try (Stream<Path> paths = Files.walk(source)) {
			paths.forEach(sourcePath -> {
				Path targetPath = target.resolve(source.relativize(sourcePath));
				try {
					if (Files.isDirectory(sourcePath)) {
						if (!Files.exists(targetPath)) {
							Files.createDirectories(targetPath);
						}
					} else if (Files.isRegularFile(sourcePath)) {
						Files.createDirectories(targetPath.getParent());
						Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
					} else {
						plugin.getSLF4JLogger().warn("Cannot copy non-standard file {}", sourcePath);
					}
				} catch (IOException e) {
					plugin.getSLF4JLogger().error("Failed to copy {}. Reason: {}", sourcePath, e.getMessage());
				}
			});
		} catch (IOException e) {
			plugin.getSLF4JLogger().error("Failed to traverse source directory {}. Reason: {}", source, e.getMessage());
		}
	}
}
