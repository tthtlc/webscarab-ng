/*
 * BaseEntity.java
 *
 * Created on 09 March 2006, 06:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.owasp.webscarab.domain;

import java.util.List;

/**
 *
 * @author rdawes
 */
public class BaseEntity implements Entity {

    private Integer id = null;

    /** Creates a new instance of BaseEntity */
    public BaseEntity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isNew() {
        return getId() == null;
    }

    public static <T extends BaseEntity> boolean contains(List<T> newV, T be) {
        boolean foundVar = false;
        try {
            for (int i = 0; i < newV.size(); ++i) {
                if (newV.get(i).isNew()) {
                    continue;
                }
                int vid = be.getId();
                int nid = newV.get(i).getId();
                if (nid == vid) {
                    foundVar = true;
                    break;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return foundVar;
    }
}
