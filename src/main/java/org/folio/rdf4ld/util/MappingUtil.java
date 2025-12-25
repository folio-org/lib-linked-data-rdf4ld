package org.folio.rdf4ld.util;

import static java.util.Optional.ofNullable;

import java.util.Set;
import lombok.experimental.UtilityClass;
import org.folio.rdf4ld.model.BfResourceDef;
import org.folio.rdf4ld.model.ResourceInternalMapping;
import org.folio.rdf4ld.model.ResourceMapping;

@UtilityClass
public class MappingUtil {

  public static ResourceMapping getEdgeMapping(ResourceInternalMapping resourceMapping, int number) {
    return ofNullable(resourceMapping)
      .map(ResourceInternalMapping::getOutgoingEdges)
      .filter(oe -> oe.size() > number)
      .map(oe -> oe.toArray(new ResourceMapping[number])[number])
      .orElse(null);
  }

  public static String getEdgePredicate(ResourceInternalMapping resourceMapping, int number) {
    return ofNullable(getEdgeMapping(resourceMapping, number))
      .map(ResourceMapping::getBfResourceDef)
      .map(BfResourceDef::getPredicate)
      .orElse(null);
  }

  public static Set<String> getEdgeTypeSet(ResourceInternalMapping resourceMapping, int number) {
    return ofNullable(getEdgeMapping(resourceMapping, number))
      .map(ResourceMapping::getBfResourceDef)
      .map(BfResourceDef::getTypeSet)
      .orElse(Set.of());
  }
}
