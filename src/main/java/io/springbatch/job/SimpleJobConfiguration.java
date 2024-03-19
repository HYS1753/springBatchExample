package io.springbatch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SimpleJobConfiguration {
    @Bean
    public Job simpleJob(JobRepository jobRepository,
                        @Qualifier("simpleJobStep1") Step simpleJobStep1,
                        @Qualifier("simpleJobStep2") Step simpleJobStep2,
                        @Qualifier("simpleJobStep3") Step simpleJobStep3) {
        return new JobBuilder("simpleJob", jobRepository)
                .start(simpleJobStep1)
                .next(simpleJobStep2)
                .next(simpleJobStep3)
                .build();
    }

    @Bean(name = "simpleJobStep1")
    public Step simpleJobStep1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("simpleJobStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(" ================================= ");
                    System.out.println(" >> SimpleJob Step 1 has executed!!");
                    System.out.println(" ================================= ");

                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean(name = "simpleJobStep2")
    public Step simpleJobStep2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("simpleJobStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(" ================================= ");
                    System.out.println(" >> SimpleJob Step 2 has executed!!");
                    System.out.println(" ================================= ");

                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean(name = "simpleJobStep3")
    public Step simpleJobStep3(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("simpleJobStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 임의로 step의 상태를 fail로 만드는 방법
                    //chunkContext.getStepContext().getStepExecution().setStatus(BatchStatus.FAILED);
                    //contribution.setExitStatus(ExitStatus.STOPPED);
                    System.out.println(" ================================= ");
                    System.out.println(" >> SimpleJob Step 3 has executed!!");
                    System.out.println(" ================================= ");

                    return RepeatStatus.FINISHED;
                }, tx).build();
    }
}
