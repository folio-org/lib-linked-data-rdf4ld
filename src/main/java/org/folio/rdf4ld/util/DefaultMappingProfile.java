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
    return new MappingProfile(
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
      Set.of("http://id.loc.gov/ontologies/bibframe/Instance"),
      "http://id.loc.gov/ontologies/bibframe/instances/",
      new LdResourceDef(Set.of(INSTANCE), null),
      getInstanceMapping()
    );
  }

  private static ResourceMapping getInstanceMapping() {
    return new ResourceMapping(
      Set.of(
        new PropertyMapping(DIMENSIONS, "http://id.loc.gov/ontologies/bibframe/dimensions", null, null, null),
        new PropertyMapping(STATEMENT_OF_RESPONSIBILITY, "http://id.loc.gov/ontologies/bibframe/responsibilityStatement", null, null, null)
      ),
      Set.of(
        new EdgeMapping(getTitleMapping(),
          false,
          "http://id.loc.gov/sources/titles/",
          new LdResourceDef(Set.of(TITLE), PredicateDictionary.TITLE),
          new BfResourceDef(Set.of("http://id.loc.gov/ontologies/bibframe/Title"), "http://id.loc.gov/ontologies/bibframe/title")
        ),
        new EdgeMapping(getTitleMapping(),
          false,
          "http://id.loc.gov/sources/titles/",
          new LdResourceDef(Set.of(VARIANT_TITLE), PredicateDictionary.TITLE),
          new BfResourceDef(Set.of("http://id.loc.gov/ontologies/bibframe/VariantTitle"), "http://id.loc.gov/ontologies/bibframe/title")
        )
      ),
      Set.of(
        new EdgeMapping(getWorkMapping(),
          true,
          "http://id.loc.gov/ontologies/bibframe/works",
          new LdResourceDef(Set.of(WORK), PredicateDictionary.INSTANTIATES),
          new BfResourceDef(Set.of("http://id.loc.gov/ontologies/bibframe/Work"), "http://id.loc.gov/ontologies/bibframe/instanceOf")
        )
      )
    );
  }

  private static ResourceMapping getTitleMapping() {
    return new ResourceMapping(
      Set.of(
        new PropertyMapping(MAIN_TITLE, "http://id.loc.gov/ontologies/bibframe/mainTitle", null, null, null),
        new PropertyMapping(SUBTITLE, "http://id.loc.gov/ontologies/bibframe/subtitle", null, null, null)
      ),
      Set.of(),
      Set.of()
    );
  }

  private static ResourceMapping getWorkMapping() {
    return new ResourceMapping(
      Set.of(
        new PropertyMapping(LABEL, "http://www.w3.org/2000/01/rdf-schema#label", null, null, null)
      ),
      Set.of(),
      Set.of()
    );
  }
}
