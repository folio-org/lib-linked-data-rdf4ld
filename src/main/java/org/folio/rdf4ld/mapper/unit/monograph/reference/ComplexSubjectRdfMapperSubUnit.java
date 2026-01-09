package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.eclipse.rdf4j.model.util.Values.bnode;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.rdf4ld.util.MappingUtil.getEdgeMapping;
import static org.folio.rdf4ld.util.MappingUtil.getEdgePredicate;
import static org.folio.rdf4ld.util.MappingUtil.getEdgeTypeSet;
import static org.folio.rdf4ld.util.RdfUtil.extractRdfList;
import static org.folio.rdf4ld.util.RdfUtil.getByPredicate;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;
import static org.folio.rdf4ld.util.RdfUtil.writeBlankNode;
import static org.folio.rdf4ld.util.RdfUtil.writeExtraTypes;
import static org.folio.rdf4ld.util.ResourceUtil.addProperty;
import static org.folio.rdf4ld.util.ResourceUtil.copyExcluding;
import static org.folio.rdf4ld.util.ResourceUtil.getCurrentIdentifierLink;
import static org.folio.rdf4ld.util.ResourceUtil.getPropertyForSubFocusType;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.dictionary.util.LabelGenerator;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;
import org.folio.rdf4ld.util.ResourceUtil;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ComplexSubjectRdfMapperSubUnit {

  private static final int COMPONENT_LIST_EDGE_NUMBER = 0;

  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final FingerprintHashService hashService;
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final MockLccnResourceService mockLccnResourceService;

  public Optional<Resource> readComplexSubject(Model model,
                                               org.eclipse.rdf4j.model.Resource resource,
                                               ResourceMapping resourceMapping,
                                               Resource parent) {
    var components = getComponentList(model, resource, resourceMapping);
    if (components.isEmpty() || components.size() < 2) {
      log.debug("ComplexSubject with invalid component list (empty or single element), skipping. Resource: {}",
        resource);
      return Optional.empty();
    }

    var componentMapping = getEdgeMapping(resourceMapping.getResourceMapping(), COMPONENT_LIST_EDGE_NUMBER);

    var focus = processComponentResource(model, components.getFirst(), componentMapping, parent);
    if (focus == null || focus.isOfType(CONCEPT)) {
      log.debug("ComplexSubject focus is invalid (null or CONCEPT), skipping. Resource: {}", resource);
      return Optional.empty();
    }

    var subFocuses = components.subList(1, components.size()).stream()
      .map(subFocusRes -> processComponentResource(model, subFocusRes, componentMapping, parent))
      .filter(sf -> sf != null && !sf.isOfType(CONCEPT))
      .toList();

    if (subFocuses.isEmpty()) {
      log.debug("ComplexSubject has no valid sub-focus components, skipping. Resource: {}", resource);
      return Optional.empty();
    }

    return Optional.of(createConceptFromComponents(focus, subFocuses));
  }

  private List<org.eclipse.rdf4j.model.Resource> getComponentList(Model model,
                                                                  org.eclipse.rdf4j.model.Resource resource,
                                                                  ResourceMapping resourceMapping) {
    var componentListPredicate = getEdgePredicate(resourceMapping.getResourceMapping(), COMPONENT_LIST_EDGE_NUMBER);
    return getByPredicate(model, resource, componentListPredicate)
      .filter(Value::isResource)
      .map(org.eclipse.rdf4j.model.Resource.class::cast)
      .findFirst()
      .map(listHead -> extractRdfList(model, listHead))
      .orElse(List.of());
  }

  private Resource processComponentResource(Model model,
                                            org.eclipse.rdf4j.model.Resource componentResource,
                                            ResourceMapping componentMapping,
                                            Resource parent) {
    var mappedOpt = baseRdfMapperUnit.mapToLd(model, componentResource, componentMapping, parent)
      .map(mapped -> ResourceUtil.enrichResource(mapped, model, componentResource, hashService));

    if (componentResource instanceof IRI iri) {
      return mockLccnResourceService.mockLccnResource(mappedOpt.orElse(null), iri.getLocalName());
    }

    return mappedOpt.orElse(null);
  }

  public Resource enrichConceptFromComponents(Resource concept) {
    var focus = concept.getOutgoingEdges().stream()
      .filter(re -> re.getPredicate() == FOCUS)
      .map(ResourceEdge::getTarget)
      .toList()
      .getFirst();
    var subFocuses = concept.getOutgoingEdges().stream()
      .filter(re -> re.getPredicate() == SUB_FOCUS)
      .map(ResourceEdge::getTarget)
      .toList();
    return createConceptFromComponents(focus, subFocuses);
  }

  private Resource createConceptFromComponents(Resource focus, List<Resource> subFocuses) {
    var concept = new Resource()
      .setDoc(copyExcluding(focus, RESOURCE_PREFERRED, LABEL))
      .addType(CONCEPT);
    focus.getTypes().forEach(concept::addType);
    concept.addOutgoingEdge(new ResourceEdge(concept, focus, FOCUS));
    subFocuses.forEach(sf -> {
      concept.addOutgoingEdge(new ResourceEdge(concept, sf, SUB_FOCUS));
      addProperty(concept.getDoc(), getPropertyForSubFocusType(sf.getTypes()), sf.getLabel());
      if (sf.isOfType(MOCKED_RESOURCE)) {
        concept.addType(MOCKED_RESOURCE);
      }
    });
    var label = LabelGenerator.generateLabel(concept);
    concept.setLabel(label);
    addProperty(concept.getDoc(), LABEL, label);
    concept.setId(hashService.hash(concept));
    return concept;
  }

  public void writeComplexSubject(Resource subject,
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
      .map(f -> getCurrentIdentifierLink(f)
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

  public boolean isComplexSubject(Model model, org.eclipse.rdf4j.model.Resource resource, ResourceMapping mapping) {
    var complexSubjectTypes = getEdgeTypeSet(mapping.getResourceMapping(), COMPONENT_LIST_EDGE_NUMBER);
    return complexSubjectTypes.stream().anyMatch(type -> model.contains(resource, RDF.TYPE, Values.iri(type)));
  }
}
