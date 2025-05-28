package org.folio.rdf4ld.mapper.unit.monograph.agent;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.BfResourceDef;
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
                          ResourceInternalMapping resourceMapping,
                          Set<ResourceTypeDictionary> ldTypes,
                          Boolean localOnly) {
    var agentPredicate = getAgentPredicate(resourceMapping);
    if (isNull(agentPredicate)) {
      log.warn("No agent predicate was provided in Contribution mapping of ldTypes [{}]", ldTypes);
      return null;
    }
    var agentResource = getAgentResource(model, contributionResource, agentPredicate);
    if (isNull(agentResource)) {
      log.warn("No agent resource was found for Contribution of ldTypes: {}", ldTypes);
      return null;
    }
    var lccn = agentResource.getLocalName();
    return resourceProvider.apply(lccn)
      .orElse(null);
  }

  private String getAgentPredicate(ResourceInternalMapping resourceMapping) {
    return ofNullable(resourceMapping)
      .map(ResourceInternalMapping::getOutgoingEdges)
      .map(Set::iterator)
      .map(i -> i.hasNext() ? i.next() : null)
      .map(ResourceMapping::getBfResourceDef)
      .map(BfResourceDef::getPredicate)
      .orElse(null);
  }

  private SimpleIRI getAgentResource(Model model,
                                     org.eclipse.rdf4j.model.Resource contributionResource,
                                     String agentPredicate) {
    return model.filter(contributionResource, Values.iri(agentPredicate), null)
      .objects()
      .stream()
      .map(SimpleIRI.class::cast)
      .findFirst()
      .orElse(null);
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceInternalMapping resourceMapping,
                            String nameSpace,
                            Set<String> bfTypeSet) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping, nameSpace, bfTypeSet);
  }
}
