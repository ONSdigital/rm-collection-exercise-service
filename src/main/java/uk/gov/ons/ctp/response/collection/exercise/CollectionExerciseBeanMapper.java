package uk.gov.ons.ctp.response.collection.exercise;

import ma.glasnost.orika.impl.ConfigurableMapper;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.stereotype.Component;

/**
 * The MapperFactory to obtain the MapperFacade to map Entity objects to/from presentation objects.
 */
@CoverageIgnore
@Component
public class CollectionExerciseBeanMapper extends ConfigurableMapper {}
