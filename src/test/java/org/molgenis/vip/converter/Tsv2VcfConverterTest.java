package org.molgenis.vip.converter;

import static org.junit.jupiter.api.Assertions.*;
import static org.molgenis.vip.converter.model.Constants.INFO_DELIMITER;
import static org.molgenis.vip.converter.model.Constants.LENGTH_ATTR;
import static org.molgenis.vip.converter.model.Constants.LINE_ATTR;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.junit.jupiter.api.Test;
import org.molgenis.vip.converter.model.Mapping;

class Tsv2VcfConverterTest {

  @Test
  void createVariantContext() {
    Tsv2VcfConverter tsv2VcfConverter = new Tsv2VcfConverter();
    String[] line = new String[]{"MT","2","A","other thing","other","CC","3"};
    Mapping mapping = Mapping.builder().chromIdx(0).posIdx(1).refIdx(5).altIdx(2).stopIdx(6).build();

    VariantContextBuilder builder = new VariantContextBuilder();
    builder.chr("MT");
    builder.start(2);
    builder.stop(3);
    builder.attribute(LENGTH_ATTR, 1);
    builder.alleles("CC", "A");
    builder.attribute(LINE_ATTR, line);
    VariantContext expected = builder.make();

    //toString because VariantContext lacks an equals method.
    assertEquals("[MT, 2, A, other%sthing, other, CC, 3]", tsv2VcfConverter.createVariantContext(mapping, line).getAttributeAsString(LINE_ATTR,""));
  }
}