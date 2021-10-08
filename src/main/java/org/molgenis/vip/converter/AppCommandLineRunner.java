package org.molgenis.vip.converter;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.vip.converter.AppCommandLineOptions.OPT_DEBUG;
import static org.molgenis.vip.converter.AppCommandLineOptions.OPT_FORCE;
import static org.molgenis.vip.converter.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vip.converter.AppCommandLineOptions.OPT_MAPPINGS;
import static org.molgenis.vip.converter.AppCommandLineOptions.OPT_OUTPUT;
import static org.molgenis.vip.converter.model.Constants.ALT;
import static org.molgenis.vip.converter.model.Constants.CHROM;
import static org.molgenis.vip.converter.model.Constants.POS;
import static org.molgenis.vip.converter.model.Constants.REF;
import static org.molgenis.vip.converter.model.Constants.TSV;
import static org.molgenis.vip.converter.model.Constants.TSV_GZ;
import static org.molgenis.vip.converter.model.Constants.VCF;

import ch.qos.logback.classic.Level;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.molgenis.vip.converter.model.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class AppCommandLineRunner implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppCommandLineRunner.class);

  private static final int STATUS_MISC_ERROR = 1;
  private static final int STATUS_COMMAND_LINE_USAGE_ERROR = 64;
  public static final String KEY_VALUE_PATTERN = "(\\w+)=(.*?)(?=,\\w+=|$)";

  private final String appName;
  private final String appVersion;
  private final CommandLineParser commandLineParser;

  AppCommandLineRunner(
      @Value("${app.name}") String appName,
      @Value("${app.version}") String appVersion) {
    this.appName = requireNonNull(appName);
    this.appVersion = requireNonNull(appVersion);
    this.commandLineParser = new DefaultParser();
  }

  @Override
  public void run(String... args) {
    if (args.length == 1
        && (args[0].equals("-" + AppCommandLineOptions.OPT_VERSION)
        || args[0].equals("--" + AppCommandLineOptions.OPT_VERSION_LONG))) {
      LOGGER.info("{} {}", appName, appVersion);
      return;
    }

    for (String arg : args) {
      if (arg.equals('-' + OPT_DEBUG) || arg.equals('-' + AppCommandLineOptions.OPT_DEBUG_LONG)) {
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (!(rootLogger instanceof ch.qos.logback.classic.Logger)) {
          throw new ClassCastException("Expected root logger to be a logback logger");
        }
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.DEBUG);
        break;
      }
    }

    CommandLine commandLine = getCommandLine(args);
    AppCommandLineOptions.validateCommandLine(commandLine);
    Settings settings = mapSettings(commandLine);
    try {
      if (settings.getInput().toString().endsWith(TSV) || settings.getInput().toString().endsWith(
          TSV_GZ)) {
        Tsv2VcfConverter tsv2VcfConverter = new Tsv2VcfConverter();
        tsv2VcfConverter.convert(settings);
      } else {
        Vcf2TsvConverter vcf2TsvConverter = new Vcf2TsvConverter();
        vcf2TsvConverter.convert(settings);
      }
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage(), e);
      System.exit(STATUS_MISC_ERROR);
    }
  }


  private Settings mapSettings(CommandLine commandLine) {
    String inputPathValue = commandLine.getOptionValue(OPT_INPUT);
    Path inputPath = Path.of(inputPathValue);
    String mappingValue = commandLine.getOptionValue(OPT_MAPPINGS);
    Map<String, String> mappings = mapMappings(mappingValue);
    Path outputPath;
    if (commandLine.hasOption(OPT_OUTPUT)) {
      outputPath = Path.of(commandLine.getOptionValue(OPT_OUTPUT));
    } else {
      if (inputPath.endsWith(TSV) || inputPath.endsWith(TSV_GZ)) {
        outputPath = Path.of(inputPath.toString().replace(TSV, VCF));
      } else {
        outputPath = Path.of(inputPath.toString().replace(VCF, TSV));
      }
    }
    boolean overwriteOutput = commandLine.hasOption(OPT_FORCE);

    boolean debugMode = commandLine.hasOption(OPT_DEBUG);

    return Settings.builder()
        .input(inputPath)
        .mappings(mappings)
        .output(outputPath)
        .overwrite(overwriteOutput)
        .debug(debugMode)
        .build();
  }

  private Map<String, String> mapMappings(String mappingValue) {
    Map<String, String> attr = new HashMap<>();
    Matcher m = Pattern.compile(KEY_VALUE_PATTERN).matcher(mappingValue);
    while (m.find()) {
      attr.put(m.group(1), m.group(2));
    }

    if (!attr.containsKey(CHROM) || !attr.containsKey(POS) || !attr.containsKey(REF) || !attr
        .containsKey(ALT)) {
      throw new MappingException();
    }

    return attr;
  }

  private CommandLine getCommandLine(String[] args) {
    CommandLine commandLine = null;
    try {
      commandLine = commandLineParser.parse(AppCommandLineOptions.getAppOptions(), args);
    } catch (ParseException e) {
      logException(e);
      System.exit(STATUS_COMMAND_LINE_USAGE_ERROR);
    }
    return commandLine;
  }

  @SuppressWarnings("java:S106")
  private void logException(ParseException e) {
    LOGGER.error(e.getLocalizedMessage(), e);

    // following information is only logged to system out
    System.out.println();
    HelpFormatter formatter = new HelpFormatter();
    formatter.setOptionComparator(null);
    String cmdLineSyntax = "java -jar " + appName + ".jar";
    formatter.printHelp(cmdLineSyntax, AppCommandLineOptions.getAppOptions(), true);
    System.out.println();
    formatter.printHelp(cmdLineSyntax, AppCommandLineOptions.getAppVersionOptions(), true);
  }
}
