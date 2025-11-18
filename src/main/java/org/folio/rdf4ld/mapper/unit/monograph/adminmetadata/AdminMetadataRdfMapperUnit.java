package org.folio.rdf4ld.mapper.unit.monograph.adminmetadata;

import static org.eclipse.rdf4j.model.util.Values.bnode;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.PropertyDictionary.CONTROL_NUMBER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;

import java.util.Optional;
import java.util.UUID;
import java.util.function.LongFunction;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
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
  private static final String BF_LOCAL_TYPE = "http://id.loc.gov/ontologies/bibframe/Local";
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
    // TODO - remove any identifiedBy/Local statements since local IDs are for the original context
    return baseRdfMapperUnit.mapToLd(model, resource, resourceMapping, parent);
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            Resource parent) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping, parent);
    addLocalIdentifierAndNote(resource, modelBuilder);
  }
  
  private void addLocalIdentifierAndNote(Resource resource, ModelBuilder modelBuilder) {
    if (resource.getDoc().has(CONTROL_NUMBER.getValue())) {
      var resourceIri = iri(resourceUrlProvider.apply(resource.getId()));
      resource.getDoc().get(CONTROL_NUMBER.getValue()).iterator().forEachRemaining(
        controlNumberNode -> {
          var controlNumber = controlNumberNode.asText();
          var local = bnode(controlNumber);
          modelBuilder.add(local, RDF.TYPE, iri(BF_LOCAL_TYPE));
          modelBuilder.add(local, RDF.VALUE, controlNumber);
          var note = bnode("note_" + controlNumber);
          modelBuilder.add(note, RDF.TYPE, iri(BF_NOTE_TYPE));
          if (isUuid(controlNumber)) {
            modelBuilder.add(note, RDFS.LABEL, FOLIO_UUID);
          } else {
            modelBuilder.add(note, RDFS.LABEL, FOLIO_HRID);
          }
          linkResources(local, note, BF_NOTE, modelBuilder);
          linkResources(resourceIri, local, BF_IDENTIFIED_BY, modelBuilder);
        }
      );
    }
  }

  private boolean isUuid(String id) {
    try {
      UUID.fromString(id);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
