package mu.semte.ch.privacy.centric.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldConfig {
  public enum Cardinality {ONE, MANY}
  private String nsType;
  private String property;
  private Cardinality cardinality;
}
