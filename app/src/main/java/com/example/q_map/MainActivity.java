package com.example.q_map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.example.q_map.overlayutil.BikingRouteOverlay;
import com.example.q_map.overlayutil.DrivingRouteOverlay;
import com.example.q_map.overlayutil.MassTransitRouteOverlay;
import com.example.q_map.overlayutil.PoiOverlay;
//规划路线
import com.baidu.mapapi.model.LatLng;
import com.example.q_map.overlayutil.TransitRouteOverlay;
import com.example.q_map.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private Context context;
    private PoiSearch mPoiSearch;
    private SuggestionSearch mSuggestionSearch;
    //定位相关
    private double mLatitude;
    private double mLongtitude;
    //方向传感器
    private MyOrientationListener mMyOrientationListener;
    private float mCurrentX;
    //自定义图标
    private BitmapDescriptor mIconLocation;
    private LocationClient mLocationClient;
    public BDAbstractLocationListener myListener;
    private LatLng mLastLocationData;
    private boolean isFirstin = true;
    private PoiResult mPoiResult = null;
    private LocationManager lm;
    private EditText ed1;
    private EditText ed2;
    private EditText go;
    private EditText end;
    private AlertDialog.Builder builder;
    private LinearLayout buju;
    private LinearLayout buju2;
    private RoutePlanSearch mSearch = null;
    private String cityname;
    private String districtname;
    private LatLng home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        SDKInitializer.setCoordType(CoordType.BD09LL);
        this.context = this;
        mMapView = findViewById(R.id.dhmapView);
        //获取地图控件引用
        mBaiduMap = mMapView.getMap();
        mMapView.removeViewAt(1);//隐藏百度Logo
        //界面布局隐藏
        buju = findViewById(R.id.edit_layout);
        buju.setVisibility(View.GONE);
        buju2 = findViewById(R.id.xuanzechuxing);
        buju2.setVisibility(View.GONE);
        initMyLocation();
        button();
        poishow();
        initPoutePlan();
    }

    //周边搜索
    private void poisearchstar() {
        mPoiSearch.searchNearby((new PoiNearbySearchOption())
                .location(new LatLng(mLatitude, mLongtitude))
                .radius(3000)//半径范围
                .scope(2)
                .keyword(ed2.getText().toString()));
    }

    private void poishow() {
        ed2 = findViewById(R.id.et2);
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
        ed2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    poisearchstar();
                    return true;
                }
                return false;
            }
        });

    }

    //官方class
    public class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap baiduMap, PoiSearch mPoiSearch) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int i) {
            super.onPoiClick(i);
            return true;
        }
    }

    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @SuppressLint("ShowToast")
        @Override
        public void onGetPoiResult(PoiResult result) {
            if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_SHORT).show();
            } else if (result.error == SearchResult.ERRORNO.NO_ERROR && result != null) {
                //创建PoiOverlay
                PoiOverlay overlay = new MyPoiOverlay(mBaiduMap, mPoiSearch);
                //设置PoiOverlay数据
                overlay.setData(result);
                //添加PoiOverlay到地图中
                overlay.addToMap();
                overlay.zoomToSpan();
                mBaiduMap.setOnMarkerClickListener(overlay);//设置overlay可以处理标注点击事件
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult result) {
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    };

    //监听事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_Loc: {
                centerToMyLocation(mLatitude, mLongtitude);
                break;//定位自己
            }
            case R.id.search: {
                poisearchstar();
                break;
            }
            case R.id.guanyu: {
                builder = new AlertDialog.Builder(this).setIcon(R.drawable.control)
                        .setTitle("联系我")
                        .setMessage("E-mail：szy0810@qq.com"+"\n"+"微博：失败的罗非鱼"+"\n"+"贴吧：小C娱乐"+"\n"+"个人网站：szy0810.xyz")
                        .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                break;
            }
            case R.id.exit: {
                exit();
                break;
            }
            case R.id.buslistener: {
                Intent intent = new Intent(MainActivity.this, bus.class);
                startActivity(intent);
                break;
            }
            case R.id.buju: {
                //显示规划路线LinearLayout
                buju.setVisibility(View.VISIBLE);

                break;
            }
            case R.id.btn_Plan: {
                buju.setVisibility(View.GONE);
                buju2.setVisibility(View.VISIBLE);
                break;
            }

            case R.id.zoulu: {
                zouludaohang();
                buju2.setVisibility(View.GONE);
                break;
            }

            case R.id.kaiche: {
                kaichedaohang();
                buju2.setVisibility(View.GONE);
                break;
            }
            case R.id.gongjiao: {
                gongjiao();
                buju2.setVisibility(View.GONE);
                break;
            }
            case R.id.shouce:
                Intent intent = new Intent(MainActivity.this,usebook.class);
                startActivity(intent);
                break;
        }

    }


    //按钮响应
    private void button() {
        //规划路线+隐藏布局
        ImageButton gongjiao = findViewById(R.id.gongjiao);
        gongjiao.setOnClickListener(this);
        ImageButton zoulu = findViewById(R.id.zoulu);
        zoulu.setOnClickListener(this);
        ImageButton kaiche = findViewById(R.id.kaiche);
        kaiche.setOnClickListener(this);
        buju.setOnClickListener(this);
        buju2.setOnClickListener(this);
        Button plan = findViewById(R.id.btn_Plan);
        plan.setOnClickListener(this);
        go = findViewById(R.id.driver_start);
        end = findViewById(R.id.driver_end);
        ImageButton xy = findViewById(R.id.buju);
        xy.setOnClickListener(this);
        //导航
        LinearLayout book = findViewById(R.id.shouce);
        book.setOnClickListener(this);
        //公交车跳转
        LinearLayout bus = findViewById(R.id.buslistener);
        bus.setOnClickListener(this);
        //关于
        LinearLayout line1 = findViewById(R.id.guanyu);
        line1.setOnClickListener(this);
        //退出
        LinearLayout line2 = findViewById(R.id.exit);
        line2.setOnClickListener(this);
        //定位，搜索，清除的图片按钮
        ImageButton mbut_Loc = findViewById(R.id.but_Loc);
        ImageButton search = findViewById(R.id.search);
        mbut_Loc.setOnClickListener(this);
        search.setOnClickListener(this);
        CheckBox c1 = findViewById(R.id.checkBox);
        c1.setOnCheckedChangeListener(this);
        CheckBox c2 = findViewById(R.id.checkBox2);
        c2.setOnCheckedChangeListener(this);
        //设置两个radiobutton互斥
        final RadioButton ra1 = findViewById(R.id.normal_map);
        final RadioButton ra2 = findViewById(R.id.gps_map);
        ra1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    ra2.setChecked(false);
                }
            }
        });
        ra2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    ra1.setChecked(false);
                }
            }
        });
    }


    //checkbox监听
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.checkBox) {
            if (isChecked) {
                mBaiduMap.setBaiduHeatMapEnabled(true);
            } else {
                mBaiduMap.setBaiduHeatMapEnabled(false);
            }
        }
        if (buttonView.getId() == R.id.checkBox2) {
            if (isChecked) {
                mBaiduMap.setTrafficEnabled(true);
            } else {
                mBaiduMap.setTrafficEnabled(false);
            }
        }
    }

    //初始化定位
    private void initMyLocation() {
        //缩放地图
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        //声明LocationClient类
        mLocationClient = new LocationClient(this);
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式，高精度，低功耗，仅设备
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);//设置是否需要地址信息
        option.setScanSpan(1000);//设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setLocationNotify(true);//设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationPoiList(true);//设置是否需要POI结果
        //设置locationClientOption
        mLocationClient.setLocOption(option);
        myListener = new MyLocationListener();
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        //初始化图标
        mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.na_gps);
        initOrientation();
        //开始定位
        mLocationClient.start();
    }

    //定位
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentX).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            //设置自定义图标
            MyLocationConfiguration config = new
                    MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true, mIconLocation);
            mBaiduMap.setMyLocationConfiguration(config);
            //更新经纬度
            mLatitude = location.getLatitude();
            mLongtitude = location.getLongitude();
            home = new LatLng(location.getLatitude(), location.getLongitude());
            //获得城市名
            cityname = location.getCity();
            districtname = location.getCity();
            //设置起点
            mLastLocationData = new LatLng(mLatitude, mLongtitude);
            if (isFirstin) {
                centerToMyLocation(location.getLatitude(), location.getLongitude());
                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    // GPS定位结果
                    Toast.makeText(context, "正在使用GPS定位...", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    // 网络定位结果
                    Toast.makeText(context, "正在使用融合定位...", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                    // 离线定位结果
                    Toast.makeText(context, "离线定位:" + location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(context, "定位:服务器错误", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(context, "定位:网络错误", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Toast.makeText(context, "定位:手机模式错误，请检查是否飞行", Toast.LENGTH_SHORT).show();
                }
                isFirstin = false;
            }
        }
    }


    //回到定位中心
    private void centerToMyLocation(double latitude, double longtitude) {
        mBaiduMap.clear();
        mLastLocationData = new LatLng(latitude, longtitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mLastLocationData);
        mBaiduMap.animateMapStatus(msu);
    }

    //传感器
    private void initOrientation() {
        //传感器
        mMyOrientationListener = new MyOrientationListener(context);
        mMyOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }

    //返回键的监听
    @Override
    public void onBackPressed() {
        exit();
    }

    //退出弹窗
    private void exit() {
        builder = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("您确定要退出吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishAffinity();
                        System.exit(0);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }


    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted())
            mLocationClient.start();
        //开启方向传感器
        mMyOrientationListener.start();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        //停止方向传感器
        mMyOrientationListener.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    //路线规划模块
    private void initPoutePlan() {
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(listener);
    }

    OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this, "路线规划:未找到结果,检查输入", Toast.LENGTH_SHORT).show();
                isFirstin = false;
            }
            assert result != null;
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                result.getSuggestAddrInfo();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                Toast.makeText(MainActivity.this, "路线规划:搜索完成", Toast.LENGTH_SHORT).show();
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
            isFirstin = false;
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this, "路线规划:未找到结果,检查输入", Toast.LENGTH_SHORT).show();
                //禁止定位
                isFirstin = false;
            }
            assert result != null;
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                result.getSuggestAddrInfo();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                Toast.makeText(MainActivity.this, "路线规划:搜索完成", Toast.LENGTH_SHORT).show();
                TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
            //禁止定位
            isFirstin = false;
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this, "路线规划:未找到结果,检查输入", Toast.LENGTH_SHORT).show();
                //禁止定位
                isFirstin = false;
            }
            assert result != null;
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                result.getSuggestAddrInfo();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                Toast.makeText(MainActivity.this, "路线规划:搜索完成", Toast.LENGTH_SHORT).show();
                MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mBaiduMap);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
            //禁止定位
            isFirstin = false;
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this, "路线规划:未找到结果,检查输入", Toast.LENGTH_SHORT).show();
                //禁止定位
                isFirstin = false;
            }
            assert result != null;
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                result.getSuggestAddrInfo();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                Toast.makeText(MainActivity.this, "路线规划:搜索完成", Toast.LENGTH_SHORT).show();
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
            //禁止定位
            isFirstin = false;
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult result) {

        }
    };
    //步行路线导航函数
    private void zouludaohang() {
        SDKInitializer.initialize(getApplicationContext());
        String serch_textinfo = go.getText().toString();
        if("".equals(serch_textinfo)){
            PlanNode stNode = PlanNode.withLocation(home);
            PlanNode enNode = PlanNode.withCityNameAndPlaceName(districtname, end.getText().toString());
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }else{
            PlanNode stNode = PlanNode.withCityNameAndPlaceName(districtname,go.getText().toString());
            PlanNode enNode = PlanNode.withCityNameAndPlaceName(districtname, end.getText().toString());
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }
    }

    //驾驶路线导航函数
    private void kaichedaohang(){
        SDKInitializer.initialize(getApplicationContext());
        String serch_textinfo = go.getText().toString();
        if ("".equals(serch_textinfo)){
            PlanNode stNode = PlanNode.withLocation(home);
            PlanNode enNode = PlanNode.withCityNameAndPlaceName(districtname,end.getText().toString());
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }else {
            PlanNode stNode = PlanNode.withCityNameAndPlaceName(districtname,go.getText().toString());
            PlanNode enNode = PlanNode.withCityNameAndPlaceName(districtname,end.getText().toString());
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }

    }
    //公交导航
    private void gongjiao(){
        SDKInitializer.initialize(getApplicationContext());
        String serch_textinfo = go.getText().toString();
        if ("".equals(serch_textinfo)){
            PlanNode stNode = PlanNode.withLocation(home);
            PlanNode enNode = PlanNode.withCityNameAndPlaceName(districtname,end.getText().toString());
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode)
                    .to(enNode)
                    .city(districtname));
        }else{
            PlanNode stNode = PlanNode.withCityNameAndPlaceName(districtname,go.getText().toString());
            PlanNode enNode = PlanNode.withCityNameAndPlaceName(districtname,end.getText().toString());
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode)
                    .to(enNode)
                    .city(districtname));
        }

    }
}