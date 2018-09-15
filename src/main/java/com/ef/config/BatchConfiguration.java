package com.ef.config;

import java.beans.PropertyEditor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.ef.JobFinishListener;
import com.ef.model.AccessLog;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Value("${accesslog}")
	private String accesslog;

	@Value("${requestDate.format}")
	private String requestDateFormat;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	@SuppressWarnings("rawtypes")
	@Bean
	public FlatFileItemReader<AccessLog> reader() {

		FlatFileItemReader<AccessLog> reader = new FlatFileItemReader<>();

		reader.setResource(new FileSystemResource(accesslog));

		reader.setResource(new FileSystemResource(accesslog));
		DefaultLineMapper<AccessLog> defaultLineMapper = new DefaultLineMapper<AccessLog>();
		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer("|");
		delimitedLineTokenizer.setNames(new String[] { "request_date", "ip", "request", "status", "user_agent" });

		BeanWrapperFieldSetMapper<AccessLog> fieldSetMapper = new BeanWrapperFieldSetMapper<AccessLog>();
		fieldSetMapper.setTargetType(AccessLog.class);

		final DateFormat df = new SimpleDateFormat(requestDateFormat);
		final Map<Class, PropertyEditor> customEditors = Stream.of(new AbstractMap.SimpleEntry<>(Date.class, new CustomDateEditor(df, false)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		fieldSetMapper.setCustomEditors(customEditors);

		defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
		defaultLineMapper.setFieldSetMapper(fieldSetMapper);
		reader.setLineMapper(defaultLineMapper);

		return reader;
	}

	@Bean
	public JdbcBatchItemWriter<AccessLog> writer() {
		JdbcBatchItemWriter<AccessLog> writer = new JdbcBatchItemWriter<>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
		writer.setSql("INSERT INTO access_log_line (request_date, ip, request, status, user_agent) "
				+ "VALUES (:request_date, :ip, :request, :status, :user_agent)");
		writer.setDataSource(dataSource);
		return writer;
	}

	@Bean
	public Job acceslogJob(JobFinishListener listener) {
		return jobBuilderFactory.get("acceslogJob").incrementer(new RunIdIncrementer()).listener(listener)
				.flow(step1()).end().build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<AccessLog, AccessLog>chunk(1000).reader(reader()).writer(writer())
				.build();
	}

}
