package org.folio.rdf4ld.test;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;

@UtilityClass
public class RdfTestUtil {

  public static Resource createRdfList(Model model, List<Resource> elements) {
    if (elements == null || elements.isEmpty()) {
      return RDF.NIL;
    }

    var modelBuilder = new ModelBuilder(model);
    var listHead = Values.bnode("list0");
    var currentNode = listHead;

    for (int i = 0; i < elements.size(); i++) {
      var element = elements.get(i);
      var nextNode = (i < elements.size() - 1) ? Values.bnode("list" + (i + 1)) : null;

      modelBuilder.subject(currentNode)
        .add(RDF.FIRST, element)
        .add(RDF.REST, (nextNode != null) ? nextNode : RDF.NIL);

      currentNode = nextNode;
    }

    return listHead;
  }

  public static Resource createRdfListFromIris(Model model, String... iriStrings) {
    var elements = new ArrayList<Resource>();
    for (String iriString : iriStrings) {
      elements.add(Values.iri(iriString));
    }
    return createRdfList(model, elements);
  }

  public static Resource createSingleElementList(Model model, Resource element) {
    var listHead = Values.bnode("list");
    var modelBuilder = new ModelBuilder(model);
    modelBuilder.subject(listHead)
      .add(RDF.FIRST, element)
      .add(RDF.REST, RDF.NIL);
    return listHead;
  }

  public static Resource createMixedRdfList(Model model, Resource... elements) {
    return createRdfList(model, List.of(elements));
  }

  public static Resource createBlankNode(String id) {
    return Values.bnode(id);
  }

  public static Resource createIri(String iri) {
    return Values.iri(iri);
  }
}

