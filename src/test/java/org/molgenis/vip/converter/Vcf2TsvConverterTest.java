package org.molgenis.vip.converter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.molgenis.vip.converter.model.Constants.LENGTH_ATTR;
import static org.molgenis.vip.converter.model.Constants.LINE_ATTR;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.junit.jupiter.api.Test;
import org.molgenis.vip.converter.model.Mapping;

class Vcf2TsvConverterTest {

  @Test
  void parseLine() {
    Vcf2TsvConverter vcf2TsvConverter = new Vcf2TsvConverter();

    Mapping mapping = Mapping.builder().chromIdx(0).posIdx(1).refIdx(5).altIdx(2).stopIdx(6).build();

    VariantContextBuilder builder = new VariantContextBuilder();
    builder.chr("chrMT");
    builder.start(6);
    builder.stop(7);
    builder.attribute(LENGTH_ATTR, 1);
    builder.alleles("CC", "A");
    builder.attribute(LINE_ATTR, new String[]{"MT","2","A","other","other","CC","3"});
    VariantContext vc = builder.make();

    assertArrayEquals(new String[]{"chrMT","6","A","other","other","CC","7"},vcf2TsvConverter.parseLine(true, vc,mapping));

  }
}