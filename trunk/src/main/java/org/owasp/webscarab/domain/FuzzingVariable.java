/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.domain;

/**
 * @author Lpz
 */
public final class FuzzingVariable extends BaseEntity {

    public static final String PROPERTY_NAME = "variableName";
    public static final String PROPERTY_PATH = "sourcePath";
    
    String variableName;
    String sourcePath;
    
    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public FuzzingVariable() {
        variableName = "$(new_variable)";
        sourcePath = "";
    }

    public FuzzingVariable(String name, String path) {
        setVariableName(name);
        setSourcePath(path);
    }
}
