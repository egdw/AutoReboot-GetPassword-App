package com.example.getpassword;

/**
 * Created by hdy on 2018/12/5.
 */

public class Password {
    private String next_update_time;
    private String new_password;

    public Password(String next_update_time, String new_password) {
        this.next_update_time = next_update_time;
        this.new_password = new_password;
    }

    public String getNext_update_time() {
        return next_update_time;
    }

    public void setNext_update_time(String next_update_time) {
        this.next_update_time = next_update_time;
    }

    public String getNew_password() {
        return new_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }

    @Override
    public String toString() {
        return "Password{" +
                "next_update_time='" + next_update_time + '\'' +
                ", new_password='" + new_password + '\'' +
                '}';
    }
}
