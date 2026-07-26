package com.dorm.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OperationLogSchemaInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public OperationLogSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS operation_log (
              id bigint NOT NULL AUTO_INCREMENT,
              operator_id bigint NOT NULL,
              operator_name varchar(100) NOT NULL,
              module varchar(50) NOT NULL,
              action varchar(20) NOT NULL,
              path varchar(255) NOT NULL,
              result varchar(20) NOT NULL,
              summary varchar(255) DEFAULT NULL,
              create_time datetime DEFAULT CURRENT_TIMESTAMP,
              PRIMARY KEY (id),
              KEY idx_operation_log_time (create_time),
              KEY idx_operation_log_module (module)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
    }
}
