package org.folio.rdf4ld.test;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.folio.rdf4ld", "org.folio.ld.fingerprint"})
public class SpringTestConfig {
}
