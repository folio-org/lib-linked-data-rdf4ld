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
public class WorkMapperUnit implements MapperUnit {

  private final String typeIri = MappingProvider.getTopMapping().typeIri();
  private final ResourceMapping workMapping = MappingProvider.getWorkMapping();
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final CoreLd2RdfMapper coreLd2RdfMapper;

  @Override
  public Resource mapToLd(Model model, Statement statement, boolean fetchRemote) {
    var result = new Resource();
    result.setCreatedDate(new Date());
    result.setTypes(Set.of(ResourceTypeDictionary.WORK));
    result.setDoc(coreRdf2LdMapper.mapDoc(statement, model, workMapping.properties()));
    var outEdges = coreRdf2LdMapper.mapEdges(workMapping.outgoingEdges(), model, result, true, typeIri);
    var inEdges = coreRdf2LdMapper.mapEdges(workMapping.incomingEdges(), model, result, false, typeIri);
    result.setOutgoingEdges(outEdges);
    result.setIncomingEdges(inEdges);
    return result;
  }

  @Override
  public void mapToBibframe(Resource work, ModelBuilder modelBuilder, String nameSpace, Set<String> bfTypeSet) {
    modelBuilder.subject(coreLd2RdfMapper.getResourceIri(nameSpace, String.valueOf(work.getId())))
      .add(RDF.TYPE, bfTypeSet.iterator().next());

    workMapping.properties()
      .forEach(p -> coreLd2RdfMapper.mapProperty(modelBuilder, p.bfProperty(), work, p.ldProperty()));
  }
}
