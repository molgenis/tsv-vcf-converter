package org.molgenis.vip.converter;

import static com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER;
import static org.molgenis.vip.converter.model.Constants.LENGTH_ATTR;
import static org.molgenis.vip.converter.model.Constants.LINE_ATTR;
import static org.molgenis.vip.converter.model.Constants.TAB;
import static org.molgenis.vip.converter.model.Constants.TSV_GZ;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPOutputStream;
import org.molgenis.vip.converter.model.Mapping;
import org.molgenis.vip.converter.model.Settings;

public class Vcf2TsvConverter {

  public void convert(Settings settings) {
    try (VCFFileReader vcfFileReader = new VCFFileReader(settings.getInput(), false);) {
      boolean isZipped = settings.getOutput().toString().endsWith(TSV_GZ);
      ICSVWriter csvWriter;
      if (isZipped) {
        try (FileOutputStream outputStream = new FileOutputStream(settings.getOutput().toFile());
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
            Writer fileWriter = new OutputStreamWriter(gzipOutputStream)) {
          csvWriter = new CSVWriterBuilder(fileWriter).withSeparator(TAB)
              .withQuoteChar(NO_QUOTE_CHARACTER).build();
          processFile(vcfFileReader, csvWriter, settings.getMappings());
        }
      } else {
        try (Writer fileWriter = new FileWriter(settings.getOutput().toFile())) {
          csvWriter = new CSVWriterBuilder(fileWriter).withSeparator(TAB)
              .withQuoteChar(NO_QUOTE_CHARACTER).build();
          processFile(vcfFileReader, csvWriter, settings.getMappings());
        }
      }
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }

  private void processFile(VCFFileReader vcfFileReader, ICSVWriter writer,
      Map<String, String> mappings) {
    VCFHeader header = vcfFileReader.getFileHeader();
    String headerline = header.getInfoHeaderLine(LINE_ATTR).getDescription();
    VCFInfoHeaderLine lengthHeader = header.getInfoHeaderLine(LENGTH_ATTR);

    String[] headers = headerline.split(",");
    writer.writeNext(headers);
    Mapping mapping = MappingUtil.getMapping(mappings, headers);

    StreamSupport.stream(vcfFileReader.spliterator(), false).forEach(variantContext -> {
      boolean isLengthPresent = lengthHeader != null;
      String[] line = parseLine(isLengthPresent,
          variantContext, mapping);
      writer.writeNext(line);
    });
  }

  String[] parseLine(boolean isLengthPresent, VariantContext variantContext, Mapping mapping) {
    List<String> lineValue = variantContext.getAttributeAsStringList(LINE_ATTR, "");
    List<String> decoded = lineValue.stream().map(this::unEscape)
        .toList();
    String[] line = decoded.toArray(String[]::new);
    line[mapping.getChromIdx()] = variantContext.getContig();
    line[mapping.getPosIdx()] = String.valueOf(variantContext.getStart());
    line[mapping.getRefIdx()] = variantContext.getReference().getBaseString();
    line[mapping.getAltIdx()] = variantContext.getAlternateAlleles().stream().map(
        Allele::getBaseString).collect(Collectors.joining(","));
    if (mapping.containsStop()) {
      if (!isLengthPresent) {
        throw new MissingLengthFieldException();
      }
      int length = Integer.parseInt(variantContext.getAttributeAsString(LENGTH_ATTR, ""));
      line[mapping.getStopIdx()] = String.valueOf(variantContext.getStart() + length);
    }
    return line;
  }

  private String unEscape(String value) {
    return value
        .replace("%20", " ")
        .replace("%2C", ",")
        .replace("%3B", ";")
        .replace("%3D", "=");
  }
}
