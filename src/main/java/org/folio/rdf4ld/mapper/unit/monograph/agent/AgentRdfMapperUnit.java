package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static java.util.Optional.empty;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.rdf4ld.util.MappingUtil.getEdgeMapping;
import static org.folio.rdf4ld.util.MappingUtil.getEdgePredicate;
import static org.folio.rdf4ld.util.RdfUtil.getByPredicate;
import static org.folio.rdf4ld.util.ResourceUtil.getCurrentLccnLink;

import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.RoleDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;

@Log4j2
@RequiredArgsConstructor
public abstract class AgentRdfMapperUnit implements RdfMapperUnit {
  private static final int AGENT_EDGE_NUMBER = 0;
  private static final int ROLE_EDGE_NUMBER = 1;
  private static final int LCCN_EDGE_NUMBER = 2;
  private static final String ROLES_NAMESPACE = "http://id.loc.gov/vocabulary/relators/";
  private static final String AGENT_RDF_TYPE = "http://id.loc.gov/ontologies/bibframe/Agent";
  private static final ImmutableBiMap<ResourceTypeDictionary, String> AGENT_LD_TO_BF_TYPES =
    new ImmutableBiMap.Builder<ResourceTypeDictionary, String>()
      .put(PERSON, "http://id.loc.gov/ontologies/bibframe/Person")
      .put(FAMILY, "http://id.loc.gov/ontologies/bibframe/Family")
      .put(ORGANIZATION, "http://id.loc.gov/ontologies/bibframe/Organization")
      .put(MEETING, "http://id.loc.gov/ontologies/bibframe/Meeting")
      .put(JURISDICTION, "http://id.loc.gov/ontologies/bibframe/Jurisdiction")
      .build();
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final FingerprintHashService hashService;
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final Function<String, Optional<Resource>> resourceProvider;

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
        Optional<Resource> agentOptional = empty();
        if (ar instanceof IRI iri) {
          agentOptional = resourceProvider.apply(iri.getLocalName());
        }
        if (ar instanceof BNode node) {
          agentOptional = mapAgent(model, node, mapping, parent);
        }
        agentOptional
          .ifPresent(agent -> addRoles(agent, parent, model, contributionResource, mapping.getResourceMapping()));
        return agentOptional;
      })
      .get();
  }

  private Optional<Resource> mapAgent(Model model, BNode agentNode, ResourceMapping mapping, Resource parent) {
    return baseRdfMapperUnit.mapToLd(model, agentNode, mapping, parent)
      .map(agent -> {
        model.filter(agentNode, RDF.TYPE, null)
          .stream()
          .map(Statement::getObject)
          .map(Value::stringValue)
          .filter(type -> AGENT_LD_TO_BF_TYPES.inverse().containsKey(type))
          .map(type -> AGENT_LD_TO_BF_TYPES.inverse().get(type))
          .forEach(agent::addType);
        agent.setId(hashService.hash(agent));
        return agent;
      });
  }

  private void addRoles(Resource agent,
                        Resource parent,
                        Model model,
                        org.eclipse.rdf4j.model.Resource contributionResource,
                        ResourceInternalMapping resourceMapping) {
    var rolePredicate = getEdgePredicate(resourceMapping, ROLE_EDGE_NUMBER);
    getByPredicate(model, contributionResource, rolePredicate)
      .map(SimpleIRI.class::cast)
      .map(SimpleIRI::getLocalName)
      .map(RoleDictionary::getRole)
      .map(p -> new ResourceEdge(parent, agent, p))
      .forEach(parent::addOutgoingEdge);
  }

  @Override
  public void mapToBibframe(Resource agent,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            Resource parent) {
    var contributionNode = Values.bnode("_" + agent.getId());
    writeContributionLink(contributionNode, modelBuilder, resourceMapping, parent);
    getCurrentLccnLink(agent).ifPresentOrElse(lccnLink -> {
        var agentIri = Values.iri(lccnLink);
        writeContributionResource(agent, contributionNode, agentIri, modelBuilder, resourceMapping, parent);
      }, () -> {
        var agentNode = Values.bnode("_" + agent.getId() + "_agent");
        writeContributionResource(agent, contributionNode, agentNode, modelBuilder, resourceMapping, parent);
        writeAgentResource(agent, agentNode, modelBuilder, resourceMapping);
      }
    );
  }

  private void writeContributionLink(BNode bnode, ModelBuilder modelBuilder, ResourceMapping mapping, Resource parent) {
    coreLd2RdfMapper.linkResources(modelBuilder, coreLd2RdfMapper.getResourceIri(parent.getId().toString()), bnode,
      mapping.getBfResourceDef().getPredicate());
  }

  private void writeContributionResource(Resource agent,
                                         BNode contributionNode,
                                         org.eclipse.rdf4j.model.Resource agentRdf,
                                         ModelBuilder modelBuilder,
                                         ResourceMapping mapping,
                                         Resource parent) {
    modelBuilder.subject(contributionNode);
    mapping.getBfResourceDef().getTypeSet().forEach(type -> modelBuilder.add(RDF.TYPE, Values.iri(type)));
    var agentPredicate = getEdgePredicate(mapping.getResourceMapping(), AGENT_EDGE_NUMBER);
    modelBuilder.add(agentPredicate, agentRdf);
    writeRoles(agent, modelBuilder, mapping, parent);
  }

  private void writeRoles(Resource agent, ModelBuilder modelBuilder, ResourceMapping mapping, Resource parent) {
    var rolePredicate = getEdgePredicate(mapping.getResourceMapping(), ROLE_EDGE_NUMBER);
    parent.getOutgoingEdges()
      .stream()
      .filter(e -> e.getTarget().equals(agent))
      .filter(e -> e.getPredicate() != CREATOR && e.getPredicate() != CONTRIBUTOR)
      .map(ResourceEdge::getPredicate)
      .map(RoleDictionary::getCode)
      .forEach(rc -> modelBuilder.add(rolePredicate, Values.iri(ROLES_NAMESPACE, rc)));
  }

  private void writeAgentResource(Resource agent, BNode agentNode, ModelBuilder modelBuilder, ResourceMapping mapping) {
    modelBuilder.subject(agentNode);
    modelBuilder.add(RDF.TYPE, Values.iri(AGENT_RDF_TYPE));
    agent.getTypes()
      .stream()
      .filter(AGENT_LD_TO_BF_TYPES::containsKey)
      .map(AGENT_LD_TO_BF_TYPES::get)
      .forEach(at -> modelBuilder.add(RDF.TYPE, Values.iri(at)));
    coreLd2RdfMapper.mapProperties(agent, modelBuilder, mapping);
    writeIdentifiers(agent, agentNode, modelBuilder, mapping);
  }

  private void writeIdentifiers(Resource agent, BNode agentNode, ModelBuilder modelBuilder, ResourceMapping mapping) {
    var lccnEdgeMapping = getEdgeMapping(mapping.getResourceMapping(), LCCN_EDGE_NUMBER);
    var lccnEdgePredicate = getEdgePredicate(mapping.getResourceMapping(), LCCN_EDGE_NUMBER);
    agent.getOutgoingEdges()
      .stream()
      .filter(oe -> oe.getPredicate() == MAP)
      .forEach(oe -> {
        baseRdfMapperUnit.mapToBibframe(oe.getTarget(), modelBuilder, lccnEdgeMapping, null);
        coreLd2RdfMapper.linkResources(modelBuilder, agentNode,
          coreLd2RdfMapper.getResourceIri(oe.getTarget().getId().toString()), lccnEdgePredicate);
      });
  }
}
