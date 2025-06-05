package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static java.util.Objects.isNull;
import static org.folio.rdf4ld.util.ResourceUtil.getIrisByPredicate;
import static org.folio.rdf4ld.util.ResourceUtil.getPredicate;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;

@Log4j2
@RequiredArgsConstructor
public abstract class AgentRdfMapperUnit implements RdfMapperUnit {
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final Function<String, Optional<Resource>> resourceProvider;

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource contributionResource,
                          ResourceMapping mapping,
                          Map<String, PredicateDictionary> roleMapping,
                          Resource parent) {
    var agentPredicate = getPredicate(mapping.getResourceMapping(), 0);
    if (isNull(agentPredicate)) {
      log.warn("No agent predicate was provided in Contribution mapping of ldTypes [{}]",
        mapping.getLdResourceDef().getTypeSet());
      return null;
    }
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
        addRoles(agent, parent, model, contributionResource, mapping.getResourceMapping(), roleMapping, ldTypes);
        return agent;
      })
      .orElse(null);
  }

  private void addRoles(Resource agent,
                        Resource parent,
                        Model model,
                        org.eclipse.rdf4j.model.Resource contributionResource,
                        ResourceInternalMapping resourceMapping,
                        Map<String, PredicateDictionary> roleMapping,
                        Set<ResourceTypeDictionary> ldTypes) {
    var rolePredicate = getPredicate(resourceMapping, 1);
    if (isNull(rolePredicate)) {
      log.warn("No role predicate was provided in Contribution mapping of ldTypes [{}]", ldTypes);
      return;
    }
    getIrisByPredicate(model, contributionResource, rolePredicate)
      .map(SimpleIRI::getLocalName)
      .map(roleMapping::get)
      .map(p -> new ResourceEdge(parent, agent, p))
      .forEach(parent::addOutgoingEdge);
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping);
  }
}
