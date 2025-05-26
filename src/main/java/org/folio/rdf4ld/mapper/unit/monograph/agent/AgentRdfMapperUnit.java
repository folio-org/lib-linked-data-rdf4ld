package org.folio.rdf4ld.mapper.unit.monograph.agent;

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
import org.folio.rdf4ld.model.ResourceInternalMapping;

@Log4j2
@RequiredArgsConstructor
public abstract class AgentRdfMapperUnit implements RdfMapperUnit {
  private static final String AGENT_PREDICATE = "http://id.loc.gov/ontologies/bibframe/agent";
  private final BaseRdfMapperUnit baseRdfMapperUnit;

  @Override
  public Resource mapToLd(Model model,
                          org.eclipse.rdf4j.model.Resource resource,
                          ResourceInternalMapping resourceMapping,
                          Set<ResourceTypeDictionary> ldTypes,
                          Boolean localOnly,
                          Function<String, Resource> resourceProvider) {
    var agentResources = model.filter(resource, Values.iri(AGENT_PREDICATE), null).objects();
    if (agentResources.isEmpty()) {
      log.warn("No agent resources found for contribution resource: {}", resource);
      return null;
    }
    var lccn = ((SimpleIRI) agentResources.iterator().next()).getLocalName();
    return resourceProvider.apply(lccn);
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
