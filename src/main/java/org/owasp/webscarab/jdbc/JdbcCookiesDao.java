/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.owasp.webscarab.dao.CookiesDao;

import org.owasp.webscarab.domain.CookieConfiguration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author lpz
 *
 */
public class JdbcCookiesDao extends PropertiesJdbcDaoSupport implements CookiesDao {

	private void createTables() {
		getJdbcTemplate().execute(getProperty("cookies.createTable"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.dao.support.DaoSupport#initDao()
	 */
	@Override
	protected void initDao() throws Exception {
		super.initDao();

		try {
			get(new Integer(0));
		} catch (Exception e) {
			createTables();
			get(new Integer(0));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.AnnotationDao#get(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public CookieConfiguration get(Integer id) {
		JdbcTemplate jt = getJdbcTemplate();
		String query = "SELECT id, url, value FROM cookies WHERE id = ?";
		Object[] args = new Object[] { id };
		RowMapper rowMapper = new CookieRowMapper();
		Collection<CookieConfiguration> results = jt.query(query, args, rowMapper);
		if (results.size() == 0) return null;
		if (results.size() > 1) throw new IncorrectResultSizeDataAccessException(1, results.size());
		return results.iterator().next();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.AnnotationDao#update(org.owasp.webscarab.Annotation)
	 */
	public void update(CookieConfiguration cc) {
		JdbcTemplate jt = getJdbcTemplate();
		String statement = "UPDATE cookies SET url = ?, value = ? WHERE id = ?";
		Object[] args = new Object[] { cc.getCookieUri(), cc.getCookieValue(),
				cc.getId() };
		if (jt.update(statement, args) == 0) {
			statement = "INSERT INTO cookies (url, value, id) VALUES (?,?,?)";
			jt.update(statement, args);
		}
	}

	public void delete(Integer id) {
		JdbcTemplate jt = getJdbcTemplate();
		String statement = "DELETE FROM cookies WHERE id = ?";
		Object[] args = new Object[] { id };
		jt.update(statement, args);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.AnnotationDao#getAll()
	 */
	@SuppressWarnings("unchecked")
	public Collection<CookieConfiguration> getAll() {
		JdbcTemplate jt = getJdbcTemplate();
		String query = "SELECT id, url, value FROM cookies";
		RowMapper rowMapper = new CookieRowMapper();
		return jt.query(query, rowMapper);
	}

	private class CookieRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int index) throws SQLException {
			CookieConfiguration cc = new CookieConfiguration();
                        cc.setCookieUri(rs.getString("url"));
                        cc.setCookieValue(rs.getString("value"));
			cc.setId(new Integer(rs.getInt("id")));
			return cc;
		}
	}

}
