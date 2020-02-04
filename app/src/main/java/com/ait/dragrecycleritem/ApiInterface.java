package com.ait.dragrecycleritem;

import com.ait.dragrecycleritem.model.DistanceMatrixModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Url;

/*Dependencies Required*//*
*//*Retrofit*//*
    implementation 'com.squareup.retrofit2:retrofit:2.4.0'
            implementation 'com.google.code.gson:gson:2.6.2'
            implementation 'com.squareup.retrofit2:converter-gson:2.4.0'

            *//*okHttpLoggingInterceptor*//*
            implementation 'com.squareup.okhttp3:logging-interceptor:3.10.0'
            implementation 'com.squareup.okhttp3:okhttp:3.10.0'
            //for maps
                implementation 'com.squareup.retrofit2:converter-scalars:2.4.0'*/
public interface ApiInterface {
    @GET
    Call<String> getPath(@Url String url);

    @GET
    Call<DistanceMatrixModel> getDistanceMatrixApi(@Url String url);
}
