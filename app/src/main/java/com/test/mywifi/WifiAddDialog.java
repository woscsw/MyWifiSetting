package com.test.mywifi;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.test.mywifi.utils.WifiUtil;

/**
 * Created by Admin on 2017/7/24.
 */

public class WifiAddDialog extends AlertDialog implements View.OnClickListener {

    private EditText ssidEt;
    private EditText passwordEt;
    private RadioGroup typeRG;
    private View cancleBtn;
    private View saveBtn;
    private View passwordV;
    private int type = WifiUtil.WIFICIPHER_NOPASS;
    private WifiUtil wifiUtil;
    private Context context;
    protected WifiAddDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setView(new EditText(context));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ldklz_carcool_wifi_dialog_wifi_add);
        wifiUtil = new WifiUtil(context);
        ssidEt = (EditText) findViewById(R.id.et_wifi_ssid);
        passwordEt = (EditText) findViewById(R.id.et_password);
        typeRG = (RadioGroup) findViewById(R.id.rg_type);
        cancleBtn = findViewById(R.id.btn_cancle);
        saveBtn =  findViewById(R.id.btn_save);
        saveBtn.setAlpha(0.8f);
        saveBtn.setEnabled(false);
        passwordV =  findViewById(R.id.ll_password);
        cancleBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        typeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_null:
                        passwordV.setVisibility(View.GONE);
                        type = WifiUtil.WIFICIPHER_NOPASS;
                        if (ssidEt.getText().length() > 0) {
                            saveBtn.setAlpha(1);
                            saveBtn.setEnabled(true);
                        }
                        break;
                    case R.id.rb_wep:
                        if (ssidEt.getText().length()<1||passwordEt.getText().length()<8) {
                            saveBtn.setAlpha(0.8f);
                            saveBtn.setEnabled(false);
                        }
                        passwordV.setVisibility(View.VISIBLE);
                        type = WifiUtil.WIFICIPHER_WEP;
                        break;
                    case R.id.rb_wpa:
                        if (ssidEt.getText().length()<1||passwordEt.getText().length()<8) {
                            saveBtn.setAlpha(0.8f);
                            saveBtn.setEnabled(false);
                        }
                        type = WifiUtil.WIFICIPHER_WPA;
                        passwordV.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_802://
                        if (ssidEt.getText().length()<1||passwordEt.getText().length()<8) {
                            saveBtn.setAlpha(0.8f);
                            saveBtn.setEnabled(false);
                        }
                        passwordV.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        ssidEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1) {
                    saveBtn.setAlpha(0.8f);
                    saveBtn.setEnabled(false);
                }
                if (type == WifiUtil.WIFICIPHER_NOPASS||passwordEt.getText().length()>7) {
                    if (s.length() > 0) {
                        saveBtn.setAlpha(1);
                        saveBtn.setEnabled(true);
                    }
                } 
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordEt.getVisibility()==View.VISIBLE) {
                    if (s.length() < 8) {
                        saveBtn.setAlpha(0.7f);
                        saveBtn.setEnabled(false);
                    } else {
                        if (ssidEt.getText().length() > 0) {
                            saveBtn.setAlpha(1);
                            saveBtn.setEnabled(true);
                        }
                    }
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle:
                dismiss();//finish();
                break;
            case R.id.btn_save://添加并连接
                if (wifiUtil.IsExsits(ssidEt.getText().toString()) == null) {
                    if(wifiUtil.addNetwork(wifiUtil.createWifiInfo(ssidEt.getText().toString(), passwordEt.getText().toString(), type))) {
//                    Toast.makeText(WifiAddDialog.this, "成功", Toast.LENGTH_SHORT).show();
                        dismiss();// finish();
                    } else {
                        Toast.makeText(context, context.getString(R.string.wifi_failure), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, ssidEt.getText().toString()+context.getString(R.string.wifi_existed), Toast.LENGTH_SHORT).show();
//                    wifiUtil.connectWifi(ssidEt.getText().toString());
                }
                
                
                break;
        }
    }
}
