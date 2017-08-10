package com.test.mywifi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Admin on 2017/7/22.
 */

public class WifiListModel {
    private List<ScanResultModel> data;


    public List<ScanResultModel> getData() {
        return data;
    }

    public void setData(List<ScanResultModel> data) {
        this.data = data;
    }

    public static class ScanResultModel implements Parcelable {
        public String SSID;//不带""
        public int level;
        public String capabilities;
        public ScanResultModel() {
            
        }
        protected ScanResultModel(Parcel in) {
            SSID = in.readString();
            level = in.readInt();
            capabilities = in.readString();
        }

        public static final Creator<ScanResultModel> CREATOR = new Creator<ScanResultModel>() {
            @Override
            public ScanResultModel createFromParcel(Parcel in) {
                return new ScanResultModel(in);
            }

            @Override
            public ScanResultModel[] newArray(int size) {
                return new ScanResultModel[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(SSID);
            dest.writeInt(level);
            dest.writeString(capabilities);
        }
    }
    
}

