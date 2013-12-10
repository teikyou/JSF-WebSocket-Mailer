package jp.co.oracle.samples.beans;

import java.io.Serializable;

/**
 *
 * @author Yoshio Terada
 */

public class FolderName implements Serializable {

    /**
     * Creates a new instance of FolderName
     */
    public FolderName() {
        ;
    }
    private String name;
    private String fullName;

    public FolderName(String name, String fullName) {
        this.name = name;
        this.fullName = fullName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
