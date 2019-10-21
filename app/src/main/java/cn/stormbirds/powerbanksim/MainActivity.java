package cn.stormbirds.powerbanksim;

import androidx.appcompat.app.AppCompatActivity;
import rx.Observer;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements PayResultListener{

    private MQTTManager mqttManager;
    private PayWayDialog dialog;
    private ImageButton qrBtn;
    private Button closeDeviceBtn;
    private boolean devicePowerOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttManager = MqttServiceFactory.INSTANCE.buildMQTTClient(getApplicationContext());
        devicePowerOn = true;
        qrBtn = findViewById(R.id.qr_scan_btn);
        qrBtn.setOnClickListener(v -> initDialog());
        closeDeviceBtn = findViewById(R.id.device_close_btn);
        closeDeviceBtn.setOnClickListener(v -> {
            if(devicePowerOn){
                mqttManager.closeMQTT();
                closeDeviceBtn.setBackgroundColor(Color.RED);
                qrBtn.setAlpha(0.4f);
                qrBtn.setClickable(false);
            }else{
                mqttManager = MqttServiceFactory.INSTANCE.buildMQTTClient(getApplicationContext());
                closeDeviceBtn.setBackgroundColor(Color.GREEN);
                qrBtn.setAlpha(1f);
                qrBtn.setClickable(true);
            }

            devicePowerOn=!devicePowerOn;
        });
    }

    /**
     * 初始化支付方式Dialog
     */
    private void initDialog() {

        dialog = new PayWayDialog(this,this, false, true,
                v -> Toast.makeText(this, "支付方式" + dialog.payWay + "--" ,Toast.LENGTH_SHORT).show());
        dialog.show();
        dialog.setRechargeNum(1000.00,900.0);
    }

    @Override
    public void payResult(int code, String result) {
        Log.i("MainActivity",String.format("Code: %d Result: %s",code,result));
        HttpApi.toRent(100, new Observer<ResultJson>() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this,"租借成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this,"无法连接服务器"+e.getMessage()   ,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(ResultJson resultJson) {

            }
        });
    }
}
