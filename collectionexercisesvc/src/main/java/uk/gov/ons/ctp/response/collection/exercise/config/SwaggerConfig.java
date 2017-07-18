package uk.gov.ons.ctp.response.collection.exercise.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.gov.ons.ctp.response.collection.exercise.endpoint.CollectionExerciseEndpoint;



/**
 * Config POJO for Swagger UI
 */
@Configuration
@EnableSwagger2
@ComponentScan(basePackageClasses = {
        CollectionExerciseEndpoint.class
})
public class SwaggerConfig {

  @Autowired
  private AppConfig appConfig;

  @Bean
  public Docket postsApi() {

    SwaggerSettings swaggerSettings = appConfig.getSwaggerSettings();

    ApiInfo apiInfo = new ApiInfoBuilder()
            .title(swaggerSettings.getTitle())
            .description(swaggerSettings.getDescription())
            .version(swaggerSettings.getVersion())
            .build();

    java.util.function.Predicate<String> pathSelector;

    if (swaggerSettings.getSwaggerUiActive()) {
      pathSelector = PathSelectors.any()::apply;
    } else {
      pathSelector = PathSelectors.none()::apply;
    }

    return new Docket(DocumentationType.SWAGGER_2)
            .groupName(swaggerSettings.getGroupName())
            .apiInfo(apiInfo)
            .select()
            .apis(RequestHandlerSelectors.basePackage("uk.gov.ons.ctp.response.collection.exercise.endpoint"))
            .paths(pathSelector::test)
            .build();
  }

}
