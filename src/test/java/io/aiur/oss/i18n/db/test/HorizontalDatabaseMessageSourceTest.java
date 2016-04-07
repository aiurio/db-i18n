package io.aiur.oss.i18n.db.test;

import io.aiur.oss.i18n.db.HorizontalDatabaseMessageSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by dave on 4/7/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HorizontalDatabaseMessageSourceTest.Config.class)
public class HorizontalDatabaseMessageSourceTest {


    @Inject
    private HorizontalDatabaseMessageSource messageSource;


    @Test
    public void test(){
        String value = messageSource.getMessage("app.startup.successful", null, "Nothing found", Locale.US);
        System.out.print(value);

    }

    @Configuration
    static class Config {

        @Bean
        public EmbeddedDatabase db() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .addScript("db/sql/test-data.sql")
                    .build();
        }

        @Bean
        public JdbcTemplate jdbcTemplate() {
            return new JdbcTemplate(db());
        }

        @Bean @Primary
        public HorizontalDatabaseMessageSource messageSource() {
            HorizontalDatabaseMessageSource src = new HorizontalDatabaseMessageSource();
            src.setJdbcTemplate(jdbcTemplate());
            return src;
        }
    }
}
