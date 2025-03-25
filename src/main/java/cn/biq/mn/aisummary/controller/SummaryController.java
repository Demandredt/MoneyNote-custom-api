package cn.biq.mn.aisummary.controller;

import cn.biq.mn.aisummary.model.SummaryRequest;
import cn.biq.mn.aisummary.service.SummaryService;
import cn.biq.mn.aisummary.utils.AiSummaryUtil;
import cn.biq.mn.book.BookDetails;
import cn.biq.mn.book.BookQueryForm;
import cn.biq.mn.book.BookService;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataMessageResponse;
import cn.biq.mn.response.DataResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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
                    aiSummaryUtil.getSummary(
                            summaryService.getBillStat(
                                    id,
                                    System.currentTimeMillis() - 2678400000L,
                                    System.currentTimeMillis()));


            return new DataResponse<String>(summaryResponse);
        }catch (Exception e){
            System.err.println("ERRORLLLL:::");
            e.printStackTrace();
            throw e;
        }
    }
}
