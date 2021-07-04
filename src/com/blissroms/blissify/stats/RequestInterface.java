package com.blissroms.blissify.stats;

import com.blissroms.blissify.stats.models.ServerRequest;
import com.blissroms.blissify.stats.models.ServerResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestInterface {

    @POST("stats/")
    Observable<ServerResponse> operation(@Body ServerRequest request);

}
