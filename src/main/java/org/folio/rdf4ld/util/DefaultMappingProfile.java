package org.folio.rdf4ld.util;

import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.EdgeMapping;
import org.folio.rdf4ld.model.LdResourceDef;
import org.folio.rdf4ld.model.MappingProfile;
import org.folio.rdf4ld.model.PropertyMapping;
import org.folio.rdf4ld.model.ResourceMapping;

public class DefaultMappingProfile {

  public static MappingProfile get() {
    return new MappingProfile()
      .typeIri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
      .topBfTypeSet(Set.of("http://id.loc.gov/ontologies/bibframe/Instance"))
      .topBfNameSpace("http://id.loc.gov/ontologies/bibframe/instances/")
      .topLdDef(new LdResourceDef().typeSet(Set.of(INSTANCE)))
      .topMapping(getInstanceMapping());
  }

  private static ResourceMapping getInstanceMapping() {
    return new ResourceMapping()
      .properties(Set.of(
        new PropertyMapping()
          .ldProperty(DIMENSIONS)
          .bfProperty("http://id.loc.gov/ontologies/bibframe/dimensions"),
        new PropertyMapping()
          .ldProperty(STATEMENT_OF_RESPONSIBILITY)
          .bfProperty("http://id.loc.gov/ontologies/bibframe/responsibilityStatement")
      ))
      .outgoingEdges(Set.of(
        new EdgeMapping()
          .resourceMapping(getTitleMapping())
          .fetchRemote(false)
          .bfNameSpace("http://id.loc.gov/sources/titles/")
          .ldResourceDef(new LdResourceDef()
            .typeSet(Set.of(TITLE))
            .predicate(PredicateDictionary.TITLE)
          )
          .bfResourceDef(new BfResourceDef()
            .typeSet(Set.of("http://id.loc.gov/ontologies/bibframe/Title"))
            .predicate("http://id.loc.gov/ontologies/bibframe/title")
          ),
        new EdgeMapping()
          .resourceMapping(getTitleMapping())
          .fetchRemote(false)
          .bfNameSpace("http://id.loc.gov/sources/titles/")
          .ldResourceDef(new LdResourceDef()
            .typeSet(Set.of(VARIANT_TITLE))
            .predicate(PredicateDictionary.TITLE)
          )
          .bfResourceDef(new BfResourceDef()
            .typeSet(Set.of("http://id.loc.gov/ontologies/bibframe/VariantTitle"))
            .predicate("http://id.loc.gov/ontologies/bibframe/title")
          )
      ))
      .incomingEdges(Set.of(
        new EdgeMapping()
          .resourceMapping(getWorkMapping())
          .fetchRemote(true)
          .bfNameSpace("http://id.loc.gov/ontologies/bibframe/works")
          .ldResourceDef(new LdResourceDef()
            .typeSet(Set.of(WORK))
            .predicate(PredicateDictionary.INSTANTIATES)
          )
          .bfResourceDef(new BfResourceDef()
            .typeSet(Set.of("http://id.loc.gov/ontologies/bibframe/Work"))
            .predicate("http://id.loc.gov/ontologies/bibframe/instanceOf")
          )
      ));
  }

  private static ResourceMapping getTitleMapping() {
    return new ResourceMapping()
      .properties(Set.of(
        new PropertyMapping()
          .ldProperty(MAIN_TITLE)
          .bfProperty("http://id.loc.gov/ontologies/bibframe/mainTitle"),
        new PropertyMapping()
          .ldProperty(SUBTITLE)
          .bfProperty("http://id.loc.gov/ontologies/bibframe/subtitle")
      ));
  }

  private static ResourceMapping getWorkMapping() {
    return new ResourceMapping()
      .properties(Set.of(
        new PropertyMapping()
          .ldProperty(LABEL)
          .bfProperty("http://www.w3.org/2000/01/rdf-schema#label")
      ));
  }
}
