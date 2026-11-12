package com.FSA;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class ImageStore {

    private static final Path DIR =
            Path.of(System.getProperty("user.home"), ".cs-management", "projects");

    private ImageStore() {}

    /** Copies the chosen file into the app folder and returns its new name (what the database stores). */
    public static String save(File source) throws IOException {
        Files.createDirectories(DIR);
        String name = UUID.randomUUID() + extension(source.getName());   // random name: no clashes
        Files.copy(source.toPath(), DIR.resolve(name));
        return name;
    }

    /** URL you can give to CSS or Image, or null if there is no such file. */
    public static String url(String name) {
        if (name == null || name.isBlank()) return null;
        Path file = DIR.resolve(name).normalize();
        if (!file.startsWith(DIR) || !Files.exists(file)) return null;
        return file.toUri().toString();
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot).toLowerCase();
    }
}