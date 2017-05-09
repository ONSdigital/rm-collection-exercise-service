package uk.gov.ons.ctp.response.collection.exercise;

import org.springframework.stereotype.Component;

import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;

/**
 * The MapperFactory to obtain the MapperFacade to map Entity objects to/from
 * presentation objects.
 */
@Component
public class CollectionExerciseBeanMapper extends ConfigurableMapper {

  @Override
  public void configureFactoryBuilder(DefaultMapperFactory.Builder builder) {
    builder.compilerStrategy(new EclipseJdtCompilerStrategy());
  }
}
