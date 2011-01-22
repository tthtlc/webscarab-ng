/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.owasp.webscarab.dao.HeaderConfigurationDao;

import org.owasp.webscarab.domain.HeaderConfiguration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author lpz
 *
 */
public class JdbcHeaderConfigurationDao extends PropertiesJdbcDaoSupport implements HeaderConfigurationDao {
    //default values
    private String userAgent = "Opera/9.80 (Windows NT 6.1; U; pl) Presto/2.6.30 Version/10.63";
    private String accept = "text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1";
    private String acceptLanguage = "pl-PL,pl;q=0.9,en;q=0.8";
    private String acceptCharset = "iso-8859-1, utf-8, utf-16, *;q=0.1";
    private String acceptEncoding = "deflate, gzip, x-gzip, identity, *;q=0";
    private String proxyConnection = "Keep-Alive";

    private void createTables() {
        getJdbcTemplate().execute(getProperty("headerConfigurations.createTable"));
        this.update(new HeaderConfiguration("User-Agent", userAgent));
        this.update(new HeaderConfiguration("Accept", accept));
        this.update(new HeaderConfiguration("Accept-Language", acceptLanguage));
        this.update(new HeaderConfiguration("Accept-Charset", acceptCharset));
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
    public HeaderConfiguration get(Integer id) {
        JdbcTemplate jt = getJdbcTemplate();
        String query = "SELECT id, name, value FROM header_configurations WHERE id = ?";
        Object[] args = new Object[]{id};
        RowMapper rowMapper = new HeaderConfRowMapper();
        Collection<HeaderConfiguration> results = jt.query(query, args, rowMapper);
        if (results.size() == 0) {
            return null;
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.iterator().next();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.owasp.webscarab.dao.AnnotationDao#update(org.owasp.webscarab.Annotation)
     */
    public void update(HeaderConfiguration hc) {
        JdbcTemplate jt = getJdbcTemplate();
        String statement = "UPDATE header_configurations SET name = ?, value = ? WHERE id = ?";
        Object[] args = new Object[]{hc.getHeaderName(), hc.getHeaderValue(),
            hc.getId()};
        if (jt.update(statement, args) == 0) {
            statement = "INSERT INTO header_configurations (name, value, id) VALUES (?,?,?)";
            jt.update(statement, args);
        }
    }

    public void delete(Integer id) {
        JdbcTemplate jt = getJdbcTemplate();
        String statement = "DELETE FROM header_configurations WHERE id = ?";
        Object[] args = new Object[]{id};
        jt.update(statement, args);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.owasp.webscarab.dao.AnnotationDao#getAll()
     */
    @SuppressWarnings("unchecked")
    public Collection<HeaderConfiguration> getAll() {
        JdbcTemplate jt = getJdbcTemplate();
        String query = "SELECT id, name, value FROM header_configurations";
        RowMapper rowMapper = new HeaderConfRowMapper();
        return jt.query(query, rowMapper);
    }

    private class HeaderConfRowMapper implements RowMapper {

        public Object mapRow(ResultSet rs, int index) throws SQLException {
            HeaderConfiguration hc = new HeaderConfiguration();
            hc.setHeaderName(rs.getString("name"));
            hc.setHeaderValue(rs.getString("value"));
            hc.setId(new Integer(rs.getInt("id")));
            return hc;
        }
    }
}
