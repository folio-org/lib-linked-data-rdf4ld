package org.folio.rdf4ld.mapper.unit.monograph;

import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.mapper.unit.MapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.folio.rdf4ld.util.MappingProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InstanceMapperUnit implements MapperUnit {

  private final String typeIri = MappingProvider.getTopMapping().typeIri();
  private final ResourceMapping instanceMapping = MappingProvider.getInstanceMapping();
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final CoreLd2RdfMapper coreLd2RdfMapper;

  @Override
  public Resource mapToLd(Model model, Statement statement, boolean fetchRemote) {
    var result = new Resource();
    result.setCreatedDate(new Date());
    result.setTypes(Set.of(ResourceTypeDictionary.INSTANCE));
    result.setDoc(coreRdf2LdMapper.mapDoc(statement, model, instanceMapping.properties()));
    var outEdges = coreRdf2LdMapper.mapEdges(instanceMapping.outgoingEdges(), model, result, true, typeIri);
    var inEdges = coreRdf2LdMapper.mapEdges(instanceMapping.incomingEdges(), model, result, false, typeIri);
    result.setOutgoingEdges(outEdges);
    result.setIncomingEdges(inEdges);
    return result;
  }

  @Override
  public void mapToBibframe(Resource instance, ModelBuilder modelBuilder, String nameSpace, Set<String> bfTypeSet) {
    modelBuilder.subject(coreLd2RdfMapper.getResourceIri(nameSpace, String.valueOf(instance.getId())))
      .add(RDF.TYPE, bfTypeSet.iterator().next());
    instanceMapping.properties()
      .forEach(p -> coreLd2RdfMapper.mapProperty(modelBuilder, p.bfProperty(), instance, p.ldProperty()));
    instance.getOutgoingEdges()
      .forEach(oe -> coreLd2RdfMapper.mapOutgoingEdge(modelBuilder, oe, instanceMapping, nameSpace));
    instance.getIncomingEdges()
      .forEach(ie -> coreLd2RdfMapper.mapIncomingEdge(modelBuilder, ie, instanceMapping, nameSpace));
  }

}
