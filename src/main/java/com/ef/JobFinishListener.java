package com.ef;

import com.ef.model.AccessResult;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobFinishListener extends JobExecutionListenerSupport {

	@Value("${startDate}")
	private String startDate;

	@Value("${duration}")
	private String duration;

	@Value("${threshold}")
	private int threshold;

	@Value("${queryDate.format}")
	private String queryDateFormat;
	
	@Value("${inputDate.format}")
	private String inputDateFormat;

	@Autowired
	public JdbcTemplate jdbcTemplate;

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			List<AccessResult> results = jdbcTemplate.query(
					"SELECT ip, count(1) as request_count FROM access_log_line WHERE "
							+ "request_date >= STR_TO_DATE(?, ?) AND "
							+ "request_date <= DATE_ADD(STR_TO_DATE(?, ?), INTERVAL 1 "
							+ (duration.equals(Duration.HOURLY.toString().toLowerCase()) ? "HOUR" : "DAY") + ") "
							+ "GROUP BY ip HAVING request_count > ?",
					new Object[] { startDate, queryDateFormat, startDate, queryDateFormat, threshold },
					(resulSet, rownum) -> {
						AccessResult accessResult = new AccessResult();
						accessResult.setIp(resulSet.getString(1));
						accessResult.setComment("has " + threshold + " or more request between " + startDate + " and "
								+ Duration.valueOf(duration.toUpperCase()).dateFrom(startDate, inputDateFormat));
						return accessResult;
					});

			results.forEach((singeResult) -> {
				System.out.println("the ip :" + singeResult.getIp() + " " + singeResult.getComment());
				jdbcTemplate.update("INSERT INTO access_log_result (ip, comment) VALUES (?, ?)", singeResult.getIp(),
						singeResult.getComment());
			});

		}
	}

}