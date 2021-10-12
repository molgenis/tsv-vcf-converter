package org.molgenis.vip.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MappingExceptionTest {
  @Test
  void getMessage() {
    assertEquals(
        "Mapping should contain values for at least CHROM, POS, REF and ALT.",
        new MappingException().getMessage());
  }
}
