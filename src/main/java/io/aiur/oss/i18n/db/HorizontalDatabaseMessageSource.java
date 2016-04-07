package io.aiur.oss.i18n.db;

import org.springframework.dao.DataAccessException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;

public class HorizontalDatabaseMessageSource extends DatabaseMessageSourceBase {

    private final String query, codeColumn;

    public HorizontalDatabaseMessageSource() {
        this("select * from t_i18n_horizontal", "code");
    }
    public HorizontalDatabaseMessageSource(String query, String codeColumn) {
        this.query = query;
        this.codeColumn = codeColumn;
    }

    @Override
    protected String getI18NSqlQuery() {
		return query;
	}

	@Override
	protected Messages extractI18NData(ResultSet rs) throws SQLException,
            DataAccessException {
		Messages messages = new Messages();
		ResultSetMetaData metaData = rs.getMetaData();
		int noOfColumns = metaData.getColumnCount();
		while (rs.next()) {
			String key = rs.getString(codeColumn);
			for (int i = 1; i <= noOfColumns; i++) {
				String columnName = metaData.getColumnName(i);
				if (!codeColumn.equalsIgnoreCase(columnName)) {
					Locale locale = new Locale(columnName);
					String msg = rs.getString(columnName);
					messages.addMessage(key, locale, msg);
				}
			}
		}
		return messages;
	}
}