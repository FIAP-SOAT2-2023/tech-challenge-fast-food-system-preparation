package br.fiap.pos.fastfood.preparation.listener;

import br.fiap.pos.fastfood.preparation.domain.Item;
import br.fiap.pos.fastfood.preparation.domain.Order;
import br.fiap.pos.fastfood.preparation.repository.OrderRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentListener.class);
    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/339713107443/Pagamento01-basico.fifo";

    @Autowired
    private OrderRepository orderRepository;

    @SqsListener(QUEUE_URL)
    public void receiveSQSMessage(Message message) {

        logger.info("Received message: {}", message.body());

        // Parse JSON message to Order object
        Order order = parseOrderFromMessage(message.body());

        logger.info("Received order: {}", order);

        // Save the order in the repository
        orderRepository.save(order);
    }

    private Order parseOrderFromMessage(String messageBody) {
        // Parse JSON message to Order object using Gson
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(messageBody, JsonObject.class);

        JsonObject basketObject = jsonObject.getAsJsonObject("basket");
        // Extract relevant fields from JSON object
        String paymentId = basketObject.getAsJsonObject("order").getAsJsonPrimitive("payment").getAsString();
        String orderId = jsonObject.getAsJsonObject("basket").getAsJsonObject("order").getAsJsonPrimitive("code").getAsString();

        // Extract items from JSON object
        JsonArray itemsJsonArray = jsonObject.getAsJsonObject("basket").getAsJsonArray("items");
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < itemsJsonArray.size(); i++) {
            JsonObject itemJsonObject = itemsJsonArray.get(i).getAsJsonObject();
            String category = itemJsonObject.getAsJsonPrimitive("category").getAsString();
            int quantity = itemJsonObject.getAsJsonPrimitive("quantity").getAsInt();
            items.add(new Item(category, quantity ));
        }

        String statusKey = basketObject.getAsJsonObject("order").getAsJsonObject("status").get("key").getAsString();

        // Construct Order object
        Order order = new Order();
        order.setPaymentId(paymentId);
        order.setId(orderId);
        order.setItems(items);
        order.setStatus(statusKey);
        if (jsonObject.has("customer") && !jsonObject.get("customer").isJsonNull()) {
            JsonObject customer = jsonObject.getAsJsonObject("customer");

            order.setUserId(customer.get("uuid").getAsString());
            order.setUserName(customer.get("firstName").getAsString().concat(" ").concat(customer.get("lastName").getAsString()));
        }

        //jsonObject.get

        return order;
    }
}
