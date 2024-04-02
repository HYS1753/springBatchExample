package io.springbatch.job;
/****************************************************************************************
 * Copyright(c) 2021-2023 Kyobo Book Centre All right reserved.
 * This software is the proprietary information of Kyobo Book.
 *
 * Revision History
 * Author                         Date          Description
 * --------------------------     ----------    ----------------------------------------
 * hys1753@kyobobook.co.kr        2024-04-02
 *
 ****************************************************************************************/

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * @author : hys1753@kyobobook.co.kr
 * @Project : springBatchExample
 * @FileName : ChunkJobConfiguration
 * @Date : 2024-04-02
 * @description :
 */
@Configuration
@RequiredArgsConstructor
public class ChunkJobConfiguration {

    @Bean(name = "chunkJob")
    public Job chunkJob(JobRepository jobRepository,
                         @Qualifier("chunkStep1") Step chunkStep1,
                         @Qualifier("chunkStep2") Step chunkStep2) {
        return new JobBuilder("chunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStep1)
                .next(chunkStep2)
                .build();
    }

    @Bean(name = "chunkStep1")
    public Step chunkStep1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("chunkStep1", jobRepository)
                .<String, String>chunk(2, tx)  // <> 안에 Input Output Type 을 적어줌.
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5")))
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        Thread.sleep(300);
                        System.out.println("Item = " + item);
                        return "my " + item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(Chunk<? extends String> chunk) throws Exception {
                        Thread.sleep(300);
                        System.out.println("items = " + chunk);
                        chunk.forEach(item -> System.out.println(item));
                    }
                })
                .build();
    }

    @Bean(name = "chunkStep2")
    public Step chunkStep2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("chunkStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(" ====================== ");
                    System.out.println(" >> Chunk Step 2 has executed");
                    System.out.println(" ====================== ");

                    return RepeatStatus.FINISHED;
                }, tx).build();
    }
}
