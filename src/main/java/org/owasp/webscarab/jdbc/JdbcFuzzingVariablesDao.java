/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import org.owasp.webscarab.dao.FuzzingVariableDao;
import org.owasp.webscarab.domain.FuzzingVariable;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author lpz
 *
 */
public class JdbcFuzzingVariablesDao extends PropertiesJdbcDaoSupport implements FuzzingVariableDao {
    private void createTables() {
        getJdbcTemplate().execute(getProperty("requestVariables.createTable"));
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
    public FuzzingVariable get(Integer id) {
        JdbcTemplate jt = getJdbcTemplate();
        String query = "SELECT id, name, path FROM request_variables WHERE id = ?";
        Object[] args = new Object[]{id};
        RowMapper rowMapper = new VariableRowMapper();
        Collection<FuzzingVariable> results = jt.query(query, args, rowMapper);
        if (results.isEmpty()) {
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
    public int update(FuzzingVariable hc) {
        int id = 0;
        synchronized(this) {
            JdbcTemplate jt = getJdbcTemplate();
            String statement = "UPDATE request_variables SET name = ?, path = ? WHERE id = ?";
            Object[] args = new Object[]{hc.getVariableName(), hc.getSourcePath(),
                hc.getId()};
            if (jt.update(statement, args) == 0) {
                statement = "INSERT INTO request_variables (name, path, id) VALUES (?,?,?)";
                jt.update(statement, args);
            }
            FuzzingVariable fz = null;
            Iterator<FuzzingVariable> it = this.getAll().iterator();
            while(it.hasNext())
                fz = it.next();
            id = fz.getId();
        }
        return id;
    }

    public void delete(Integer id) {
        JdbcTemplate jt = getJdbcTemplate();
        String statement = "DELETE FROM request_variables WHERE id = ?";
        Object[] args = new Object[]{id};
        jt.update(statement, args);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.owasp.webscarab.dao.AnnotationDao#getAll()
     */
    @SuppressWarnings("unchecked")
    public Collection<FuzzingVariable> getAll() {
        JdbcTemplate jt = getJdbcTemplate();
        String query = "SELECT id, name, path FROM request_variables";
        RowMapper rowMapper = new VariableRowMapper();
        return jt.query(query, rowMapper);
    }

    private class VariableRowMapper implements RowMapper {

        public Object mapRow(ResultSet rs, int index) throws SQLException {
            FuzzingVariable hc = new FuzzingVariable();
            hc.setVariableName(rs.getString("name"));
            hc.setSourcePath(rs.getString("path"));
            hc.setId(new Integer(rs.getInt("id")));
            return hc;
        }
    }
}
