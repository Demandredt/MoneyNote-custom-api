package cn.biq.mn.aisummary.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AddBalance {
    @SystemMessage("{{s}}")
    String add(@V("s") String systemmsg, @UserMessage String usermsg);
}
