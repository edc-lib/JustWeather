package com.edward.justweather.util;

/**
 * Created by Edward on 2015-04-21.
 */
public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);
}
