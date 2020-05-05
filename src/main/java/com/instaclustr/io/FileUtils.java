package com.instaclustr.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static void cleanDirectory(final File dir) {

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] fileList = dir.listFiles();

        if (fileList == null) {
            return;
        }

        for (File file : fileList) {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }

            if (!file.delete()) {
                logger.warn("Failed to delete {}", file.getAbsolutePath());
            }
        }
    }

    public static void deleteDirectory(final Path dir) throws Exception {
        org.apache.commons.io.FileUtils.deleteDirectory(dir.toFile());
    }

    public static void deleteDirectory(final File dir) throws Exception {
        org.apache.commons.io.FileUtils.deleteDirectory(dir);
    }

    public static void cleanDirectory(final Path path) {
        cleanDirectory(path.toFile());
    }

    public static void replaceInFile(final Path file,
                                     final String toReplace, final String replacement) throws IOException {

        final String contents = new String(Files.readAllBytes(file));

        Files.write(file, ImmutableList.of(contents.replace(toReplace, replacement)), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
    }

    public static void appendToFile(final Path file,
                                    final String content) throws IOException {

        final String contents = new String(Files.readAllBytes(file));

        Files.write(file, ImmutableList.of(content), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    public static void appendToFile(final Path file,
                                    final Path fileToAppend) throws IOException {
        appendToFile(file, new String(Files.readAllBytes(fileToAppend)));
    }

    public static void copy(final File source, final File target) throws IOException {
        org.apache.commons.io.FileUtils.copyFile(source, target);
    }

    public static void copy(final Path source, final Path target) throws IOException {
        org.apache.commons.io.FileUtils.copyFile(source.toFile(), target.toFile());
    }

    public static boolean createDirectory(final Path dir) throws Exception {
        if (dir == null) {
            throw new IllegalStateException("Directory to create can not be null Path");
        }

        if (!dir.toFile().exists()) {
            try {
                return dir.toFile().mkdirs();
            } catch (final Exception ex) {
                throw new IllegalStateException(String.format("Unable to create directory %s", dir.toAbsolutePath().toString()), ex);
            }
        } else if (dir.toFile().isFile()) {
            throw new IllegalStateException("Directory can not be created because file with same path already exists.");
        }

        return false;
    }

    public static void createOrCleanDirectory(final Path dir) throws Exception {
        createDirectory(dir);
        cleanDirectory(dir);
    }
}
