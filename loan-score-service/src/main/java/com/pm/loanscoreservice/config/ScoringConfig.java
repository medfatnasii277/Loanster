package com.pm.loanscoreservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration class for loan scoring weights and thresholds.
 * Maps the scoring parameters from application.properties to Java objects.
 */
@Configuration
@ConfigurationProperties(prefix = "loan.scoring")
@Data
public class ScoringConfig {

    private Weights weights = new Weights();
    private Thresholds thresholds = new Thresholds();

    @Data
    public static class Weights {
        private Employment employment = new Employment();
        private Income income = new Income();
        private LoanAmount loanAmount = new LoanAmount();
        private InterestRate interestRate = new InterestRate();
        private EmploymentYears employmentYears = new EmploymentYears();
        private LoanTerm loanTerm = new LoanTerm();

        @Data
        public static class Employment {
            private Integer unemployed = -50;
            private Integer employed = 100;
            private Integer selfEmployed = 75;
            private Integer student = 25;
            private Integer retired = 50;
        }

        @Data
        public static class Income {
            private Double multiplier = 0.001;
        }

        @Data
        public static class LoanAmount {
            private Double ratio = -0.5;
        }

        @Data
        public static class InterestRate {
            private Integer penalty = -10;
        }

        @Data
        public static class EmploymentYears {
            private Integer bonus = 5;
        }

        @Data
        public static class LoanTerm {
            private Integer penalty = -2;
        }
    }

    @Data
    public static class Thresholds {
        private Integer excellent = 750;
        private Integer good = 650;
        private Integer fair = 550;
        private Integer poor = 400;
    }
}
