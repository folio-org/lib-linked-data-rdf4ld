package org.folio.rdf4ld.config;

import org.folio.rdf4ld.util.ExportedResourceModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {
  @Bean
  public ExportedResourceModule resourceModule() {
    return new ExportedResourceModule();
  }
}
