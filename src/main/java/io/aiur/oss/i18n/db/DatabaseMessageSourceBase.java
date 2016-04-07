package io.aiur.oss.i18n.db;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public abstract class DatabaseMessageSourceBase extends AbstractMessageSource {

	private Messages messages;

	@Inject @Setter @Getter
	private JdbcTemplate jdbcTemplate;

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		String msg = messages.getMessage(code, locale);
		return createMessageFormat(msg, locale);

	}

	@PostConstruct
	public void init() {

		String i18nQuery = this.getI18NSqlQuery();

		log.info("Initializing message source using query [{}]", i18nQuery);

		this.messages = jdbcTemplate.query(i18nQuery, this::extractI18NData);
	}

	/**
	 * Returns sql query used to fetch the messages from the database.
	 * 
	 * @return sql query string
	 */
	protected abstract String getI18NSqlQuery();

	/**
	 * Extracts messages from the given result set.
	 * 
	 * @param rs
	 *            is a result set
	 * @return initialized {@link Messages} instance
	 * @throws SQLException
	 *             if a SQLException is encountered getting column values or
	 *             navigating (that is, there's no need to catch SQLException)
	 * @throws DataAccessException
	 *             in case of custom exceptions
	 */
	protected abstract Messages extractI18NData(ResultSet rs)
			throws SQLException, DataAccessException;

	/**
	 * 
	 * Messages bundle
	 */
	protected static final class Messages {

		/* <code, <locale, message>> */
		private Map<String, Map<String, String>> messages;

		public void addMessage(String code, Locale locale, String msg) {
			if (messages == null)
				messages = new HashMap<>();

			Map<String, String> data = messages.get(code);
			if (data == null) {
				data = new HashMap<>();
				messages.put(code, data);
			}

			data.put( toKey(locale), msg);
		}

		public String getMessage(String code, Locale locale) {
            String result = null;
			Map<String, String> data = messages.get(code);
            if( data != null ){
                result = data.get( toKey(locale) );
                if( result == null ){
                    String key = toKey( new Locale(locale.getLanguage()) );
                    result = data.get( key );
                }
            }
			return result;
		}

        private String toKey(Locale locale){
            return locale.toString().toLowerCase();
        }
	}



}