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
public class ApiRequest {
  private String operation; // read, update
  private String type;
  private String requester;
  private String reason;
  private String property;
  @Builder.Default
  private Map<String, String> attributes = new HashMap<>();
  @Builder.Default
  private List<Map<String, String>> relationships = new ArrayList<>();
}
