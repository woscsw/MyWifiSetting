package com.test.mywifi.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.mywifi.R;
import com.test.mywifi.model.WifiListModel.ScanResultModel;
import com.test.mywifi.utils.WifiUtil;

import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;

/**
 * Created by Admin on 2017/7/21.
 */
public class WifiListAdapterII extends BaseAdapter{
    private Context context;
    private WifiUtil wifiUtil;
    private int pageNum = 0;//当前页数
    private int maxSize = 0;//一共页数
    private static final int pageSize = 5;//一页几个
    private String connectingSSID;
    private List<ScanResultModel> datas;

    public WifiListAdapterII(Context context,OnItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        wifiUtil = new WifiUtil(context);
    }

    public void setPageNum(int num) {
        pageNum = num;
        notifyDataSetChanged();
    }

    public int getPageNum() {
        return pageNum + 1;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void nextPage() {
        if (pageNum < maxSize - 1) {
            pageNum++;
            notifyDataSetChanged();
        }
    }

    public void prePage() {
        if (pageNum > 0) {
            pageNum--;
            notifyDataSetChanged();
        }

    }

    private List<String> errorList = new ArrayList<>();

    public void setConnectingSSID(String ssid) {
        connectingSSID = ssid;
//            //找出是谁干的，把他放在第一个
//            for (int i=0;i<maxSize;i++) {
//                for (int j = 0;j<datas.get(i).getData().size();j++) {
//                    if (datas.get(i).getData().get(j).SSID.equals(connectingSSID)) {
//                        ScanResultModel srm = datas.get(i).getData().get(j);
//                        datas.get(i).getData().remove(j);
//                        datas.get(i).getData().add(0,srm);
//                    }
//                }
//            }
        notifyDataSetChanged();
    }

    //清除连接中的标识
    public void reSetConnectingSSID() {
        connectingSSID = null;
    }

    //设置密码错误的标识
    public void setErrorSSID(String ssid) {
        if (ssid != null) {
            errorList.add(ssid);
            notifyDataSetChanged();
        }
    }

    //清楚密码错误的标识
    public void removeErrorSSID(String ssid) {
        if (ssid != null && errorList.size() > 0) {
            for (String s : errorList) {
                if (s.equals(ssid)) {
                    errorList.remove(s);
                }
            }
        }

    }

    //清楚所有密码错误的标识
    public void removeAllErrorSSID() {
        errorList.clear();
    }

    public void setData(List<ScanResultModel> data) {
        if (data != null && data.size() > 0) {
            datas = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                if (TextUtils.isEmpty(data.get(i).SSID)) {
                    data.remove(i);//清除SSID为“”的
                }
            }
            int yu = data.size() % pageSize;
            if (yu == 0) {
                maxSize = data.size() / pageSize;
            } else {
                maxSize = data.size() / pageSize + 1;
            }
            datas.addAll(data);
        } else {
            datas = null;
            maxSize = 0;
        }
        pageNum = 0;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public ScanResultModel getItem(int position) {
        return datas == null ? null : datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private OnItemClickListener itemClickListener;
    public interface OnItemClickListener {
        void onItemClick(ScanResultModel data);
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ldklz_carcool_wifi_item_wifi_list, parent, false);
        }
        View itemView = convertView.findViewById(R.id.wifi_item);
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_wifi_name);
        TextView tvStatus = (TextView) convertView.findViewById(R.id.tv_wifi_status);
        ImageView ivStatus = (ImageView) convertView.findViewById(R.id.iv_wifi_status);
        ImageView ivProtection = (ImageView) convertView.findViewById(R.id.iv_wifi_protection);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(context, WifiDetailDialog.class);
//                intent.putExtra("data", datas.get(position));
//                context.startActivity(intent);
                //
                itemClickListener.onItemClick(datas.get(position));
            }
        });
        tvName.setText(datas.get(position).SSID);
        String str = datas.get(position).capabilities;
        StringBuilder stringBuilder = new StringBuilder();
        if (str.contains("WPA-PSK-CCMP")) {
            stringBuilder.append("WPA");
            if (str.contains("WPA2-PSK-CCMP")) {
                stringBuilder.append("/WPA2");
            }
        } else if (str.contains("WPA2-PSK-CCMP")) {
            stringBuilder.append("WPA2");
        } else if (str.contains("WEP")) {
            stringBuilder.append("WEP");
        } else if (TextUtils.isEmpty(str) || "[ESS]".equals(str)) {
            stringBuilder.append("无");
            ivProtection.setImageResource(R.drawable.ldklz_carcool_wifi_suokai);
        }
        if (wifiUtil.IsExsits(datas.get(position).SSID) != null) {
            //已保存
            if (wifiUtil.isConnectWifi() && datas.get(position).SSID.equals(wifiUtil.getSSID())) {
                //已连接当前wifi
                tvStatus.setText(context.getString(R.string.connected));
            } else if (wifiUtil.checkState() == WIFI_STATE_ENABLING) {

            } else if ("无".equals(stringBuilder.toString())) {
                tvStatus.setText(context.getString(R.string.saved));
            } else {
                tvStatus.setText(context.getString(R.string.saved_by)+" " + stringBuilder.toString() + context.getString(R.string.protection));
            }

        } else {
            tvStatus.setText(context.getString(R.string.by)+" " + stringBuilder.toString() + context.getString(R.string.protection));
            if ("无".equals(stringBuilder.toString())) {
                tvStatus.setText("");
            }
        }

        if (errorList.size() > 0) {
            for (String s : errorList) {
                if (s.equals("\"" + datas.get(position).SSID + "\"")) {
                    tvStatus.setText(context.getString(R.string.authentication_error));
                }
            }
        }
        if (connectingSSID != null) {
            if (connectingSSID.equals("\"" + datas.get(position).SSID + "\"")) {
                tvStatus.setText(context.getString(R.string.connecting));
                itemView.setEnabled(false);
            } else {
                itemView.setEnabled(true);
            }
        } else {
            itemView.setEnabled(true);
        }
        ivStatus.setImageResource(wifiUtil.getSignalStrengthDrawable(datas.get(position).level));
//            tvStatus.setText();
        //已连接
        //已保存，通过WPA2进行保护
        //已保存，通过WPA/WPA2进行保护
        //通过WPA/WPA2进行保护
        //通过WPA/WPA2进行保护(可使用WPS)


//            TextView TvFileTitle = (TextView)convertView.findViewById(R.id.TvFileTitle);
//            View LytLstItemRoot = convertView.findViewById(R.id.LytLstItemRoot);
//
//            if (mActivity != null && mActivity.mSrchFile != null) {
//                FilePathInfo data = mActivity.mSrchFile.getCurrDirFileListData(mCurShowStorageType, position);
//                if (data != null) {
//                    try {
//                        if (TvFileTitle != null) {
//                            String StrFilePath = data.mStrFilePath.substring(data.mStrFilePath.lastIndexOf(File.separator) + 1);
//                            TvFileTitle.setText(StrFilePath);
//                        }
//                        if (mCurShowStorageType == mActivity.mSrchFile.getMediaStorageType()
//                                && position == (mActivity.mSrchFile.getCurPlayFile() - mActivity.mSrchFile.getCurFolderStartPlayFileId())) {
//                            if (mActivity != null && mActivity.isVWRedStyleUI()) {
//                                //if (LytLstItemRoot != null) LytLstItemRoot.setBackgroundColor(0xFFD00F0F);
//                                if (LytLstItemRoot != null) LytLstItemRoot.setBackgroundColor(0xFFFF8C85);
//                            } else {
//                                if (LytLstItemRoot != null) LytLstItemRoot.setBackgroundColor(0xFF9BC1FF);
//                            }
//                        } else {
//                            if (LytLstItemRoot != null) LytLstItemRoot.setBackgroundColor(0x0);
//                        }
//                    } catch (Exception e) {
//                    }
//                }
//            }
        return convertView;
    }


//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            if (mActivity != null && mActivity.mSrchFile != null) {
//                int curPlayIndx = position;
//                curPlayIndx += mActivity.mSrchFile.getCurFolderStartPlayFileId();
//                if (mCurShowStorageType == mActivity.getMediaStorageType() && curPlayIndx == mActivity.mSrchFile.getCurPlayFile()) {
//                } else {
//                    mActivity.setMediaStorageType(mCurShowStorageType);
//                    FilePathInfo data = mActivity.mSrchFile.getCurrDirFileListData(mCurShowStorageType, position);
//                    if (data != null) {
//                        Log.i(TAG, "onItemClick path = " + data.mStrFilePath + " pos = " + position + " startid = " + mActivity.mSrchFile.getCurFolderStartPlayFileId());
//                        mActivity.mSrchFile.setCurPlayFile(position + mActivity.mSrchFile.getCurFolderStartPlayFileId());
//                        mActivity.playFile(data.mStrFilePath, position + mActivity.mSrchFile.getCurFolderStartPlayFileId());
//                    } else {
//                        Log.i(TAG, "onItemClick data == null");
//                    }
//                }
//                popPage();
//            }
//        }

}
