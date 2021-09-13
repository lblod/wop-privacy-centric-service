package mu.semte.ch.privacy.centric.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mu.semte.ch.lib.utils.ModelUtils;

import java.util.Collection;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Type("person-information-updates")
public class PersonInformationUpdate {

  @Builder.Default
  @Id
  private String id = ModelUtils.uuid();
  @JsonProperty("date-of-birth")
  private String dateOfBirth;

  @JsonProperty("registration")
  private String registrationNumber;
  @Relationship("reason")
  private RequestReason reason;

  @Relationship("person")
  private Person person;
  @Relationship("nationalities")
  private Collection<Nationality> nationalities;
  @Relationship("gender")
  private Gender gender;
}
