package com.example.q_map;
import java.util.ArrayList;
import java.util.List;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.q_map.overlayutil.BusLineOverlay;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class bus extends AppCompatActivity{
    private List<String> buslineIdList;// 存储公交线路的uid
    private int buslineIndex = 0;// 标记路线
    private PoiSearch mPoiSearch;
    private BusLineSearch busLineSearch;
    private EditText b;
    private EditText a;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_bus);
        buslineIdList = new ArrayList<String>();
        mMapView = findViewById(R.id.bus_map);
        mBaiduMap = mMapView.getMap();
        a = findViewById(R.id.bus_e1);
        b = findViewById(R.id.bus_e2);
        ImageButton btn = findViewById(R.id.bus_btn);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPoiSearch = PoiSearch.newInstance();//城市检索实例
                mPoiSearch.setOnGetPoiSearchResultListener(listener);
                busLineSearch = BusLineSearch.newInstance();
                busLineSearch.setOnGetBusLineSearchResultListener(busLineSearchResultListener);
                PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
                citySearchOption.city(a.getText().toString());
                citySearchOption.keyword(b.getText().toString());
                citySearchOption.scope(2);
                mPoiSearch.searchInCity(citySearchOption);
            }
        });
    }
    OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult == null|| poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
                Toast.makeText(bus.this, "未找到结果",Toast.LENGTH_LONG).show();
                return;
            }
            buslineIdList.clear();
            for (PoiInfo poi : poiResult.getAllPoi()){
                    buslineIdList.add(poi.uid);
                    break;
            }
            searchBusline();
            mPoiSearch.destroy();
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };
    private void searchBusline(){
        if (buslineIndex >= buslineIdList.size()){
            buslineIndex = 0;
        }
        if (buslineIndex >= 0 && buslineIdList.size() > 0){
            busLineSearch.searchBusLine((new BusLineSearchOption()
                    .city(a.getText().toString())
                    .uid(buslineIdList.get(buslineIndex)
                    )));
        }
        buslineIndex++;
    }
    OnGetBusLineSearchResultListener busLineSearchResultListener = new OnGetBusLineSearchResultListener() {
        @Override
        public void onGetBusLineResult(BusLineResult busLineResult ) {
            if (busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(bus.this, "未找到结果,请输入地级市或以上", Toast.LENGTH_SHORT).show();
                mBaiduMap.clear();
            } else {
                mBaiduMap.clear();
                BusLineOverlay overlay = new MyBuslineOverlay(mBaiduMap);// 用于显示一条公交详情结果的Overlay
                overlay.setData(busLineResult);
                overlay.addToMap();
                overlay.zoomToSpan();
                mBaiduMap.setOnMarkerClickListener(overlay);
                // 公交线路名称
                Toast.makeText(bus.this, busLineResult.getBusLineName(), Toast.LENGTH_SHORT) .show();
            }
        };
    };

    public class MyBuslineOverlay extends BusLineOverlay {

        public MyBuslineOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public boolean onBusStationClick(int arg0) {
            MarkerOptions options = (MarkerOptions) getOverlayOptions().get(arg0);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(options.getPosition()));
            return true;
        }
    }

}


