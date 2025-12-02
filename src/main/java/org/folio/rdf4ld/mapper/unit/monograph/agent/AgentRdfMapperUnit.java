package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.rdf4ld.util.MappingUtil.getEdgeMapping;
import static org.folio.rdf4ld.util.MappingUtil.getEdgePredicate;
import static org.folio.rdf4ld.util.RdfUtil.getByPredicate;
import static org.folio.rdf4ld.util.RdfUtil.linkResources;
import static org.folio.rdf4ld.util.RdfUtil.readSupportedExtraTypes;
import static org.folio.rdf4ld.util.RdfUtil.writeBlankNode;
import static org.folio.rdf4ld.util.RdfUtil.writeExtraTypes;
import static org.folio.rdf4ld.util.ResourceUtil.copyLongestLabelToName;
import static org.folio.rdf4ld.util.ResourceUtil.getCurrentLccnLink;

import java.util.Optional;
import java.util.function.LongFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.dictionary.specific.RoleDictionary;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.service.lccn.MockLccnResourceService;

@Log4j2
@RequiredArgsConstructor
public abstract class AgentRdfMapperUnit implements RdfMapperUnit {
  private static final int AGENT_EDGE_NUMBER = 0;
  private static final int ROLE_EDGE_NUMBER = 1;
  private static final String ROLES_NAMESPACE = "http://id.loc.gov/vocabulary/relators/";
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final FingerprintHashService hashService;
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final LongFunction<String> resourceUrlProvider;
  private final MockLccnResourceService mockLccnResourceService;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource contributionResource,
                                    ResourceMapping mapping,
                                    Resource parent) {
    var agentPredicate = getEdgePredicate(mapping.getResourceMapping(), AGENT_EDGE_NUMBER);
    var agentResourceOptional = getByPredicate(model, contributionResource, agentPredicate)
      .findFirst();
    var ldTypes = mapping.getLdResourceDef().getTypeSet();
    if (agentResourceOptional.isEmpty()) {
      log.warn("No agent resource was found for Contribution of ldTypes: {}", ldTypes);
      return empty();
    }
    return agentResourceOptional
      .map(ar -> {
        var agentMapping = getEdgeMapping(mapping.getResourceMapping(), AGENT_EDGE_NUMBER);
        var agentOptional = mapAgent(model, (org.eclipse.rdf4j.model.Resource) ar, agentMapping, parent)
          .map(agent -> addRoles(agent, parent, model, contributionResource, mapping.getResourceMapping()));
        if (ar instanceof IRI iri) {
          agentOptional = of(mockLccnResourceService.mockLccnResource(agentOptional.orElse(null), iri.getLocalName()));
        }
        return agentOptional;
      })
      .get();
  }

  private Optional<Resource> mapAgent(Model model, org.eclipse.rdf4j.model.Resource agentNode,
                                      ResourceMapping mapping, Resource parent) {
    return baseRdfMapperUnit.mapToLd(model, agentNode, mapping, parent)
      .map(agent -> {
        copyLongestLabelToName(agent);
        readSupportedExtraTypes(model, agentNode).forEach(agent::addType);
        agent.setId(hashService.hash(agent));
        return agent;
      });
  }

  private Resource addRoles(Resource agent,
                            Resource parent,
                            Model model,
                            org.eclipse.rdf4j.model.Resource contributionResource,
                            ResourceInternalMapping resourceMapping) {
    var rolePredicate = getEdgePredicate(resourceMapping, ROLE_EDGE_NUMBER);
    getByPredicate(model, contributionResource, rolePredicate)
      .map(SimpleIRI.class::cast)
      .map(SimpleIRI::getLocalName)
      .map(RoleDictionary::getValue)
      .flatMap(Optional::stream)
      .map(p -> new ResourceEdge(parent, agent, p))
      .forEach(parent::addOutgoingEdge);
    return agent;
  }

  @Override
  public void mapToBibframe(Resource agent,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            Resource parent) {
    var nodeId = getNodeId(agent);
    var contributionNode = Values.bnode(nodeId);
    writeContributionLink(contributionNode, modelBuilder, resourceMapping, parent);
    getCurrentLccnLink(agent).ifPresentOrElse(lccnLink -> {
        var agentIri = iri(lccnLink);
        writeContributionResource(agent, contributionNode, agentIri, modelBuilder, resourceMapping, parent);
      }, () -> {
        var agentNode = Values.bnode(nodeId + "_agent");
        writeContributionResource(agent, contributionNode, agentNode, modelBuilder, resourceMapping, parent);
        writeAgentResource(agent, agentNode, modelBuilder, resourceMapping);
      }
    );
  }

  private String getNodeId(Resource agent) {
    var predicate = this.getClass().getAnnotation(RdfMapperDefinition.class).predicate().name();
    return predicate + "_" + agent.getId();
  }

  private void writeContributionLink(BNode bnode, ModelBuilder modelBuilder, ResourceMapping mapping, Resource parent) {
    linkResources(iri(resourceUrlProvider.apply(parent.getId())), bnode,
      mapping.getBfResourceDef().getPredicate(), modelBuilder);
  }

  private void writeRoles(Resource agent, ModelBuilder modelBuilder, ResourceMapping mapping, Resource parent) {
    var rolePredicate = getEdgePredicate(mapping.getResourceMapping(), ROLE_EDGE_NUMBER);
    parent.getOutgoingEdges()
      .stream()
      .filter(e -> e.getTarget().equals(agent))
      .filter(e -> e.getPredicate() != CREATOR && e.getPredicate() != CONTRIBUTOR)
      .map(ResourceEdge::getPredicate)
      .map(RoleDictionary::getCode)
      .flatMap(Optional::stream)
      .forEach(rc -> modelBuilder.add(rolePredicate, iri(ROLES_NAMESPACE, rc)));
  }

  private void writeContributionResource(Resource agent,
                                         BNode contributionNode,
                                         org.eclipse.rdf4j.model.Resource agentRdf,
                                         ModelBuilder modelBuilder,
                                         ResourceMapping mapping,
                                         Resource parent) {
    writeBlankNode(contributionNode, agent, modelBuilder, mapping, coreLd2RdfMapper);
    var agentPredicate = getEdgePredicate(mapping.getResourceMapping(), AGENT_EDGE_NUMBER);
    modelBuilder.add(agentPredicate, agentRdf);
    writeRoles(agent, modelBuilder, mapping, parent);
  }

  private void writeAgentResource(Resource agent, BNode agentNode, ModelBuilder modelBuilder, ResourceMapping mapping) {
    var agentMapping = getEdgeMapping(mapping.getResourceMapping(), AGENT_EDGE_NUMBER);
    writeBlankNode(agentNode, agent, modelBuilder, agentMapping, coreLd2RdfMapper);
    writeExtraTypes(modelBuilder, agent, agentNode);
  }

}
