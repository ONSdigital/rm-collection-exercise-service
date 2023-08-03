package uk.gov.ons.ctp.response.collection.exercise;

import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

public class CustomProvider implements SchemaFilterProvider {

  @Override
  public SchemaFilter getCreateFilter() {
    return CustomFilter.INSTANCE;
  }

  @Override
  public SchemaFilter getDropFilter() {
    return CustomFilter.INSTANCE;
  }

  @Override
  public SchemaFilter getMigrateFilter() {
    return CustomFilter.INSTANCE;
  }

  @Override
  public SchemaFilter getValidateFilter() {
    return CustomFilter.INSTANCE;
  }
}

class CustomFilter implements SchemaFilter {
  public static final CustomFilter INSTANCE = new CustomFilter();

  @Override
  public boolean includeNamespace(Namespace namespace) {
    return true;
  }

  @Override
  public boolean includeTable(Table table) {
    return true;
  }

  @Override
  public boolean includeSequence(Sequence sequence) {
    // Let Liquibase handle the sequence creation, as its done this in the past
    // and stripping that out is too messy right now (Spring Boot 2.6.6 upgrade)
    return false;
  }
}
