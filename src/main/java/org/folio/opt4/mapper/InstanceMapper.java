package org.folio.opt4.mapper;

import static org.folio.opt4.util.ExportMapperUtil.getResourceIri;
import static org.folio.opt4.util.ExportMapperUtil.mapIncomingEdge;
import static org.folio.opt4.util.ExportMapperUtil.mapOutgoingEdge;
import static org.folio.opt4.util.ExportMapperUtil.mapProperty;
import static org.folio.opt4.util.ImportMapperUtil.mapDoc;
import static org.folio.opt4.util.ImportMapperUtil.mapEdges;

import java.util.Date;
import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.opt4.util.MappingProvider;
import org.folio.opt4.mapping.ResourceMapping;

public class InstanceMapper implements Mapper {

    private final String typeIri;
    private final ResourceMapping instanceMapping;

    public InstanceMapper() {
        this.typeIri = MappingProvider.getTopMapping().typeIri();
        this.instanceMapping = MappingProvider.getInstanceMapping();
    }

    @Override
    public Resource mapToLd(Model model, Statement statement, boolean fetchRemote) {
        var result = new Resource();
        result.setCreatedDate(new Date());
        result.setTypes(Set.of(ResourceTypeDictionary.INSTANCE));
        result.setDoc(mapDoc(statement, model, instanceMapping.properties()));
        var outEdges = mapEdges(instanceMapping.outgoingEdges(), model, result, true, typeIri);
        var inEdges = mapEdges(instanceMapping.incomingEdges(), model, result, false, typeIri);
        result.setOutgoingEdges(outEdges);
        result.setIncomingEdges(inEdges);
        return result;
    }

    @Override
    public void mapToBibframe(Resource instance, ModelBuilder modelBuilder, String nameSpace, Set<String> bfTypeSet) {
        modelBuilder.subject(getResourceIri(nameSpace, String.valueOf(instance.getId())))
                .add(RDF.TYPE, bfTypeSet.iterator().next());

        instanceMapping.properties()
                .forEach(p -> mapProperty(modelBuilder, p.bfProperty(), instance, p.ldProperty()));

        instance.getOutgoingEdges().forEach(oe -> mapOutgoingEdge(modelBuilder, oe, instanceMapping, nameSpace));
        instance.getIncomingEdges().forEach(ie -> mapIncomingEdge(modelBuilder, ie, instanceMapping, nameSpace));
    }

}
