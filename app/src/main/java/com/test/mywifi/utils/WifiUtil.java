package com.test.mywifi.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ProxyInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.test.mywifi.model.WifiListModel;
import com.test.mywifi.model.WifiListModel.ScanResultModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Admin on 2017/7/21.
 * <p>
 * WifiConfiguration.SSID   WifiInfo.SSID默认带有""
 * ScanResult.SSID 不带""
 */

public class WifiUtil {
    private static final String TAG = WifiUtil.class.getSimpleName();
    private Context mContext;
    public static final int WIFICIPHER_NOPASS = 1;
    public static final int WIFICIPHER_WEP = 2;
    public static final int WIFICIPHER_WPA = 3;
    // 定义WifiManager对象  
    private WifiManager mWifiManager;
    // 定义WifiInfo对象  
    private WifiInfo mWifiInfo;

    public WifiUtil(Context context) {
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        this.mContext = context;
    }

    /**
     * 是否有保存信息
     */
    public boolean isSave(String wifiSSID) {
        List<WifiConfiguration> wifiConfigurationList = mWifiManager.getConfiguredNetworks();
        if (wifiConfigurationList != null && wifiConfigurationList.size() != 0) {
            for (int i = 0; i < wifiConfigurationList.size(); i++) {
                WifiConfiguration wifiConfiguration = wifiConfigurationList.get(i);
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.substring(1, wifiConfiguration.SSID.length() - 1).equals(wifiSSID)) {
                    return wifiConfiguration.networkId != -1;
                }
            }
        }
        return false;
    }

    /**
     * 是否存在该wifi的信息
     */
    public WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }


    public boolean cancleSave(String wifiSSID) {
        WifiConfiguration configuration = IsExsits(wifiSSID);
        int networkId = configuration == null ? -1 : configuration.networkId;
        if (networkId == -1) {
            return false;
        }
        return mWifiManager.removeNetwork(networkId);
    }

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
            mWifiManager.startScan();
        }
    }

    // 关闭WIFI  
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 添加一个网络并连接
     */
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        if (wcgID != -1) {
            boolean b = mWifiManager.enableNetwork(wcgID, true);
            mWifiInfo = mWifiManager.getConnectionInfo();
            System.out.println("添加网络*****  " + wcgID);
            System.out.println("连接网络----" + b);
            return b;
        } else {
            System.out.println("添加网络 错误");
            return false;
        }
    }

    // 得到接入点的SSID  ,mWifiInfo.getSSID()返回的String 头尾带有"
    public String getSSID() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID().substring(1, mWifiInfo.getSSID().length() - 1);
    }

    /**
     * 连接已保存的wifi
     */
    public boolean connectWifi(String wifiSSID) {
        List<WifiConfiguration> wifiConfigurationList = mWifiManager.getConfiguredNetworks();
        if (wifiConfigurationList != null && wifiConfigurationList.size() != 0) {
            for (int i = 0; i < wifiConfigurationList.size(); i++) {
                WifiConfiguration wifiConfiguration = wifiConfigurationList.get(i);
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.substring(1, wifiConfiguration.SSID.length() - 1).equals(wifiSSID)) {
                    if (wifiConfiguration.networkId != -1) {
                        boolean b = mWifiManager.enableNetwork(wifiConfiguration.networkId, true);
                        mWifiInfo = mWifiManager.getConnectionInfo();
                        return b;
                    } else {
                        return false;
                    }

                }
            }
        }
        return false;
    }
    //会清掉已保存的wifi，小心
    public WifiConfiguration createWifiInfo(String capabilities, String SSID, String Password) {
        if (capabilities.contains("WPA")) {
            return createWifiInfo(SSID, Password, WifiUtil.WIFICIPHER_WPA);
        } else if (capabilities.contains("WEP")) {
            return createWifiInfo(SSID, Password, WifiUtil.WIFICIPHER_WEP);
        } else if (TextUtils.isEmpty(capabilities) || "[ESS]".equals(capabilities)) {
            return createWifiInfo(SSID, Password, WifiUtil.WIFICIPHER_NOPASS);
        }
        return null;
    }

    /**
     * 创建保存网络信息的类
     */
    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == WIFICIPHER_NOPASS) {

            config.preSharedKey = null;
            config.wepKeys[0] = "\"" + "\"";
            config.wepTxKeyIndex = 0;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.clear();
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        }
        if (Type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

        }
        if (Type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

        }
        return config;
    }

    // 检查当前WIFI状态  
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    /**
     * 判断手机是否连接在Wifi上
     */
    public boolean isConnectWifi() {
        // 获取ConnectivityManager对象
        ConnectivityManager conMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取NetworkInfo对象
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        // 获取连接的方式为wifi
        NetworkInfo.State wifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();

        if (info != null && info.isAvailable() && wifi == NetworkInfo.State.CONNECTED)

        {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 获取wifi列表....已连接的要置顶..
     */
    public List<ScanResultModel> getScanResult() {
        // 扫描的热点数据
        List<ScanResultModel> resultList = new ArrayList<>();
        // 开始扫描热点
        mWifiManager.startScan();
        List<ScanResult> scanList = mWifiManager.getScanResults();

        for (ScanResult sr : scanList) {
            ScanResultModel model = new WifiListModel.ScanResultModel();
            model.capabilities = sr.capabilities;
            model.level = sr.level;
            model.SSID = sr.SSID;
            resultList.add(model);
        }
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        List<WifiConfiguration> eCache = new ArrayList<>();
        if (existingConfigs != null) {
            for (int i = 0; i < existingConfigs.size(); i++) {
                boolean type = true;
                for (ScanResult sr : scanList) {
                    if (existingConfigs.get(i).SSID.equals("\"" + sr.SSID + "\"")) {
                        type = false;
                    }
                }
                if (type) {
                    eCache.add(existingConfigs.get(i));
                }
            }
            if (eCache.size() > 0) {
                for (WifiConfiguration ww : eCache) {
                    //这都是搜索不到的wifi
                    ScanResultModel model = new ScanResultModel();
                    model.capabilities = getSecurity(ww);
                    model.level = -180;
                    model.SSID = ww.SSID.substring(1, ww.SSID.length() - 1);
                    resultList.add(model);
                }
            }
        }
        sortByLevel(resultList);
        if (isConnectWifi()) {
            //如果有已连接，找出来放到第一个
            String ssid = getSSID();
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).SSID.equals(ssid)) {
                    ScanResultModel srm = resultList.get(i);
                    resultList.remove(i);
                    resultList.add(0, srm);
                    break;
                }
            }
        }
        //边循环边删除
        for (int i = resultList.size() - 1; i >= 0; i--) {
            if (TextUtils.isEmpty(resultList.get(i).SSID)) {
                resultList.remove(resultList.get(i));
            }
        }
        return resultList;
    }


    static final String SECURITY_NONE = "";
    static final String SECURITY_WEP = "WEP";
    static final String SECURITY_PSK = "WPA-PSK-CCMP/WPA2-PSK-CCMP";
    static final String SECURITY_EAP = "EAP";

    /**
     * 获取安全性
     */
    static String getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    //    public void getList() {
//         List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
//        if (configs != null) {
//            for (WifiConfiguration config : configs) {
//                if (config.SSID != null) {
//                    AccessPoint accessPoint = new AccessPoint(getActivity(), config);
//                    accessPoint.update(mLastInfo, mLastState);
//                    accessPoints.add(accessPoint);
//                    apMap.put(accessPoint.ssid, accessPoint);
//                    if (getResources().getBoolean(R.bool.set_wifi_priority)) {
//                        SetAPCategory(accessPoint, mConfigedAP);
//                    }
//                }
//            }
//            if (getResources().getBoolean(R.bool.set_wifi_priority)) {
//                if (mConfigedAP != null && mConfigedAP.getPreferenceCount() == 0) {
//                    getPreferenceScreen().removePreference(mConfigedAP);
//                }
//            }
//        }
//        //获取到WiFi扫描结果，返回附近可用WiFi，包括已经连接的或者已经保存的WiFi  
//        final List<ScanResult> results = mWifiManager.getScanResults();
//        if (results != null) {
//            for (ScanResult result : results) {
//                // Ignore hidden and ad-hoc networks.  
//                if (result.SSID == null || result.SSID.length() == 0 ||
//                        result.capabilities.contains("[IBSS]")) {
//                    continue;
//                }
//
//                boolean found = false;
//                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
//                    if (accessPoint.update(result))
//                        found = true;
//                }
//                if (!found) {
//                    AccessPoint accessPoint = new AccessPoint(getActivity(), result);
//                    accessPoints.add(accessPoint);
//                    apMap.put(accessPoint.ssid, accessPoint);
//                    if (getResources().getBoolean(R.bool.set_wifi_priority)) {
//                        SetAPCategory(accessPoint, mUnKnownAP);
//                    }
//                }
//            }
//            if (getResources().getBoolean(R.bool.set_wifi_priority)) {
//                if(mUnKnownAP !=null && mUnKnownAP.getPreferenceCount() == 0){
//                    getPreferenceScreen().removePreference(mUnKnownAP);
//                }
//            }
//        }
//
//        // Pre-sort accessPoints to speed preference insertion  
//        sortByLevel(accessPoints);
//        return accessPoints;
//    }
    private void sortByLevel(List<ScanResultModel> list) {

        Collections.sort(list, new Comparator<ScanResultModel>() {

            @Override
            public int compare(ScanResultModel lhs, ScanResultModel rhs) {

                return rhs.level - lhs.level;
            }
        });
    }

    /**
     * 获取wifi的信号强度
     */
    public String getSignalStrength(int rssi) {
        int level = WifiManager.calculateSignalLevel(rssi, 5);
        switch (level) {
            case 0:
                return "无信号";
            case 1:
                return "较差";
            case 2:
                return "一般";
            case 3:
                return "较强";
            case 4:
                return "强";
            default:
                return "无信号";
        }
    }

    public WifiInfo getWifiInfo() {
        return mWifiInfo;
    }

    // 得到IP地址
    public String getIPAddress() {
        return mWifiInfo == null ? null : intToIp(mWifiInfo.getIpAddress());
    }

    // 得到IP地址
    public String getLinkSpeed() {
        return mWifiInfo == null ? null : mWifiInfo.getLinkSpeed() + "Mbps";
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "."
                + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

    public boolean addWifiInfo(String ssid, String password, int type) {
        int i = mWifiManager.addNetwork(createWifiInfo(ssid, password, type));
        return i != -1;
    }

    /**
     * 设置代理
     *
     * @param config
     * @param host
     * @param port
     * @param exclList
     * @param isReConnect 是否要断开重连
     */
    public void setWifiProxySettings(WifiConfiguration config, String host, int port, String exclList, boolean isReConnect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String str2 = exclList.trim();
            List<String> list = Arrays.asList(str2.split(","));
            setWifiProxySettingsFor21And(config, host, port, list);
        } else {
            setWifiProxySettingsFor19And(config, host, port, exclList);
        }
        if (isReConnect) {
            mWifiManager.disconnect();
            mWifiManager.reconnect();
        }
    }

    /**
     * 取消代理
     *
     * @param config
     */
    public void unSetWifiProxySettings(WifiConfiguration config) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unSetWifiProxySettingsFor21And(config);
        } else {
            unSetWifiProxySettingsFor19And(config);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setWifiProxySettingsFor21And(WifiConfiguration config, String host, int port, List<String> exclList) {
        ProxyInfo mInfo = ProxyInfo.buildDirectProxy(host, port);
        if (config != null) {
            try {
                Class clazz = Class.forName("android.net.wifi.WifiConfiguration");
                Class parmars = Class.forName("android.net.ProxyInfo");
                Method method = clazz.getMethod("setHttpProxy", parmars);
                method.invoke(config, mInfo);
                Object mIpConfiguration = getDeclaredFieldObject(config, "mIpConfiguration");

                setEnumField(mIpConfiguration, "STATIC", "proxySettings");
                setDeclardFildObject(config, "mIpConfiguration", mIpConfiguration);
                //save the settings
                mWifiManager.updateNetwork(config);

            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

        }

    }

    private void setWifiProxySettingsFor19And(WifiConfiguration config, String host, int port, String exclList) {
        try {
            Object linkProperties = getFieldObject(config, "linkProperties");
            if (null == linkProperties) return;
            //获取类 LinkProperties的setHttpProxy方法
            Class<?> proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class<?>[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class<?> lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);
            // 获取类 ProxyProperties的构造函数
            Constructor<?> proxyPropertiesCtor = proxyPropertiesClass.getConstructor(String.class, int.class, String.class);
            // 实例化类ProxyProperties
            Object proxySettings = proxyPropertiesCtor.newInstance(host, port, exclList);
            //pass the new object to setHttpProxy
            Object[] params = new Object[1];
            params[0] = proxySettings;
            setHttpProxy.invoke(linkProperties, params);
            setEnumField(config, "STATIC", "proxySettings");

            //save the settings
            mWifiManager.updateNetwork(config);
        } catch (Exception e) {
        }
    }

    private Object getFieldObject(Object obj, String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    // 取消代理设置
    private void unSetWifiProxySettingsFor19And(WifiConfiguration config) {
        try {
            //get the link properties from the wifi configuration
            Object linkProperties = getFieldObject(config, "linkProperties");
            if (null == linkProperties) return;
            //get the setHttpProxy method for LinkProperties
            Class<?> proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class<?>[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class<?> lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);
            //pass null as the proxy
            Object[] params = new Object[1];
            params[0] = null;
            setHttpProxy.invoke(linkProperties, params);
            setEnumField(config, "NONE", "proxySettings");
            //save the config
            mWifiManager.updateNetwork(config);
            mWifiManager.disconnect();
            mWifiManager.reconnect();
        } catch (Exception e) {
        }
    }

    /**
     * 取消代理设置
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void unSetWifiProxySettingsFor21And(WifiConfiguration configuration) {
        ProxyInfo mInfo = ProxyInfo.buildDirectProxy(null, 0);
        if (configuration != null) {
            try {
                Class clazz = Class.forName("android.net.wifi.WifiConfiguration");
                Class parmars = Class.forName("android.net.ProxyInfo");
                Method method = clazz.getMethod("setHttpProxy", parmars);
                method.invoke(configuration, mInfo);
                Object mIpConfiguration = getDeclaredFieldObject(configuration, "mIpConfiguration");
                setEnumField(mIpConfiguration, "NONE", "proxySettings");
                setDeclardFildObject(configuration, "mIpConfiguration", mIpConfiguration);
                //保存设置
                mWifiManager.updateNetwork(configuration);
                mWifiManager.disconnect();
                mWifiManager.reconnect();
            } catch (InvocationTargetException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    //------------------------------以下未转正-----------------------------------------------------
    public void getProxy(WifiConfiguration configuration) {
    }
    public static String[] getUserProxy(Context context) {
        Method method = null;
        try {
            method = ConnectivityManager.class.getMethod("getProxy");
        } catch (NoSuchMethodException e) {
            // Normal situation for pre-ICS devices
            return null;
        } catch (Exception e) {
            return null;
        }

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Object pp = method.invoke(connectivityManager);
            if (pp == null)
                return null;

            return getUserProxy(pp);
        } catch (Exception e) {
            return null;
        }
    }


    private static String[] getUserProxy(Object pp) throws Exception {
        String[] userProxy = new String[3];

        String className = "android.net.ProxyProperties";
        Class<?> c = Class.forName(className);
        Method method;

        method = c.getMethod("getHost");
        userProxy[0] = (String) method.invoke(pp);

        method = c.getMethod("getPort");
        userProxy[1] = String.valueOf((Integer) method.invoke(pp));


        method = c.getMethod("getExclusionList");
        userProxy[2] = (String) method.invoke(pp);

        if (userProxy[0] != null)
            return userProxy;
        else
            return null;
    }


    public static Object getDeclaredFieldObject(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    public static void setDeclardFildObject(Object obj, String name, Object object) {
        Field f = null;
        try {
            f = obj.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        f.setAccessible(true);
        try {
            f.set(obj, object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }


    String ipAddress = "192.168.1.148";
    int preLength = 24;
    String getWay = "192.168.1.1";
    String dns1 = "192.168.1.1";
    String dns2 = "61.134.1.9";


    public void setIp(String ipAddress, int preLength,
                      String getWay, String dns1, String dns2) {
        this.ipAddress = ipAddress;
        this.preLength = preLength;
        this.getWay = getWay;
        this.dns1 = dns1;
        this.dns2 = dns2;
    }

    //    String netmaskIpS=long2ip(mWifiManager.getDhcpInfo().netmask);//子网掩码地址
    //网关地址
    public String getGateway() {
        return long2ip(mWifiManager.getDhcpInfo().gateway);
    }

    String long2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }

    // 配置wifi的静态ip
    public void confingStaticIp(WifiConfiguration wifiConfig) {
        // 如果是android2.x版本的话
        if (android.os.Build.VERSION.SDK_INT < 11) {
            ContentResolver ctRes = mContext.getContentResolver();
            Settings.System
                    .putInt(ctRes, Settings.System.WIFI_USE_STATIC_IP, 1);
            Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_IP,
                    ipAddress);
            Settings.System.putString(ctRes,
                    Settings.System.WIFI_STATIC_NETMASK, "255.255.255.0");
            Settings.System.putString(ctRes,
                    Settings.System.WIFI_STATIC_GATEWAY, getWay);
            Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_DNS1,
                    dns1);
            Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_DNS2,
                    dns2);
        }
        // 如果是android3.x版本及以上的话
        else {
            try {
                setIpType("STATIC", wifiConfig);
                setIpAddress(InetAddress.getByName(ipAddress), preLength,
                        wifiConfig);
                setGateway(InetAddress.getByName(getWay), wifiConfig);
                setDNS(InetAddress.getByName(dns1), wifiConfig);
                Log.i(TAG, "静态ip设置成功！");
                Toast.makeText(mContext, "静态ip设置成功！", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "静态ip设置失败！");
                Toast.makeText(mContext, "静态ip设置失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 设置DNS
     *
     * @param wifiConfig 操作的wifi配置对象
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     */
    private void setDNS(InetAddress dns, WifiConfiguration wifiConfig) {
        // 获得wifiConfig中linkProperties连接属性集合中的值
        Object linkProperties = null;
        try {
            linkProperties = getFieldValue(wifiConfig, "linkProperties");
            if (linkProperties == null) {
                return;
            }
            ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(
                    linkProperties, "mDnses");
            mDnses.clear(); // 清除原有DNS设置（如果只想增加，不想清除，此句可省略）
            mDnses.add(dns); // 增加新的DNS
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置网关
     *
     * @param gateWay    网关
     * @param wifiConfig 操作的wifi配置对象
     */
    private void setGateway(InetAddress gateWay, WifiConfiguration wifiConfig) {
        // 获得wifiConfig中linkProperties连接属性集合中的值
        Object linkProperties = null;
        try {
            linkProperties = getFieldValue(wifiConfig, "linkProperties");
            if (linkProperties == null) {
                return;
            }
            // android4.x版本
            if (android.os.Build.VERSION.SDK_INT >= 14) {

                // 获得了路由信息类
                Class<?> routeInfoClass = Class.forName("android.net.RouteInfo");
                // 获得路由信息类的一个构造器，参数是网关
                Constructor<?> routeInfoConstructor = routeInfoClass
                        .getConstructor(new Class[]{InetAddress.class});
                // 生成指定网关的路由信息类对象
                Object routeInfo = routeInfoConstructor.newInstance(gateWay);
                ArrayList<Object> routes = (ArrayList<Object>) getDeclaredField(
                        linkProperties, "mRoutes");
                routes.clear();
                routes.add(routeInfo);
            }
            // android3.x版本
            else {
                ArrayList<InetAddress> gateWays = (ArrayList<InetAddress>) getDeclaredField(
                        linkProperties, "mGateWays");
                gateWays.clear();
                gateWays.add(gateWay);
            }
        } catch (NoSuchFieldException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置IP地址
     *
     * @param ipAddress  IP地址
     * @param preLength  前缀长度
     * @param wifiConfig 操作的wifi配置对象
     */
    private void setIpAddress(InetAddress ipAddress, int preLength,
                              WifiConfiguration wifiConfig) {
        try {
            // 获得wifiConfig中linkProperties连接属性集合中的值
            Object linkProperties = getFieldValue(wifiConfig, "linkProperties");
            if (linkProperties == null) {
                return;
            }
            // 获得一个LinkAddress链接地址类
            Class<?> linkAddressClass = Class.forName("android.net.LinkAddress");
            // 获得LinkAddress的一个构造器,参数为ip地址和前缀长度
            Constructor<?> linkAddressConstrcutor = linkAddressClass
                    .getConstructor(new Class[]{InetAddress.class, int.class});
            // 通过该构造器获得一个LinkAddress对象
            Object linkAddress = linkAddressConstrcutor.newInstance(ipAddress,
                    preLength);
            // 获得linkProperties连接属性集合中linkAddresses连接地址集合中的值

            ArrayList<Object> linkAddresses = (ArrayList<Object>) getDeclaredField(linkProperties, "mLinkAddresses");
            // 清空linkAddresses
            linkAddresses.clear();
            // 添加用户设置的linkAddress链接地址。
            linkAddresses.add(linkAddress);
        } catch (Exception e) {
        }


    }

    /**
     * 获得某对象中指定区域中的值。该区域是被声明过的。
     *
     * @param obj  对象
     * @param name 区域名
     * @return 返回该对象中该区域中的值
     * @throws Exception
     */
    private Object getDeclaredField(Object obj, String name) {
        // 获obj类中指定区域名为name的区域
        Field f = null;
        try {
            f = obj.getClass().getDeclaredField(name);
            // 设置该区域可以被访问
            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得某对象中指定区域中的值。根据对象，和区域名。
     *
     * @param obj  对象
     * @param name 区域名
     * @return 返回该对象中名为name区域中的值
     * @throws Exception
     */
    private Object getFieldValue(Object obj, String name) throws Exception {
        // 获得obj的类，再获得区域名为name的区域
        Field f = obj.getClass().getField(name);
        // 返回obj中f区域中的值
        return f.get(obj);
    }

    /**
     * 设置IP地址类型
     *
     * @param ipType     IP地址类型
     * @param wifiConfig 操作的wifi配置对象
     * @throws Exception
     */
    private void setIpType(String ipType, WifiConfiguration wifiConfig)
            throws Exception {
        Field f = wifiConfig.getClass().getField("ipAssignment");
        f.set(wifiConfig, Enum.valueOf((Class<Enum>) f.getType(), ipType));
    }


    /**
     * 修改网络
     */
    public boolean updateNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.updateNetwork(wcg);
        System.out.println("修改网络(-1为错误)*****  " + wcgID);
        return wcgID != -1;
    }

//    public static void setIpAssignment(String assign, WifiConfiguration wifiConf)
//            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
//        setEnumField(wifiConf, assign, "ipAssignment");
//    }

//    public static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
//            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
//            NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException {
//        Object linkProperties = getField(wifiConf, "linkProperties");
//        if (linkProperties == null) return;
//        Class laClass = Class.forName("android.net.LinkAddress");
//        Constructor laConstructor = laClass.getConstructor(new Class[]{InetAddress.class, int.class});
//        Object linkAddress = laConstructor.newInstance(addr, prefixLength);
//        ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties, "mLinkAddresses");
//        mLinkAddresses.clear();
//        mLinkAddresses.add(linkAddress);
//    }

//    public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf) {//throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
//        //ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException
//        Object linkProperties = null;
//        try {
//            linkProperties = getField(wifiConf, "linkProperties");
//            if (linkProperties == null) return;
//            Class routeInfoClass = Class.forName("android.net.RouteInfo");
//            Constructor routeInfoConstructor = routeInfoClass.getConstructor(new Class[]{InetAddress.class});
//            Object routeInfo = routeInfoConstructor.newInstance(gateway);
//            ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties, "mRoutes");
//            mRoutes.clear();
//            mRoutes.add(routeInfo);
//        } catch (NoSuchFieldException e1) {
//            e1.printStackTrace();
//        } catch (IllegalAccessException e1) {
//            e1.printStackTrace();
//        } catch (NoSuchMethodException e1) {
//            e1.printStackTrace();
//        } catch (InstantiationException e1) {
//            e1.printStackTrace();
//        } catch (InvocationTargetException e1) {
//            e1.printStackTrace();
//        } catch (ClassNotFoundException e1) {
//            e1.printStackTrace();
//        }
//
//    }

//    public static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
//            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
//        Object linkProperties = getField(wifiConf, "linkProperties");
//        if (linkProperties == null) return;
//        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
//        mDnses.clear(); //or add a new dns address , here I just want to replace DNS1
//        mDnses.add(dns);
//    }
//
//    public static Object getField(Object obj, String name)
//            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//        Field f = obj.getClass().getField(name);
//        Object out = f.get(obj);
//        return out;
//    }

//    public static Object getDeclaredField(Object obj, String name) {
//        Field f = null;
//        try {
//            f = obj.getClass().getDeclaredField(name);
//            f.setAccessible(true);
//            Object out = f.get(obj);
//            return out;
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//    }

//    public static void setEnumField(Object obj, String value, String name)
//            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//        Field f = obj.getClass().getField(name);
//        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
//    }

    //    try{
//        setIpAssignment("STATIC", wifiConf); //or "DHCP" for dynamic setting
//        setIpAddress(InetAddress.getByName("192.168.0.100"), 24, wifiConf);
//        setGateway(InetAddress.getByName("4.4.4.4"), wifiConf);
//        setDNS(InetAddress.getByName("4.4.4.4"), wifiConf);
//        wifiManager.updateNetwork(wifiConf); //apply the setting
//    }catch(Exception e){
//        e.printStackTrace();
//    }
//    public static void setGateway2(InetAddress gateway, WifiConfiguration wifiConf) {
//        //throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
////                ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException
//        Object linkProperties = null;
//        try {
//            linkProperties = getField(wifiConf, "linkProperties");
//            ArrayList mGateways = (ArrayList) getDeclaredField(linkProperties, "mGateways");
//            mGateways.clear();
//            mGateways.add(gateway);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        if (linkProperties == null) return;
//
//    }

    /**
     * 获取当前手机所连接的wifi信息
     */
    public WifiInfo getCurrentWifiInfo() {
        return mWifiManager.getConnectionInfo();
    }

//    /**
//     * 添加一个网络并连接
//     * 传入参数：WIFI发生配置类WifiConfiguration
//     */
//    public boolean addNetwork2(WifiConfiguration wcg) {
//        int wcgID = mWifiManager.addNetwork(wcg);
//        return mWifiManager.enableNetwork(wcgID, true);
//    }

    /**
     * 搜索附近的热点信息，并返回所有热点为信息的SSID集合数据
     */
    public List<String> getScanSSIDsResult() {
        // 扫描的热点数据
        List<ScanResult> resultList;
        // 开始扫描热点
        mWifiManager.startScan();
        resultList = mWifiManager.getScanResults();
        ArrayList<String> ssids = new ArrayList<String>();
        if (resultList != null) {
            for (ScanResult scan : resultList) {
                ssids.add(scan.SSID);// 遍历数据，取得ssid数据集
            }
        }
        return ssids;
    }


    /**
     * 得到手机搜索到的ssid集合，从中判断出设备的ssid（dssid）
     */
//    public List<String> accordSsid() {
//        List<String> s = getScanSSIDsResult();
//        List<String> result = new ArrayList<String>();
//        for (String str : s) {
//            if (checkDssid(str)) {
//                result.add(str);
//            }
//        }
//        return result;
//    }

    /**
     * 检测指定ssid是不是匹配的ssid，目前支持GBELL，TOP,后续可添加。
     *
     * @param ssid
     * @return
     */
    private boolean checkDssid(String ssid, String condition) {
        if (!TextUtils.isEmpty(ssid) && !TextUtils.isEmpty(condition)) {
            //这里条件根据自己的需求来判断，我这里就是随便写的一个条件
            if (ssid.length() > 8 && (ssid.substring(0, 8).equals(condition))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 连接wifi
     * 参数：wifi的ssid及wifi的密码
     */
//    public boolean connectWifiTest(final String ssid, final String pwd) {
//        boolean isSuccess = false;
//        boolean flag = false;
//        mWifiManager.disconnect();
//        boolean addSucess = addNetwork(CreateWifiInfo(ssid, pwd, 3));
//        if (addSucess) {
//            while (!flag && !isSuccess) {
//                try {
//                    Thread.sleep(10000);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//                String currSSID = getCurrentWifiInfo().getSSID();
//                if (currSSID != null)
//                    currSSID = currSSID.replace("\"", "");
//                int currIp = getCurrentWifiInfo().getIpAddress();
//                if (currSSID != null && currSSID.equals(ssid) && currIp != 0) {
//                    //这里还需要做优化处理，增强结果判断 
//                    isSuccess = true;
//                } else {
//                    flag = true;
//                }
//            }
//        }
//        return isSuccess;
//
//    }


    //--------------------------------------------------------------------
    // 网络连接列表  
    private List<WifiConfiguration> mWifiConfiguration;
    // 定义一个WifiLock  
    android.net.wifi.WifiManager.WifiLock mWifiLock;


    // 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock  
    public void releaseWifiLock() {
        // 判断时候锁定  
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建一个WifiLock  
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    // 得到配置好的网络  
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    // 指定配置好的网络进行连接  
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回  
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络  
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }

    // 得到MAC地址  
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 得到接入点的BSSID  
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }


    // 得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

//    // 得到WifiInfo的所有信息包  
//    public String getWifiInfo() {
//        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
//    }


    // 断开指定ID的网络  
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }


    //------***************************************************************************


}
