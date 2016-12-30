package com.ntu.claw.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ntu.claw.utils.L;

public class OfflineMapDownService extends Service {

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public OfflineMapDownService getService() {
            return OfflineMapDownService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mOfflineMap.init(mMKOfflineMapListener);

        return super.onStartCommand(intent, flags, startId);
    }

    public final static String ACTION_DOWNLOAD_UPDATE = "action_download_update";
    public final static String EXTRA_DATA_RATIO = "extra_data_ratio";
    public final static String EXTRA_DATA_STATE = "extra_data_state";

    private MKOfflineMap mOfflineMap = new MKOfflineMap();

    private MKOfflineMapListener mMKOfflineMapListener = new MKOfflineMapListener() {
        @Override
        public void onGetOfflineMapState(int type, int state) {
            switch (type) {
                case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
                    MKOLUpdateElement update = mOfflineMap.getUpdateInfo(state);
                    L.d("OfflineMapDownService: " + update.cityName + " ," + update.ratio);
                    Intent intent = new Intent(ACTION_DOWNLOAD_UPDATE);
                    intent.putExtra(EXTRA_DATA_STATE, state);
                    intent.putExtra(EXTRA_DATA_RATIO, update.ratio);
                    sendBroadcast(intent);

                    if (update.ratio >= 100) {
                        toDownNextTarget(state);
                    }
                    break;
                case MKOfflineMap.TYPE_NEW_OFFLINE:
                    L.d("OfflineMapDownService: TYPE_NEW_OFFLINE");
                    break;
                case MKOfflineMap.TYPE_VER_UPDATE:
                    L.d("OfflineMapDownService: TYPE_VER_UPDATE");
                    break;
            }
        }
    };

    private void toDownNextTarget(int completedCityId) {
        mCityCodes.remove((Object) completedCityId);

        if (mCityCodes.size() > 0) {
            mOfflineMap.start(mCityCodes.get(0));
        } else {
            stopSelf(); // all target down completed
        }
    }

    public List<Integer> mCityCodes = new ArrayList<Integer>();

    public boolean isDownloadOnTheTask(int cityId) {
        return mCityCodes.contains((Object) cityId);
    }

    public void removeTaskFromQueue(int cityId) {
        mCityCodes.remove((Object) cityId);
        mOfflineMap.pause(cityId);
    }

    public void addToDownloadQueue(int cityId) {
        mCityCodes.add(cityId);
        mOfflineMap.start(cityId);
    }

    @Override
    public void onDestroy() {
        mOfflineMap.destroy();
        super.onDestroy();
    }

    public ArrayList<MKOLSearchRecord> getOfflineCityList() {
        return mOfflineMap.getOfflineCityList();
    }

    public ArrayList<MKOLUpdateElement> getAllUpdateInfo() {
        return mOfflineMap.getAllUpdateInfo();
    }
}

