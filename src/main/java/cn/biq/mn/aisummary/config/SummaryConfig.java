package cn.biq.mn.aisummary.config;

import com.querydsl.core.annotations.Config;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SummaryConfig {

    @Value("${secret.api.key}")
    String apiKey;
    @Bean
    ArkService arkService(){

        return ArkService.builder()
                .apiKey(apiKey)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
                .build();
    }


}
