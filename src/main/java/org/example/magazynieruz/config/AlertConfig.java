package org.example.magazynieruz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "warehouse.alert")
public class AlertConfig {

    private Email email = new Email();
    private LowStock lowStock = new LowStock();
    private Templates templates = new Templates();

    @Data
    public static class Email {
        private boolean enabled = true;
        private String from;
        private String fromName;
        private List<String> recipients;
        private String subjectPrefix = "[Alert]";
    }

    @Data
    public static class LowStock {
        private int threshold = 20;
        private boolean eventDriven = true;
    }

    @Data
    public static class Templates {
        private String lowStockSubject;
        private String lowStockBody;
    }
}
