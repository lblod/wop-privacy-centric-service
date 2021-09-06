package mu.semte.ch.privacy.centric.jsonapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mu.semte.ch.lib.utils.JacksonRawDeserialize;

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
  private String property;
  private String type;
  //private String requester;
  //private String reason;

  @JsonDeserialize(using= JacksonRawDeserialize.class)
  private String data;
  //private Map<String, String> attributes = new HashMap<>();
  //private List<Map<String, String>> relationships = new ArrayList<>();
}
