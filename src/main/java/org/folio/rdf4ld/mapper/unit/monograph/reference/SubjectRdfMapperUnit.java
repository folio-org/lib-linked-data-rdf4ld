package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.eclipse.rdf4j.model.util.Values.bnode;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;
import static org.folio.rdf4ld.util.RdfUtil.writeBlankNode;
import static org.folio.rdf4ld.util.RdfUtil.writeExtraTypes;
import static org.folio.rdf4ld.util.ResourceUtil.addProperty;
import static org.folio.rdf4ld.util.ResourceUtil.copyExcluding;
import static org.folio.rdf4ld.util.ResourceUtil.getCurrentLccnLink;

import java.util.Optional;
import java.util.function.LongFunction;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.dictionary.util.LabelGenerator;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RdfMapperDefinition(predicate = SUBJECT)
public class SubjectRdfMapperUnit extends ReferenceRdfMapperUnit {

  private final ComplexSubjectRdfMapperUnit complexSubjectRdfMapperUnit;

  public SubjectRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit,
                              MockLccnResourceService mockLccnResourceService,
                              FingerprintHashService hashService,
                              CoreLd2RdfMapper coreLd2RdfMapper,
                              LongFunction<String> resourceUrlProvider,
                              ComplexSubjectRdfMapperUnit complexSubjectRdfMapperUnit) {
    super(baseRdfMapperUnit, hashService, mockLccnResourceService, resourceUrlProvider, coreLd2RdfMapper);
    this.complexSubjectRdfMapperUnit = complexSubjectRdfMapperUnit;
  }

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping resourceMapping,
                                    Resource parent) {
    if (complexSubjectRdfMapperUnit.isComplexSubject(model, resource, resourceMapping)) {
      return complexSubjectRdfMapperUnit.processComplexSubject(model, resource, resourceMapping, parent);
    }
    return super.mapToLd(model, resource, resourceMapping, parent)
      .map(subject -> isConceptOrMock(subject) ? subject : wrapWithConcept(subject));
  }

  @Override
  public Resource enrichUnMockedResource(Resource subject) {
    return subject.isOfType(CONCEPT) ? subject : wrapWithConcept(subject);
  }

  private boolean isConceptOrMock(Resource subject) {
    return subject.isOfType(CONCEPT) || mockLccnResourceService.isMockLccnResource(subject);
  }


  private Resource wrapWithConcept(Resource subject) {
    var concept = new Resource()
      .setDoc(copyExcluding(subject, RESOURCE_PREFERRED, LABEL))
      .addType(CONCEPT);
    subject.getTypes().forEach(concept::addType);
    concept.addOutgoingEdge(new ResourceEdge(concept, subject, FOCUS));
    var label = LabelGenerator.generateLabel(concept);
    concept.setLabel(label);
    addProperty(concept.getDoc(), LABEL, label);
    concept.setId(hashService.hash(concept));
    return concept;
  }

  @Override
  public void mapToBibframe(Resource subject,
                            ModelBuilder modelBuilder,
                            ResourceMapping mapping,
                            Resource parent) {
    var parentIri = iri(resourceUrlProvider.apply(parent.getId()));
    var predicate = mapping.getBfResourceDef().getPredicate();

    var conceptLccnLink = getCurrentLccnLink(subject);
    if (conceptLccnLink.isPresent()) {
      linkResources(parentIri, iri(conceptLccnLink.get()), predicate, modelBuilder);
      return;
    }

    var hasSubFocus = subject.getOutgoingEdges().stream()
      .anyMatch(oe -> oe.getPredicate() == SUB_FOCUS);

    if (hasSubFocus) {
      complexSubjectRdfMapperUnit.writeComplexSubject(subject, modelBuilder, mapping, parentIri);
    } else {
      subject.getOutgoingEdges()
        .stream()
        .filter(oe -> oe.getPredicate() == FOCUS)
        .map(ResourceEdge::getTarget)
        .forEach(resource -> writeSingleSubject(resource, modelBuilder, mapping, parentIri));
    }
  }

  private void writeSingleSubject(Resource subject,
                                  ModelBuilder modelBuilder,
                                  ResourceMapping mapping,
                                  org.eclipse.rdf4j.model.Resource parent) {
    var predicate = mapping.getBfResourceDef().getPredicate();
    getCurrentLccnLink(subject)
      .ifPresentOrElse(link -> linkResources(parent, iri(link), predicate, modelBuilder),
        () -> {
          var node = bnode("_" + subject.getId());
          linkResources(parent, node, predicate, modelBuilder);
          writeBlankNode(node, subject, modelBuilder, mapping, coreLd2RdfMapper);
          writeExtraTypes(modelBuilder, subject, node);
        }
      );
  }


}
