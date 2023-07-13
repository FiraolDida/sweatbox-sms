package com.example.sweatbox_sms_test.Interfaces;

import com.example.sweatbox_sms_test.Models.UserModel;
import com.google.gson.JsonObject;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MyAPIService {
    @GET("google-sheet/all")
    Call<List<UserModel>> getAll();

    @GET("google-sheet/newlyRegisteredUsers")
    Call<List<UserModel>> getNewlyRegisteredUsers();

    @GET("google-sheet/renewedUsers")
    Call<List<UserModel>> getRenewedUsers();

    @GET("google-sheet/nearExpirationUsers")
    Call<List<UserModel>> getNearExpirationUsers();

    @GET("google-sheet/expiredUsers")
    Call<List<UserModel>> getExpiredUsers();

    @POST("google-sheet")
    Call<JsonObject> updateUserStatus(@Body JsonObject jsonObject);
}
