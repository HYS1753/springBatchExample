package io.springbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 스프링 배치 작동을 위한 어노테이션(스프링 배치 초기화 실행 구성)
// 스프링 배치 5 이상 부터는 config로 직접 설정 할 것이 아니라면 제거해야 함.
//@EnableBatchProcessing
public class SpringBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchApplication.class, args);
	}

}
