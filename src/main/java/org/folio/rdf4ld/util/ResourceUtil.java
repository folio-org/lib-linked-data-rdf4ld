package org.folio.rdf4ld.util;

import static java.lang.String.join;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;

@UtilityClass
public class ResourceUtil {

  public static List<String> getPrimaryMainTitles(Resource titledRresource) {
    if (isNull(titledRresource) || isNull(titledRresource.getOutgoingEdges())) {
      return new ArrayList<>();
    }
    return titledRresource.getOutgoingEdges().stream()
      .filter(e -> e.getPredicate() == PredicateDictionary.TITLE)
      .map(ResourceEdge::getTarget)
      .filter(t -> t.getTypes().contains(ResourceTypeDictionary.TITLE))
      .map(Resource::getDoc)
      .filter(d -> d.has(MAIN_TITLE.getValue()) || d.has(SUBTITLE.getValue()))
      .map(d -> join(" ", getFirstValue(() -> d.findValuesAsText(MAIN_TITLE.getValue())),
        getFirstValue(() -> d.findValuesAsText(SUBTITLE.getValue()))))
      .map(String::trim)
      .toList();
  }

  public static String getFirstValue(Supplier<List<String>> valuesSupplier) {
    if (isNull(valuesSupplier)) {
      return "";
    }
    var values = valuesSupplier.get();
    if (isNotEmpty(values)) {
      return values.stream()
        .filter(StringUtils::isNotBlank)
        .findFirst()
        .orElse("");
    }
    return "";
  }
}
