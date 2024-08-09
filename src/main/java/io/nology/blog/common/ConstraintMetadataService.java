package io.nology.blog.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConstraintMetadataService {

    private static final Logger logger = Logger.getLogger(ConstraintMetadataService.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public HashMap<String, String> getDatabaseErrorsForUniqueConstraintViolation(String message) {
        HashMap<String, String> results = extractConstraintName(message);

        if (results.isEmpty()) {
            return results;
        }

        return jdbcTemplate.execute((Connection connection) -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String schema = connection.getCatalog();
                String tableName = results.get("table");

                try (ResultSet rs = metaData.getIndexInfo(null, schema, tableName, true, true)) {
                    while (rs.next()) {
                        String indexName = rs.getString("INDEX_NAME");
                        String columnName = rs.getString("COLUMN_NAME");

                        if (indexName.equals(results.get("constraint"))) {
                            results.put("column", columnName);
                        }
                    }
                    logger.warning("No unique constraint found for table: " + tableName);
                }
            } catch (SQLException e) {
                logger.severe("Error retrieving database metadata: " + e.getMessage());
                e.printStackTrace();
            }
            return results;
        });
    }

    private HashMap<String, String> extractConstraintName(String message) {
        System.out.println(message);
        HashMap<String, String> results = new HashMap<>();
        Pattern pattern = Pattern.compile("Duplicate entry '([^']+)' for key '([^']+)\\.(UK_[^']+)'");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            results.put("value", matcher.group(1));
            results.put("table", matcher.group(2));
            results.put("constraint", matcher.group(3));
        }
        return results;
    }

}