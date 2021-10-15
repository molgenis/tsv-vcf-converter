package org.molgenis.vip.converter;

import static org.molgenis.vip.converter.model.Constants.ALT;
import static org.molgenis.vip.converter.model.Constants.CHROM;
import static org.molgenis.vip.converter.model.Constants.POS;
import static org.molgenis.vip.converter.model.Constants.REF;
import static org.molgenis.vip.converter.model.Constants.STOP;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.molgenis.vip.converter.model.Mapping;

public class MappingUtil {

  private MappingUtil() {
  }

  static Mapping getMapping(Map<String, String> mappingMap, String[] header) {
    List<String> headerList = Arrays.asList(header);
    int chromIdx = headerList.indexOf(mappingMap.get(CHROM));
    int posIdx = headerList.indexOf(mappingMap.get(POS));
    int refIdx = headerList.indexOf(mappingMap.get(REF));
    int altIdx = headerList.indexOf(mappingMap.get(ALT));
    int stopIdx = -1;
    if (mappingMap.containsKey(STOP)) {
      stopIdx = headerList.indexOf(mappingMap.get(STOP));
    }
    return Mapping.builder().chromIdx(chromIdx).posIdx(posIdx).refIdx(refIdx).altIdx(altIdx).stopIdx(stopIdx).build();
  }
}
