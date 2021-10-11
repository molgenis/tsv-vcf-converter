package org.molgenis.vip.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

public class AppIT {

  @TempDir
  Path sharedTempDir;

  @Test
  void test() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:example.tsv").toString();
    String mapping = "CHROM=chromosome,POS=start,STOP=stop,REF=ref,ALT=alt";
    String outputFile = sharedTempDir.resolve("actual.vcf").toString();

    String[] args = {"-i", inputFile, "-m", mapping, "-o", outputFile};
    SpringApplication.run(App.class, args);

    String actual = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:example.vcf").toPath();
    String expected = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expected, actual);
  }

  @Test
  void testReverse() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf").toString();
    String mapping = "CHROM=chromosome,POS=start,STOP=stop,REF=ref,ALT=alt";
    String outputFile = sharedTempDir.resolve("actual.tsv").toString();

    String[] args = {"-i", inputFile, "-m", mapping, "-o", outputFile};
    SpringApplication.run(App.class, args);

    String actual = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:example.tsv").toPath();
    String expected = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expected, actual);
  }

  @Test
  void testGzInput() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:example.tsv.gz").toString();
    String mapping = "CHROM=chromosome,POS=start,STOP=stop,REF=ref,ALT=alt";
    String outputFile = sharedTempDir.resolve("actual.vcf").toString();

    String[] args = {"-i", inputFile, "-m", mapping, "-o", outputFile};
    SpringApplication.run(App.class, args);

    String actual = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:example.vcf").toPath();
    String expected = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expected, actual);
  }

  @Test
  void testReverseGz() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:example.vcf.gz").toString();
    String mapping = "CHROM=chromosome,POS=start,STOP=stop,REF=ref,ALT=alt";
    Path outputFile = sharedTempDir.resolve("actual.tsv.gz");
    Path outputFileUnzipped = sharedTempDir.resolve("actual.tsv");

    String[] args = {"-i", inputFile, "-m", mapping, "-o", outputFile.toString()};
    SpringApplication.run(App.class, args);
    decompressGzip(outputFile, outputFileUnzipped);
    String actual = Files.readString(outputFileUnzipped);

    Path expectedOutputFile = ResourceUtils.getFile("classpath:example.tsv").toPath();
    String expected = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expected, actual);
  }

  public static void decompressGzip(Path source, Path target) throws IOException {

    try (GZIPInputStream gis = new GZIPInputStream(
        new FileInputStream(source.toFile()));
        FileOutputStream fos = new FileOutputStream(target.toFile())) {

      // copy GZIPInputStream to FileOutputStream
      byte[] buffer = new byte[1024];
      int len;
      while ((len = gis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }

    }

  }
}
