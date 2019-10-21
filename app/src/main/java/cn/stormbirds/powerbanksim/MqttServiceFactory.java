package cn.stormbirds.powerbanksim;


import android.content.Context;

/**
 * Copyright (c) 小宝 @2019
 *
 * @Description：cn.stormbirds.powerbanksim
 * @Author：stormbirds
 * @Email：xbaojun@gmail.com
 * @Created At：2019-10-19 18:55
 */


public enum MqttServiceFactory {
    INSTANCE(){
        private MQTTManager mqttManager;

        @Override
        public MQTTManager buildMQTTClient(Context context) {
            if (this.mqttManager == null)
                this.mqttManager = new MQTTManager(context);
            this.mqttManager.buildClient();
            return this.mqttManager;
        }
    };

    public abstract MQTTManager buildMQTTClient(Context context);
}
