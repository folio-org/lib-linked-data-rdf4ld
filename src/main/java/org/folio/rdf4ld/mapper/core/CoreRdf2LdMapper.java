package org.folio.rdf4ld.mapper.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.rdf4ld.mapper.unit.MapperUnit;
import org.folio.rdf4ld.model.EdgeMapping;
import org.folio.rdf4ld.model.LdResourceDef;
import org.folio.rdf4ld.model.PropertyMapping;

public interface CoreRdf2LdMapper {

  JsonNode mapDoc(Statement statement, Model model, Set<PropertyMapping> propertyMappings);

  Stream<Statement> selectStatementsByType(Model model, String typeIri, Set<String> bfTypeSet);

  MapperUnit getMapper(LdResourceDef ldResourceDef);

  Set<ResourceEdge> mapEdges(Set<EdgeMapping> edgeMappings,
                             Model model,
                             Resource parent,
                             boolean outgoingOrIncoming,
                             String typeIri);
}
