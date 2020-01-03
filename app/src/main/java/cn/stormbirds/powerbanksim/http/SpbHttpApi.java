package cn.stormbirds.powerbanksim.http;


import cn.stormbirds.powerbanksim.domain.ResultJson;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Copyright (c) 小宝 @2019
 *
 * @Description：cn.stormbirds.powerbanksim
 * @Author：stormbirds
 * @Email：xbaojun@gmail.com
 * @Created At：2019-10-20 16:50
 */


public interface SpbHttpApi {

    @POST("app/v1/order/toRent")
    Observable<ResultJson> toRent(@Query("eqCode") int start);
}
