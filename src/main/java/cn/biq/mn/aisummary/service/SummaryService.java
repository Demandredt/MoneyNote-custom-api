package cn.biq.mn.aisummary.service;

import cn.biq.mn.account.AccountService;
import cn.biq.mn.balanceflow.FlowType;
import cn.biq.mn.category.CategoryType;
import cn.biq.mn.report.CategoryReportQueryForm;
import cn.biq.mn.report.ChartVO;
import cn.biq.mn.report.ReportService;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.user.User;
import cn.biq.mn.utils.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SummaryService {

    @Autowired
    private SessionUtil sessionUtil;


    @Autowired
    private ReportService reportService;
    @Autowired
    private AccountService accountService;

    public String getBillStat(Integer bookId,Long beginTime, Long endTime){
        User user = sessionUtil.getCurrentUser();


//        构建查询条件
        CategoryReportQueryForm categoryReportQueryForm = new CategoryReportQueryForm();
        categoryReportQueryForm.setBook(bookId);
        categoryReportQueryForm.setMinTime(beginTime);
        categoryReportQueryForm.setMaxTime(endTime);
        // 设置标题模糊查询
//        categoryReportQueryForm.setTitle("餐饮"); // 匹配包含"餐饮"的流水标题


        List<List<ChartVO>> dataResponseList = new ArrayList<>();
        dataResponseList.add(reportService.reportCategory(categoryReportQueryForm, CategoryType.EXPENSE));
        dataResponseList.add(reportService.reportCategory(categoryReportQueryForm, CategoryType.INCOME));
        dataResponseList.add(reportService.reportTag(categoryReportQueryForm, FlowType.EXPENSE));
        dataResponseList.add(reportService.reportTag(categoryReportQueryForm, FlowType.INCOME));
//        余额汇总
        List<List<ChartVO>> balance = reportService.reportBalance();
//        资产负债净资产
        BigDecimal[] overview = accountService.overview();


        StringBuilder summaryRequest = new StringBuilder();
        summaryRequest
                .append("接下来是统计信息。资产：")
                .append(overview[0])
                .append("，负债：")
                .append(overview[1])
                .append("，净资产：")
                .append(overview[2])
                .append("\n之后是余额以及来源的汇总：\n");
        for (List<ChartVO> list : balance){
            list.forEach(summaryRequest::append);
        }
        summaryRequest.append("接下来是按分类统计的各项支出：");
        dataResponseList.get(0).forEach(summaryRequest::append);
        summaryRequest.append("接下来是按分类统计的各项收入：");
        dataResponseList.get(1).forEach(summaryRequest::append);
        summaryRequest.append("接下来是按标签统计的各项支出：");
        dataResponseList.get(2).forEach(summaryRequest::append);
        summaryRequest.append("接下来是按标签统计的各项收入：");
        dataResponseList.get(3).forEach(summaryRequest::append);





        return summaryRequest.toString();
    }
}
