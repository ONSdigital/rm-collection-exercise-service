package uk.gov.ons.ctp.response.collection.exercise;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

public class CustomSpringLiquibase implements InitializingBean, BeanNameAware, ResourceLoaderAware {
  private SpringLiquibase springLiquibase;

  public CustomSpringLiquibase(SpringLiquibase liquibase) {
    this.springLiquibase = liquibase;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    springLiquibase.afterPropertiesSet();
  }

  @Override
  public void setBeanName(String name) {
    springLiquibase.setBeanName(name);
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    springLiquibase.setResourceLoader(resourceLoader);
  }
}
