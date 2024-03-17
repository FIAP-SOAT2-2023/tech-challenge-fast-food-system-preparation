package br.fiap.pos.fastfood.preparation.listener;

import br.fiap.pos.fastfood.preparation.domain.Item;
import br.fiap.pos.fastfood.preparation.domain.Order;
import br.fiap.pos.fastfood.preparation.repository.OrderRepository;
import br.fiap.pos.fastfood.preparation.service.OrderExternalService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentListener.class);

    @Autowired
    private OrderRepository orderRepository;

    @Value("${payment.compensation.queue.url}")
    private String paymentCompensationQueueUrl;

    @Autowired
    private OrderExternalService orderExternalService;

    @SqsListener("${sqs.queue.url}")
    public void receiveSQSMessage(Message message) {

        logger.info("Received message: {}", message.body());

        // Parse JSON message to Order object
        Order order = parseOrderFromMessage(message.body());

        try {

            if (order.getItems().stream().findFirst().get().getQuantity() == 1313) {
                throw new RuntimeException("Error Test Compensation SQS");
            }

            logger.info("Received order: {}", order);

            // Save the order in the repository
            orderRepository.save(order);

        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage());

            orderExternalService.sendMessageToQueueSQS(message.body(), paymentCompensationQueueUrl, "paymentCompensation", order.getId());
        }
    }

    private Order parseOrderFromMessage(String messageBody) {
        // Parse JSON message to Order object using Gson
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(messageBody, JsonObject.class);

        // Extract relevant fields from JSON object
        String paymentId = jsonObject.getAsJsonObject("order").getAsJsonPrimitive("payment").getAsString();
        String orderId = jsonObject.getAsJsonObject("order").getAsJsonPrimitive("uuid").getAsString();

        // Extract items from JSON object
        JsonArray itemsJsonArray = jsonObject.getAsJsonArray("items");
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < itemsJsonArray.size(); i++) {
            JsonObject itemJsonObject = itemsJsonArray.get(i).getAsJsonObject();
            String category = itemJsonObject.getAsJsonPrimitive("category").getAsString();
            int quantity = itemJsonObject.getAsJsonPrimitive("quantity").getAsInt();
            items.add(new Item(category, quantity));
        }

        String statusKey = jsonObject.getAsJsonObject("order").getAsJsonObject("status").get("key").getAsString();

        // Construct Order object
        Order order = new Order();
        order.setPaymentId(paymentId);
        order.setId(orderId);
        order.setItems(items);
        order.setStatus(statusKey);
        order.setCreatedAt(LocalDateTime.now());
        if (jsonObject.has("customer") && !jsonObject.get("customer").isJsonNull()) {
            JsonObject customer = jsonObject.getAsJsonObject("customer");

            order.setUserId(customer.get("uuid").getAsString());
            order.setUserName(customer.get("firstName").getAsString().concat(" ").concat(customer.get("lastName").getAsString()));
        }

        //jsonObject.get

        return order;
    }
}
