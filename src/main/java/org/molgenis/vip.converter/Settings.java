package org.molgenis.vip.converter;

import java.nio.file.Path;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Settings {

  Path input;
  Map<String, String> mappings;
  Path output;
  boolean overwrite;
  boolean debug;
}