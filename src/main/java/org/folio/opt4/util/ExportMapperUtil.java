package org.folio.opt4.util;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.opt4.util.ImportMapperUtil.getMapper;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.opt4.mapping.ResourceMapping;

public class ExportMapperUtil {

    public static IRI getResourceIri(String nameSpace, String id) {
        return Values.iri(nameSpace, id);
    }

    public static void mapProperty(ModelBuilder modelBuilder,
                                   String predicate,
                                   Resource resource,
                                   PropertyDictionary property) {
        if (property == PropertyDictionary.LABEL) {
            modelBuilder.add(predicate, resource.getLabel());
            return;
        }
        if (nonNull(resource.getDoc().get(property.getValue()))) {
            resource.getDoc().get(property.getValue())
                    .forEach(node -> modelBuilder.add(predicate, node.asText()));
        }

    }

    public static void mapOutgoingEdge(ModelBuilder modelBuilder,
                                       ResourceEdge edge,
                                       ResourceMapping resourceMapping,
                                       String nameSpace) {
        resourceMapping.outgoingEdges().stream()
                .filter(oem -> edge.getTarget().getTypes().equals(oem.ldResourceDef().typeSet())
                        && edge.getPredicate().equals(oem.ldResourceDef().predicate()))
                .findFirst()
                .ifPresentOrElse(oem -> ofNullable(getMapper(oem.ldResourceDef()))
                        .ifPresentOrElse(mapper -> {
                            mapper.mapToBibframe(edge.getTarget(), modelBuilder, oem.bfNameSpace(), oem.bfResourceDef().typeSet());
                            linkResources(modelBuilder, edge, nameSpace, oem.bfNameSpace(), oem.bfResourceDef().predicate());
                        }, () -> System.out.println("No mapper present for edge from Instance to "
                                + edge.getTarget().getTypes()
                                + " under predicate " + edge.getPredicate())), () -> System.out.println("No mapping present for edge from Instance to "
                        + edge.getTarget().getTypes()
                        + " under predicate " + edge.getPredicate())
                );
    }

    public static void mapIncomingEdge(ModelBuilder modelBuilder,
                                       ResourceEdge edge,
                                       ResourceMapping resourceMapping,
                                       String nameSpace) {
        resourceMapping.incomingEdges().stream()
                .filter(iem -> edge.getSource().getTypes().equals(iem.ldResourceDef().typeSet())
                        && edge.getPredicate().equals(iem.ldResourceDef().predicate()))
                .findFirst()
                .ifPresentOrElse(iem -> ofNullable(getMapper(iem.ldResourceDef()))
                                .ifPresentOrElse(mapper -> {
                                    mapper.mapToBibframe(edge.getSource(), modelBuilder, iem.bfNameSpace(), iem.bfResourceDef().typeSet());
                                    linkResources(modelBuilder, edge, nameSpace,
                                            iem.bfNameSpace(), iem.bfResourceDef().predicate());
                                }, () -> System.out.println("No mapper present for edge from "
                                        + edge.getSource().getTypes()
                                        + " to Instance under predicate " + edge.getPredicate())),
                        () -> System.out.println("No mapping present for edge from " + edge.getSource().getTypes()
                                + " to Instance under predicate " + edge.getPredicate())
                );
    }

    public static void linkResources(ModelBuilder modelBuilder,
                                     ResourceEdge edge,
                                     String parentNamesSpace,
                                     String targetNamesSpace,
                                     String bfPredicate) {
        modelBuilder.subject(getResourceIri(parentNamesSpace, String.valueOf(edge.getSource().getId())));
        var iri = getResourceIri(targetNamesSpace, String.valueOf(edge.getTarget().getId()));
        modelBuilder.add(bfPredicate, iri);
    }
}
