/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.owasp.webscarab.dao.FormValueConfigurationDao;
import org.owasp.webscarab.domain.FormValueConfiguration;

import org.owasp.webscarab.domain.HeaderConfiguration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author lpz
 *
 */
public class JdbcFormValueConfigurationDao extends PropertiesJdbcDaoSupport implements FormValueConfigurationDao {

    private void createTables() {
        getJdbcTemplate().execute(getProperty("formValueConfigurations.createTable"));
        this.update(new FormValueConfiguration(".*", "test", false));
        this.update(new FormValueConfiguration("user*", "lpz", true));
        this.update(new FormValueConfiguration("pass.*", "secretPassword", true));
        this.update(new FormValueConfiguration("search.*", "<script>alert('XSS')</script>", true));
        this.update(new FormValueConfiguration("id.*", "1", true));
        this.update(new FormValueConfiguration("test.*", "test", true));

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
    public FormValueConfiguration get(Integer id) {
        JdbcTemplate jt = getJdbcTemplate();
        String query = "SELECT id, name, value, overwrite FROM form_values_configurations WHERE id = ?";
        Object[] args = new Object[]{id};
        RowMapper rowMapper = new FormValueConfRowMapper();
        Collection<FormValueConfiguration> results = jt.query(query, args, rowMapper);
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
    public void update(FormValueConfiguration hc) {
        JdbcTemplate jt = getJdbcTemplate();
        String statement = "UPDATE form_values_configurations SET name = ?, value = ?, overwrite = ? WHERE id = ?";
        Object[] args = new Object[]{hc.getFormName(), hc.getFormValue(), hc.getOverwrite(),
            hc.getId()};
        if (jt.update(statement, args) == 0) {
            statement = "INSERT INTO form_values_configurations (name, value, overwrite, id) VALUES (?,?,?,?)";
            jt.update(statement, args);
        }
    }

    public void delete(Integer id) {
        JdbcTemplate jt = getJdbcTemplate();
        String statement = "DELETE FROM form_values_configurations WHERE id = ?";
        Object[] args = new Object[]{id};
        jt.update(statement, args);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.owasp.webscarab.dao.AnnotationDao#getAll()
     */
    @SuppressWarnings("unchecked")
    public Collection<FormValueConfiguration> getAll() {
        JdbcTemplate jt = getJdbcTemplate();
        String query = "SELECT id, name, value, overwrite FROM form_values_configurations";
        RowMapper rowMapper = new FormValueConfRowMapper();
        return jt.query(query, rowMapper);
    }

    private class FormValueConfRowMapper implements RowMapper {

        public Object mapRow(ResultSet rs, int index) throws SQLException {
            FormValueConfiguration hc = new FormValueConfiguration();
            hc.setFormName(rs.getString("name"));
            hc.setFormValue(rs.getString("value"));
            hc.setId(new Integer(rs.getInt("id")));
            hc.setOverwrite(rs.getBoolean("overwrite"));
            return hc;
        }
    }
}
