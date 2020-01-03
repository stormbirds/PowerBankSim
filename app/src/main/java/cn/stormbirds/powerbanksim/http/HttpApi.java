package cn.stormbirds.powerbanksim.http;


import cn.stormbirds.powerbanksim.domain.ResultJson;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Copyright (c) 小宝 @2019
 *
 * @Description：cn.stormbirds.powerbanksim
 * @Author：stormbirds
 * @Email：xbaojun@gmail.com
 * @Created At：2019-10-20 17:41
 */


public class HttpApi {
    private static SpbHttpApi httpApi = RetrofitServiceManager.getInstance().create(SpbHttpApi.class);

    /*
     * 获取豆瓣电影榜单
     * **/
    public static void toRent(int eqCode, Observer<ResultJson> observer) {
        setSubscribe(httpApi.toRent(eqCode), observer);
    }

    private static <T> void setSubscribe(Observable<T> observable, Observer<T> observer) {
        observable.subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())//子线程访问网络
                .observeOn(AndroidSchedulers.mainThread())//回调到主线程
                .subscribe(observer);
    }
}
