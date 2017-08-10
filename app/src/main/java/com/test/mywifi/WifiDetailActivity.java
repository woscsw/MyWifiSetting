package com.test.mywifi;

import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.test.mywifi.model.WifiListModel;
import com.test.mywifi.utils.WifiUtil;


/**
 * Created by Admin on 2017/7/21.
 */

public class WifiDetailActivity extends BaseActivity implements View.OnClickListener {
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

    /**
     * 已连接		取消  取消保存    修改网络
     * 已保存		取消  取消保存    修改网络    连接
     * 未保存		取消  连接
     * <p>
     * 未连接时只显示  信号强度、安全性
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_detail);
        wifiUtil = new WifiUtil(this);
        Intent intent = getIntent();
        scanResult = intent.getParcelableExtra("data");
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
//        ((TextView) findViewById(R.id.tv_status_info)).setText(scanResult.);
        ((TextView) findViewById(R.id.tv_title)).setText(scanResult.SSID);
        ((TextView) findViewById(R.id.tv_signal_strength)).setText(wifiUtil.getSignalStrength(scanResult.level));
        

        connectLayout = findViewById(R.id.layout_connect);
        notConnectLayout = findViewById(R.id.layout_not_connect);
        connectBtn = notConnectLayout.findViewById(R.id.btn_connect);
//        ((TextView) findViewById(R.id.tv_ip_address)).setText();//未连接看不到?
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

            ((TextView) findViewById(R.id.tv_status_info)).setText("已连接");
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
        }else if (scanResult.capabilities.contains("WEP")) {
            stringBuilder.append("WEP");
        } else {
            if (TextUtils.isEmpty(scanResult.capabilities)||"[ESS]".equals(scanResult.capabilities)) {
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
                finish();
                break;
            case R.id.btn_cancle_save:
                boolean cancleBl = wifiUtil.cancleSave(scanResult.SSID);
                if (cancleBl) {
                    Toast.makeText(this, "取消保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "取消保存失败", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btn_connect:
                if (scanResult.capabilities.contains("WPA")) {
                    if (wifiUtil.IsExsits(scanResult.SSID) == null||passwordEt.getText().length()>=8) {
                        wifiUtil.addNetwork(wifiUtil.createWifiInfo(scanResult.SSID, passwordEt.getText().toString(), WifiUtil.WIFICIPHER_WPA));

                    } else {
                        wifiUtil.connectWifi(scanResult.SSID);
                    }
                } else if (scanResult.capabilities.contains("WEP")) {
                    if (wifiUtil.IsExsits(scanResult.SSID) == null||passwordEt.getText().length()>=8) {
                        wifiUtil.addNetwork(wifiUtil.createWifiInfo(scanResult.SSID, passwordEt.getText().toString(), WifiUtil.WIFICIPHER_WEP));

                    } else {
                        wifiUtil.connectWifi(scanResult.SSID);
                    }
                } else {
                    if (TextUtils.isEmpty(scanResult.capabilities)||"[ESS]".equals(scanResult.capabilities)) {
                        wifiUtil.addNetwork(wifiUtil.createWifiInfo(scanResult.SSID, passwordEt.getText().toString(), WifiUtil.WIFICIPHER_NOPASS));
                    }
                }
                
                finish();//无法得知wifi是否连接成功
                break;
            case R.id.btn_edit_wifi://不会自动连接网络,,,,没实现完
                Toast.makeText(this, "修改???", Toast.LENGTH_SHORT).show();
                if (wifiUtil.IsExsits(scanResult.SSID) != null) {
                    //有保存的可以不填密码

                }
                if (proxySw.isChecked()) {

                }
                if (ipSettingSw.isChecked()) {
                    wifiUtil.setIp(ipAddressEt.getText().toString(),
                            Integer.parseInt(TextUtils.isEmpty(preLengthEt.getText().toString()) ? "24" : preLengthEt.getText().toString()),
                            gatewayEt.getText().toString(), dns1Et.getText().toString(), dns2Et.getText().toString());
                    //获得当前在已经连接的wifi配置对象
                    WifiConfiguration configuration = wifiUtil.IsExsits(wifiUtil.getWifiInfo().getSSID());
                    wifiUtil.confingStaticIp(configuration);
                }
//                boolean editResult = wifiUtil.updateNetwork(wifiUtil.createWifiInfo(scanResult.SSID,passwordEt.getText().toString(),WifiUtil.WIFICIPHER_WPA));
//                if (editResult) {
//                    Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    Toast.makeText(this, "修改???", Toast.LENGTH_SHORT).show();
//                }
                break;
        }
    }
}
