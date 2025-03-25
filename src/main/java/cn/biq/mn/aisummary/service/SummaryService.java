package cn.biq.mn.aisummary.service;

import cn.biq.mn.account.AccountService;
import cn.biq.mn.balanceflow.*;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.Book;
import cn.biq.mn.book.BookRepository;
import cn.biq.mn.category.CategoryType;
import cn.biq.mn.report.CategoryReportQueryForm;
import cn.biq.mn.report.ChartVO;
import cn.biq.mn.report.ReportService;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.user.User;
import cn.biq.mn.utils.MessageSourceUtil;
import cn.biq.mn.utils.SessionUtil;
import cn.biq.mn.utils.WebUtils;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Log4j2
@Service
public class SummaryService {

    @Autowired
    private SessionUtil sessionUtil;


    @Autowired
    private ReportService reportService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private  BaseService baseService;
    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Autowired
    private BalanceFlowMapper balanceFlowMapper;
    @Autowired
    private BalanceFlowRepository balanceFlowRepository;

    @Autowired
    private BookRepository bookRepository;


    /**
     * 生成总体收支数据统计
     * @param bookId
     * @param beginTime
     * @param endTime
     * @return
     */
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
                .append("\n接下来是你要处理的统计信息。资产：")
                .append(overview[0])
                .append("，负债：")
                .append(overview[1])
                .append("，净资产：")
                .append(overview[2])
                .append("\n之后是余额以及来源的汇总：\n");
        for (List<ChartVO> list : balance){
            list.forEach(summaryRequest::append);
        }
        summaryRequest.append("接下来是按分类统计的各项支出(冒号前为分类名，冒号后为金额)：");
        dataResponseList.get(0).forEach(summaryRequest::append);
        summaryRequest.append("接下来是按分类统计的各项收入(冒号前为分类名，冒号后为金额)：");
        dataResponseList.get(1).forEach(summaryRequest::append);
        summaryRequest.append("接下来是按标签统计的各项支出(冒号前为标签名，冒号后为金额)：\n");
        dataResponseList.get(2).forEach(summaryRequest::append);
        summaryRequest.append("接下来是按标签统计的各项收入(冒号前为标签名，冒号后为金额)：\n");
        dataResponseList.get(3).forEach(summaryRequest::append);
        summaryRequest.append("\n接下来是详细的各项收支数据：\n");
        summaryRequest.append(generateFlowTextReport(bookId,8));




        log.info(summaryRequest.toString());
        return summaryRequest.toString();
    }

    /**
     * 生成详细的收支数据
     * @param id
     * @param timeZoneOffset
     * @return
     */
    @Transactional
    public String generateFlowTextReport(Integer id, Integer timeZoneOffset) {
        Book book = baseService.getBookInGroup(id);

        // 保留原有限制逻辑（根据需要取消注释）
        // if (CalendarUtil.inLastDay(book.getExportAt())) {
        //     throw new FailureMessageException("book.export.limit.fail");
        // }

        // 构建文本内容缓冲区
        StringBuilder reportBuilder = new StringBuilder();

        // 添加元数据头
        reportBuilder.append("=== 账单数据报告 ===").append("\n");
        reportBuilder.append("账簿名称: ").append(book.getName()).append("\n");
        reportBuilder.append("生成时间: ").append(formatCurrentTime(timeZoneOffset)).append("\n\n");

        // 构建字段说明头
        String[] fieldLabels = {
                messageSourceUtil.getMessage("book.export.title"),
                messageSourceUtil.getMessage("book.export.type"),
                messageSourceUtil.getMessage("book.export.amount"),
                messageSourceUtil.getMessage("book.export.time"),
                messageSourceUtil.getMessage("book.export.account"),
                messageSourceUtil.getMessage("book.export.category"),
                messageSourceUtil.getMessage("book.export.tag"),
                messageSourceUtil.getMessage("book.export.payee"),
                messageSourceUtil.getMessage("book.export.note"),
                messageSourceUtil.getMessage("book.export.confirm"),
                messageSourceUtil.getMessage("book.export.include"),
        };

        // 获取数据（保留原有查询逻辑）
//        List<BalanceFlow> balanceFlows = balanceFlowRepository.findAllByBookOrderByCreateTimeDesc(book);
        List<BalanceFlow> balanceFlows = balanceFlowRepository.findAllByBookWithAssociations(book);


        balanceFlows.forEach(flow -> {
            if(flow.getAccount() != null) flow.getAccount().getId();
            if(flow.getPayee() != null) flow.getPayee().getId();
            if(flow.getTo() != null) flow.getTo().getId();
            // 对于集合需要特殊处理
            flow.getTags().size();
            flow.getCategories().size();
        });



        List<BalanceFlowDetails> detailsList = balanceFlows.stream()
                .map(balanceFlowMapper::toDetails)
                .toList();

        // 构建数据记录
        int recordCount = 1;
        for (BalanceFlowDetails item : detailsList) {
            reportBuilder.append("记录#").append(recordCount++).append(":\n");

            // 使用显式字段说明格式
            appendField(reportBuilder, fieldLabels[0], item.getTitle());
            appendField(reportBuilder, fieldLabels[1], item.getTypeName());
            appendField(reportBuilder, fieldLabels[2], item.getAmount().toString());

            // 时间格式化（保留原有逻辑）
            Date createDate = new Date(item.getCreateTime());
            String lang = WebUtils.getAcceptLang();
            String formattedTime = formatDateTime(createDate, timeZoneOffset, lang);
            appendField(reportBuilder, fieldLabels[3], formattedTime);

            appendField(reportBuilder, fieldLabels[4], item.getAccountName());
            appendField(reportBuilder, fieldLabels[5], item.getCategoryName());
            appendField(reportBuilder, fieldLabels[6], item.getTagsName());

            // 处理可能为空的字段
            String payeeName = (item.getPayee() != null) ? item.getPayee().getName() : "";
            appendField(reportBuilder, fieldLabels[7], payeeName);

            appendField(reportBuilder, fieldLabels[8], item.getNotes());

            // 布尔值转换（保留原有多语言处理）
            String confirmStatus = item.getConfirm() ?
                    messageSourceUtil.getMessage("yes") :
                    messageSourceUtil.getMessage("no");
            appendField(reportBuilder, fieldLabels[9], confirmStatus);

            if (item.getInclude() != null) {
                String includeStatus = item.getInclude() ?
                        messageSourceUtil.getMessage("yes") :
                        messageSourceUtil.getMessage("no");
                appendField(reportBuilder, fieldLabels[10], includeStatus);
            }

            reportBuilder.append("\n"); // 记录分隔空行
        }

        // 更新导出时间（保留原有逻辑）
        book.setExportAt(System.currentTimeMillis());
        bookRepository.save(book);

        return reportBuilder.toString();
    }

    // 以下是新增的辅助方法
    private void appendField(StringBuilder builder, String label, String value) {
        builder.append("  ")
                .append(label).append(": ")
                .append(value != null ? value.replace("\n", " ") : "") // 处理null值
                .append(", \n");
    }

    private String formatCurrentTime(Integer timeZoneOffset) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(timeZoneOffset))));
        return sdf.format(new Date());
    }

    private String formatDateTime(Date date, Integer timeZoneOffset, String lang) {
        String pattern = "zh-CN".equals(lang) ?
                "yyyy-MM-dd HH:mm:ss" :
                "MM/dd/yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(timeZoneOffset))));
        return sdf.format(date);
    }
}
