package org.folio.rdf4ld.mapper.unit;

import static org.folio.rdf4ld.util.ResourceUtil.getPropertiesString;

import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.core.CoreLd2RdfMapper;
import org.folio.rdf4ld.mapper.core.CoreRdf2LdMapper;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaseMapperUnit implements MapperUnit {
  private final CoreRdf2LdMapper coreRdf2LdMapper;
  private final CoreLd2RdfMapper coreLd2RdfMapper;
  private final FingerprintHashService hashService;

  @Override
  public Resource mapToLd(Model model,
                          Statement statement,
                          ResourceMapping resourceMapping,
                          Set<ResourceTypeDictionary> ldTypes,
                          String typeIri,
                          boolean fetchRemote) {
    var result = new Resource();
    result.setCreatedDate(new Date());
    result.setTypes(ldTypes);
    result.setDoc(coreRdf2LdMapper.mapDoc(statement, model, resourceMapping.getProperties()));
    result.setLabel(getPropertiesString(result.getDoc(), PropertyDictionary.LABEL));
    var outEdges = coreRdf2LdMapper.mapEdges(resourceMapping.getOutgoingEdges(), model, result, true, typeIri);
    var inEdges = coreRdf2LdMapper.mapEdges(resourceMapping.getIncomingEdges(), model, result, false, typeIri);
    result.setOutgoingEdges(outEdges);
    result.setIncomingEdges(inEdges);
    result.setId(hashService.hash(result));
    return result;
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            String nameSpace,
                            Set<String> bfTypeSet) {
    modelBuilder.subject(coreLd2RdfMapper.getResourceIri(nameSpace, String.valueOf(resource.getId())))
      .add(RDF.TYPE, bfTypeSet.iterator().next());
    resourceMapping.getProperties()
      .forEach(p -> coreLd2RdfMapper.mapProperty(modelBuilder, p.getBfProperty(), resource, p.getLdProperty()));
    resource.getOutgoingEdges()
      .forEach(oe -> coreLd2RdfMapper.mapOutgoingEdge(modelBuilder, oe, resourceMapping, nameSpace));
    resource.getIncomingEdges()
      .forEach(ie -> coreLd2RdfMapper.mapIncomingEdge(modelBuilder, ie, resourceMapping, nameSpace));
  }

}
