package org.folio.rdf4ld.mapper.unit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RdfMapperDefinition {

  ResourceTypeDictionary[] types();

  PredicateDictionary predicate() default PredicateDictionary.NULL;

}
