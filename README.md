# lib-linked-data-rdf4ld
Copyright (C) 2025 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file "[LICENSE](LICENSE)" for more information.

## Third party libraries used in this software
This software uses the following BSD-3-Clause licensed software library:
- [rdf4j](https://github.com/eclipse-rdf4j/rdf4j)

This software uses the following Weak Copyleft (Eclipse Public License 1.0 / 2.0) licensed software libraries:
- [jakarta.annotation-api](https://projects.eclipse.org/projects/ee4j.ca)
- [jakarta.json-api](https://github.com/jakartaee/jsonp-api)
- [junit](https://junit.org/)

## Purpose
Lib-linked-data-rdf4ld is a Java library designed for converting RDF files with BIBFRAME records to Linked Data Graphs and vice versa.
## Compiling
```bash
mvn clean install
```
## Using the library

### Convert Bibframe 2 RDF to Linked Data Graph
```java
@Service
public class MyService {
  private final Rdf4LdService rdf4LdService;

  public MyService(Rdf4LdService rdf4LdService) {
    this.rdf4LdService = rdf4LdService;
  }

  public void convertBibframe2RdfToLinkedDataGraph() {
    InputStream rdfInputStream = ...;
    var contentType = "application/ld+json";
    var result = rdf4LdService.mapBibframe2RdfToLd(rdfInputStream, contentType);
    // Process the result as needed
  }
}
```

### Convert Linked Data Graph to Bibframe 2 RDF
```java
@Service
public class MyService {
  private final Rdf4LdService rdf4LdService;

  public MyService(Rdf4LdService rdf4LdService) {
    this.rdf4LdService = rdf4LdService;
  }

  public void convertLinkedDataGraphToBibframe2Rdf() {
    Resource linkedDataGraph = ...;
    var rdfFormat = RDFFormat.JSONLD;
    var result = rdf4LdService.mapLdToBibframe2Rdf(linkedDataGraph, rdfFormat);
    // Process the result as needed
  }
}
```

### Dependencies
- [lib-linked-data-dictionary](https://github.com/folio-org/lib-linked-data-dictionary)
- [lib-linked-data-fingerprint](https://github.com/folio-org/lib-linked-data-fingerprint)

## Download and configuration
The built artifacts for this module are available. See [configuration](https://dev.folio.org/download/artifacts/) for repository access.

## Mapping development

The primary mechanism to map between Linked Data and BIBFRAME is through configuration profiles in JSON, found under `src/main/resources/mappingProfile/bibframe2.0`. The JSON schema for these files is defined in the `model/` subdirectory.

In general, the expectation when using configuration profiles is that the overall graph shape is nearly identical independent of vocabulary specifics, with only the terms changing (though properties can become predicates, altering the shape). For more complex transformations, you can add a new Java class that supplements the configuration profile.

### Adding a new mapping

Define the new mapping and add it to the directory above. Also update `org.folio.rdf4ld.util.MappingProfileReader`, defining a new static constant pointing to the added file and adding it to the appropriate work and/or instance section.

### Schema terms

#### `ldResourceDef`

Top level. Matches a Linked Data resource by the incoming predicate and optionally by its set of types, either as an exact match or a required subset. Leave out the type set entirely if not relevant.

See [typeSet](#typeSet), [partialTypesMatch](#partialTypesMatch), [predicate](#predicate).

#### `bfResourceDef`

Top level. Matches a BIBFRAME resource by the incoming predicate and optionally by its full set of types.

See [typeSet](#typeSet), [predicate](#predicate).

#### `localOnly`

*Not implemented*

#### `resourceMapping`

Top level, can be used recursively. Add mappings for properties and outgoing predicates.

See [label](#label), [properties](#properties), [outgoingEdges](#outgoingedges).

#### `label`

An array of Linked Data properties to use as a BIBFRAME label. Use constants from the `PropertyDictionary`. Defaults to `LABEL` and `LABEL_RDF` when not set.

#### `properties`

An array of property mappings.

See [ldProperty](#ldproperty), [bfProperty](#bfproperty), [edgeParentBfDef](#edgeparentbfdef), [outgoingEdgeParentLdDef](#outgoingedgeparentlddef), [incomingEdgeParentLdDef](#incomingedgeparentlddef).

#### `outgoingEdges`

An array of predicate mappings.

See [ldResourceDef](#ldresourcedef), [bfResourceDef](#bfresourcedef), [resourceMapping](#resourcemapping).

#### `typeSet`

An array of types to match against. For `ldResourceDef`, you can use the constants from the `ResourceTypesDictionary`. For `bfResourceDef`, use the full term URIs.

#### `partialTypesMatch`

Set to true to indicate a matching types set is not exhaustive. Other types can be used outside of the defined types set and still match. The default is false, which indicates an exact match of the set of types is required; additional types on a candidate would result in no match.

#### `predicate`

An incoming predicate to match against. For `ldResourceDef`, you can use the constants from the `PredicateDictionary`. For `bfResourceDef`, use the full term URI.

#### `ldProperty`

Match a Linked Data property. Use the constants from the `PropertyDictionary`.

#### `bfProperty`

Match a BIBFRAME property. Use the full term URI.

#### `edgeParentBfDef`

An `ldResourceDef`. Instead of transforming to a BIBFRAME property with a value, transform into a predicate with a resource as described by the `ldResourceDef`, and the new resource will have a `bfProperty` property.

See [ldResourceDef](#ldresourcedef).

#### `outgoingEdgeParentLdDef`

*Not implemented*

#### `incomingEdgeParentLdDef`

*Not implemented*

### Adding a new class

The class must be annotated as an `@RdfMapperDefinition`. The annotation can take `types` and `predicate` arguments, at least one of which must be present. `types` should be one or more `ResourceTypesDictionary` values to match a candidate resource against, `predicate` should be one value from `PredicateDictionary` to match against.

The class must implement `RdfMapperUnit` or an abstract subclass of it, overriding `mapToLd` and `mapToBibframe`. Each should build on work done by a configuration profile. It may be useful to call `BaseRdfMapperUnit`'s implemntation of these methods, which do a generic copy from one model to the other, following the configuration profile first. You can then further modify the profile output to better fit the desired end state, or you can leave out the base call and build the output from scratch.