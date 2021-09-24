package org.junitpioneer.jupiter;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

class PathDeleterTests {
  @Test
  void deletingNonExistentFileProducesNoIOException() throws IOException {
    try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
      // Expected not to throw an exception
      PathDeleter.INSTANCE.visitFile(
          fileSystem.getPath("some", "arbitrary", "file.txt"), null);
    }
  }

  @Test
  void deletingNonExistentDirectoryProducesNoIOException() throws IOException {
    try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
      // Expected not to throw an exception
      PathDeleter.INSTANCE.postVisitDirectory(
          fileSystem.getPath("some", "arbitrary", "directory"), null);
    }
  }
}