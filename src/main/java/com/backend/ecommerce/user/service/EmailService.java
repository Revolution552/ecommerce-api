package com.backend.ecommerce.user.service;

import com.backend.ecommerce.favorites.model.Favorite;
import com.backend.ecommerce.order.model.Order;
import com.backend.ecommerce.order.model.OrderItem;
import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.seller.model.Seller;
import com.backend.ecommerce.shop.model.Shop;
import com.backend.ecommerce.user.exception.EmailFailureException;
import com.backend.ecommerce.user.model.User;
import com.backend.ecommerce.user.model.VerificationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    private SimpleMailMessage createMailMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        return message;
    }

    public void sendVerificationEmail(VerificationToken verificationToken) throws EmailFailureException {
        User user = verificationToken.getUser();
        String toEmail = user.getEmail();
        String token = verificationToken.getToken();
        String verificationLink = "Verification Token is: " + token;

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify Your Email to Activate Your Account");
        message.setText("Hi " + user.getFirstName() + ",\n\n"
                + "Please verify your email by Entering the following Token:\n"
                + verificationLink + "\n\n"
                + "This Token is valid for 1 hour.\n\nThank you!");

        logger.info("Attempting to send verification email to: {}", toEmail);
        logger.debug("Verification email content: \n{}", message.getText());

        try {
            javaMailSender.send(message);
            logger.info("Verification email successfully sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send verification email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(User user, String token) throws EmailFailureException {
        String toEmail = user.getEmail();
        String resetLink = "Reset-password Token is: " + token;

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset Your Password");
        message.setText("Hi " + user.getFirstName() + ",\n\n"
                + "You requested to reset your password. Enter the Token below to proceed:\n"
                + resetLink + "\n\n If you didn’t request this, please ignore this email.");

        logger.info("Attempting to send password reset email to: {}", toEmail);
        logger.debug("Password reset email content: \n{}", message.getText());

        try {
            javaMailSender.send(message);
            logger.info("Password reset email successfully sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send password reset email", e);
        }
    }

    // Seller methods to your existing EmailService.java

    public void sendSellerRegistrationConfirmation(Seller seller) throws EmailFailureException {
        User user = seller.getUser();
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Seller Registration Submitted Successfully");
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Thank you for registering as a seller on our platform.\n\n" +
                "Your application details:\n" +
                "- Business Name: " + seller.getBusinessName() + "\n" +
                "- Business Phone: " + seller.getBusinessPhone() + "\n" +
                "- Business Address: " + seller.getBusinessAddress() + "\n\n" +
                "Your application is currently under review. You will receive another email once your application has been processed.\n\n" +
                "Thank you for your patience.\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Seller registration confirmation email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send seller registration confirmation email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send seller registration confirmation email", e);
        }
    }

    public void sendSellerVerificationApprovedEmail(Seller seller) throws EmailFailureException {
        User user = seller.getUser();
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Congratulations! Your Seller Application is Approved");
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Congratulations! Your seller application for '" + seller.getBusinessName() + "' has been approved.\n\n" +
                "You can now start listing your products and managing your shop.\n\n" +
                "Login to your account to get started.\n\n" +
                "Welcome aboard!\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Seller approval email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send seller approval email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send seller approval email", e);
        }
    }

    public void sendSellerVerificationRejectedEmail(Seller seller, String reason) throws EmailFailureException {
        User user = seller.getUser();
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Update on Your Seller Application");
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Thank you for your interest in becoming a seller on our platform.\n\n" +
                "After careful review, we regret to inform you that your seller application for '" +
                seller.getBusinessName() + "' has not been approved at this time.\n\n" +
                (reason != null && !reason.isEmpty() ? "Reason: " + reason + "\n\n" : "") +
                "If you believe this is an error or would like to submit a new application with updated information, " +
                "please feel free to contact our support team.\n\n" +
                "Thank you for your understanding.\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Seller rejection email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send seller rejection email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send seller rejection email", e);
        }
    }

    // Shop methods to EmailService.java

    public void sendShopCreatedEmail(Shop shop, Seller seller) throws EmailFailureException {
        User user = seller.getUser();
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Shop Has Been Created - " + shop.getName());
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Congratulations! A shop has been created for you on our platform.\n\n" +
                "Shop Details:\n" +
                "- Shop Name: " + shop.getName() + "\n" +
                "- Shop URL: " + frontendUrl + "/shop/" + shop.getSlug() + "\n" +
                "- Status: " + shop.getStatus().getDescription() + "\n\n" +
                "Your shop is currently pending approval. You will receive another email once your shop has been reviewed and approved.\n\n" +
                "Once approved, you can start adding products and managing your shop from your seller dashboard.\n\n" +
                "Thank you for partnering with us!\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Shop creation email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send shop creation email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send shop creation email", e);
        }
    }

    public void sendShopApprovedEmail(Shop shop, Seller seller) throws EmailFailureException {
        User user = seller.getUser();
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Great News! Your Shop Has Been Approved - " + shop.getName());
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Great news! Your shop '" + shop.getName() + "' has been approved and is now live on our platform!\n\n" +
                "You can now:\n" +
                "- Add products to your shop\n" +
                "- Manage your inventory\n" +
                "- Process orders\n" +
                "- Track your sales and analytics\n\n" +
                "Access your seller dashboard here: " + frontendUrl + "/seller/dashboard\n" +
                "Your shop URL: " + frontendUrl + "/shop/" + shop.getSlug() + "\n\n" +
                "We're excited to have you on board and look forward to your success!\n\n" +
                "If you have any questions, please don't hesitate to contact our seller support team.\n\n" +
                "Happy selling!\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Shop approval email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send shop approval email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send shop approval email", e);
        }
    }

    public void sendShopRejectedEmail(Shop shop, Seller seller, String reason) throws EmailFailureException {
        User user = seller.getUser();
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Update Regarding Your Shop - " + shop.getName());
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Thank you for your patience while we reviewed your shop '" + shop.getName() + "'.\n\n" +
                "After careful review, we regret to inform you that your shop has not been approved at this time.\n\n" +
                (reason != null && !reason.isEmpty() ? "Reason: " + reason + "\n\n" : "") +
                "If you would like to make changes and resubmit your shop for approval, " +
                "please contact our seller support team for assistance.\n\n" +
                "We appreciate your interest in selling on our platform and hope to work with you in the future.\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Shop rejection email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send shop rejection email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send shop rejection email", e);
        }
    }

    // Favorites methods to EmailService.java

    public void sendFavoriteSaleNotificationEmail(User user, List<Favorite> favorites) throws EmailFailureException {
        String toEmail = user.getEmail();

        StringBuilder productsList = new StringBuilder();
        for (Favorite favorite : favorites) {
            Product product = favorite.getProduct();
            BigDecimal savings = product.getCompareAtPrice().subtract(product.getPrice());
            BigDecimal discountPercent = savings.divide(product.getCompareAtPrice(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            productsList.append("• ").append(product.getName())
                    .append(" - Was: $").append(product.getCompareAtPrice())
                    .append(", Now: $").append(product.getPrice())
                    .append(" (Save ").append(String.format("%.0f", discountPercent)).append("%)\n");
        }

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Price Drop Alert! Items in Your Wishlist Are On Sale!");
        message.setText("Hi " + user.getFirstName() + ",\n\n" +
                "Great news! Items in your wishlist are now on sale:\n\n" +
                productsList.toString() + "\n" +
                "Don't miss out on these deals! Visit your wishlist to grab them before they're gone.\n\n" +
                "View Wishlist: " + frontendUrl + "/wishlist\n\n" +
                "Happy Shopping!\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Favorite sale notification email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send favorite sale notification email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send favorite sale notification email", e);
        }
    }

    public void sendFavoriteStockNotificationEmail(User user, List<Favorite> favorites) throws EmailFailureException {
        String toEmail = user.getEmail();

        StringBuilder productsList = new StringBuilder();
        for (Favorite favorite : favorites) {
            Product product = favorite.getProduct();
            productsList.append("• ").append(product.getName())
                    .append(" - ").append(product.getQuantity())
                    .append(" left in stock\n");
        }

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Low Stock Alert! Items in Your Wishlist Are Running Out!");
        message.setText("Hi " + user.getFirstName() + ",\n\n" +
                "Items in your wishlist are running low on stock:\n\n" +
                productsList.toString() + "\n" +
                "Don't wait too long! These items might sell out soon.\n\n" +
                "View Wishlist: " + frontendUrl + "/wishlist\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Favorite stock notification email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send favorite stock notification email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send favorite stock notification email", e);
        }
    }

    // Order methods to EmailService.java

    public void sendOrderConfirmationEmail(User user, Order order) throws EmailFailureException {
        String toEmail = user.getEmail();

        StringBuilder itemsList = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            itemsList.append("• ").append(item.getProductName())
                    .append(" x ").append(item.getQuantity())
                    .append(" - $").append(item.getTotalPrice())
                    .append("\n");
        }

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Confirmation - " + order.getOrderNumber());
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Thank you for your order! Your order has been received and is being processed.\n\n" +
                "Order Number: " + order.getOrderNumber() + "\n" +
                "Order Date: " + order.getCreatedAt() + "\n\n" +
                "Order Summary:\n" +
                itemsList.toString() + "\n" +
                "Subtotal: $" + order.getSubtotal() + "\n" +
                "Shipping: $" + order.getShippingCost() + "\n" +
                "Tax: $" + order.getTax() + "\n" +
                "Total: $" + order.getTotal() + "\n\n" +
                "Shipping Address:\n" +
                order.getShipping().getFullName() + "\n" +
                order.getShipping().getAddressLine1() + "\n" +
                (order.getShipping().getAddressLine2() != null ? order.getShipping().getAddressLine2() + "\n" : "") +
                order.getShipping().getCity() + ", " + order.getShipping().getState() + " " + order.getShipping().getPostalCode() + "\n" +
                order.getShipping().getCountry() + "\n\n" +
                "You can track your order status here: " + frontendUrl + "/orders/" + order.getOrderNumber() + "\n\n" +
                "Thank you for shopping with us!\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Order confirmation email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send order confirmation email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send order confirmation email", e);
        }
    }

    public void sendOrderStatusUpdateEmail(User user, Order order) throws EmailFailureException {
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Status Update - " + order.getOrderNumber());
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Your order status has been updated.\n\n" +
                "Order Number: " + order.getOrderNumber() + "\n" +
                "New Status: " + order.getStatus().getDescription() + "\n\n" +
                "You can track your order here: " + frontendUrl + "/orders/" + order.getOrderNumber() + "\n\n" +
                "Thank you for shopping with us!\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Order status update email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send order status update email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send order status update email", e);
        }
    }

    public void sendPaymentConfirmationEmail(User user, Order order) throws EmailFailureException {
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Payment Confirmed - " + order.getOrderNumber());
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Your payment has been successfully processed.\n\n" +
                "Order Number: " + order.getOrderNumber() + "\n" +
                "Amount Paid: $" + order.getTotal() + "\n" +
                "Payment Method: " + order.getPayment().getPaymentMethod().getDescription() + "\n" +
                "Transaction ID: " + order.getPayment().getTransactionId() + "\n\n" +
                "Your order is now being processed.\n\n" +
                "Track your order: " + frontendUrl + "/orders/" + order.getOrderNumber() + "\n\n" +
                "Thank you for your purchase!\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Payment confirmation email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send payment confirmation email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send payment confirmation email", e);
        }
    }

    public void sendOrderCancellationEmail(User user, Order order, String reason) throws EmailFailureException {
        String toEmail = user.getEmail();

        SimpleMailMessage message = createMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Cancelled - " + order.getOrderNumber());
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Your order has been cancelled as requested.\n\n" +
                "Order Number: " + order.getOrderNumber() + "\n" +
                "Cancellation Reason: " + reason + "\n\n" +
                "If you did not request this cancellation or have any questions, " +
                "please contact our customer support team.\n\n" +
                "We hope to serve you again soon.\n\n" +
                "Best regards,\n" +
                "Ecommerce Team");

        try {
            javaMailSender.send(message);
            logger.info("Order cancellation email sent to: {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send order cancellation email to: {}", toEmail, e);
            throw new EmailFailureException("Failed to send order cancellation email", e);
        }
    }
}
