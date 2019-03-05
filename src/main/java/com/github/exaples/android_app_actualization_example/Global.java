package com.github.exaples.android_app_actualization_example;

import android.app.Application;

public class Global extends Application {

    private final String ip = "server_ip_here:8022";

    //private final String IP = "192.168.11.122:8080";

    public String getIP() { return ip; }

}