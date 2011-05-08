/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.request;

import java.util.ArrayList;
import org.owasp.webscarab.dao.FuzzingVariableDao;
import org.owasp.webscarab.domain.BaseEntity;
import org.owasp.webscarab.domain.FuzzingVariable;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.jdbc.NamedValueDao;

/**
 *
 * @author Lpz
 */
public class FuzzingModel {

    public static enum FuzzingModes {

        DEPTH_FIRST, BREADTH_FIRST, RANDOM
    }
    public final static String fuzzingModeDbString = "request_maker_conf_fuzzing_mode";
    private NamedValueDao namedValueDao;
    private FuzzingModes defaultFuzzingMode = FuzzingModes.DEPTH_FIRST;
    private FuzzingVariableDao fuzzingVariableDao;
    private ArrayList<FuzzingVariable> fuzzingVariables = new ArrayList<FuzzingVariable>();

    public FuzzingVariableDao getFuzzingVariableDao() {
        return fuzzingVariableDao;
    }

    public void setFuzzingVariableDao(FuzzingVariableDao fuzzingVariableDao) {
        this.fuzzingVariableDao = fuzzingVariableDao;
    }

    public NamedValueDao getNamedValueDao() {
        return namedValueDao;
    }

    public void setNamedValueDao(NamedValueDao namedValueDao) {
        this.namedValueDao = namedValueDao;
    }

    public FuzzingModes getFuzzingMode() {
        NamedValue val = namedValueDao.findNamedValue(fuzzingModeDbString);
        if (val == null) {
            val = new NamedValue(fuzzingModeDbString, defaultFuzzingMode.toString());
            namedValueDao.saveNamedValue(val);
        }
        return FuzzingModes.valueOf(val.getValue());
    }

    public static ArrayList<String> GetFuzzingModes() {
        ArrayList<String> v = new ArrayList<String>();
        //TODO: fix getIndex implementation for these two
        //v.add(FuzzingModes.DEPTH_FIRST.toString());
        //v.add(FuzzingModes.BREADTH_FIRST.toString());
        v.add(FuzzingModes.RANDOM.toString());
        return v;
    }

    public void setFuzzingMode(FuzzingModes fuzzingMode) {
        NamedValue val = new NamedValue(fuzzingModeDbString, fuzzingMode.toString());
        namedValueDao.saveNamedValue(val);
    }

    public ArrayList<FuzzingVariable> getFuzzingVariables() {
        synchronized (this) {
            fuzzingVariables.clear();
            for (FuzzingVariable fz : fuzzingVariableDao.getAll()) {
                fuzzingVariables.add(fz);
            }
        }
        return this.fuzzingVariables;
    }
    public void setFuzzingVariables(ArrayList<FuzzingVariable> fuzzingVariables) {
        synchronized (this) {
            //adding new
            for (FuzzingVariable fz : fuzzingVariables) {
                fuzzingVariableDao.update(fz);
            }
            //filtering removed
            for (FuzzingVariable fz : this.fuzzingVariables) {
                if (fz.isNew()==false && BaseEntity.contains(fuzzingVariables, fz)==false) {
                    fuzzingVariableDao.delete(fz.getId());
                }
            }
             this.fuzzingVariables = getFuzzingVariables();
        }
    }


}
