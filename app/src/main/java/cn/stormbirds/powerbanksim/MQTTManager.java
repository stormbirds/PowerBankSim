package cn.stormbirds.powerbanksim;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MQTTManager {

    private Context mContext;
    private MqttAndroidClient mqttAndroidClient;
//    private String clientId;//自定义

    private MqttConnectOptions mqttConnectOptions;

    private ScheduledExecutorService reconnectPool;//重连线程池

    public MQTTManager(Context mContext) {
        this.mContext = mContext;
    }

    public void buildClient() {
        closeMQTT();//先关闭上一个连接

        buildMQTTClient();
    }

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.i("MQTTManager","connect-"+"onSuccess");
            closeReconnectTask();
            subscribeToTopic();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            //connect-onFailure-MqttException (0) - java.net.UnknownHostException
            Log.i("MQTTManager","connect-"+ "onFailure-"+exception);
            startReconnectTask();
        }
    };

    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            //close-connectionLost-等待来自服务器的响应时超时 (32000)
            //close-connectionLost-已断开连接 (32109)
            Log.i("MQTTManager","close-"+"connectionLost-"+cause);
            if (cause != null) {//null表示被关闭
                startReconnectTask();
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String body = new String(message.getPayload());
            Log.i("MQTTManager","messageArrived-"+message.getId()+"-"+body);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {
                Log.i("MQTTManager","deliveryComplete-"+token.getMessage().toString());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    };

    private void buildMQTTClient(){
        mqttAndroidClient = new MqttAndroidClient(mContext, MQTTCons.serverUri, MQTTCons.clientId);
        mqttAndroidClient.setCallback(mqttCallback);

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setKeepAliveInterval(20);
        mqttConnectOptions.setCleanSession(true);
        try {
            mqttConnectOptions.setUserName("Signature|" + MQTTCons.accessKey + "|" + MQTTCons.instanceId);
            mqttConnectOptions.setPassword(MacSignature.macSignature(MQTTCons.clientId, MQTTCons.secretKey).toCharArray());
        } catch (Exception e) {
        }
        doClientConnection();
    }

    private synchronized void startReconnectTask(){
        if (reconnectPool != null)return;
        reconnectPool = Executors.newScheduledThreadPool(1);
        reconnectPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doClientConnection();
            }
        } , 0 , 5*1000 , TimeUnit.MILLISECONDS);
    }

    private synchronized void closeReconnectTask(){
        if (reconnectPool != null) {
            reconnectPool.shutdownNow();
            reconnectPool = null;
        }
    }

    /**
     * 连接MQTT服务器
     */
    private synchronized void doClientConnection() {
        if (!mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, iMqttActionListener);
                Log.d("MQTTManager","mqttAndroidClient-connecting-"+mqttAndroidClient.getClientId());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void subscribeToTopic() {//订阅之前会取消订阅，避免重连导致重复订阅
        try {
            String registerTopic = "registerTopic";//自定义
            String controlTopic = "controlTopic";//自定义
            String[] topicFilter=new String[]{registerTopic , controlTopic };
            int[] qos={2,2};
            mqttAndroidClient.unsubscribe(topicFilter, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("MQTTManager","unsubscribe-"+"success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("MQTTManager","unsubscribe-"+"failed-"+exception);
                }
            });
            mqttAndroidClient.subscribe(topicFilter, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {//订阅成功
                    Log.i("MQTTManager","subscribe-"+"success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    startReconnectTask();
                    Log.i("MQTTManager","subscribe-"+"failed-"+exception);
                }
            });

        } catch (MqttException ex) {
        }
    }

    public void sendMQTT(String topicSep, final String msg) {
        try {
            if (mqttAndroidClient == null)return;
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            String topic = "";//自定义
            mqttAndroidClient.publish(topic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    TVLog.i("sendMQTT-"+"success:" + msg);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    startReconnectTask();
                    Log.i("MQTTManager","sendMQTT-"+"failed:" + msg);
                }
            });
        } catch (MqttException e) {
        }
    }

    public void closeMQTT(){
        closeReconnectTask();
        if (mqttAndroidClient != null){
            try {
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.disconnect();
                Log.i("MQTTManager","closeMQTT-"+mqttAndroidClient.getClientId());
                mqttAndroidClient = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

}