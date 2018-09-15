package com.ef.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class BatchTableConfiguration {

	@Value("classpath:org/springframework/batch/core/schema-drop-mysql.sql")
	private Resource schemaDropMySql;

	@Value("classpath:org/springframework/batch/core/schema-mysql.sql")
	private Resource schemaMySql;

	@Autowired
	public DataSource dataSource;

	@Bean
	public DataSourceInitializer dataSourceInitializer(DatabasePopulator dbPopulator) {
		DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource);
		initializer.setDatabasePopulator(dbPopulator);
		return initializer;
	}

	@Bean
	public DatabasePopulator databasePopulator() {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(schemaDropMySql);
		populator.addScript(schemaMySql);
		return populator;
	}

}
