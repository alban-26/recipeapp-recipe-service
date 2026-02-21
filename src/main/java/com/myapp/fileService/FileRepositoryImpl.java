package com.myapp.fileService;

import com.my.common.api.FileRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
@Slf4j
public class FileRepositoryImpl implements FileRepository {

    @Override
    public Path saveFile(byte[] data, Path path) {
        try {

            Path savedPath = Files.write(path, data);
            log.info("Image successfully saved for file: {}", path);
            return savedPath;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    @Override
    public byte[] readFile(String path) {
        try {
            Path filePath = Paths.get(path);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            Path filePath = Paths.get(path);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

}
