package com.pm.pi_mg.providers;

import com.pm.pi_mg.models.FCMBody;
import com.pm.pi_mg.models.FCMResponse;
import com.pm.pi_mg.retrofit.IFCMApi;
import com.pm.pi_mg.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
