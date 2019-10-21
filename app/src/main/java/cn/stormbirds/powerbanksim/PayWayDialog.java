package cn.stormbirds.powerbanksim;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Copyright (c) 小宝 @2019
 *
 * @description ：cn.stormbirds.powerbanksim
 * @author ：stormbirds xbaojun@gmail.com
 * @since ：2019-10-20 11:09
 */


public class PayWayDialog extends Dialog implements View.OnClickListener {

    private TextView dialogBalance;

    private LinearLayout dialogMyWallet;
    private LinearLayout dialogZhifubao;
    private LinearLayout dialogWechat;

    private ImageView rechargeWalletCB;
    private ImageView rechargeZhifubaoCB;
    private ImageView rechargeWechatCB;
    private ImageView rechargeDialogClose;

    private TextView zhifubaoTv;
    private TextView wechatTv;

    private TextView rechargeNum;

    private TextView dialogConfirmPay;

    /** 判断是充值&支付 **/
    private boolean isRecharge;


    /** 区别三种支付方式 0:我的钱包 1:支付宝 2:微信支付 **/
    public static int payWay = 0;

    private PayResultListener mPayResultListener;


    protected PayWayDialog(@NonNull Context context, PayResultListener payResultListener, boolean isRecharge, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.isRecharge = isRecharge;
        this.mPayResultListener = payResultListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pay);

        dialogBalance = findViewById(R.id.dialog_balance);
        dialogMyWallet = findViewById(R.id.dialog_my_wallet);
        dialogZhifubao = findViewById(R.id.dialog_zhifubao);
        dialogWechat = findViewById(R.id.dialog_wechat);
        dialogMyWallet.setOnClickListener(this);
        dialogZhifubao.setOnClickListener(this);
        dialogWechat.setOnClickListener(this);

        rechargeWalletCB = findViewById(R.id.recharge_wallet_cb);
        rechargeZhifubaoCB = findViewById(R.id.recharge_zhifubao_cb);
        rechargeWechatCB = findViewById(R.id.recharge_wechat_cb);

        zhifubaoTv = findViewById(R.id.dialog_zhifubao_tv);
        wechatTv = findViewById(R.id.dialog_wechat_tv);

        rechargeDialogClose = findViewById(R.id.recharge_dialog_right);
        rechargeDialogClose.setOnClickListener(this);

        rechargeNum = findViewById(R.id.recharge_num);
        dialogConfirmPay = findViewById(R.id.dialog_confirm_pay);

        if (isRecharge) {
            dialogMyWallet.setVisibility(View.GONE);
            rechargeZhifubaoCB.setVisibility(View.VISIBLE);
            payWay = 1;
        }
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            dialogWindow.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = AbsListView.LayoutParams.MATCH_PARENT;
            lp.y = 0;//设置Dialog距离底部的距离
            dialogWindow.setAttributes(lp);
        }

        dialogConfirmPay.setOnClickListener(this);

    }

    /**
     * 设置充值金额
     * @param num
     */
    public void setRechargeNum(Double num, Double balance) {
        rechargeNum.setText("￥ " + numberFormat(num));
        if (!isRecharge) {
            dialogBalance.setText("可用余额："+ numberFormat(balance));
        }
    }

    private String numberFormat(double parseDouble) {
        return String.format("%.2f", parseDouble);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recharge_dialog_right:
                dismiss();
                mPayResultListener.payResult(-1,"取消支付");
                break;
            case R.id.dialog_confirm_pay:
                dismiss();
                mPayResultListener.payResult(1,"支付成功");
                break;
            case R.id.dialog_my_wallet:
                rechargeWalletCB.setVisibility(View.VISIBLE);
                rechargeZhifubaoCB.setVisibility(View.GONE);
                rechargeWechatCB.setVisibility(View.GONE);
                break;
            case R.id.dialog_zhifubao:
                rechargeWalletCB.setVisibility(View.GONE);
                rechargeZhifubaoCB.setVisibility(View.VISIBLE);
                rechargeWechatCB.setVisibility(View.GONE);
                break;
            case R.id.dialog_wechat:
                rechargeWalletCB.setVisibility(View.GONE);
                rechargeZhifubaoCB.setVisibility(View.GONE);
                rechargeWechatCB.setVisibility(View.VISIBLE);
                break;
        }
    }
}
