package com.sujin.nearbytransfer;

import android.database.Observable;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Api {
    String BASE_URL = "https://mighty-gorge-53328.herokuapp.com";

    @POST("/addMessage")
    Call<String> sendMessage(@Body Message message);



    @Multipart
    @POST("/upload")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("upload") RequestBody name);


    @Multipart
    @POST("/upload")
    Observable<ResponseBody> updateProfile(@Part("user_id") RequestBody id,
                                           @Part("full_name") RequestBody fullName,
                                           @Part MultipartBody.Part image,
                                           @Part("other") RequestBody other);

    //@POST("/del")
    //Call<PostResult> sendBookmarkDeletion(@Body AddBookmark addBookmark);





}