package com.baby.project.projectbaby.process.bean;

import com.baby.project.projectbaby.sign.bean.User;
import com.google.gson.annotations.SerializedName;

/**
 * 工程版本bean类
 * objectId，updateTime，createdTime，acl继承自AvObject
 * 别名暂不需要...
 * Created by yosemite on 2018/3/17.
 */

public class ProjectVersion {

    private static final String FIELD_VERSION_NAME = "version_name";
    private static final String FIELD_VERSION_CODE = "version_code";
    private static final String FIELD_VERSION_DESCRIBE = "version_describe";
    private static final String FIELD_VERSION_REMARK = "version_remark";
    private static final String FIELD_VERSION_OPERATER = "operater";

    @SerializedName(FIELD_VERSION_NAME)
    private String versionName;

    @SerializedName(FIELD_VERSION_CODE)
    private int versionCode;

    @SerializedName(FIELD_VERSION_DESCRIBE)
    private String describe;

    @SerializedName(FIELD_VERSION_REMARK)
    private String remark;

    @SerializedName(FIELD_VERSION_OPERATER)
    private User operater;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public User getOperater() {
        return operater;
    }

    public void setOperater(User operater) {
        this.operater = operater;
    }
}
