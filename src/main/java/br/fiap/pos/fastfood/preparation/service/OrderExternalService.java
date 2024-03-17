package br.fiap.pos.fastfood.preparation.service;

import br.fiap.pos.fastfood.preparation.domain.MessageOrder;
import br.fiap.pos.fastfood.preparation.domain.Order;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class OrderExternalService {

    private static final Logger logger = LoggerFactory.getLogger(OrderExternalService.class);


    private final SqsClient sqsClient;


    private final String preparationQueueUrl;

    private final String customerQueueUrl;

    public OrderExternalService(@Value("${aws.accessKey}") String accessKey,
                                @Value("${aws.secretKey}") String accessSecret,
                                @Value("${preparation.queue.url}") String preparationQueueUrl,
                                @Value("${customer.queue.url}") String customerQueueUrl
                                ){


        AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, accessSecret);
        this.sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        this.preparationQueueUrl = preparationQueueUrl;
        this.customerQueueUrl = customerQueueUrl;

    }


    // publish message in queue SQS
    public void publishMessageInQueueSQS(Order order) {

        String messageJSONBody = new Gson().toJson(convertOrderToMessageOrder(order));

        // Send Prepraration message to the queue
        sendMessageToQueueSQS(messageJSONBody, preparationQueueUrl, "Preparation", order.getId());

        // Send Customer message to the queue
        sendMessageToQueueSQS(messageJSONBody, customerQueueUrl, "Customer", order.getId());

        logger.info("Finish Publish message, orderId: {}", order.getId());
    }

    public void sendMessageToQueueSQS(String message, String queueUrl, String groupName, String deduplicationId) {

        logger.info("Sending message to SQS: {} queueUrl: {} groupName: {} deduplicationId: {}", message, queueUrl, groupName, deduplicationId);

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .messageGroupId(groupName)
                .messageDeduplicationId(deduplicationId)
                .build();

        // Send message to the queue
        SendMessageResponse result = sqsClient.sendMessage(sendMessageRequest);

        logger.info("Message sent to SQS with messageId: {}", result.messageId());
    }


    public MessageOrder convertOrderToMessageOrder(Order order) {

        MessageOrder messageOrder = new MessageOrder();

        messageOrder.setOrderId(order.getId());
        messageOrder.setPaymentId(order.getPaymentId());
        messageOrder.setStatus(order.getStatus());
        messageOrder.setReceivedDateTime(order.getCreatedAt().toString());
        messageOrder.setUserId(order.getUserId());
        messageOrder.setUserName(order.getUserName());

        return messageOrder;
    }
}