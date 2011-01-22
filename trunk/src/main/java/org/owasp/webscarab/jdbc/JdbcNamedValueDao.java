/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.owasp.webscarab.domain.NamedValue;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

/**
 * @author rdawes
 *
 */
public class JdbcNamedValueDao extends PropertiesJdbcDaoSupport implements NamedValueDao {

    private NamedValueIdQuery namedValueIdQuery;
    private NamedValueQuery namedValueQuery;
    private NamedValueInsert namedValueInsert;
    private NamedValueNameQuery namedValueNameQuery;
    private NamedValueUpdate namedValueUpdate;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.dao.support.DaoSupport#initDao()
     */
    @Override
    protected void initDao() throws Exception {
        super.initDao();
        namedValueIdQuery = new NamedValueIdQuery();
        namedValueQuery = new NamedValueQuery();
        namedValueInsert = new NamedValueInsert();
        namedValueNameQuery = new NamedValueNameQuery();
        namedValueUpdate = new NamedValueUpdate();
        NamedValue nv = new NamedValue("", "");
        try {
            findNamedValueId(nv);
        } catch (Exception e) {
            createTables();
            findNamedValueId(nv);
        }
    }

    private void createTables() {
        getJdbcTemplate().execute(getProperty("named_values.createTable"));
    }

    public Integer findNamedValueId(NamedValue nv) {
        return namedValueIdQuery.getId(nv);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.jdbc.NamedValueDao#saveNamedValue(org.owasp.webscarab.NamedValue)
     */
    public Integer saveNamedValue(NamedValue nv) {
        NamedValue dbnv = findNamedValue(nv.getName());
        if(findNamedValue(nv.getName())==null)
            return namedValueInsert.insert(nv);
        else {
            nv.setId(dbnv.getId());
            return namedValueUpdate.update(nv);
        }
            
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.jdbc.NamedValueDao#findNamedValue(java.lang.Integer)
     */
    public NamedValue findNamedValue(Integer id) {
        return namedValueQuery.getNamedValue(id);
    }

    public NamedValue findNamedValue(String name) {
        return namedValueNameQuery.getNamedValue(name);
    }

    private class NamedValueIdQuery extends MappingSqlQuery {

        public NamedValueIdQuery() {
            super(getDataSource(),
                    "SELECT id FROM named_values WHERE name = ? AND value = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public Integer getId(NamedValue nv) {
            Object[] params = new Object[]{nv.getName(), nv.getValue()};
            nv.setId((Integer) findObject(params));
            return nv.getId();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet,
         *      int)
         */
        @Override
        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            return new Integer(rs.getInt("id"));
        }
    }

    private class NamedValueNameQuery extends MappingSqlQuery {

        public NamedValueNameQuery() {
            super(getDataSource(),
                    "SELECT id, name, value FROM named_values WHERE name = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public NamedValue getNamedValue(String name) {
            Object[] params = new Object[]{name};
            NamedValue nv = (NamedValue) findObject(params);
            return nv;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet,
         *      int)
         */
        @Override
        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            NamedValue nv = new NamedValue(rs.getString("name"), rs.getString("value"));
            nv.setId(rs.getInt("id"));
            return nv;
        }
    }

    private class NamedValueQuery extends MappingSqlQuery {

        public NamedValueQuery() {
            super(getDataSource(),
                    "SELECT name, value FROM named_values WHERE id = ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public NamedValue getNamedValue(Integer id) {
            Object[] params = new Object[]{id};
            NamedValue nv = (NamedValue) findObject(params);
            nv.setId(id);
            return nv;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet,
         *      int)
         */
        @Override
        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            return new NamedValue(rs.getString("name"), rs.getString("value"));
        }
    }

    private class NamedValueInsert extends SqlUpdate {

        protected NamedValueInsert() {
            super(getDataSource(),
                    "INSERT INTO named_values (name, value) VALUES (?,?)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected Integer insert(NamedValue nv) {
            Object[] objs = new Object[]{nv.getName(), nv.getValue()};
            update(objs);
            Integer id = retrieveIdentity();
            nv.setId(id);
            return id;
        }
    }
     private class NamedValueUpdate extends SqlUpdate {

        protected NamedValueUpdate() {
            super(getDataSource(),
                    "UPDATE named_values SET name = ? , value = ? WHERE ID = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Integer update(NamedValue nv) {
            Object[] objs = new Object[]{nv.getName(), nv.getValue(), nv.getId()};
            update(objs);
            Integer id = retrieveIdentity();
            nv.setId(id);
            return id;
        }
    }
}
