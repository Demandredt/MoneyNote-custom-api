package cn.biq.mn.aisummary.controller;

import cn.biq.mn.aisummary.service.SummaryService;
import cn.biq.mn.aisummary.utils.AiSummaryUtil;
import cn.biq.mn.book.BookService;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SummaryController {


    @Autowired
    SummaryService summaryService;

    @Autowired
    BookService bookService;

    @Autowired
    AiSummaryUtil aiSummaryUtil;

    @PostMapping("/aisummary")
    public BaseResponse getAiSummary(@RequestBody String rawRequest){

            int id;
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(rawRequest);
                id = root.get("id").asInt();
            } catch (JsonProcessingException e) {
                System.out.println("请求反序列化错误");
                throw new RuntimeException(e);
            }
//        BookQueryForm form = new BookQueryForm();
//        form.setEnable(true); // 获取已启用的账本
//        List<BookDetails> books = bookService.queryAll(form);
//       查询账本，目前默认查询第一本
//        Integer id = books.get(0).getId();

//        查询时间，目前默认一个月内
        try {
            String summaryResponse =
                    summaryService.startAISummary(summaryService.getBillStat(
                            id,
                            System.currentTimeMillis() - 2678400000L,
                            System.currentTimeMillis()));


            ObjectMapper mapper = new ObjectMapper();
            String content = "";
            try {
                JsonNode root = mapper.readTree(summaryResponse);
                content = root.get("content").asText();
            }catch (JsonProcessingException e){
                //todo 解析错误重试
            }
            return new DataResponse<String>(content);
        }catch (Exception e){
            System.err.println("ERRORLLLL:::");
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/intelligentAdd")
    public BaseResponse getIntelligentAdd(@RequestBody String rawRequest){
        //暂定直接传全文以及账本id
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(rawRequest);
            int bookId = jsonNode.get("bookId").asInt();
            String input = jsonNode.get("input").asText();
            summaryService.intelligentAddBalanceFlow(input,bookId);
        }catch (Exception e){
            e.printStackTrace();

        }
        return new DataResponse<String>("done");
    }
}
