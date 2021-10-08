package org.molgenis.vip.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MissingLengthFieldExceptionTest {
  @Test
  void getMessage() {
    assertEquals(
        "Mapping for STOP present but missing LENGTH header.",
        new MissingLengthFieldException().getMessage());
  }
}
