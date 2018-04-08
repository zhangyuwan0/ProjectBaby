package com.baby.project.projectbaby.sign.bean;

import android.os.Parcel;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVUser;

/**
 * 用户类，暂时未实现
 * 应放至main包下
 * Created by yosemite on 2018/3/17.
 */

@AVClassName("User")
public class User extends AVUser {

    private static final String FIELD_ID_CARD = "id_card";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SEX = "sex";
    private static final String FIELD_ETHNICITY = "ethnicity";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_CERTIFICATES = "certificates";
    private static final String FIELD_BIRTHDAY = "birthday";
    private static final String FIELD_AVATAR = "avatar";

    //此处为我们的默认实现，当然你也可以自行实现
    public static final Creator CREATOR = AVObjectCreator.instance;

    // 必须向leanCloud提供的构造函数
    public User() {}

    // 实现Parcelable接口需提供的构造函数
    public User(Parcel in){
        super(in);
    }

    public void setAvatar(String avatar) {
        this.put(FIELD_AVATAR, avatar);
    }

    public String getAvatar() {
        return this.getString(FIELD_AVATAR);
    }

    public void setBirthday(String birthday) {
        this.put(FIELD_BIRTHDAY, birthday);
    }

    public String getBirthday() {
        return this.getString(FIELD_BIRTHDAY);
    }

    public void setCertificates(String certificates) {
        this.put(FIELD_CERTIFICATES, certificates);
    }

    public String getCertificates() {
        return this.getString(FIELD_CERTIFICATES);
    }

    public void setAddress(String address) {
        this.put(FIELD_ADDRESS, address);
    }

    public String getAddress() {
        return this.getString(FIELD_ADDRESS);
    }

    public void setEthnicity(String ethnicity) {
        this.put(FIELD_ETHNICITY, ethnicity);
    }

    public String getEthnicity() {
        return this.getString(FIELD_ETHNICITY);
    }

    public void setIDCard(String idCard) {
        this.put(FIELD_ID_CARD, idCard);
    }

    public String getIDCard() {
        return this.getString(FIELD_ID_CARD);
    }

    public void setName(String name) {
        this.put(FIELD_NAME, name);
    }

    public String getName() {
        return this.getString(FIELD_NAME);
    }

    public void setSex(boolean sex) {
        this.put(FIELD_SEX, sex);
    }

    public boolean getSex() {
        return this.getBoolean(FIELD_SEX);
    }


}
