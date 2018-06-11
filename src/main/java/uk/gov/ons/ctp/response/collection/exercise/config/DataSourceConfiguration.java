package uk.gov.ons.ctp.response.collection.exercise.config;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import net.sourceforge.cobertura.CoverageIgnore;

/**
 * DataSource bean
 *
 */
@CoverageIgnore
@Configuration
@Profile("cloud")
public class DataSourceConfiguration {

    @Bean
    public Cloud cloud() {
        return new CloudFactory().getCloud();
    }
    
    @Bean
    @ConfigurationProperties(prefix="spring.datasource.tomcat")
    public DataSource dataSource() {
        return cloud().getSingletonServiceConnector(DataSource.class, null);
    }

}