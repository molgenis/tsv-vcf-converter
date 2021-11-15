package org.molgenis.vip.converter.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Mapping {
  int chromIdx;
  int posIdx;
  int refIdx;
  int altIdx;
  @Builder.Default
  int stopIdx = -1;

  public boolean containsStop(){
    return stopIdx != -1;
  }
}