package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.eclipse.rdf4j.model.util.Values.bnode;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.rdf4ld.util.MappingUtil.getEdgeMapping;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;
import static org.folio.rdf4ld.util.RdfUtil.writeBlankNode;
import static org.folio.rdf4ld.util.RdfUtil.writeExtraTypes;
import static org.folio.rdf4ld.util.ResourceUtil.copyWithoutPreferred;
import static org.folio.rdf4ld.util.ResourceUtil.getCurrentLccnLink;

import java.util.Optional;
import java.util.function.LongFunction;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(predicate = SUBJECT)
public class SubjectRdfMapperUnit extends ReferenceRdfMapperUnit {

  public SubjectRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit,
                              MockLccnResourceService mockLccnResourceService,
                              FingerprintHashService hashService,
                              CoreLd2RdfMapper coreLd2RdfMapper,
                              LongFunction<String> resourceUrlProvider) {
    super(baseRdfMapperUnit, hashService, mockLccnResourceService, resourceUrlProvider, coreLd2RdfMapper);
  }

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping resourceMapping,
                                    Resource parent) {
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

  public Resource wrapWithConcept(Resource subject) {
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
    var parentIri = iri(resourceUrlProvider.apply(parent.getId()));
    if (noSubFocuses(subject)) {
      subject.getOutgoingEdges()
        .stream()
        .filter(oe -> oe.getPredicate() == FOCUS)
        .map(ResourceEdge::getTarget)
        .forEach(resource -> writeSingleSubject(resource, modelBuilder, mapping, parentIri));
    } else {
      var predicate = mapping.getBfResourceDef().getPredicate();
      getCurrentLccnLink(subject)
        .ifPresentOrElse(link -> linkResources(parentIri, iri(link), predicate, modelBuilder),
          () -> writeComplexSubject(subject, modelBuilder, mapping, parentIri)
        );
    }
  }

  private boolean noSubFocuses(Resource resource) {
    return resource.getOutgoingEdges().stream().noneMatch(oe -> oe.getPredicate() == SUB_FOCUS);
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

  private void writeComplexSubject(Resource subject,
                                   ModelBuilder modelBuilder,
                                   ResourceMapping mapping,
                                   org.eclipse.rdf4j.model.Resource parent) {
    var complexSubjectNode = bnode("_" + subject.getId());
    var complexSubjectMapping = getEdgeMapping(mapping.getResourceMapping(), 0);
    linkResources(parent, complexSubjectNode, mapping.getBfResourceDef().getPredicate(), modelBuilder);
    writeBlankNode(complexSubjectNode, subject, modelBuilder, complexSubjectMapping, coreLd2RdfMapper);
    writeExtraTypes(modelBuilder, subject, complexSubjectNode);
    writeComponentsList(subject, modelBuilder, mapping, complexSubjectNode);
  }

  private void writeComponentsList(Resource subject,
                                   ModelBuilder modelBuilder,
                                   ResourceMapping mapping,
                                   BNode complexSubjectNode) {
    var components = subject.getOutgoingEdges().stream()
      .filter(oe -> oe.getPredicate() == FOCUS || oe.getPredicate() == SUB_FOCUS)
      .map(ResourceEdge::getTarget)
      .map(f -> getCurrentLccnLink(f)
        .map(iri -> (org.eclipse.rdf4j.model.Resource) iri(iri))
        .orElseGet(() -> {
          var nodeId = "_" + f.getId();
          var bnode = writeBlankNode(bnode(nodeId), f, modelBuilder, mapping, coreLd2RdfMapper);
          writeExtraTypes(modelBuilder, f, bnode(nodeId));
          return bnode;
        }))
      .toList();
    var listHead = bnode();
    RDFCollections.asRDF(components, listHead, modelBuilder.build());
    var complexSubjectMapping = getEdgeMapping(mapping.getResourceMapping(), 0);
    var componentListPredicate = complexSubjectMapping.getBfResourceDef().getPredicate();
    modelBuilder.add(complexSubjectNode, iri(componentListPredicate), listHead);
  }

}
