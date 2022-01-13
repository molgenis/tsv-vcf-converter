package org.molgenis.vip.converter;

import static java.lang.String.format;
import static org.molgenis.vip.converter.model.Constants.INFO_DELIMITER;
import static org.molgenis.vip.converter.model.Constants.LENGTH_ATTR;
import static org.molgenis.vip.converter.model.Constants.LENGTH_DESC;
import static org.molgenis.vip.converter.model.Constants.LINE_ATTR;
import static org.molgenis.vip.converter.model.Constants.TSV_GZ;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFHeaderVersion;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.molgenis.vip.converter.model.Mapping;
import org.molgenis.vip.converter.model.Settings;

public class Tsv2VcfConverter {

  public void convert(Settings settings) {

    boolean isZipped = settings.getInput().toString().endsWith(TSV_GZ);
    try (InputStreamReader inputStreamReader = getInputStreamReader(settings.getInput(), isZipped);
        VariantContextWriter writer = createVcfWriter(settings)) {

      CSVParser parser = new CSVParserBuilder()
          .withSeparator('\t')
          .withIgnoreQuotations(true)
          .build();

      CSVReader csvReader = new CSVReaderBuilder(inputStreamReader)
          .withSkipLines(0)
          .withCSVParser(parser)
          .build();

      String[] header = csvReader.readNext();
      Mapping mapping = MappingUtil.getMapping(settings.getMappings(), header);
      writeHeader(header, mapping.containsStop(), writer);

      String[] line;
      while ((line = csvReader.readNext()) != null) {
        VariantContext vc = createVariantContext(mapping, line);
        writer.add(vc);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (CsvValidationException e) {
      throw new IllegalArgumentException(e);
    }
  }

  VariantContext createVariantContext(Mapping mapping, String[] line) {
    VariantContextBuilder builder = new VariantContextBuilder();
    int pos = Integer.parseInt(line[mapping.getPosIdx()]);
    String ref = line[mapping.getRefIdx()];
    String alt = line[mapping.getAltIdx()];

    builder.chr(line[mapping.getChromIdx()]);
    builder.start(pos);
    if (mapping.containsStop()) {
      builder.stop(Integer.parseInt(line[mapping.getStopIdx()]));
      builder
          .attribute(LENGTH_ATTR, Integer.parseInt(line[mapping.getStopIdx()]) - pos);
    } else {
      builder.stop(pos + ref.length() - 1L);
    }
    builder.alleles(ref, alt);
    List<String> encoded = Arrays.stream(line).map(this::escape).toList();

    builder.attribute(LINE_ATTR, encoded);
    return builder.make();
  }

  private String escape(String value) {
    return value
        .replace(" ", "%20")
        .replace(",", "%2C")
        .replace(";", "%3B")
        .replace("=", "%3D");
  }

  private InputStreamReader getInputStreamReader(Path input, boolean isZipped) throws IOException {
    InputStream inputStream;
    if (isZipped) {
      inputStream = new GZIPInputStream(new FileInputStream(input.toFile()));
    } else {
      inputStream = new FileInputStream(input.toFile());
    }
    return new InputStreamReader(inputStream);
  }

  private void writeHeader(String[] headerLine, boolean isWriteLength,
      VariantContextWriter writer) {
    VCFHeader header = new VCFHeader();
    header.setVCFHeaderVersion(VCFHeaderVersion.VCF4_2);
    header.addMetaDataLine(
        new VCFInfoHeaderLine(
            LINE_ATTR,
            1,
            VCFHeaderLineType.String, String.join(INFO_DELIMITER, headerLine)));
    if (isWriteLength) {
      header.addMetaDataLine(
          new VCFInfoHeaderLine(
              LENGTH_ATTR,
              1,
              VCFHeaderLineType.String, LENGTH_DESC));
    }
    writer.writeHeader(header);
  }

  private static VariantContextWriter createVcfWriter(Settings settings) {
    Path outputVcfPath = settings.getOutput();
    if (settings.isOverwrite()) {
      try {
        Files.deleteIfExists(outputVcfPath);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } else if (Files.exists(outputVcfPath)) {
      throw new IllegalArgumentException(
          format("cannot create '%s' because it already exists.", outputVcfPath));
    }

    return new VariantContextWriterBuilder()
        .clearOptions()
        .setOutputFile(outputVcfPath.toFile())
        .build();
  }
}
