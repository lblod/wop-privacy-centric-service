package mu.semte.ch.privacy.centric.jsonapi;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mu.semte.ch.lib.utils.ModelUtils;


@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Type("genders")
public class Gender {

  @Builder.Default
  @Id
  private String id = ModelUtils.uuid();
}
