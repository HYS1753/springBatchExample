package io.springbatch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class FlowJobConfiguration {
    @Bean
    public Job flowJob(JobRepository jobRepository, @Qualifier("flow1") Flow flow1, @Qualifier("step3") Step step3) {
        return new JobBuilder("flowJob", jobRepository)
                .start(flow1)
                .next(step3)
                .end()
                .build();
    }

    @Bean(name = "flow1")
    public Flow flow1(@Qualifier("step1") Step flowStep1, @Qualifier("step2") Step flowStep2) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow1");
        flowBuilder.start(flowStep1).next(flowStep2).end();

        return flowBuilder.build();
    }

    @Bean(name = "step1")
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    System.out.println(" ====================== ");
                    System.out.println(" >> Step 1 executed !!");
                    System.out.println(" ====================== ");
                    return RepeatStatus.FINISHED;
                }, tx) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    @Bean(name = "step2")
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    System.out.println(" ====================== ");
                    System.out.println(" >> Step 2 executed !!");
                    System.out.println(" ====================== ");
                    return RepeatStatus.FINISHED;
                }, tx) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    @Bean(name = "step3")
    public Step step3(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("step3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    System.out.println(" ====================== ");
                    System.out.println(" >> Step 3 executed !!");
                    System.out.println(" ====================== ");
                    return RepeatStatus.FINISHED;
                }, tx) // or .chunk(chunkSize, transactionManager)
                .build();
    }

}
