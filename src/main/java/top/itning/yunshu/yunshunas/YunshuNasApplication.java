package top.itning.yunshu.yunshunas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YunshuNasApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunshuNasApplication.class, args);
    }

}
