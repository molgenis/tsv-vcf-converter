package org.molgenis.vip.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vip.converter.model.Constants.LINE_ATTR;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vip.converter.model.Mapping;

class Tsv2VcfConverterTest {

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
    Tsv2VcfConverter tsv2VcfConverter = new Tsv2VcfConverter();
    Mapping mapping = Mapping.builder().chromIdx(0).posIdx(1).refIdx(5).altIdx(2).stopIdx(6)
        .build();

    //toString because VariantContext lacks an equals method.
    assertEquals(expectedVariantContextStr,
        tsv2VcfConverter.createVariantContext(mapping, line).getAttributeAsString(LINE_ATTR, ""));
  }
}