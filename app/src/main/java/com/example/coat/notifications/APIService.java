package com.example.coat.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:applicatio/json",
            "Authorization:key=AAAA9hmQsV0:APA91bGNuFV7B6aiTqYkyh22sA_CKmWo6ur8AxuUbcY-VstAbGdaF-YPVucsUKR37x8i8eUmt93mxhB6ViNIXqz3RpT5ZSuRD4htFDPf0Njm3-el1vV-B52gOHhfCMm5PxOvRUUy87hY"
     })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
