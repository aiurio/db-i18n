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
import java.util.stream.Collectors;

@Slf4j
public abstract class DatabaseMessageSourceBase extends AbstractMessageSource {

	private Messages messages;

	@Inject @Setter @Getter
	private JdbcTemplate jdbcTemplate;

    @Getter @Setter
    private boolean escapeSingleQuotes = true;

    public DatabaseMessageSourceBase() {
        this.setAlwaysUseMessageFormat(true);
    }

    public Map<String, Map<String, String>> getMessages(){
        return messages.messages;
    }

    public String getMessage(Locale locale, String code){
        return messages.getMessage(code, locale);
    }

    public Map<String, String> getMessages(Locale locale){
        return messages.getMessages(locale);
    }

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
        log.info("Initialized {} messages", messages == null || messages.messages == null ? 0 : messages.messages.size());
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
	 * Messages bundle; escapes single quotes (if specified)
	 */
	protected final class Messages {

		/* <code, <locale, message>> */
		private Map<String, Map<String, String>> messages;

		public void addMessage(String code, Locale locale, String msg) {
            if( DatabaseMessageSourceBase.this.isEscapeSingleQuotes() ) {
                msg = msg.replace("'", "''");
            }

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
                    if( result == null ){
                        log.warn("Failed finding translation for locale={} code={}", locale, code);
                    }else{
                        log.warn("Failed finding dialect translation for locale={} code={} -- but found via parent={}",
                                locale, code, key);
                    }
                }
            }
			return result;
		}

        public Map<String, String> getMessages(Locale locale){
            String lang = toKey(locale);
            return messages.entrySet().stream()
                    .map(entry -> {
                        String key = entry.getKey();
                        String value = entry.getValue().get(lang);
                        return new HashMap.SimpleEntry<>(key, value);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        private String toKey(Locale locale){
            return locale.toString().toLowerCase();
        }
	}



}