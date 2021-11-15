package org.molgenis.vip.converter;

public class MappingException extends RuntimeException {

  private static final String MESSAGE = "Mapping should contain values for at least CHROM, POS, REF and ALT.";

  @Override
  public String getMessage(){
    return MESSAGE;
  }
}
