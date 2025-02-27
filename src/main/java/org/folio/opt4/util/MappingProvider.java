package org.folio.opt4.util;


import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.opt4.mapping.ResourceMapping.EdgeMapping;
import static org.folio.opt4.mapping.ResourceMapping.PropertyMapping;

import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.opt4.mapping.BfResourceDef;
import org.folio.opt4.mapping.LdResourceDef;
import org.folio.opt4.mapping.ResourceMapping;
import org.folio.opt4.mapping.TopMapping;

// This is just a dummy properties provider; instead of this class we will use Spring PropertySource functionality
public class MappingProvider {

    public static TopMapping getTopMapping() {
        return new TopMapping("http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                Set.of(ResourceTypeDictionary.INSTANCE),
                Set.of("http://id.loc.gov/ontologies/bibframe/Instance"),
                "http://id.loc.gov/ontologies/bibframe/instances/"
        );
    }

    public static ResourceMapping getInstanceMapping() {
        return new ResourceMapping(
                Set.of(
                        new PropertyMapping(DIMENSIONS, "http://id.loc.gov/ontologies/bibframe/dimensions", null, null, null),
                        new PropertyMapping(STATEMENT_OF_RESPONSIBILITY, "http://id.loc.gov/ontologies/bibframe/responsibilityStatement", null, null, null)
                ),
                Set.of(
                        new EdgeMapping(false,
                                "http://id.loc.gov/sources/titles/",
                                new LdResourceDef(Set.of(ResourceTypeDictionary.TITLE), PredicateDictionary.TITLE),
                                new BfResourceDef(Set.of("http://id.loc.gov/ontologies/bibframe/Title"), "http://id.loc.gov/ontologies/bibframe/title")
                        ),
                        new EdgeMapping(false,
                                "http://id.loc.gov/sources/titles/",
                                new LdResourceDef(Set.of(ResourceTypeDictionary.VARIANT_TITLE), PredicateDictionary.TITLE),
                                new BfResourceDef(Set.of("http://id.loc.gov/ontologies/bibframe/VariantTitle"), "http://id.loc.gov/ontologies/bibframe/title")
                        )
                ),
                Set.of(
                        new EdgeMapping(true,
                                "http://id.loc.gov/ontologies/bibframe/works",
                                new LdResourceDef(Set.of(ResourceTypeDictionary.WORK), PredicateDictionary.INSTANTIATES),
                                new BfResourceDef(Set.of("http://id.loc.gov/ontologies/bibframe/Work"), "http://id.loc.gov/ontologies/bibframe/instanceOf")
                        )
                )
        );
    }

    public static ResourceMapping getTitleMapping() {
        return new ResourceMapping(
                Set.of(
                        new PropertyMapping(MAIN_TITLE, "http://id.loc.gov/ontologies/bibframe/mainTitle", null, null, null),
                        new PropertyMapping(SUBTITLE, "http://id.loc.gov/ontologies/bibframe/subtitle", null, null, null)
                ),
                Set.of(),
                Set.of()
        );
    }

    public static ResourceMapping getWorkMapping() {
        return new ResourceMapping(
                Set.of(
                        new PropertyMapping(LABEL, "http://www.w3.org/2000/01/rdf-schema#label", null, null, null)
                ),
                Set.of(),
                Set.of()
        );
    }
}
