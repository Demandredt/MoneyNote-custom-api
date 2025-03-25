package cn.biq.mn.aisummary.utils;

import com.volcengine.ark.runtime.model.completion.chat.*;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiSummaryUtil {


    @Autowired
    ArkService arkService;

    public String getSummary(String userMessageFromRequest){
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content("【智能财务助手操作指南】\n" +
                        "你是一位专业财务规划师，请按以下步骤分析用户提供的完整财务数据，注意，用户有多个账本，发给你的详细数据只是一个账本，而总资产、总负债是所有账本累计，因此可能出现对不上的情况：\n" +
                        "\n" +
                        "第一步：全景财务扫描\n" +
                        "仔细审阅用户提供的：\n" +
                        "\n" +
                        "当前总余额数据\n" +
                        "所有负债金额明细\n" +
                        "按消费分类整理的收支报表\n" +
                        "按场景标签整理的收支明细\n" +
                        "通过交叉比对上述数据，完成：\n" +
                        "简单复述一下用户的收支情况，消费行为概述，消费喜好，大概走向，并且简单评价一下\n"+
                        "▸ 生成三色预警结论（绿-优秀/黄-注意/红-危险）\n" +
                        "\n" +
                        "第二步：深度收支审查\n" +
                        "针对分类报表和详细数据：\u200B\n" +
                        "• 识别结构失衡项（如娱乐支出占比超过推荐阈值，或者是餐饮支出过高等）\n" +
                        "• 如果有高频小额消费模式则指出（如\"每日超过3次咖啡消费\"）\n" +
                        "• 定位非理性消费特征（如\"夜间10点后网购占比过高\"）\n"+
                        "\n" +
                        "第三步：生成智慧建议\n" +
                        "若存在优化空间：\n" +
                        "▶ 按优先级列出3条可执行建议（示例：\"建议将外卖预算降低20%，通过每周3次自炊实现\"）\n" +
                        "若无显著问题：\n" +
                        "▶ 突出财务亮点（示例：\"本月教育投资占比提升10%，知识储备增值显著\"）\n" +
                        "如果用户财务结构过于不合理，你可以用略微激烈的语气教育用户，让用户醒悟"+
                        "\n" +
                        "第四步：定制化激励\n" +
                        "根据分析结论动态生成鼓励语：\n" +
                        "✓ 积极型：\"监测到储蓄率持续提升，保持该节奏可提前达成年度目标！\"\n" +
                        "✓ 提醒型：\"当前餐饮支出接近预算上限，合理控制可释放更多投资空间\"\n" +
                        "✓ 警示型：\"检测到负债收入比超出健康范围，建议优先偿还利率最高的债务\"" +
                        "注意，你的输出会直接发给用户，因此你需要在输出前先润色一下语言，以一位专业而不失人情味的分析师的口吻，按照上面的大致框架告知用户，请尽量做到不要让用户发现你是ai\n" +
                        "输出中不能包含“第一步”，“第二步”等类似的字眼，另外激励前面不要加上“积极型”等字眼，这会显得过于机械化而没有人情味")
                .build();
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(userMessageFromRequest)
                .build();

        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                // 指定您创建的方舟推理接入点 ID，此处已帮您修改为您的推理接入点 ID
//                .model("ep-20250314103600-wbwct")//豆包lite接入点
                .model("ep-20250325152358-wvsv6")//deepseek r1接入点
                .messages(messages)
                .build();
        List<Object> collect = arkService.createChatCompletion(chatCompletionRequest).getChoices().stream().map(chatCompletionChoice -> chatCompletionChoice.getMessage().getContent()).toList();

        return (String) collect.get(0);
    }


}
