package org.molgenis.vip.converter;

import static java.lang.String.format;
import static org.molgenis.vip.converter.model.Constants.TSV;
import static org.molgenis.vip.converter.model.Constants.TSV_GZ;
import static org.molgenis.vip.converter.model.Constants.VCF;
import static org.molgenis.vip.converter.model.Constants.VCF_GZ;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

class AppCommandLineOptions {

  static final String OPT_INPUT = "i";
  static final String OPT_INPUT_LONG = "input";
  static final String OPT_MAPPINGS = "m";
  static final String OPT_MAPPINGS_LONG = "mappings";
  static final String OPT_OUTPUT = "o";
  static final String OPT_OUTPUT_LONG = "output";
  static final String OPT_FORCE = "f";
  static final String OPT_FORCE_LONG = "force";
  static final String OPT_DEBUG = "d";
  static final String OPT_DEBUG_LONG = "debug";
  static final String OPT_VERSION = "v";
  static final String OPT_VERSION_LONG = "version";
  private static final Options APP_OPTIONS;
  private static final Options APP_VERSION_OPTIONS;

  static {
    Options appOptions = new Options();
    appOptions.addOption(
        Option.builder(OPT_INPUT)
            .hasArg(true)
            .required()
            .longOpt(OPT_INPUT_LONG)
            .desc(".tsv input file containing at least a chromosome, position, reference and alternative column.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_MAPPINGS)
            .hasArg(true)
            .required()
            .longOpt(OPT_MAPPINGS_LONG)
            .desc("Mapping for position columns, comma separated key=value; CHROM, POS, REF, ALT and optionally STOP.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_OUTPUT)
            .hasArg(true)
            .required()
            .longOpt(OPT_OUTPUT_LONG)
            .desc("Output file")
            .build());
    appOptions.addOption(
        Option.builder(OPT_FORCE)
            .longOpt(OPT_FORCE_LONG)
            .desc("Override the output files if it already exists.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_DEBUG)
            .longOpt(OPT_DEBUG_LONG)
            .desc("Enable debug mode (additional logging).")
            .build());
    APP_OPTIONS = appOptions;
    Options appVersionOptions = new Options();
    appVersionOptions.addOption(
        Option.builder(OPT_VERSION)
            .required()
            .longOpt(OPT_VERSION_LONG)
            .desc("Print version.")
            .build());
    APP_VERSION_OPTIONS = appVersionOptions;
  }

  private AppCommandLineOptions() {}

  static Options getAppOptions() {
    return APP_OPTIONS;
  }

  static Options getAppVersionOptions() {
    return APP_VERSION_OPTIONS;
  }

  static void validateCommandLine(CommandLine commandLine) {
    validateInput(commandLine);
    validateOutput(commandLine);
  }

  private static void validateInput(CommandLine commandLine) {
    Path inputPath = Path.of(commandLine.getOptionValue(OPT_INPUT));
    validatePath(inputPath);
  }

  private static void validatePath(Path inputPath) {
    if (!Files.exists(inputPath)) {
      throw new IllegalArgumentException(
          format("File '%s' does not exist.", inputPath.toString()));
    }
    if (Files.isDirectory(inputPath)) {
      throw new IllegalArgumentException(
          format("File '%s' is a directory.", inputPath.toString()));
    }
    if (!Files.isReadable(inputPath)) {
      throw new IllegalArgumentException(
          format("File '%s' is not readable.", inputPath.toString()));
    }
    String inputPathStr = inputPath.toString();
    if (!(inputPathStr.endsWith(TSV) || inputPathStr.endsWith(TSV_GZ)) && !(inputPathStr.endsWith(VCF)||inputPathStr.endsWith(VCF_GZ))) {
      throw new IllegalArgumentException(
          format("File '%s' is not a tsv(.gz) or vcf file(.gz).", inputPathStr));
    }
  }

  private static void validateOutput(CommandLine commandLine) {
    if (!commandLine.hasOption(OPT_OUTPUT)) {
      return;
    }

    Path outputPath = Path.of(commandLine.getOptionValue(OPT_OUTPUT));

    String outputPathStr = outputPath.toString();
    if (!(outputPathStr.endsWith(TSV) || outputPathStr.endsWith(TSV_GZ)) && !(outputPathStr.endsWith(VCF)||outputPathStr.endsWith(VCF_GZ))) {
      throw new IllegalArgumentException(
          format("Output file '%s' is not a tsv of vcf file.", outputPathStr));
    }

    if (!commandLine.hasOption(OPT_FORCE) && Files.exists(outputPath)) {
      throw new IllegalArgumentException(
          format("Output file '%s' already exists", outputPath.toString()));
    }
  }
}
