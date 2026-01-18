package org.example.magazynieruz.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.config.AlertConfig;
import org.example.magazynieruz.model.Product;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAlertService {

    private final JavaMailSender mailSender;
    private final AlertConfig alertConfig;

    @Async
    public void sendLowStockAlert(String productName, int currentQuantity, int threshold, String locationName, String warehouseName) {
        if (!alertConfig.getEmail().isEnabled()) {
            log.debug("Email alerts are disabled. Skipping low stock alert for product: {}", productName);
            return;
        }

        if (alertConfig.getEmail().getRecipients() == null || alertConfig.getEmail().getRecipients().isEmpty()) {
            log.warn("No email recipients configured. Cannot send low stock alert for product: {}", productName);
            return;
        }

        try {
            String subject = buildSubject(productName);
            String body = buildBody(productName, currentQuantity, threshold, locationName, warehouseName);

            sendEmail(alertConfig.getEmail().getRecipients(), subject, body);
            log.info("Low stock alert sent successfully for product: {}", productName);
        } catch (Exception e) {
            log.error("Failed to send low stock alert for product: {}. Error: {}", productName, e.getMessage(), e);
        }
    }

    private String buildSubject(String productName) {
        String subjectTemplate = alertConfig.getTemplates().getLowStockSubject();
        if (subjectTemplate == null || subjectTemplate.isEmpty()) {
            subjectTemplate = "Low Stock Alert - {productName}";
        }
        
        String subject = subjectTemplate.replace("{productName}", productName);
        return alertConfig.getEmail().getSubjectPrefix() + " " + subject;
    }

    private String buildBody(String productName, int currentQuantity, int threshold, String locationName, String warehouseName) {
        String bodyTemplate = alertConfig.getTemplates().getLowStockBody();
        if (bodyTemplate == null || bodyTemplate.isEmpty()) {
            bodyTemplate = "Product {productName} is low on stock.\nCurrent Quantity: {currentQuantity}\nThreshold: {threshold}\nLocation: {locationName}\nWarehouse: {warehouseName}";
        }

        return bodyTemplate
                .replace("{productName}", productName != null ? productName : "N/A")
                .replace("{currentQuantity}", String.valueOf(currentQuantity))
                .replace("{threshold}", String.valueOf(threshold))
                .replace("{locationName}", locationName != null ? locationName : "N/A")
                .replace("{warehouseName}", warehouseName != null ? warehouseName : "N/A");
    }

    private void sendEmail(List<String> recipients, String subject, String body) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(alertConfig.getEmail().getFrom(), alertConfig.getEmail().getFromName());
        helper.setTo(recipients.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(body, body.contains("<html>"));

        mailSender.send(message);
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
