package org.folio.rdf4ld.mapper.unit.monograph.adminmetadata;

import static org.eclipse.rdf4j.model.util.Values.bnode;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.PropertyDictionary.CONTROL_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.FOLIO_INVENTORY_ID;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;

import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(predicate = ADMIN_METADATA, types = ANNOTATION)
public class AdminMetadataRdfMapperUnit implements RdfMapperUnit {

  private static final String BF_IDENTIFIED_BY = "http://id.loc.gov/ontologies/bibframe/identifiedBy";
  private static final String BF_NOTE = "http://id.loc.gov/ontologies/bibframe/note";
  private static final String BF_NOTE_TYPE = "http://id.loc.gov/ontologies/bibframe/Note";
  private static final String FOLIO_HRID = "FOLIO HRID";
  private static final String FOLIO_UUID = "FOLIO Inventory UUID";

  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final LongFunction<String> resourceUrlProvider;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping resourceMapping,
                                    Resource parent) {
    // Do not process admin metadata coming from BIBFRAME.
    // Values currently would either not transfer contexts (like local identifiers
    // not applying to this system) or would be expected to be overwritten by this
    // system (like creation date).
    return Optional.empty();
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            Resource parent) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping, parent);
    addLocalIdentifiers(resource, modelBuilder);
  }

  private void addLocalIdentifiers(Resource resource, ModelBuilder modelBuilder) {
    addLocalIdentifierNote(resource, modelBuilder, CONTROL_NUMBER.getValue(), FOLIO_HRID);
    addLocalIdentifierNote(resource, modelBuilder, FOLIO_INVENTORY_ID.getValue(), FOLIO_UUID);
  }

  private void addLocalIdentifierNote(Resource resource,
                                      ModelBuilder modelBuilder,
                                      String property,
                                      String note) {
    if (resource.getDoc().has(property)) {
      var resourceIri = iri(resourceUrlProvider.apply(resource.getId()));
      resource.getDoc().get(property).iterator().forEachRemaining(
        identifierResource -> {
          var amNodes = modelBuilder.build().getStatements(
              resourceIri, iri(BF_IDENTIFIED_BY), null);
          StreamSupport.stream(amNodes.spliterator(), false)
            .map(Statement::getObject)
            .filter(Value::isResource)
            .map(org.eclipse.rdf4j.model.Resource.class::cast)
            .forEach(identifierNode -> {
              var identifiersModel = modelBuilder.build().getStatements(identifierNode, RDF.VALUE, null);
              StreamSupport.stream(identifiersModel.spliterator(), false)
                .map(Statement::getObject)
                .map(Value::stringValue)
                .filter(objVal -> objVal.equals(identifierResource.asString()))
                .forEach(id -> {
                  var noteNode = bnode("note_" + id);
                  modelBuilder.add(noteNode, RDF.TYPE, iri(BF_NOTE_TYPE));
                  modelBuilder.add(noteNode, RDFS.LABEL, note);
                  linkResources(identifierNode, noteNode, BF_NOTE, modelBuilder);
                });
            });
        });
    }
  }
}
