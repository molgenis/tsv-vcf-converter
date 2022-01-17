package org.molgenis.vip.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.vip.converter.model.Constants.LINE_ATTR;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vip.converter.model.Mapping;
import org.molgenis.vip.converter.model.Settings;
import org.springframework.util.ResourceUtils;

class Tsv2VcfConverterTest {

  private Tsv2VcfConverter tsv2VcfConverter;

  @BeforeEach
  void beforeEach() {
    tsv2VcfConverter = new Tsv2VcfConverter();
  }

  static Stream<Arguments> createVariantContextProvider() {
    return Stream.of(
        Arguments.of(new String[]{"MT", "2", "A", "other thing", "other", "CC", "3"},
            "[MT, 2, A, other%20thing, other, CC, 3]"),
        Arguments.of(
            new String[]{"MT", "2", "A", "other=thing", "other", "CC", "3"},
            "[MT, 2, A, other%3Dthing, other, CC, 3]"), Arguments.of(
            new String[]{"MT", "2", "A", "other,thing", "other", "CC", "3"},
            "[MT, 2, A, other%2Cthing, other, CC, 3]"), Arguments.of(
            new String[]{"MT", "2", "A", "other;thing", "other", "CC", "3"},
            "[MT, 2, A, other%3Bthing, other, CC, 3]"));
  }

  @ParameterizedTest
  @MethodSource("createVariantContextProvider")
  void createVariantContext(String[] line, String expectedVariantContextStr) {
    Mapping mapping = Mapping.builder().chromIdx(0).posIdx(1).refIdx(5).altIdx(2).stopIdx(6)
        .build();

    //toString because VariantContext lacks an equals method.
    assertEquals(expectedVariantContextStr,
        tsv2VcfConverter.createVariantContext(mapping, line).getAttributeAsString(LINE_ATTR, ""));
  }

  @SuppressWarnings("java:S5778")
  @Test
  void convertInvalidTsv() throws IOException {
    Path inputPath = ResourceUtils.getFile("classpath:invalid.tsv").toPath();
    Path outputPath = Files.createTempFile("invalid_output", ".vcf");
    assertThrows(
        IllegalArgumentException.class, () ->
            tsv2VcfConverter.convert(
                Settings.builder().input(inputPath).output(outputPath)
                    .build()));
  }

  @SuppressWarnings("java:S5778")
  @Test
  void convertInvalidTsvGz() throws FileNotFoundException {
    Path inputPath = ResourceUtils.getFile("classpath:invalid.tsv.gz").toPath();
    assertThrows(
        UncheckedIOException.class, () ->
            tsv2VcfConverter.convert(
                Settings.builder().input(inputPath)
                    .build()));
  }
}