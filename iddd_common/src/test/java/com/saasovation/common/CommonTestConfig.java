package com.saasovation.common;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mysql.jdbc.ConnectionProperties;

@Configuration
@ComponentScan("com.saasovation.common")
@EnableTransactionManagement
public class CommonTestConfig {

//	@Bean
//	public DataSource createH2DataSource() {
//	    // no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
//		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
//		EmbeddedDatabase db = builder
//			.setType(EmbeddedDatabaseType.H2) //.H2 or .DERBY
//			.addScript("schema-h2.sql")
//			.addScript("insert-types-h2.sql")
//			.addScript("insert-agencies-h2.sql")
//			.addScript("insert-divisions-h2.sql")
//			.addScript("data-h2.sql")
//			.build();
//		return db;
//	}
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/iddd_common_test");
		dataSource.setUsername("root");
		dataSource.setPassword("root");

		return dataSource;
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	@Bean
	public PlatformTransactionManager txManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
