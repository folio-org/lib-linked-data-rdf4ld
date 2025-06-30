package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.rdf4ld.util.ResourceUtil.getIrisByPredicate;
import static org.folio.rdf4ld.util.ResourceUtil.getPredicate;
import static org.folio.rdf4ld.util.ResourceUtil.getPropertiesString;

import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.RoleDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;

@Log4j2
@RequiredArgsConstructor
public abstract class AgentRdfMapperUnit implements RdfMapperUnit {
  private static final int AGENT_PREDICATE_NUMBER = 0;
  private static final int ROLE_PREDICATE_NUMBER = 1;
  private static final String STATUS_CURRENT = "http://id.loc.gov/vocabulary/mstatus/current";
  private static final String AGENTS_NAMESPACE = "http://id.loc.gov/rwo/agents/";
  private static final String ROLES_NAMESPACE = "http://id.loc.gov/vocabulary/relators/";
  private final Function<String, Optional<Resource>> resourceProvider;
  private final CoreLd2RdfMapper coreLd2RdfMapper;

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource contributionResource,
                          ResourceMapping mapping,
                          Resource parent) {
    var agentPredicate = getPredicate(mapping.getResourceMapping(), AGENT_PREDICATE_NUMBER);
    var agentResourceOptional = getIrisByPredicate(model, contributionResource, agentPredicate)
      .findFirst();
    var ldTypes = mapping.getLdResourceDef().getTypeSet();
    if (agentResourceOptional.isEmpty()) {
      log.warn("No agent resource was found for Contribution of ldTypes: {}", ldTypes);
      return null;
    }
    var lccn = agentResourceOptional.get().getLocalName();
    return resourceProvider.apply(lccn)
      .map(agent -> {
        addRoles(agent, parent, model, contributionResource, mapping.getResourceMapping());
        return agent;
      })
      .orElse(null);
  }

  private void addRoles(Resource agent,
                        Resource parent,
                        Model model,
                        org.eclipse.rdf4j.model.Resource contributionResource,
                        ResourceInternalMapping resourceMapping) {
    var rolePredicate = getPredicate(resourceMapping, ROLE_PREDICATE_NUMBER);
    getIrisByPredicate(model, contributionResource, rolePredicate)
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
    getCurrentLccn(agent).ifPresent(lccn -> {
      var bnode = Values.bnode("_" + agent.getId());
      writeContributionLink(bnode, modelBuilder, resourceMapping, parent);
      writeContributionResource(agent, bnode, lccn, modelBuilder, resourceMapping, parent);
    });
  }

  private Optional<String> getCurrentLccn(Resource resource) {
    return resource.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getTarget)
      .filter(target -> target.isOfType(ID_LCCN))
      .filter(this::isCurrent)
      .map(Resource::getDoc)
      .map(d -> getPropertiesString(d, NAME))
      .findFirst();
  }

  private boolean isCurrent(Resource resource) {
    if (resource.getOutgoingEdges().isEmpty()) {
      return true;
    }
    return resource.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getTarget)
      .filter(target -> target.isOfType(STATUS))
      .map(Resource::getDoc)
      .map(d -> getPropertiesString(d, LINK))
      .anyMatch(STATUS_CURRENT::equalsIgnoreCase);
  }

  private void writeContributionLink(BNode bnode, ModelBuilder modelBuilder, ResourceMapping mapping, Resource parent) {
    coreLd2RdfMapper.linkResources(modelBuilder, parent.getId().toString(), bnode,
      mapping.getBfResourceDef().getPredicate());
  }

  private void writeContributionResource(Resource agent, BNode bnode,
                                         String lccn,
                                         ModelBuilder modelBuilder,
                                         ResourceMapping mapping,
                                         Resource parent) {
    modelBuilder.subject(bnode);
    mapping.getBfResourceDef().getTypeSet().forEach(type -> modelBuilder.add(RDF.TYPE, Values.iri(type)));
    var agentPredicate = getPredicate(mapping.getResourceMapping(), AGENT_PREDICATE_NUMBER);
    modelBuilder.add(agentPredicate, Values.iri(AGENTS_NAMESPACE, lccn));
    writeRoles(agent, modelBuilder, mapping, parent);
  }

  private void writeRoles(Resource agent, ModelBuilder modelBuilder, ResourceMapping mapping, Resource parent) {
    var rolePredicate = getPredicate(mapping.getResourceMapping(), ROLE_PREDICATE_NUMBER);
    parent.getOutgoingEdges()
      .stream()
      .filter(e -> e.getTarget().equals(agent))
      .filter(e -> e.getPredicate() != CREATOR && e.getPredicate() != CONTRIBUTOR)
      .map(ResourceEdge::getPredicate)
      .map(RoleDictionary::getCode)
      .forEach(rc -> modelBuilder.add(rolePredicate, Values.iri(ROLES_NAMESPACE, rc)));
  }
}
