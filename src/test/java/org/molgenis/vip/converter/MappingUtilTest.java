package org.molgenis.vip.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.molgenis.vip.converter.model.Mapping;

class MappingUtilTest {

  @Test
  void getMapping() {
    Map<String, String> mappingMap = Map.of("CHROM","chrom","ALT","alt","REF","ref","POS","start");
    String[] header = new String[]{"start","alt","other","ref","other2","chrom"};

    Mapping expected = Mapping.builder().chromIdx(5).posIdx(0).refIdx(3).altIdx(1).build();

    assertEquals(expected, MappingUtil.getMapping(mappingMap, header));
  }

  @Test
  void getMappingStop() {
    Map<String, String> mappingMap = Map.of("CHROM","chrom","ALT","alt","REF","ref","POS","start", "STOP", "other");
    String[] header = new String[]{"start","alt","other","ref","other2","chrom"};

    Mapping expected = Mapping.builder().chromIdx(5).posIdx(0).refIdx(3).altIdx(1).stopIdx(2).build();

    assertEquals(expected, MappingUtil.getMapping(mappingMap, header));
  }
}