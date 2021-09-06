package mu.semte.ch.privacy.centric.config;

import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface FieldConfigWrapper {
  List<FieldConfig> getFields();
  default Optional<FieldConfig> getField(String key, String nsType) {
    return getFields().stream().filter(f -> f.getProperty().equals(key) && f.getNsType().equals(nsType)).findFirst();
  }
}
