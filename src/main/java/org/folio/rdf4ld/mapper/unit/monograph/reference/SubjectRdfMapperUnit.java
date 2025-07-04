package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.rdf4ld.util.RdfUtil.AGENTS_NAMESPACE;
import static org.folio.rdf4ld.util.RdfUtil.SUBJECTS_NAMESPACE;
import static org.folio.rdf4ld.util.ResourceUtil.copyWithoutPreferred;
import static org.folio.rdf4ld.util.ResourceUtil.getCurrentLccn;
import static org.folio.rdf4ld.util.ResourceUtil.isAgent;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.util.ResourceUtil;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(predicate = SUBJECT)
public class SubjectRdfMapperUnit extends ReferenceRdfMapperUnit {
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final FingerprintHashService hashService;

  public SubjectRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit,
                              Function<String, Optional<Resource>> resourceProvider,
                              FingerprintHashService hashService,
                              CoreLd2RdfMapper coreLd2RdfMapper) {
    super(baseRdfMapperUnit, resourceProvider);
    this.hashService = hashService;
    this.coreLd2RdfMapper = coreLd2RdfMapper;
  }

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping resourceMapping,
                                    Resource parent) {
    return super.mapToLd(model, resource, resourceMapping, parent)
      .map(subject -> subject.isOfType(CONCEPT) ? subject : wrapWithConcept(subject));
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

  @Override
  public void mapToBibframe(Resource subject,
                            ModelBuilder modelBuilder,
                            ResourceMapping mapping,
                            Resource parent) {
    boolean directLccnWritten = writeDirectLccn(subject, modelBuilder, mapping, parent);
    if (!directLccnWritten) {
      writeConceptSubjectLccn(subject, modelBuilder, mapping, parent);
    }
  }

  private boolean writeDirectLccn(Resource subject,
                                  ModelBuilder modelBuilder,
                                  ResourceMapping mapping,
                                  Resource parent) {
    var currentLccn = getCurrentLccn(subject);
    currentLccn.ifPresent(writeSubjectLink(subject, modelBuilder, mapping, parent));
    return currentLccn.isPresent();
  }

  private void writeConceptSubjectLccn(Resource subject,
                                       ModelBuilder modelBuilder,
                                       ResourceMapping mapping,
                                       Resource parent) {
    Stream.of(subject)
      .map(Resource::getOutgoingEdges)
      .filter(noSubFocuses())
      .flatMap(Set::stream)
      .filter(oe -> oe.getPredicate() == FOCUS)
      .map(ResourceEdge::getTarget)
      .map(ResourceUtil::getCurrentLccn)
      .filter(Optional::isPresent)
      .flatMap(Optional::stream)
      .forEach(writeSubjectLink(subject, modelBuilder, mapping, parent));
  }

  private Predicate<Set<ResourceEdge>> noSubFocuses() {
    return outgoingEdges -> outgoingEdges.stream().noneMatch(oe -> oe.getPredicate() == SUB_FOCUS);
  }

  private Consumer<String> writeSubjectLink(Resource subject,
                                            ModelBuilder modelBuilder,
                                            ResourceMapping mapping,
                                            Resource parent) {
    return lccn -> {
      var subjectIri = isAgent(subject)
        ? Values.iri(AGENTS_NAMESPACE, lccn)
        : Values.iri(SUBJECTS_NAMESPACE, lccn);
      modelBuilder.subject(coreLd2RdfMapper.getResourceIri(parent.getId().toString()));
      modelBuilder.add(mapping.getBfResourceDef().getPredicate(), subjectIri);
    };
  }

}
