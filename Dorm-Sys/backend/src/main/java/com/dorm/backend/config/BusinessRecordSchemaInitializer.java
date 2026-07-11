package com.dorm.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BusinessRecordSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public BusinessRecordSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS business_record (
              id bigint NOT NULL AUTO_INCREMENT,
              type varchar(50) NOT NULL,
              title varchar(100) NOT NULL,
              owner varchar(100) DEFAULT NULL,
              description text,
              status varchar(30) DEFAULT NULL,
              creator_id bigint DEFAULT NULL,
              event_time datetime DEFAULT NULL,
              create_time datetime DEFAULT CURRENT_TIMESTAMP,
              update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              PRIMARY KEY (id),
              KEY idx_business_record_type (type),
              KEY idx_business_record_status (status)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
    }
}
