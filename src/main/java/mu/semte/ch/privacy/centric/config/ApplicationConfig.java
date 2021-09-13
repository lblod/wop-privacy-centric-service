package mu.semte.ch.privacy.centric.config;

import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.config.CoreConfig;
import mu.semte.ch.privacy.centric.jsonapi.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CoreConfig.class)
@Slf4j
public class ApplicationConfig {

  @Value("${sparql.baseUrl}")
  private String baseUrl;

  @Bean
  public ResourceConverter resourceConverter() {
    return new ResourceConverter(baseUrl,
                                 Gender.class, Nationality.class, Person.class, PersonInformationUpdate.class,
                                 PersonInformationRequest.class,
                                 RequestReason.class);
  }

}
