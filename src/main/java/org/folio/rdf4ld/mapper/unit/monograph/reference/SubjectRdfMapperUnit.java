package org.folio.rdf4ld.mapper.unit.monograph.reference;

import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.rdf4ld.util.MappingUtil.getEdgeMapping;
import static org.folio.rdf4ld.util.RdfUtil.AUTHORITY_LD_TO_BF_TYPES;
import static org.folio.rdf4ld.util.ResourceUtil.copyWithoutPreferred;
import static org.folio.rdf4ld.util.ResourceUtil.getCurrentLccnLink;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RdfMapperDefinition(predicate = SUBJECT)
public class SubjectRdfMapperUnit extends ReferenceRdfMapperUnit {
  private static final String AUTHORITY_RDF_TYPE = "http://www.loc.gov/mads/rdf/v1#Authority";
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final FingerprintHashService hashService;

  public SubjectRdfMapperUnit(BaseRdfMapperUnit baseRdfMapperUnit,
                              Function<String, Optional<Resource>> resourceProvider,
                              FingerprintHashService hashService,
                              CoreLd2RdfMapper coreLd2RdfMapper) {
    super(baseRdfMapperUnit, hashService, resourceProvider);
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
    var parentIri = coreLd2RdfMapper.getResourceIri(parent.getId().toString());
    if (noSubFocuses(subject)) {
      subject.getOutgoingEdges()
        .stream()
        .filter(oe -> oe.getPredicate() == FOCUS)
        .map(ResourceEdge::getTarget)
        .forEach(resource -> writeSingleSubject(resource, modelBuilder, mapping, parentIri));
    } else {
      writeComplexSubject(subject, modelBuilder, mapping, parentIri);
    }
  }

  private boolean noSubFocuses(Resource resource) {
    return resource.getOutgoingEdges().stream().noneMatch(oe -> oe.getPredicate() == SUB_FOCUS);
  }

  private void writeSingleSubject(Resource subject,
                                  ModelBuilder modelBuilder,
                                  ResourceMapping mapping,
                                  org.eclipse.rdf4j.model.Resource parent) {
    getCurrentLccnLink(subject)
      .ifPresentOrElse(writeSubjectLink(modelBuilder, mapping, parent),
        () -> {
          var node = Values.bnode("_" + subject.getId());
          writeBlankNode(node, subject, AUTHORITY_RDF_TYPE, modelBuilder, mapping, parent);
        }
      );
  }

  private Consumer<String> writeSubjectLink(ModelBuilder modelBuilder,
                                            ResourceMapping mapping,
                                            org.eclipse.rdf4j.model.Resource parent) {
    return lccnLink -> {
      var subjectIri = Values.iri(lccnLink);
      modelBuilder.subject(parent);
      modelBuilder.add(mapping.getBfResourceDef().getPredicate(), subjectIri);
    };
  }

  private void writeBlankNode(BNode node,
                              Resource subject,
                              String mainType,
                              ModelBuilder modelBuilder,
                              ResourceMapping mapping,
                              org.eclipse.rdf4j.model.Resource parent) {
    coreLd2RdfMapper.linkResources(modelBuilder, parent, node, mapping.getBfResourceDef().getPredicate());
    modelBuilder.subject(node);
    modelBuilder.add(RDF.TYPE, Values.iri(mainType));
    subject.getTypes()
      .stream()
      .filter(AUTHORITY_LD_TO_BF_TYPES::containsKey)
      .map(AUTHORITY_LD_TO_BF_TYPES::get)
      .forEach(at -> modelBuilder.add(RDF.TYPE, Values.iri(at)));
    coreLd2RdfMapper.mapProperties(subject, modelBuilder, mapping);
  }

  private void writeComplexSubject(Resource subject,
                                   ModelBuilder modelBuilder,
                                   ResourceMapping mapping,
                                   org.eclipse.rdf4j.model.Resource parent) {
    var complexSubjectNode = Values.bnode("_" + subject.getId());
    var complexSubjectMapping = getEdgeMapping(mapping.getResourceMapping(), 0);
    var complexSubjectType = complexSubjectMapping.getBfResourceDef().getTypeSet().iterator().next();
    writeBlankNode(complexSubjectNode, subject, complexSubjectType, modelBuilder, mapping, parent);
    subject.getOutgoingEdges().stream()
      .filter(oe -> oe.getPredicate() == FOCUS || oe.getPredicate() == SUB_FOCUS)
      .map(ResourceEdge::getTarget)
      .forEach(f -> writeSingleSubject(f, modelBuilder, complexSubjectMapping, complexSubjectNode));
  }

}
