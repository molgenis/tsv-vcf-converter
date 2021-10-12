package org.molgenis.vip.converter;

public class MissingLengthFieldException extends RuntimeException {

  private static final String MESSAGE = "Mapping for STOP present but missing LENGTH header.";

  @Override
  public String getMessage(){
    return MESSAGE;
  }
}
