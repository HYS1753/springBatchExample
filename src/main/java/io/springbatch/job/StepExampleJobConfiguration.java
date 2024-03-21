package io.springbatch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;


@Configuration
@RequiredArgsConstructor
public class StepExampleJobConfiguration {

    @Bean
    public Job stepJob(JobRepository jobRepository,
                         @Qualifier("taskletStep") Step taskletStep,
                         @Qualifier("chunkStep") Step chunkStep,
                         @Qualifier("paritionerStep") Step paritionerStep) {
        return new JobBuilder("stepJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(taskletStep)
                .next(chunkStep)
                //.next(paritionerStep)
                .build();
    }

    @Bean(name="taskletStep")
    public Step taskletStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("taskletStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("tasklet Step has executed!");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean(name="chunkStep")
    public Step chunkStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("chunkStep", jobRepository)
                .<String, String>chunk(2, tx)
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5")))
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        return item.toUpperCase();
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(Chunk<? extends String> chunk) throws Exception {
                        chunk.forEach(item -> System.out.println(item));
                    }
                })
                .build();
    }

    @Bean(name="paritionerStep")
    public Step partitionerStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("partitionerStep", jobRepository)
                .partitioner(taskletStep(jobRepository, tx))
                .gridSize(2)
                .build();
    }

    @Bean(name="jobStep")
    public Step jobStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("jobStep", jobRepository)
                .job(jobStepExampleJob(jobRepository, tx))
                .build();
    }

    @Bean(name="flowStep")
    public Step flowStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("flowStep", jobRepository)
                .flow(flowStepExampleFlow(jobRepository, tx))
                .build();
    }

    private Job jobStepExampleJob(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new JobBuilder("jobStepExampleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(taskletStep(jobRepository, tx))
                .next(chunkStep(jobRepository, tx))
                //.next(partitionerStep(jobRepository, tx))
                .build();
    }

    private Flow flowStepExampleFlow(JobRepository jobRepository, PlatformTransactionManager tx) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flowStepExampleFlow");
        flowBuilder.start(chunkStep(jobRepository, tx)).end();
        return flowBuilder.build();
    }


}
