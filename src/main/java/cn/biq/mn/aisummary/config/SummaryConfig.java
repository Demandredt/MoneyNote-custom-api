package cn.biq.mn.aisummary.config;

import com.querydsl.core.annotations.Config;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SummaryConfig {


    @Bean
    ArkService arkService(){
        String apiKey = "8cc6b3d6-503f-481e-ad93-156d614ea958";
        return ArkService.builder()
                .apiKey(apiKey)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
                .build();
    }


}
