package io.springbatch.job;

import io.springbatch.job.decider.CustomDecider;
import io.springbatch.step.listener.CustomExitStatusListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
    @Bean(name= "simpleFlowJob")
    public Job simpleFlowJob(JobRepository jobRepository
            , @Qualifier("flow1") Flow flow1
            , @Qualifier("flow2") Flow flow2
            , @Qualifier("flowJobStep3") Step flowJobStep3) {
        return new JobBuilder("simpleFlowJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(flow1)
                .on("COMPLETED").to(flow2)
                .next(flowJobStep3)
                .end()
                .build();
    }

    @Bean(name = "flowJob")
    public Job flowJob(JobRepository jobRepository
            , @Qualifier("flowJobStep1") Step flowJobStep1
            , @Qualifier("flowJobStep2") Step flowJobStep2
            , @Qualifier("flowJobStep3") Step flowJobStep3
            , @Qualifier("flowJobStep4") Step flowJobStep4
            , @Qualifier("decider") JobExecutionDecider decider
            , @Qualifier("deciderEvenStep") Step deciderEvenStep
            , @Qualifier("deciderOddStep") Step deciderOddStep) {
        // 1번 step이 성공하면 3번 Step으로 이동, 실패하면 2번 Step 으로 이동
        return new JobBuilder("flowJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(flowJobStep1)
                    .on("COMPLETED")
                    .to(flowJobStep3)
                        .on("PASS")
                        .to(flowJobStep4)
                            .on("*")
                            .end()
                .from(flowJobStep1)
                    .on("FAILED")
                    .to(flowJobStep2)
                .from(flowJobStep3)
                    .on("*")
                    .to(decider)
                    .from(decider)
                        .on("ODD").to(deciderOddStep)
                    .from(decider)
                        .on("EVEN").to(deciderEvenStep)
                .end()
                .build();

    }

    @Bean(name = "flow1")
    public Flow flow1(@Qualifier("flowJobStep1") Step flowJobStep1, @Qualifier("flowJobStep2") Step flowJobStep2) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow1");
        flowBuilder.start(flowJobStep1).next(flowJobStep2).end();

        return flowBuilder.build();
    }

    @Bean(name = "flow2")
    public Flow flow2(@Qualifier("flowJobStep3") Step flowJobStep3, @Qualifier("flowJobStep4") Step flowJobStep4) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow2");
        flowBuilder.start(flowJobStep3)
                .on("PASS").to(flowJobStep4)
                .end();

        return flowBuilder.build();
    }

    @Bean(name = "flowJobStep1")
    public Step flowJobStep1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("flowJobStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    System.out.println(" ====================== ");
                    System.out.println(" >> Flow Job Step1 executed !!");
                    System.out.println(" ====================== ");
//                    throw new RuntimeException("Flow Job Step 1 was Failed.");
                    return RepeatStatus.FINISHED;
                }, tx) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    @Bean(name = "flowJobStep2")
    public Step flowJobStep2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("flowJobStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    System.out.println(" ====================== ");
                    System.out.println(" >> Flow Job Step 2 executed !!");
                    System.out.println(" ====================== ");
                    return RepeatStatus.FINISHED;
                }, tx) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    @Bean(name = "flowJobStep3")
    public Step flowJobStep3(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("flowJobStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    System.out.println(" ====================== ");
                    System.out.println(" >> Flow Job Step 3 executed !!");
                    System.out.println(" ====================== ");
                    return RepeatStatus.FINISHED;
                }, tx) // or .chunk(chunkSize, transactionManager)
                .listener(new CustomExitStatusListener())
                .build();
    }

    @Bean(name = "flowJobStep4")
    public Step flowJobStep4(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("flowJobStep4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    System.out.println(" ====================== ");
                    System.out.println(" >> Flow Job Step 4 executed !!");
                    System.out.println(" ====================== ");
                    return RepeatStatus.FINISHED;
                }, tx) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    @Bean(name = "decider")
    public JobExecutionDecider decider() {
        return new CustomDecider();
    }

    @Bean(name = "deciderEvenStep")
    public Step deciderEvenStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("deciderEvenStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(" ====================== ");
                    System.out.println(" >> Decider Even Step executed !!");
                    System.out.println(" ====================== ");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean(name = "deciderOddStep")
    public Step deciderOddStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("deciderOddStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(" ====================== ");
                    System.out.println(" >> Decider Odd Step executed !!");
                    System.out.println(" ====================== ");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

}
