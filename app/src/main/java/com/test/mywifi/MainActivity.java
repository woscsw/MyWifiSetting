package com.test.mywifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.test.mywifi.adapter.WifiListAdapter;
import com.test.mywifi.adapter.WifiListAdapterII;
import com.test.mywifi.utils.WifiUtil;

import static android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private ListView lvWifi;
    private TextView tvPage;
    private Switch wifiSwitch;
    private View tvWifiOff;
    private WifiListAdapterII wifiListAdapter;
    private WifiUtil wifiUtil;
    private View progressbarV;
    private final int VISIBLE_COUNT = 5;
    private int pageNum = 1;//记录当前页数
    private int pageSize = -1;//记录当前页数
    private boolean type = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiSwitch = (Switch) findViewById(R.id.switch_wifi);
        tvPage = (TextView) findViewById(R.id.tv_pageNum);
        wifiUtil = new WifiUtil(this);
        progressbarV =findViewById(R.id.progressbar);
        tvWifiOff = findViewById(R.id.tv_wifi_off);
        findViewById(R.id.btn_pre).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_scan).setOnClickListener(this);
        findViewById(R.id.btn_add_wifi).setOnClickListener(this);
        lvWifi = (ListView) findViewById(R.id.wifi_listview);
        lvWifi.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.i("setOnScrollListener","scrollState="+scrollState);
//                if (scrollState != 0) {
//                    type = true;
//                } else {
//                    type = false;
//                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.i("setOnScrollListener","firstVisibleItem="+firstVisibleItem+"---visibleItemCount="+visibleItemCount+"---totalItemCount="+totalItemCount );
                if (totalItemCount == 0||wifiListAdapter==null) {
                    return;
                }
//                if (visibleCount == -1) {
////                    visibleCount = visibleItemCount;//一页个数
//                    int yu = totalItemCount % visibleItemCount;
//                    if (yu == 0) {
//                        pageSize = totalItemCount / visibleItemCount;
//                    } else {
//                        pageSize =totalItemCount / visibleItemCount + 1;
//                    }
//                    Log.i("setOnScrollListener","pagesize="+pageSize );
//                }
                for (int i =1;i< wifiListAdapter.getMaxSize()+1;i++) {
                    if (firstVisibleItem == 0) {
                        pageNum = 1;
                        tvPage.setText(pageNum+"/"+ wifiListAdapter.getMaxSize());
                    }else if (i>1) {
                        if ((firstVisibleItem >= ((i - 2) * VISIBLE_COUNT + 1) & firstVisibleItem < (i - 1) * VISIBLE_COUNT + 1)) {
                            pageNum = i;
                            tvPage.setText(pageNum+"/"+ wifiListAdapter.getMaxSize());
                        }
                    }
                }
//                tvPage.setText(pageNum+"/"+pageSize);
            }
        });
        wifiListAdapter = new WifiListAdapterII(this);
        lvWifi.setAdapter(wifiListAdapter);
        if (wifiUtil.checkState() == WIFI_STATE_ENABLED) {
            if (wifiListAdapter != null && wifiUtil != null) {
                progressbarV.setVisibility(View.GONE);
                tvWifiOff.setVisibility(View.GONE);
                wifiListAdapter.setData(wifiUtil.getScanResult());
//                pageSize = wifiListAdapter.getMaxSize();
                tvPage.setText(wifiListAdapter.getPageNum() + "/" + wifiListAdapter.getMaxSize());
            }

            wifiSwitch.setChecked(true);
        } else {
            progressbarV.setVisibility(View.GONE);
            tvWifiOff.setVisibility(View.VISIBLE);
            wifiSwitch.setChecked(false);
        }
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    wifiSwitch.setEnabled(false);
                    wifiUtil.openWifi();
                    progressbarV.setVisibility(View.VISIBLE);
                    tvWifiOff.setVisibility(View.GONE);
                }
                else {
                    wifiSwitch.setEnabled(false);
                    wifiUtil.closeWifi();
                    wifiListAdapter.setData(null);
                    tvPage.setText("");
                    progressbarV.setVisibility(View.GONE);
                    tvWifiOff.setVisibility(View.VISIBLE);
                }
            }
        });
        
        
        
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // 表示正在创建连接的状态发生改变，又有了新的连接可用，
        // 可用获取到WiFi的具体的连接状态，如果你只是对连接的整体状态感兴趣则该广播无用
        wifiFilter.addAction(SUPPLICANT_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiFilter.addAction("com.test.mywifi.MainActivity");
        registerReceiver(mReceiver, wifiFilter);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wifiUtil.checkState() == WIFI_STATE_ENABLED) {
            if (wifiListAdapter != null && wifiUtil != null) {
                wifiListAdapter.setData(wifiUtil.getScanResult());
//                visibleCount = -1;
                pageNum = 1;
//                pageSize = 1;
                type = true;
                lvWifi.setSelection(0);
                lvWifi.scrollTo(0,0);
                tvPage.setText( "1/" + wifiListAdapter.getMaxSize());
            }

        } 
    }

    @Override
    protected void onDestroy() {
        if (mReceiver!=null)
            unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (wifiUtil.checkState() == WIFI_STATE_ENABLED) {
                    if (wifiListAdapter != null && wifiUtil != null) {
                        wifiListAdapter.setData(wifiUtil.getScanResult());
//                        tvPage.setText(wifiListAdapter.getPageNum()+"/"+wifiListAdapter.getMaxSize());
//                        visibleCount = -1;
                        pageNum = 1;
//                        pageSize = 1;
                        type = true;
                        lvWifi.setSelection(0);
                        lvWifi.scrollTo(0,0);
                    }
                }
                break;
            case R.id.btn_pre:
                if (wifiUtil.checkState() == WIFI_STATE_ENABLED&&wifiListAdapter != null) {
//                    wifiListAdapter.prePage();
//                    tvPage.setText(wifiListAdapter.getPageNum()+"/"+wifiListAdapter.getMaxSize());
//                    lvWifi.setSelection(0);
                    if (pageNum > 0) {
                        pageNum--;
                        if (pageNum <= 1) {
                            lvWifi.setSelection(0);
                            tvPage.setText("1/"+ wifiListAdapter.getMaxSize());
                        } else {
                            lvWifi.setSelection((pageNum-1)*VISIBLE_COUNT);
                            tvPage.setText(pageNum+"/"+ wifiListAdapter.getMaxSize());
                        }

                    }
                }
                break;
            case R.id.btn_next:
                if (wifiUtil.checkState() == WIFI_STATE_ENABLED&&wifiListAdapter != null) {
//                    wifiListAdapter.nextPage();
//                    tvPage.setText(wifiListAdapter.getPageNum()+"/"+wifiListAdapter.getMaxSize());
//                    lvWifi.setSelection(0);
                    if (pageNum < wifiListAdapter.getMaxSize()) {
                        pageNum++;
//                        if (pageNum == 1) {
//                            lvWifi.setSelection(0);
//                        } else {
                            lvWifi.setSelection((pageNum-1)*VISIBLE_COUNT);
//
//                        }
                        tvPage.setText(pageNum+"/"+ wifiListAdapter.getMaxSize());
                    }
                }
                break;
            case R.id.btn_add_wifi:
                if (wifiUtil.checkState() == WIFI_STATE_ENABLED) {
                    Intent intent = new Intent(this,WifiAddActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // wifi已成功扫描到可用wifi。
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                progressbarV.setVisibility(View.GONE);
                if (wifiListAdapter != null && wifiUtil != null) {
                    wifiListAdapter.setData(wifiUtil.getScanResult());
                    tvPage.setText(wifiListAdapter.getPageNum() + "/" + wifiListAdapter.getMaxSize());
                }
            }
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {//wifi连接上与否  
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    System.out.println("wifi网络连接断开");
                    Toast.makeText(MainActivity.this, "wifi已断开", Toast.LENGTH_SHORT).show();
                    wifiListAdapter.reSetConnectingSSID();
                    wifiListAdapter.notifyDataSetChanged();
                    if (wifiSwitch.isChecked()) {
                        tvPage.setText(wifiListAdapter.getPageNum() + "/" + wifiListAdapter.getMaxSize());
                    } else {
                        tvPage.setText("");
                        tvWifiOff.setVisibility(View.VISIBLE);
                    }
                    progressbarV.setVisibility(View.GONE);
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {

                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    //获取当前wifi名称  
                    System.out.println("连接到网络 " + wifiInfo.getSSID());
                    progressbarV.setVisibility(View.GONE);
                    tvWifiOff.setVisibility(View.GONE);
                    wifiListAdapter.reSetConnectingSSID();
                    wifiListAdapter.removeErrorSSID(wifiInfo.getSSID());
                    if (wifiListAdapter != null && wifiUtil != null) {
                        wifiListAdapter.setData(wifiUtil.getScanResult());
                        tvPage.setText(wifiListAdapter.getPageNum() + "/" + wifiListAdapter.getMaxSize());
                    }
                    tvPage.setText(wifiListAdapter.getPageNum() + "/" + wifiListAdapter.getMaxSize());
                    Toast.makeText(MainActivity.this, "wifi已连接", Toast.LENGTH_SHORT).show();
                } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {//连接中
//                    if (progressbarV.getVisibility()==View.GONE)
//                        progressbarV.setVisibility(View.VISIBLE);
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    wifiListAdapter.setConnectingSSID(wifiInfo.getSSID());
                }
            }
            if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                switch (intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,
                        -999)) {
                    case WifiManager.ERROR_AUTHENTICATING://密码错误
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        System.out.println("密码错误"+wifiInfo.getSSID());
                        Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                        wifiListAdapter.setErrorSSID(wifiInfo.getSSID());
                        break;
                }
            }

            //--------------------------------------------------------------------
            //当WiFi被打开、关闭、正在打开、正在关闭或者位置状态即wifi状态发生改变时系统会自动发送该广播，
            // 该广播会附带有两个值，一个是int型表示改变后的state，可通过字段EXTRA_WIFI_STATE获取，
            // 还有一个是int型的改变前的state（如果有的话）可通过字段EXTRA_PREVIOUS_WIFI_STATE获取
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN));
            }
            //WiFi扫描结束时系统会发送该广播，用户可以监听该广播通过调用WifiManager的getScanResults方法来获取到扫描结果
            else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                    "android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action) ||
                    "android.net.wifi.LINK_CONFIGURATION_CHANGED".equals(action)) {
                updateAccessPoints();//更新WiFi列表
            }
//                if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
//                    //连接发生改变时的更新，WifiManager.EXTRA_NEW_STATE存放改变后的状态  
//                    if (!mConnected.get()) {
//                        handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
//                                intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
//                    }
//                } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
//                    //网络状态发生改变时的更新  
//                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
//                            WifiManager.EXTRA_NETWORK_INFO);
//                    mConnected.set(info.isConnected());
//                    handleStateChanged(info.getDetailedState());
//                }
            
        }

        private void updateAccessPoints() {
            // Safeguard from some delayed event handling  

//            final int wifiState = mWifiManager.getWifiState();
//
//            switch (wifiState) {
//                case WifiManager.WIFI_STATE_ENABLED:
//                    // AccessPoints are automatically sorted with TreeSet.  
//                    //获取到接入点列表  
//                    final Collection<AccessPoint> accessPoints = constructAccessPoints();
//                    if (!getResources().getBoolean(R.bool.set_wifi_priority)) {
//                        getPreferenceScreen().removeAll();
//                    }
//                    if(accessPoints.size() == 0) {
//                        addMessagePreference(R.string.wifi_empty_list_wifi_on);
//                    }
//                    if (!getResources().getBoolean(R.bool.set_wifi_priority)) {
//                        for (AccessPoint accessPoint : accessPoints) {
//                                    //WiFisettings的xml文件的根节点为preferencescreen，所以通过如下方法添加  
//                                    preferencegetPreferenceScreen().addPreference(accessPoint); } }
//                    if (accessPoints.isEmpty()){ addMessagePreference(R.string.wifi_empty_list_wifi_on); }
//                    break; 
//                case WifiManager.WIFI_STATE_ENABLING://如果WiFi处于正在打开的状态，则清除列表 。。。。。。。。。 
//                     } 
        }

        private void updateWifiState(int state) {

            switch (state) {
                case WifiManager.WIFI_STATE_ENABLED://打开WiFi  
                    wifiSwitch.setEnabled(true);
                    wifiSwitch.setChecked(true);
                    return; // not break, to avoid the call to pause() below  

                case WifiManager.WIFI_STATE_ENABLING://正在打开WiFi  
                    wifiSwitch.setEnabled(false);
                    break;
                case WifiManager.WIFI_STATE_DISABLING://正在关闭WiFi  
                    wifiSwitch.setEnabled(false);
                    break;
                case WifiManager.WIFI_STATE_DISABLED://关闭WiFi  
                    //用户可以在wlan-->高级选项中去设置时是否随时都可以扫描（关闭WiFi后也可以扫描），根据用户的选择，  
                    //设置在关闭WLAN后显示界面上的文本  
//                    setOffMessage();
                    wifiSwitch.setEnabled(true);
                    wifiSwitch.setChecked(false);
                    wifiListAdapter.removeAllErrorSSID();
                    break;
            }

        }
    };


}
