package com.test.mywifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.test.mywifi.model.WifiListModel;
import com.test.mywifi.utils.WifiUtil;


/**
 * Created by Admin on 2017/7/21.
 */

public class WifiDetailDialog extends AlertDialog implements View.OnClickListener {
    private final String TAG = WifiDetailDialog.class.getSimpleName();
    private EditText passwordEt;
    private WifiUtil wifiUtil;
    private WifiListModel.ScanResultModel scanResult;
    private View connectLayout;
    private View notConnectLayout;
    private Switch proxySw;
    private Switch ipSettingSw;
    private EditText ipAddressEt;
    private EditText gatewayEt;
    private EditText preLengthEt;
    private EditText dns1Et;
    private EditText dns2Et;
    private View connectBtn;

    private EditText proxyHostNameEt;//代理主机名
    private EditText proxyPortEt;//代理端口
    private EditText proxyFilterEt;//代理过滤
    private Context context;
    protected WifiDetailDialog(Context context, WifiListModel.ScanResultModel scanResult) {
        super(context);
        this.context = context;
        this.scanResult = scanResult;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setView(new EditText(context));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ldklz_carcool_wifi_dialog_wifi_detail);
        wifiUtil = new WifiUtil(context);
//        Intent intent = getIntent();
//        scanResult = intent.getParcelableExtra("data");
        initView();
        setListener();


    }

    private void setListener() {
        connectLayout.findViewById(R.id.btn_cancle).setOnClickListener(this);
        connectLayout.findViewById(R.id.btn_cancle_save).setOnClickListener(this);
        connectLayout.findViewById(R.id.btn_edit_wifi).setOnClickListener(this);
        notConnectLayout.findViewById(R.id.btn_cancle).setOnClickListener(this);
        notConnectLayout.findViewById(R.id.btn_cancle_save).setOnClickListener(this);
        notConnectLayout.findViewById(R.id.btn_edit_wifi).setOnClickListener(this);
        connectBtn.setOnClickListener(this);
        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((wifiUtil.IsExsits(scanResult.SSID) == null && s.toString().length() < 8)
                        || (wifiUtil.IsExsits(scanResult.SSID) != null && s.toString().length() > 0 && s.toString().length() < 8)) {
                    connectBtn.setEnabled(false);
                    connectBtn.setAlpha(0.5f);
                } else {
                    connectBtn.setEnabled(true);
                    connectBtn.setAlpha(1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ((CheckBox) findViewById(R.id.cb_password)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        proxySw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.ll_proxy).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.ll_proxy).setVisibility(View.GONE);
                }
            }
        });
        ipSettingSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.ll_ip_setting).setVisibility(View.VISIBLE);
                    //

                } else {
                    findViewById(R.id.ll_ip_setting).setVisibility(View.GONE);
                }
            }
        });
    }

    private void initView() {
        ((TextView) findViewById(R.id.tv_title)).setText(scanResult.SSID);
        ((TextView) findViewById(R.id.tv_signal_strength)).setText(wifiUtil.getSignalStrength(scanResult.level));


        connectLayout = findViewById(R.id.layout_connect);
        notConnectLayout = findViewById(R.id.layout_not_connect);
        connectBtn = notConnectLayout.findViewById(R.id.btn_connect);
        passwordEt = (EditText) findViewById(R.id.et_password);
        proxySw = ((Switch) findViewById(R.id.switch_proxy));
        ipSettingSw = ((Switch) findViewById(R.id.switch_ip_setting));

        //IP设置
        ipAddressEt = (EditText) findViewById(R.id.et_ip_address);
        gatewayEt = (EditText) findViewById(R.id.et_gateway);
        preLengthEt = (EditText) findViewById(R.id.et_pre_length);
        dns1Et = (EditText) findViewById(R.id.et_dns1);
        dns2Et = (EditText) findViewById(R.id.et_dns2);
        //代理
        proxyHostNameEt = (EditText) findViewById(R.id.et_proxy_host_name);
        proxyPortEt = (EditText) findViewById(R.id.et_proxy_port);
        proxyFilterEt = (EditText) findViewById(R.id.et_proxy_filter);

        //应该判断该wifi有没保存过
        if (wifiUtil.IsExsits(scanResult.SSID) != null) {
            notConnectLayout.findViewById(R.id.btn_cancle_save).setVisibility(View.VISIBLE);
            notConnectLayout.findViewById(R.id.btn_edit_wifi).setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(passwordEt.getText().toString())) {
                passwordEt.setHint("(未更改)");
            }
            String proHost = android.net.Proxy.getDefaultHost();
        } else {
            //没保存过
            notConnectLayout.findViewById(R.id.btn_cancle_save).setVisibility(View.GONE);
            notConnectLayout.findViewById(R.id.btn_edit_wifi).setVisibility(View.GONE);
            connectBtn.setAlpha(0.5f);
            connectBtn.setEnabled(false);
        }

        // TODO: 2017/7/25  暂时隐藏，功能没实现
        connectLayout.findViewById(R.id.btn_edit_wifi).setVisibility(View.GONE);
        notConnectLayout.findViewById(R.id.btn_edit_wifi).setVisibility(View.GONE);

        if (wifiUtil.isConnectWifi() && scanResult.SSID.equals(wifiUtil.getSSID())) {
            //已连接
            connectLayout.setVisibility(View.VISIBLE);
            notConnectLayout.setVisibility(View.GONE);
            findViewById(R.id.ll_status_info).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_connection_speed).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_ip_address).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_password).setVisibility(View.GONE);

            ((TextView) findViewById(R.id.tv_status_info)).setText(context.getResources().getString(R.string.connected));
            ((TextView) findViewById(R.id.tv_connection_speed)).setText(wifiUtil.getLinkSpeed() + "");
            ((TextView) findViewById(R.id.tv_ip_address)).setText(wifiUtil.getIPAddress() + "");
        } else {
            //未连接
            connectLayout.setVisibility(View.GONE);
            notConnectLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.ll_status_info).setVisibility(View.GONE);
            findViewById(R.id.ll_ip_address).setVisibility(View.GONE);
            findViewById(R.id.ll_connection_speed).setVisibility(View.GONE);
            findViewById(R.id.ll_password).setVisibility(View.VISIBLE);
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (scanResult.capabilities.contains("WPA-PSK-CCMP")) {
            stringBuilder.append("WPA");
            if (scanResult.capabilities.contains("WPA2-PSK-CCMP")) {
                stringBuilder.append("/WPA2");
            }
        } else if (scanResult.capabilities.contains("WPA2-PSK-CCMP")) {
            stringBuilder.append("WPA2");
        } else if (scanResult.capabilities.contains("WEP")) {
            stringBuilder.append("WEP");
        } else {
            if (TextUtils.isEmpty(scanResult.capabilities) || "[ESS]".equals(scanResult.capabilities)) {
                stringBuilder.append("无");
                findViewById(R.id.ll_password).setVisibility(View.GONE);
                connectBtn.setEnabled(true);
                connectBtn.setAlpha(1);
            }
        }
        ((TextView) findViewById(R.id.tv_security)).setText(stringBuilder.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle:
                dismiss();//finish();
                break;
            case R.id.btn_cancle_save:
                boolean cancleBl = wifiUtil.cancleSave(scanResult.SSID);
                if (cancleBl) {
                    Log.i(TAG, "onClick: 取消保存成功");
                    dismiss();//finish();
                } else {
                    Log.i(TAG, "onClick: 取消保存失败");
                }

                break;
            case R.id.btn_connect:
                connect();
                break;
            case R.id.btn_edit_wifi://不会自动连接网络的,,,,没实现完
                editWifi();
                break;
        }
    }

    private void connect() {//不开放高级设置时，代码没问题

        if (passwordEt.getText().length() >= 8) {
            if (wifiUtil.IsExsits(scanResult.SSID) == null) {
                Log.i(TAG, "connect: 没有保存的");
                WifiConfiguration configuration = wifiUtil.createWifiInfo(scanResult.capabilities, scanResult.SSID, passwordEt.getText().toString());
                wifiUtil.addNetwork(configuration);
            } else {
                Log.i(TAG, "connect: 有保存的");//需要修改密码
                WifiConfiguration configuration = wifiUtil.createWifiInfo(scanResult.capabilities, scanResult.SSID, passwordEt.getText().toString());
                wifiUtil.addNetwork(configuration);
//                wifiUtil.connectWifi(scanResult.SSID);
            }
        } else if (TextUtils.isEmpty(scanResult.capabilities) || "[ESS]".equals(scanResult.capabilities)) {
            Log.i(TAG, "connect: 没有密码");
            wifiUtil.connectWifi(scanResult.SSID);
        } else if (wifiUtil.IsExsits(scanResult.SSID) != null) {//有保存的可以不需要修改密码
            Log.i(TAG, "connect: 有保存的");
            wifiUtil.connectWifi(scanResult.SSID);
        } else {
            Log.i(TAG, "connect: return");
            return;
        }
        dismiss();//finish();//无法得知wifi是否连接成功，想知道就要开个广播监听
    }

    private void editWifi() {
        WifiConfiguration configuration = null;
        if (passwordEt.getText().length() >= 8) {//需要修改密码
            configuration = wifiUtil.createWifiInfo(scanResult.capabilities, scanResult.SSID, passwordEt.getText().toString());
        } else if (TextUtils.isEmpty(scanResult.capabilities) || "[ESS]".equals(scanResult.capabilities)) {//不要密码
            configuration = wifiUtil.createWifiInfo(scanResult.capabilities, scanResult.SSID, passwordEt.getText().toString());
        } else if (wifiUtil.IsExsits(scanResult.SSID) != null) {//有保存的可以不需要修改密码
            configuration = wifiUtil.IsExsits(scanResult.SSID);
        } else {
            //条件不足,密码不全
            return;
        }

            if (proxySw.isChecked()) {
                if (wifiUtil.isConnectWifi() && scanResult.SSID.equals(wifiUtil.getSSID())) {
                    wifiUtil.setWifiProxySettings(configuration, proxyHostNameEt.getText().toString(), Integer.valueOf(proxyPortEt.getText().toString()), proxyFilterEt.getText().toString(), true);
                } else {
                    wifiUtil.setWifiProxySettings(configuration, proxyHostNameEt.getText().toString(), Integer.valueOf(proxyPortEt.getText().toString()), proxyFilterEt.getText().toString(),false);
                }
            } else {
                wifiUtil.unSetWifiProxySettings(configuration);
            }
            if (ipSettingSw.isChecked()) {
                wifiUtil.setIp(ipAddressEt.getText().toString(),
                        Integer.parseInt(TextUtils.isEmpty(preLengthEt.getText().toString()) ? "24" : preLengthEt.getText().toString()),
                        gatewayEt.getText().toString(), dns1Et.getText().toString(), dns2Et.getText().toString());
                //获得当前在已经连接的wifi配置对象
                wifiUtil.confingStaticIp(configuration);
            } else {

            }
//                boolean editResult = wifiUtil.updateNetwork(wifiUtil.createWifiInfo(scanResult.SSID,passwordEt.getText().toString(),WifiUtil.WIFICIPHER_WPA));
//                if (editResult) {
//                    Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    Toast.makeText(this, "修改???", Toast.LENGTH_SHORT).show();
//                }
    }
}
