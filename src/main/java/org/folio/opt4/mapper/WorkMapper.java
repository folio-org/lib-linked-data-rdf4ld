package org.folio.opt4.mapper;

import static org.folio.opt4.util.ExportMapperUtil.getResourceIri;
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

public class WorkMapper implements Mapper {

    private final String typeIri;
    private final ResourceMapping workMapping;


    public WorkMapper() {
        this.typeIri = MappingProvider.getTopMapping().typeIri();
        this.workMapping = MappingProvider.getWorkMapping();
    }

    @Override
    public Resource mapToLd(Model model, Statement statement, boolean fetchRemote) {
        var result = new Resource();
        result.setCreatedDate(new Date());
        result.setTypes(Set.of(ResourceTypeDictionary.WORK));
        result.setDoc(mapDoc(statement, model, workMapping.properties()));
        var outEdges = mapEdges(workMapping.outgoingEdges(), model, result, true, typeIri);
        var inEdges = mapEdges(workMapping.incomingEdges(), model, result, false, typeIri);
        result.setOutgoingEdges(outEdges);
        result.setIncomingEdges(inEdges);
        return result;
    }

    @Override
    public void mapToBibframe(Resource work, ModelBuilder modelBuilder, String nameSpace, Set<String> bfTypeSet) {
        modelBuilder.subject(getResourceIri(nameSpace, String.valueOf(work.getId())))
                .add(RDF.TYPE, bfTypeSet.iterator().next());

        workMapping.properties()
                .forEach(p -> mapProperty(modelBuilder, p.bfProperty(), work, p.ldProperty()));
    }
}
