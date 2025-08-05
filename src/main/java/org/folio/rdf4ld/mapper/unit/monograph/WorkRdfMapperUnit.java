package org.folio.rdf4ld.mapper.unit.monograph;

import static java.lang.String.valueOf;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.rdf4ld.util.RdfUtil.WORK_LD_TO_BF_TYPES;
import static org.folio.rdf4ld.util.ResourceUtil.getPrimaryMainTitle;

import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.rdf4ld.mapper.unit.BaseRdfMapperUnit;
import org.folio.rdf4ld.mapper.unit.RdfMapperDefinition;
import org.folio.rdf4ld.mapper.unit.RdfMapperUnit;
import org.folio.rdf4ld.model.ResourceMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RdfMapperDefinition(types = WORK, predicate = INSTANTIATES)
public class WorkRdfMapperUnit implements RdfMapperUnit {
  private final BaseRdfMapperUnit baseRdfMapperUnit;
  private final FingerprintHashService hashService;
  private final Supplier<String> baseUrlProvider;

  @Override
  public Optional<Resource> mapToLd(Model model,
                                    org.eclipse.rdf4j.model.Resource resource,
                                    ResourceMapping mapping,
                                    Resource parent) {
    return baseRdfMapperUnit.mapToLd(model, resource, mapping, parent)
      .map(work -> {
        work.setLabel(getPrimaryMainTitle(work));
        readWorkType(model, resource, work);
        work.setId(hashService.hash(work));
        return work;
      });
  }

  private void readWorkType(Model model, org.eclipse.rdf4j.model.Resource resource, Resource work) {
    model.filter(resource, RDF.TYPE, null)
      .stream()
      .map(Statement::getObject)
      .map(Value::stringValue)
      .filter(type -> WORK_LD_TO_BF_TYPES.inverse().containsKey(type))
      .map(type -> WORK_LD_TO_BF_TYPES.inverse().get(type))
      .forEach(work::addType);
  }

  @Override
  public void mapToBibframe(Resource resource,
                            ModelBuilder modelBuilder,
                            ResourceMapping resourceMapping,
                            Resource parent) {
    baseRdfMapperUnit.mapToBibframe(resource, modelBuilder, resourceMapping, parent);
    writeWorkType(modelBuilder, resource);
  }

  private void writeWorkType(ModelBuilder modelBuilder, Resource resource) {
    var resourceIri = iri(baseUrlProvider.get(), valueOf(resource.getId()));
    modelBuilder.subject(resourceIri);
    resource.getTypes()
      .stream()
      .filter(WORK_LD_TO_BF_TYPES::containsKey)
      .map(WORK_LD_TO_BF_TYPES::get)
      .forEach(t -> modelBuilder.add(RDF.TYPE, iri(t)));
  }
}
