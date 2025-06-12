package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.rdf4ld.util.ResourceUtil.copyWithoutPreferred;

import java.util.Optional;
import java.util.function.Function;
import org.eclipse.rdf4j.model.Model;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(types = CONCEPT, predicate = SUBJECT)
public class SubjectRdfMapperUnit extends ReferenceRdfMapperUnit {
  private final FingerprintHashService hashService;

  public SubjectRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit,
                              Function<String, Optional<Resource>> resourceProvider,
                              FingerprintHashService hashService) {
    super(baseRdfMapperUnit, resourceProvider);
    this.hashService = hashService;
  }

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource resource,
                          ResourceMapping resourceMapping,
                          Resource parent) {
    var subject = super.mapToLd(model, resource, resourceMapping, parent);
    return subject.isOfType(CONCEPT) ? subject : wrapWithConcept(subject);
  }

  private Resource wrapWithConcept(Resource subject) {
    var concept = new Resource()
      .setLabel(subject.getLabel())
      .setDoc(copyWithoutPreferred(subject))
      .addType(CONCEPT);
    subject.getTypes().forEach(concept::addType);
    concept.addOutgoingEdge(new ResourceEdge(concept, subject, FOCUS));
    concept.setId(hashService.hash(concept));
    return concept;
  }

}
