package com.wang.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.wang.bi.common.ErrorCode;
import com.wang.bi.constant.CommonConstant;
import com.wang.bi.exception.BusinessException;
import com.wang.bi.manager.AiManager;
import com.wang.bi.model.entity.Chart;
import com.wang.bi.service.ChartService;
import com.wang.bi.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

// 使用@Component注解标记该类为一个组件，让Spring框架能够扫描并将其纳入管理
@Component
// 使用@Slf4j注解生成日志记录器
@Slf4j
public class BiMessageConsumer {

    @Resource
    private AiManager aiManager;

    @Resource
    private ChartService chartService;

    /**
     * 接收消息的方法
     *
     * @param message     接收到的消息内容，是一个字符串类型
     * @param channel     消息所在的通道，可以通过该通道与 RabbitMQ 进行交互，例如手动确认消息、拒绝消息等
     * @param deliveryTag 消息的投递标签，用于唯一标识一条消息
     */
    // 使用@SneakyThrows注解简化异常处理
    @SneakyThrows
    // 使用@RabbitListener注解指定要监听的队列名称为"code_queue"，并设置消息的确认机制为手动确认
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    // @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag是一个方法参数注解,用于从消息头中获取投递标签(deliveryTag),
    // 在RabbitMQ中,每条消息都会被分配一个唯一的投递标签，用于标识该消息在通道中的投递状态和顺序。通过使用@Header(AmqpHeaders.DELIVERY_TAG)注解,可以从消息头中提取出该投递标签,并将其赋值给long deliveryTag参数。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receive message: {}", message);
        if (StringUtils.isBlank(message)){
            //如果更新失败，拒绝当前消息，让消息重新进入队列
            channel.basicNack(deliveryTag, false,false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null){
            //如果图表为空，拒绝消息并抛出业务异常
            channel.basicNack(deliveryTag, false,false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表为空");
        }
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        //把任务改为执行中
        updateChart.setStatus("running");
        boolean update = chartService.updateById(updateChart);
        //如果提交失败，一般情况下，更新失败可能意味着数据库出现问题了
        if (!update) {
            //如果更新图表执行中状态失败，拒绝消息并处理图表更新错误
            channel.basicNack(deliveryTag, false,false);
            handlerChartUpdateError(chart.getId(), "更新图表执行中状态失败");
            return;
        }
        //调用AI
        String result = aiManager.doChat(CommonConstant.BI_ID, buildUserInput(chart));
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            channel.basicNack(deliveryTag, false,false);
            handlerChartUpdateError(chart.getId(), "AI生成错误");
            return;
        }

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus("succeed");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            channel.basicNack(deliveryTag, false,false);
            handlerChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
        // 投递标签是一个数字标识,它在消息消费者接收到消息后用于向RabbitMQ确认消息的处理状态。通过将投递标签传递给channel.basicAck(deliveryTag, false)方法,可以告知RabbitMQ该消息已经成功处理,可以进行确认和从队列中删除。
        // 手动确认消息的接收，向RabbitMQ发送确认消息
        channel.basicAck(deliveryTag, false);
    }


    private void handlerChartUpdateError(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage("execMessage");
        boolean updateResult = chartService.updateById(updateChart);
        if (!updateResult) {
            log.error("更新图表失败{},{}", chartId, execMessage);
        }
    }

    public String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String csvData = chart.getChartData();
        String chartType = chart.getChartType();

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }



}
