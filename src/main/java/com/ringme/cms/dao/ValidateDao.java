package com.ringme.cms.dao;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class ValidateDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public boolean isExist(Object firstValue, Object firstColumn, Object secondValue, Object secondColumn, Object table) {
        log.info("firstValue: {}| firstColumn: {}| secondValue: {}| secondColumn: {}| table: {}",
                firstValue, firstColumn, secondValue, secondColumn, table);
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + firstColumn + " = :firstValue AND (:secondValue IS NULL OR " + secondColumn + " != :secondValue)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("firstValue", firstValue)
                .addValue("secondValue", secondValue);
        try {
            Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
        }

        return true;
    }
}
