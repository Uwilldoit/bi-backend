package com.wang.bi.mq;

import com.rabbitmq.client.*;

public class DirectConsumer {
    // 定义我们正在监听的交换机名称"direct-exchange"
    private static final String EXCHANGE_NAME = "direct-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 创建队列，随机分配一个队列名称，并绑定到 "laowang" 路由键
        String queueName = "laowang_queue";
        // 声明队列，设置队列为持久化的，非独占的，非自动删除的
        channel.queueDeclare(queueName, true, false, false, null);
        // 将队列绑定到指定的交换机上，并指定绑定的路由键为 "laownag"
        channel.queueBind(queueName, EXCHANGE_NAME, "laowang");

        // 创建队列，随机分配一个队列名称，并绑定到 "laoli" 路由键
        String queueName2 = "laoli_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        // 将队列绑定到指定的交换机上，并指定绑定的路由键为 "laoli"
        channel.queueBind(queueName2, EXCHANGE_NAME, "laoli");
        // 打印等待消息的提示信息
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 创建一个 DeliverCallback 实例来处理接收到的消息（laownag）
        DeliverCallback xiaoyuDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [laowang] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 创建一个 DeliverCallback 实例来处理接收到的消息（laoli）
        DeliverCallback xiaopiDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [laoli] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        // 开始消费队列中的消息（laowang），设置自动确认消息已被消费
        channel.basicConsume(queueName, true, xiaoyuDeliverCallback, consumerTag -> {
        });
        // 开始消费队列中的消息（laoli），设置自动确认消息已被消费
        channel.basicConsume(queueName2, true, xiaopiDeliverCallback, consumerTag -> {
        });
    }
}
