package com.zeros.demo.model;

/**
 * Created on 2018/1/31.
 *
 * @author 迹_Jason
 */
public class Role {
    private int id;
    private String roleCode;
    private String roleName;
    private int delFlag;

    public int getId() {
        return id;
    }

    public Role setId(int id) {
        this.id = id;
        return this;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public Role setRoleCode(String roleCode) {
        this.roleCode = roleCode;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public Role setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public int getDelFlag() {
        return delFlag;
    }

    public Role setDelFlag(int delFlag) {
        this.delFlag = delFlag;
        return this;
    }
}
