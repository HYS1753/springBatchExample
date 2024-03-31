package io.springbatch.job;

import io.springbatch.job.listener.JobScopeJobListener;
import io.springbatch.job.listener.JobScopeStepListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class BatchScopeJobConfiguration {
    @Bean(name= "scopeJob")
    public Job scopeJob(JobRepository jobRepository
            , @Qualifier("scopeStep1") Step scopeStep1
            , @Qualifier("scopeStep2") Step scopeStep2) {
        return new JobBuilder("scopeJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(scopeStep1)
                .next(scopeStep2)
                .listener(new JobScopeJobListener())
                .build();
    }

    @Bean(name = "scopeStep1")
    @JobScope
    public Step scopeStep1(JobRepository jobRepository, PlatformTransactionManager tx
                          , @Value("#{jobParameters['message']}") String message) {
        System.out.println("mesage = " + message);
        return new StepBuilder("scopeStep1", jobRepository)
                .tasklet( tasklet1(null), tx).build();
    }

    @Bean(name = "scopeStep2")
    public Step scopeStep2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("scopeStep2", jobRepository)
                .tasklet(tasklet2(null), tx)
                .listener(new JobScopeStepListener())
                .build();
    }

    @StepScope
    @Bean(name = "scopeStepTasklet1")
    public Tasklet tasklet1(@Value("#{jobExecutionContext['name']}") String name) {
        System.out.println("name = " + name);
        return (contribution, chunkContext) -> {
            System.out.println(" ========================== ");
            System.out.println(" >> ScopeJob1 has executed! ");
            System.out.println(" ========================== ");
            return RepeatStatus.FINISHED;
        };
    }

    @StepScope
    @Bean(name = "scopeStepTasklet2")
    public Tasklet tasklet2(@Value("#{stepExecutionContext['name2']}") String name2) {
        System.out.println("name2 = " + name2);
        return (contribution, chunkContext) -> {
            System.out.println(" ========================== ");
            System.out.println(" >> ScopeJob2 has executed! ");
            System.out.println(" ========================== ");
            return RepeatStatus.FINISHED;
        };
    }
}
