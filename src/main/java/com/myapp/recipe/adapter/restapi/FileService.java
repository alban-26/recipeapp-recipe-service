package com.myapp.recipe.adapter.restapi;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@ApplicationScoped
public class FileService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 5MB

    public void saveImage(Path filePath, InputStream imageInputStream) throws IOException {
        validateInput(filePath, imageInputStream);

        Path parentDir = filePath.getParent();
        if (parentDir != null && Files.notExists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        try (InputStream is = imageInputStream;
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int read;
            int total = 0;

            while ((read = is.read(buffer)) != -1) {
                total += read;
                if (total > MAX_FILE_SIZE) {
                    throw new IOException("File size exceeds the 5MB limit.");
                }
                out.write(buffer, 0, read);
            }

            Files.write(
                    filePath,
                    out.toByteArray(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        }
    }



    public File getImage(Path directoryPath) {
        validateDirectoryAndId(directoryPath);

        File imageFile = directoryPath.toFile();
        return imageFile.exists() ? imageFile : null;
    }

    public void deleteImage(Path directoryPath) {
        validateDirectoryAndId(directoryPath);

        File imageFile = directoryPath.resolve(directoryPath).toFile();
        FileUtils.deleteQuietly(imageFile);
    }

    private void validateInput(Path directoryPath, InputStream imageInputStream) {
        if (imageInputStream == null || directoryPath == null) {
            throw new IllegalArgumentException("Invalid directory path, recipe ID, or image file.");
        }
    }

    private void validateDirectoryAndId(Path directoryPath) {
        if (directoryPath == null) {
            throw new IllegalArgumentException("Invalid directory path or recipe ID.");
        }
    }
}
