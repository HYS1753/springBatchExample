package io.springbatch.controller;

import io.springbatch.config.CustomJobLauncher;
import io.springbatch.data.dto.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
public class JobLauncherController {

    private final CustomJobLauncher jobLauncher;
    private final Job helloJob;

    @PostMapping("/helloBatch")
    public String helloBatch(@RequestBody Member member) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Job Parameter 생성
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("id", member.getId())
                .addDate("date", new Date())
                .toJobParameters();

        // JobLauncher 실행
        JobExecution jobExecution = jobLauncher.defaultJobLauncher().run(helloJob, jobParameters);

        return "Batch Completed. Job Exit Status : " + jobExecution.getExitStatus().getExitCode();
    }

    @PostMapping("/asyncHelloBatch")
    public String asyncHelloBatch(@RequestBody Member member) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Job Parameter 생성
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("id", member.getId())
                .addDate("date", new Date())
                .toJobParameters();

        // Async JobLauncher 생성 및 실행
        JobExecution jobExecution = jobLauncher.asyncJobLauncher().run(helloJob, jobParameters);

        return "Async Batch Completed. Job Exit Status : " + jobExecution.getExitStatus().getExitCode();
    }
}
