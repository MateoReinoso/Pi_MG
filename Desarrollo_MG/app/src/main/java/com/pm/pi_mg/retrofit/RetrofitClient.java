package com.pm.pi_mg.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    private static Retrofit retorfit = null;

    public static Retrofit getClient(String url){
        if (retorfit== null){
            retorfit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retorfit;
    }

}
