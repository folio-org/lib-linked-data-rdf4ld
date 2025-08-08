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
