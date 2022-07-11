package com.zenika.users.utils;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class CsvToBeanConverter {

  public static <T> List<T> convertCsvToBean(InputStream inputStreamCsvData, Class<T> var) {
    try (Reader reader = new BufferedReader(new InputStreamReader(inputStreamCsvData))) {
      return new CsvToBeanBuilder<T>(reader).withType(var).build().parse();
    } catch (IOException ex) {
      log.error("Failed to convert input stream to bean", ex);
      throw new RuntimeException("Error occurred during consuming csv input stream", ex);
    }
  }
}
