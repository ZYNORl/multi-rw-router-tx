package top.zynorl.mrrtx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@MapperScan(basePackages = {"top.zynorl.mrrtx.mapper"})
public class MultiRwRouterTxApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiRwRouterTxApplication.class, args);
    }

}
