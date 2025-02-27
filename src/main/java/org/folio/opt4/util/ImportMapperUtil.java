package org.folio.opt4.util;

import static java.util.Optional.ofNullable;
import static java.util.stream.StreamSupport.stream;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.opt4.mapping.ResourceMapping.EdgeMapping;
import static org.folio.opt4.mapping.ResourceMapping.PropertyMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.opt4.mapper.InstanceMapper;
import org.folio.opt4.mapper.Mapper;
import org.folio.opt4.mapper.TitleMapper;
import org.folio.opt4.mapper.WorkMapper;
import org.folio.opt4.mapping.LdResourceDef;

public class ImportMapperUtil {

    public static JsonNode mapDoc(Statement statement, Model model, Set<PropertyMapping> propertyMappings) {
        var doc = new HashMap<String, List<String>>();
        propertyMappings
                // TODO: look for a property under different subject (resource) if required by mapping profile
                // not needed for Thin Thread
                .forEach(pm -> model.getStatements(statement.getSubject(), Values.iri(pm.bfProperty()), null)
                        .forEach(st -> {
                                    var props = doc.computeIfAbsent(pm.ldProperty().getValue(), _ -> new ArrayList<>());
                                    props.add(st.getObject().stringValue());
                                }
                        ));
        return doc.isEmpty() ? null : toJson(doc);
    }

    private static JsonNode toJson(Map<String, List<String>> map) {
        var node = MonographUtil.getJsonNode(map);
        return ! (node instanceof NullNode) ? node : MonographUtil.createObjectNode();
    }

    public static Stream<Statement> selectStatementsByType(Model model, String typeIri, Set<String> bfTypeSet) {
        return model.stream()
                .filter(st -> st.getPredicate().stringValue().equals(typeIri)
                        && bfTypeSet.contains(st.getObject().stringValue()))
                .map(Statement::getSubject)
                .flatMap(subject -> stream(model.getStatements(subject, null, null).spliterator(), false));
    }

    // TODO this is dummy mapper selector, it should be replaced with Spring Bean resolving by custom annotation
    public static Mapper getMapper(LdResourceDef ldResourceDef) {
        if (ldResourceDef.typeSet().equals(Set.of(INSTANCE))) {
            return new InstanceMapper();
        } else if (ldResourceDef.typeSet().equals(Set.of(TITLE))
                && PredicateDictionary.TITLE.equals(ldResourceDef.predicate())) {
            return new TitleMapper();
        } else if (ldResourceDef.typeSet().equals(Set.of(WORK))
                && PredicateDictionary.INSTANTIATES.equals(ldResourceDef.predicate())) {
            return new WorkMapper();
        } else {
            // more resources in future
            return null;
        }
    }

    public static Set<ResourceEdge> mapEdges(Set<EdgeMapping> edgeMappings,
                                             Model model,
                                             Resource parent,
                                             boolean outgoingOrIncoming,
                                             String typeIri) {
        return ofNullable(edgeMappings)
                .stream()
                .flatMap(Set::stream)
                .flatMap(oem -> mapEdges(model, oem, typeIri).stream().
                        map(r -> new ResourceEdge(
                                getSource(parent, outgoingOrIncoming, r),
                                getTarget(parent, outgoingOrIncoming, r),
                                oem.ldResourceDef().predicate()))
                )
                .collect(Collectors.toSet());
    }

    private static Resource getTarget(Resource parent, boolean outgoingOrIncoming, Resource current) {
        return outgoingOrIncoming ? current : parent;
    }

    private static Resource getSource(Resource parent, boolean outgoingOrIncoming, Resource current) {
        return outgoingOrIncoming ? parent : current;
    }

    private static Set<Resource> mapEdges(Model model, EdgeMapping edgeMapping, String typeIri) {
        // TODO fetch remote resource if edgeMapping.fetchRemote() is true
        return ofNullable(getMapper(edgeMapping.ldResourceDef()))
                .map(mapper -> selectStatementsByType(model, typeIri, edgeMapping.bfResourceDef().typeSet())
                        .map(st -> mapper.mapToLd(model, st, edgeMapping.fetchRemote()))
                        .collect(Collectors.toSet()))
                .orElseGet(() -> {
                    System.out.println("No mapper present for edge mapping " + edgeMapping);
                    return new HashSet<>();
                });
    }

}
