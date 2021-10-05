package org.molgenis.vip.converter;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFHeaderVersion;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tsv2VcfConverter {

  public static final String LENGTH_ATTR = "LENGTH";
  public static final String LENGTH_DESC = "Stop minus start position";
  public static final String LINE_ATTR = "LINE";
  public static final String STOP = "STOP";
  public static final String ALT = "ALT";
  public static final String REF = "REF";
  public static final String POS = "POS";
  public static final String CHROM = "CHROM";
  private final VariantContextWriter vcfWriter;

  public Tsv2VcfConverter(VariantContextWriter vcfWriter) {
    this.vcfWriter = vcfWriter;
  }

  public void convert(Settings settings) {

    try (
        InputStreamReader inputStreamReader = new InputStreamReader(
            new FileInputStream(settings.getInput().toFile()))) {

      CSVParser parser = new CSVParserBuilder()
          .withSeparator('\t')
          .withIgnoreQuotations(true)
          .build();

      CSVReader csvReader = new CSVReaderBuilder(inputStreamReader)
          .withSkipLines(0)
          .withCSVParser(parser)
          .build();

      String[] header = csvReader.readNext();
      List<String> headerList = Arrays.asList(header);
      Map<String, String> mapping = settings.getMappings();
      int chromIdx = headerList.indexOf(mapping.get(CHROM));
      int posIdx = headerList.indexOf(mapping.get(POS));
      int refIdx = headerList.indexOf(mapping.get(REF));
      int altIdx = headerList.indexOf(mapping.get(ALT));
      int stopIdx = -1;
      if (mapping.containsKey(STOP)) {
        stopIdx = headerList.indexOf(mapping.get(STOP));
      }

      writeHeader(header, stopIdx);

      String[] line;
      while ((line = csvReader.readNext()) != null) {
        VariantContextBuilder builder = new VariantContextBuilder();
        builder.chr(line[chromIdx]);
        builder.start(Integer.valueOf(line[posIdx]));
        if (stopIdx != -1) {
          builder.stop(Integer.valueOf(line[stopIdx]));
          builder
              .attribute(LENGTH_ATTR, Integer.valueOf(line[stopIdx]) - Integer.valueOf(line[posIdx]));
        }else{
          builder.stop(Integer.valueOf(line[posIdx]));
        }
        builder.alleles(line[refIdx], line[altIdx]);
        builder.attribute(LINE_ATTR, String.join(",", line));
        vcfWriter.add(builder.make());
      }
      vcfWriter.close();
    } catch (FileNotFoundException fileNotFoundException) {
      fileNotFoundException.printStackTrace();
    } catch (IOException ioException) {
      ioException.printStackTrace();
    } catch (CsvValidationException e) {
      e.printStackTrace();
    }
  }

  private void writeHeader(String[] headerLine, int stopIdx) {
    VCFHeader header = new VCFHeader();
    header.setVCFHeaderVersion(VCFHeaderVersion.VCF4_2);
    header.addMetaDataLine(
        new VCFInfoHeaderLine(
            LINE_ATTR,
            1,
            VCFHeaderLineType.String, String.join(",", headerLine)));
    if (stopIdx != -1) {
      header.addMetaDataLine(
          new VCFInfoHeaderLine(
              LENGTH_ATTR,
              1,
              VCFHeaderLineType.String, LENGTH_DESC));
    }
    vcfWriter.writeHeader(header);
  }
}
