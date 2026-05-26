package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.model.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class StorageServiceTest {

    private StorageService storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        storageService = new StorageService();

        // ARRANGE
        Field basePathField = StorageService.class.getDeclaredField("BASE_PATH");
        basePathField.setAccessible(true);
        basePathField.set(storageService, tempDir.toAbsolutePath().toString() + File.separator);
    }

    @Test
    void shouldGetPreviewPath() {
        // ARRANGE
        int projectId = 99;
        String expectedSuffix = "99/preview.png";
        // ACT
        String result = storageService.getPreviewPath(projectId);

        // ASSERT
        assertTrue(result.contains(expectedSuffix));
        assertTrue(result.startsWith("file:"));
    }

    @Test
    void shouldDeleteProjectFiles() throws IOException {
        // ARRANGE
        int projectId = 1;
        Path projectPath = tempDir.resolve(String.valueOf(projectId));
        Files.createDirectories(projectPath);
        Path fakeFile = projectPath.resolve("data.json");
        Files.createFile(fakeFile);

        assertTrue(Files.exists(projectPath));
        assertTrue(Files.exists(fakeFile));

        // ACT
        storageService.deleteProjectFiles(projectId);

        // ASSERT
        assertFalse(Files.exists(fakeFile));
        assertFalse(Files.exists(projectPath));
    }

    @Test
    void shouldExportProjectAsZip() throws IOException {
        // ARRANGE
        int projectId = 2;
        Project project = new Project(projectId, "Test Zip");

        Path projectPath = tempDir.resolve(String.valueOf(projectId));
        Files.createDirectories(projectPath);
        Files.writeString(projectPath.resolve("test.txt"), "Hello World");

        File targetZip = tempDir.resolve("exported.zip").toFile();

        // ACT
        storageService.exportProjectAsZip(project, targetZip);

        // ASSERT
        assertTrue(targetZip.exists());
        assertTrue(targetZip.length() > 0);
    }

    @Test
    void shouldThrowExceptionWhenExportingNonExistentProject() {
        // ARRANGE
        Project project = new Project(999, "Project");
        File targetZip = tempDir.resolve("ghost.zip").toFile();

        // ACT & ASSERT
        IOException exception = assertThrows(
                IOException.class,
                () -> storageService.exportProjectAsZip(project, targetZip)
        );
        assertTrue(exception.getMessage().contains("File Doesn't exist"));
    }

    @Test
    void shouldImportProjectFromZip() throws IOException {
        // ARRANGE
        int targetProjectId = 3;
        File sourceZip = tempDir.resolve("source.zip").toFile();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(sourceZip))) {
            ZipEntry entry = new ZipEntry("imported.txt");
            zos.putNextEntry(entry);
            zos.write("Imported Data".getBytes());
            zos.closeEntry();
        }

        // ACT
        storageService.importProjectFromZip(sourceZip, targetProjectId);

        // ASSERT
        Path extractedFile = tempDir.resolve(String.valueOf(targetProjectId)).resolve("imported.txt");
        assertTrue(Files.exists(extractedFile));
        assertEquals("Imported Data", Files.readString(extractedFile));
    }
}