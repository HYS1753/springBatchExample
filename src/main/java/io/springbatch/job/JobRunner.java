package io.springbatch.job;

public class JobRunner {

}

/**
 * ApplicationRunner를 상속 받아 시스템 자체적으로 run 시키는 방법.
 */

//@RequiredArgsConstructor
//@Component
//public class JobRunner implements ApplicationRunner {
//
//    private final JobLauncher jobLauncher;
//
//    private final Job helloJob;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addString("name", "user1")
//                .addLong("seq", 2L)
//                .addDate("date", new Date())
//                .addDouble("age", 20.0)
//                .toJobParameters();
//
//        jobLauncher.run(helloJob, jobParameters);
//    }
//}
