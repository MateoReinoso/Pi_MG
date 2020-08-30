package com.pm.pi_mg.retrofit;

import com.pm.pi_mg.models.FCMBody;
import com.pm.pi_mg.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAibZZ-3E:APA91bHoLdamXyDBiDvp09TJkTnRw5ZfGDycdh4nPk0ZidVeZa-MXVbE_w3DfuEwoG-QP361ZwFZTo5gDAszcyB6iiZyJ4Ss9aGfBXTF5PSKAWJhu5tU1K1TZo6iJ-0ZdHB-wS8Rz1Hy"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
