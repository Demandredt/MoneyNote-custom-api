package cn.biq.mn.aisummary.controller;

import cn.biq.mn.aisummary.service.SummaryService;
import cn.biq.mn.aisummary.utils.AiSummaryUtil;
import cn.biq.mn.book.BookDetails;
import cn.biq.mn.book.BookQueryForm;
import cn.biq.mn.book.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public String getAiSummary(@RequestBody String userMessage){
        BookQueryForm form = new BookQueryForm();
        form.setEnable(true); // 获取已启用的账本
        List<BookDetails> books = bookService.queryAll(form);
//       查询账本，目前默认查询第一本
        Integer id = books.get(0).getId();

//        查询时间，目前默认一个月内
        String summaryResponse =
                aiSummaryUtil.getSummary(
                        summaryService.getBillStat(
                                id,
                                System.currentTimeMillis() - 2678400000L,
                                System.currentTimeMillis()));


        return summaryResponse;
    }
}
