package com.chaincloud.chaincloudv.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by songchenwen on 15/7/24.
 */
public class User implements Serializable {
    public enum Gender {
        Undefined(-1),
        @SerializedName("1")
        Male(1),
        @SerializedName("0")
        Female(0);

        private int value;

        Gender(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Gender gender(int value) {
            for (Gender g : Gender.values()) {
                if (g.value() == value) {
                    return g;
                }
            }
            return Undefined;
        }
    }

    @SerializedName("user_name")
    private String name;
    @SerializedName("user_id")
    private int id;
    private Gender gender;
    private String avatar;
    private String phone;
    private long balance;
    private BigInteger balanceStr;
    private String address;
    private int receivingIndex = -1;

    public User(int id, String name, Gender gender, String avatar, String phone) {
        this(id, name, gender, avatar, phone, 0, null);
    }

    public User(int id, String name, Gender gender, String avatar, String phone, long balance,
                String address) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.avatar = avatar;
        this.phone = phone;
        this.balance = balance;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Gender getGender() {
        return gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public long getBalance() {
        return balance;
    }

    public BigInteger getBalanceStr() {
        return balanceStr;
    }

    public int getReceivingIndex() {
        return receivingIndex;
    }


    public static boolean isAdmin(int userId) {
        return userId == 10001;
    }

    public static final UserBuilder builder() {
        return new UserBuilder();
    }

    public static final class UserBuilder implements Serializable {
        private String name;
        private int id;
        private String avatar;
        private Gender gender;
        private String phone;
        private String address;
        private long balance;

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder id(int id) {
            this.id = id;
            return this;
        }

        public UserBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserBuilder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserBuilder balance(long balance) {
            this.balance = balance;
            return this;
        }

        public UserBuilder address(String address) {
            this.address = address;
            return this;
        }

        public User build() {
            if (gender == null) {
                gender = Gender.Undefined;
            }
            return new User(id, name, gender, avatar, phone, balance, address);
        }
    }
}
