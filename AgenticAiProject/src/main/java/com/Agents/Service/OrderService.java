package com.Agents.Service;

import com.Agents.Entity.Order;
import com.Agents.Entity.Product;
import com.Agents.Exception.OrderNotFoundException;
import com.Agents.OrderRepository.OrderRepository;
import com.Agents.OrderRepository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final RestClient restClient;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.restClient = RestClient.create("http://localhost:11434");
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public String processChat(String message) {
        try {
            String enhancedPrompt = message;
            String lowerMessage = message.toLowerCase().trim();

            Pattern idPattern = Pattern.compile("(?i)(ORD-\\d+)");
            Matcher idMatcher = idPattern.matcher(message);
            String extractedOrderId = idMatcher.find() ? idMatcher.group(1).toUpperCase() : null;

            // 1. DELETE / REMOVE ORDER FROM DATABASE
            if (lowerMessage.contains("cancel") || lowerMessage.contains("delete") || lowerMessage.contains("remove") || lowerMessage.contains("return")) {
                if (extractedOrderId != null) {
                    Optional<Order> orderOpt = orderRepository.findById(extractedOrderId);
                    if (orderOpt.isPresent()) {
                        Order order = orderOpt.get();
                        String currentStatus = order.getStatus().toUpperCase();

                        if ("SHIPPED".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
                            enhancedPrompt = String.format(
                                "Customer Name: %s. Order ID: %s. Status: %s.\n" +
                                "Write a formal 5 to 7 line notification informing %s that order %s is currently '%s' and cannot be deleted or cancelled from our active database per store policy. " +
                                "Explain that once an order is shipped or delivered, standard return procedures apply. " +
                                "Do NOT write an email header, sign-off, or placeholder.",
                                order.getCustomerName(), extractedOrderId, currentStatus, order.getCustomerName(), extractedOrderId, currentStatus
                            );
                        } else {
                            orderRepository.deleteById(extractedOrderId);
                            enhancedPrompt = String.format(
                                "Customer Name: %s. Order ID: %s.\n" +
                                "Write a formal 5 to 7 line notification confirming that order %s for customer %s has been permanently removed from our active database. " +
                                "Acknowledge the cancellation request, confirm the order database record is deleted, and state that no further billing will occur. " +
                                "Do NOT write an email header, sign-off, or placeholder.",
                                order.getCustomerName(), extractedOrderId, extractedOrderId, order.getCustomerName()
                            );
                        }
                    } else {
                        throw new OrderNotFoundException("Order with ID " + extractedOrderId + " was not found in database records.");
                    }
                } else {
                    List<Order> allOrders = orderRepository.findAll();
                    List<Order> matched = allOrders.stream()
                        .filter(o -> lowerMessage.contains(o.getCustomerName().toLowerCase()))
                        .collect(Collectors.toList());

                    if (!matched.isEmpty()) {
                        for (Order ord : matched) {
                            orderRepository.deleteById(ord.getOrderId());
                        }
                        String deletedIds = matched.stream().map(Order::getOrderId).collect(Collectors.joining(", "));
                        enhancedPrompt = String.format(
                            "Write a formal 5 to 7 line notification statement informing the user that order(s) [%s] matching their request have been permanently removed from the database.",
                            deletedIds
                        );
                    } else {
                        throw new OrderNotFoundException("No matching orders found for the specified customer name to delete.");
                    }
                }
            }

            // 2. UPDATE STATUS
            else if (lowerMessage.contains("update") || lowerMessage.contains("ship") || lowerMessage.contains("deliver") || lowerMessage.contains("dispatch")) {
                if (extractedOrderId != null) {
                    Optional<Order> orderOpt = orderRepository.findById(extractedOrderId);
                    if (orderOpt.isPresent()) {
                        Order order = orderOpt.get();
                        if (lowerMessage.contains("shipped") || lowerMessage.contains("ship") || lowerMessage.contains("dispatch")) {
                            order.setStatus("SHIPPED");
                        } else if (lowerMessage.contains("delivered") || lowerMessage.contains("deliver")) {
                            order.setStatus("DELIVERED");
                        } else {
                            order.setStatus("IN_TRANSIT");
                        }
                        orderRepository.save(order);

                        enhancedPrompt = String.format(
                            "Customer Name: %s. Order ID: %s. New Status: %s. Item: %d x %s. Total: $%.2f.\n" +
                            "Write a formal 5 to 7 line status update notification addressed to %s. " +
                            "Explicitly state that order %s has been updated to status '%s' in our system, include the item details, total amount, and estimated delivery schedule. " +
                            "Do NOT write an email signature, sign-off, or bracket placeholders.",
                            order.getCustomerName(), extractedOrderId, order.getStatus(), order.getQuantity(), order.getItem(), order.getTotalAmount(),
                            order.getCustomerName(), extractedOrderId, order.getStatus()
                        );
                    } else {
                        throw new OrderNotFoundException("Order with ID " + extractedOrderId + " was not found for status update.");
                    }
                } else {
                    throw new OrderNotFoundException("Please provide a valid Order ID (e.g., ORD-1234) to update status.");
                }
            }

            // 3. PLACE / CREATE NEW ORDER
            else if (lowerMessage.contains("create") || lowerMessage.contains("place") || lowerMessage.contains("buy") || (lowerMessage.contains("order") && (lowerMessage.contains("new") || lowerMessage.contains("item") || lowerMessage.contains("for")))) {
                
                String extractionPrompt = String.format(
                    "Extract customer name, product item name (singular), and integer quantity from this input: '%s'. " +
                    "Return ONLY valid JSON with keys: \"customer\", \"item\", \"quantity\". No additional prose.", message
                );

                Map<String, Object> parsePayload = Map.of(
                    "model", "llama3.2",
                    "prompt", extractionPrompt,
                    "format", "json",
                    "stream", false
                );

                Map<String, Object> parseResponse = restClient.post()
                        .uri("/api/generate")
                        .body(parsePayload)
                        .retrieve()
                        .body(Map.class);

                String customer = "Customer";
                String item = "Phone";
                int quantity = 1;

                if (parseResponse != null && parseResponse.containsKey("response")) {
                    try {
                        JsonNode json = mapper.readTree(parseResponse.get("response").toString());
                        if (json.has("customer") && !json.get("customer").asText().isEmpty()) {
                            customer = json.get("customer").asText();
                        }
                        if (json.has("item") && !json.get("item").asText().isEmpty()) {
                            item = json.get("item").asText();
                        }
                        if (json.has("quantity")) {
                            quantity = json.get("quantity").asInt(1);
                        }
                    } catch (Exception ignored) {}
                }

                String lookupItem = item.toLowerCase().replaceAll("s$", "");
                if (lookupItem.equals("mobile")) lookupItem = "phone";

                // Fetch product and price from database repository instead of static map
                Product prod = productRepository.findById(lookupItem).orElse(new Product(lookupItem, 99.99));
                double unitPrice = prod.getUnitPrice();
                
                double total = unitPrice * quantity;
                String newOrderId = "ORD-" + (int)(Math.random() * 9000 + 1000);
                LocalDate deliveryDate = LocalDate.now().plusDays(3);

                Order newOrder = new Order();
                newOrder.setOrderId(newOrderId);
                newOrder.setCustomerName(customer);
                newOrder.setItem(item);
                newOrder.setQuantity(quantity);
                newOrder.setUnitPrice(unitPrice);
                newOrder.setTotalAmount(total);
                newOrder.setStatus("PROCESSING");
                newOrder.setEstimatedDelivery(deliveryDate);

                orderRepository.save(newOrder);

                enhancedPrompt = String.format(
                    "Customer Name: %s. Order ID: %s. Item: %d x %s. Unit Price: $%.2f. Total Amount: $%.2f. Delivery Date: %s.\n" +
                    "Write a formal 5 to 7 line order creation receipt addressed to %s. " +
                    "Summarize all order details clearly, confirm processing status, and provide estimated delivery dates. " +
                    "Do NOT write an email letter header, email sign-off, or bracket placeholders.",
                    customer, newOrderId, quantity, item, unitPrice, total, deliveryDate, customer
                );
            }

            // 4. TRACK / CHECK ORDER STATUS
            else if (extractedOrderId != null || lowerMessage.contains("check") || lowerMessage.contains("track") || lowerMessage.contains("where") || lowerMessage.contains("status") || lowerMessage.contains("find")) {
                if (extractedOrderId != null) {
                    Optional<Order> orderOpt = orderRepository.findById(extractedOrderId);
                    if (orderOpt.isPresent()) {
                        Order order = orderOpt.get();
                        enhancedPrompt = String.format(
                            "Customer Name: %s. Order ID: %s. Item: %d x %s. Total: $%.2f. Status: %s. Estimated Delivery: %s.\n" +
                            "Write a formal 5 to 7 line order tracking summary addressed to %s providing a real-time status update.",
                            order.getCustomerName(), order.getOrderId(), order.getQuantity(), order.getItem(), order.getTotalAmount(), order.getStatus(), order.getEstimatedDelivery(),
                            order.getCustomerName()
                        );
                    } else {
                        throw new OrderNotFoundException("Order with ID " + extractedOrderId + " could not be found.");
                    }
                } else {
                    List<Order> allOrders = orderRepository.findAll();
                    List<Order> matched = allOrders.stream()
                        .filter(o -> lowerMessage.contains(o.getCustomerName().toLowerCase()) || lowerMessage.contains(o.getItem().toLowerCase()))
                        .collect(Collectors.toList());

                    if (!matched.isEmpty()) {
                        String summary = matched.stream()
                            .map(o -> String.format("Order #%s [%s x %d] - Status: %s - Total: $%.2f - Delivery: %s",
                                    o.getOrderId(), o.getItem(), o.getQuantity(), o.getStatus(), o.getTotalAmount(), o.getEstimatedDelivery()))
                            .collect(Collectors.joining("\n"));
                        enhancedPrompt = String.format("Found these active orders:\n%s\nWrite a formal 5 to 7 line customer account summary presenting these active orders.", summary);
                    } else {
                        throw new OrderNotFoundException("No orders matched your search criteria.");
                    }
                }
            }

            // 5. SHOW ALL ORDERS
            else if (lowerMessage.contains("all orders") || lowerMessage.contains("list orders") || lowerMessage.contains("show orders")) {
                List<Order> allOrders = orderRepository.findAll();
                if (allOrders.isEmpty()) {
                    enhancedPrompt = "Write a formal 5 line statement informing the user that the store database is currently empty with no active orders.";
                } else {
                    String list = allOrders.stream()
                        .map(o -> String.format("• Order #%s | Customer: %s | Item: %d x %s | Total: $%.2f | Status: %s",
                                o.getOrderId(), o.getCustomerName(), o.getQuantity(), o.getItem(), o.getTotalAmount(), o.getStatus()))
                        .collect(Collectors.joining("\n"));
                    enhancedPrompt = String.format(
                        "All Active Store Orders:\n%s\n" +
                        "Write a formal 5 to 7 line system inventory summary introducing this order list clearly to the store administrator.",
                        list
                    );
                }
            }

            Map<String, Object> payload = Map.of(
                "model", "llama3.2",
                "prompt", enhancedPrompt,
                "stream", false
            );

            Map<String, Object> response = restClient.post()
                    .uri("/api/generate")
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("response")) {
                return response.get("response").toString();
            }
            return "No response received from local AI.";

        } catch (OrderNotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("AI Processing Error: " + e.getMessage());
        }
    }
}