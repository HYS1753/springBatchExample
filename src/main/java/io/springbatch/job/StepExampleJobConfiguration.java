package io.springbatch.job;

import io.springbatch.tasklet.CustomTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.core.step.job.JobParametersExtractor;
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
                         @Qualifier("customTaskletStep") Step customTaskletStep,
                         @Qualifier("chunkStep") Step chunkStep,
                         @Qualifier("jobStep") Step jobStep,
                         @Qualifier("paritionerStep") Step paritionerStep) {
        return new JobBuilder("stepJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(taskletStep)
                .next(customTaskletStep)
                .next(chunkStep)
                .next(jobStep)
                //.next(paritionerStep)
                .build();
    }

    @Bean(name="taskletStep")
    public Step taskletStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("taskletStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {  // 익명 클래스로 생성하는 방법.
                    System.out.println("tasklet Step has executed!");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean(name="customTaskletStep")
    public Step customTaskletStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("customTaskletStep", jobRepository)
                .tasklet(new CustomTasklet(), tx)
                .startLimit(3)              // 최대 실행 가능 횟수 초과 시 StartLimitExceededExecption 발생
                .allowStartIfComplete(true) // Step 이 성공했어도 다시 실행 하도록 설정하는 값.
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        StepExecutionListener.super.beforeStep(stepExecution);
                        System.out.println("before CustomTaskletStep.");
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        System.out.println("After CustomTaskletStep.");
                        return StepExecutionListener.super.afterStep(stepExecution);
                    }
                })
                .build();
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
    public Step jobStep(JobRepository jobRepository, PlatformTransactionManager tx, JobLauncher jobLauncher) {
        return new StepBuilder("jobStep", jobRepository)
                .job(childJob(jobRepository, tx))
                .launcher(jobLauncher)
                .parametersExtractor(jobParametersExtractor())
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        StepExecutionListener.super.beforeStep(stepExecution);
                        stepExecution.getExecutionContext().putString("name", "user1");
                    }
                })
                .build();
    }

    @Bean(name="flowStep")
    public Step flowStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("flowStep", jobRepository)
                .flow(flowStepExampleFlow(jobRepository, tx))
                .build();
    }

    private Job childJob(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new JobBuilder("childJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(taskletStep(jobRepository, tx))
                .build();
    }

    private DefaultJobParametersExtractor jobParametersExtractor() {
        // StepExecution context 내의 값을 Child Job의 JobExecution context 의 값으로 변경시킴.
        // setKeys의 경우 stepExecution Context 내에 설정한 Key 값이 있으면 넣겠다는 의미(여기서는 name)
        DefaultJobParametersExtractor extractor = new DefaultJobParametersExtractor();
        extractor.setKeys(new String[]{"name"});
        return extractor;
    }

    private Flow flowStepExampleFlow(JobRepository jobRepository, PlatformTransactionManager tx) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flowStepExampleFlow");
        flowBuilder.start(chunkStep(jobRepository, tx)).end();
        return flowBuilder.build();
    }


}
