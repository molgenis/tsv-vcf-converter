package org.molgenis.vip.converter;

import com.opencsv.CSVWriter;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Vcf2TsvConverter {

  private final CSVWriter writer;

  public static final String LENGTH_ATTR = "LENGTH";
  public static final String LENGTH_DESC = "Stop minus start position";
  public static final String LINE_ATTR = "LINE";
  public static final String STOP = "STOP";
  public static final String ALT = "ALT";
  public static final String REF = "REF";
  public static final String POS = "POS";
  public static final String CHROM = "CHROM";

  public Vcf2TsvConverter(CSVWriter writer) {
    this.writer = writer;
  }

  public void convert(Settings settings) {
    try {
      VCFFileReader vcfFileReader = new VCFFileReader(settings.getInput(), false);
      VCFHeader header = vcfFileReader.getFileHeader();
      String headerline = header.getInfoHeaderLine("LINE").getDescription();
      VCFInfoHeaderLine lengthHeader = header.getInfoHeaderLine("LENTGH");

      String[] headers = headerline.split(",");
      List<String> headerList = Arrays.asList(headers);

      writer.writeNext(headers);

      Map<String, String> mapping = settings.getMappings();
      int chromIdx = headerList.indexOf(mapping.get(CHROM));
      int posIdx = headerList.indexOf(mapping.get(POS));
      int refIdx = headerList.indexOf(mapping.get(REF));
      int altIdx = headerList.indexOf(mapping.get(ALT));
      int stopIdx = mapping.containsKey(STOP) ? headerList.indexOf(mapping.get(STOP)):-1;

      StreamSupport.stream(vcfFileReader.spliterator(), false).forEach(variantContext -> {
        List<String> lineValue = variantContext.getAttributeAsStringList("LINE", "");
        String[] line = lineValue.toArray(String[]::new);
        line[chromIdx] = variantContext.getContig();
        line[posIdx] = String.valueOf(variantContext.getStart());
        line[refIdx] = variantContext.getReference().getBaseString();
        line[altIdx] = String.join(",", variantContext.getAlternateAlleles().stream().map(
            Allele::getBaseString).collect(Collectors.toList()));
        if (stopIdx != -1) {
          int length = Integer.valueOf(variantContext.getAttributeAsString("LENGTH", ""));
          line[stopIdx] = String.valueOf(variantContext.getStart() + length);
        }
        writer.writeNext(line);
      });
    }      finally{
      try {
        writer.close();
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }
  }
}
