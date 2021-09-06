package mu.semte.ch.privacy.centric.jsonapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
  private String type; // read, update
  @Builder.Default
  private Map<String, String> attributes = new HashMap<>();
  @Builder.Default
  private List<Map<String, String>> relationships = new ArrayList<>();
}
