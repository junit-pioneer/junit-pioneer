package org.junitpioneer.jupiter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

// Visible for testing purposes
class PathDeleter extends SimpleFileVisitor<Path> {

  static final PathDeleter INSTANCE = new PathDeleter();

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    Files.deleteIfExists(file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    Files.deleteIfExists(dir);
    return FileVisitResult.CONTINUE;
  }

  // Prevent instantiation
  private PathDeleter() {
  }

}
