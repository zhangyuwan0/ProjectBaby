package com.baby.project.projectbaby.sign.bean;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * 用户类，暂时未实现
 * 应放至main包下
 * Created by yosemite on 2018/3/17.
 */

@AVClassName("User")
public class User extends AVObject{

    private static final String FIELD_ID_CARD = "id_card";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SEX = "sex";
    private static final String FIELD_ETHNICITY = "ethnicity";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_CERTIFICATES = "certificates";
    private static final String FIELD_BIRTHDAY = "birthday";
    private static final String FIELD_AVATAR = "avatar";

    public void setIDCard(String idCard){
        this.put(FIELD_ID_CARD,idCard);
    }

    public String getIDCard() {
        return this.getString(FIELD_ID_CARD);
    }

    public void setName(String name){
        this.put(FIELD_NAME,name);
    }

    public String getName() {
        return this.getString(FIELD_NAME);
    }

    public void setSex(boolean sex){
        this.put(FIELD_SEX,sex);
    }

    public boolean getSex() {
        return this.getBoolean(FIELD_SEX);
    }

    


}
