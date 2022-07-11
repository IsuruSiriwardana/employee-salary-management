package com.zenika.users.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestFileReader {

  public static InputStream readFile(String src) throws URISyntaxException, IOException {
    return Files.newInputStream(Paths.get(src));
  }
}
