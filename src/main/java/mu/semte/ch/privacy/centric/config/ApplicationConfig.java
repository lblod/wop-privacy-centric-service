package mu.semte.ch.privacy.centric.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.config.CoreConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

import static mu.semte.ch.lib.utils.ModelUtils.filenameToLang;
import static mu.semte.ch.lib.utils.ModelUtils.toModel;

@Configuration
@Import(CoreConfig.class)
@Slf4j
public class ApplicationConfig {
  @Value("${fields.config}")
  private Resource fieldConfigFile;

  @Bean
  public FieldConfigWrapper fieldConfigWrapper() throws IOException {
    var mapper = new ObjectMapper();
    var config = List.of(mapper.readValue(fieldConfigFile.getInputStream(), FieldConfig[].class));
    return () -> config;
  }

  public static void main(String[] args) {
  }
}
