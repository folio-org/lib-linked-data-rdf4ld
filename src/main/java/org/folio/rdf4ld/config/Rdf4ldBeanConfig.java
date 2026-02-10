package org.folio.rdf4ld.config;

import static java.util.Optional.of;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;

import java.util.Optional;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.label.LabelGeneratorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class Rdf4ldBeanConfig {

  @Bean
  @ConditionalOnMissingBean(name = "resourceUrlProvider")
  public LongFunction<String> dummyResourceUrlProvider() {
    log.warn("No resourceUrlProvider bean has been found, using the dummy one");
    return id -> "http://test-tobe-changed.folio.com/resources/" + id;
  }

  @Bean
  @ConditionalOnMissingBean(name = "defaultWorkTypeProvider")
  public Supplier<Optional<ResourceTypeDictionary>> dummyDefaultWorkTypeProvider() {
    log.warn("No defaultWorkTypeProvider bean has been found, using the dummy one");
    return () -> of(BOOKS);
  }

  @Bean
  public LabelGeneratorService labelGeneratorService() {
    return new LabelGeneratorService();
  }

}
