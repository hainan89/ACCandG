package com.example.mobilesensorx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class MainActivity extends Activity {

	Button btStart;
	Button btEnd;
	Button btGetAllSensors;
	Button btClose;
	
	EditText etFrequency;
	int sampleFre;
	
	Spinner spFre;
	private ArrayAdapter<String> adapterSp;
	int freUnitTimes = 1;
	
	TextView txReadingData;
	TextView txStoragePath;
	LinearLayout lySensorList;
	ScrollView lyScrollSensors;
	private SensorManager agSensorManager = null;
	
	List<Sensor> allSensorList;
	List<CheckBox> sensorBoxList;
	
	private static final String[] freUnit={"uS","mS","S"};
	
	int totalMessageNum = 0;
	
	String[] sensor_type_strs = {"TYPE_NULL",
									"TYPE_ACCELEROMETER",
	                                 "TYPE_MAGNETIC_FIELD",
	                                 "TYPE_ORIENTATION",
	                                 "TYPE_GYROSCOPE",
	                                 "TYPE_LIGHT",
	                                 "TYPE_PRESSURE",
	                                 "TYPE_TEMPERATURE",
	                                 "TYPE_PROXIMITY",
	                                 "TYPE_GRAVITY",
	                                 "TYPE_LINEAR_ACCELERATION",
	                                 "TYPE_ROTATION_VECTOR",
	                                 "TYPE_RELATIVE_HUMIDITY",
	                                 "TYPE_AMBIENT_TEMPERATURE",
	                                 "TYPE_MAGNETIC_FIELD_UNCALIBRATED",
	                                 "TYPE_GAME_ROTATION_VECTOR",
	                                 "TYPE_GYROSCOPE_UNCALIBRATED",
	                                 "TYPE_SIGNIFICANT_MOTION",
	                                 "TYPE_STEP_DETECTOR",
	                                 "TYPE_STEP_COUNTER",
	                                 "TYPE_GEOMAGNETIC_ROTATION_VECTOR",
	                                 "TYPE_HEART_RATE"};
	SparseArray<FileOutputStream> sensorDataFileArray = new SparseArray<FileOutputStream>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    	
        setContentView(R.layout.activity_main);
        
        //edPeriod = (EditText) findViewById(R.id.ed_period);
        btStart = (Button) findViewById(R.id.bt_start_s);
        btEnd = (Button) findViewById(R.id.bt_end_s);
        btGetAllSensors = (Button) findViewById(R.id.bt_get_all_sensors_s);
        btClose = (Button) findViewById(R.id.bt_close);
        
    	txReadingData = (TextView) findViewById(R.id.tx_reading_data);
    	txReadingData.setMovementMethod(ScrollingMovementMethod.getInstance());
    	
    	txStoragePath = (TextView) findViewById(R.id.tx_storage_path);
    	//txStoragePath.setMovementMethod(ScrollingMovementMethod.getInstance());
    	
    	etFrequency = (EditText) findViewById(R.id.et_frequency);
    	etFrequency.setText("1000");
    	
    	spFre = (Spinner) findViewById(R.id.sp_unit);
    	adapterSp = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,freUnit);
    	adapterSp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spFre.setAdapter(adapterSp);
    	spFre.setOnItemSelectedListener(new OnItemSelectedListener(){
    		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                    long arg3) {  
    			freUnitTimes = (int)Math.pow(1000, arg2);
    			//txReadingData.append(String.format("%d", freUnitTimes));
            }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
    	});
    	
    	agSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    	
    	lySensorList = (LinearLayout) findViewById(R.id.ly_sensor_list);
    	lyScrollSensors = (ScrollView) findViewById(R.id.ly_scroll_list);
    	//lySensorList).setMovementMethod(ScrollingMovementMethod.getInstance());
    	
    	btEnd.setEnabled(false);
    	btStart.setEnabled(false);  	
    	
    	btGetAllSensors.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				lySensorList.removeAllViews();
				
				listAllSensors();
				
				btStart.setEnabled(true);
			}
		});
    	
    	
        btStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				txReadingData.setText("");
		    	txStoragePath.setText("");
		    	
				if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					Toast.makeText(MainActivity.this, "No SDCard or SDCard can't be writen!", Toast.LENGTH_SHORT).show();
					return ;
				}
				
				btStart.setEnabled(false);
				btEnd.setEnabled(true);
				
				registeSensors();
			}
		});
        
        btEnd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				unRegistSensors();
				
				btStart.setEnabled(true);
			}
		});
        
        btClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				unRegistSensors();
				MainActivity.this.finish();
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    
    public void listAllSensors(){
    	allSensorList = agSensorManager.getSensorList(Sensor.TYPE_ALL);

		Iterator<Sensor> sI = allSensorList.iterator();
		while (sI.hasNext()) {
			Sensor se = sI.next();
			int sType = se.getType();
			if (sType >= 1 && sType <= 21){
				CheckBox oneSensor = new CheckBox(MainActivity.this);
				oneSensor.setText(sensor_type_strs[sType]);
				oneSensor.setChecked(false);
				lySensorList.addView(oneSensor);
			}else{
				Toast.makeText(MainActivity.this, "Unrecognized device", Toast.LENGTH_SHORT).show();
			}
		}
    }
    
      
    public void registeSensors(){
    	
    	sampleFre = Integer.parseInt(etFrequency.getText().toString());
    	sampleFre = sampleFre * freUnitTimes; // us数
    	
    	//Toast.makeText(MainActivity.this, String.format("--%d--", sampleFre), Toast.LENGTH_SHORT).show();
    	
    	Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。  
		t.setToNow(); // 取得系统时间。  
		int t_year = t.year;  
		int t_month = t.month;
		int t_date = t.monthDay;  
		int t_hour = t.hour;
		int t_minute = t.minute;  
		int t_second = t.second;
		String fileName;
		
    	//lySensorList = (LinearLayout) findViewById(R.id.ly_sensor_list);
		CheckBox oneBox;
		int checkBoxI = 0;
		for(int i = 0 ; i < lySensorList.getChildCount(); i++){
			oneBox = (CheckBox)lySensorList.getChildAt(i);
			if(oneBox.isChecked()){
				checkBoxI++;
				Sensor se = allSensorList.get(i);
				fileName = String.format("%d%d%d%d%d%d-%s.txt", t_year,t_month,t_date,t_hour,t_minute,t_second, sensor_type_strs[se.getType()]);
				
				// 注册对应的传感器监听
				//sampleFre = sampleFre * freUnitTimes;
				agSensorManager.registerListener(agSensorListener,
						agSensorManager.getDefaultSensor(se.getType()),
						sampleFre);
						//SensorManager.SENSOR_DELAY_NORMAL);
				//txReadingData.append(String.format("--%d--", sampleFre));
				// 穿件对应的数据存储文件
				try {
					File recordFile = new File(android.os.Environment.getExternalStorageDirectory() + "/" + fileName);
					
					FileOutputStream oneFout =  new FileOutputStream(recordFile, true);
					sensorDataFileArray.append(se.getType(), oneFout);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//show the path of the storage
		txStoragePath.append("Storage Path:" + android.os.Environment.getExternalStorageDirectory().toString() + "/");
		if(checkBoxI == 0){
			Toast.makeText(MainActivity.this, "No selected sensors!", Toast.LENGTH_SHORT).show();
			btStart.setEnabled(true);
		}

    }
    
    public void unRegistSensors(){
    	// 释放监听
    	agSensorManager.unregisterListener(agSensorListener);
    	
    	// 关闭数据文件
    	int keyF = 0;
    	for(int i = 0 ; i < sensorDataFileArray.size() ; i++){
    		keyF = sensorDataFileArray.keyAt(i);
    		FileOutputStream sF = sensorDataFileArray.get(keyF);
    		try{
    			sF.close();
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	sensorDataFileArray.clear();
    	Toast.makeText(MainActivity.this, "Reading Over!", Toast.LENGTH_SHORT).show();
    }
    
//    public void setCheckBoxAble(boolean flag){
//    	for(int i = 0 ; i < lySensorList.getChildCount(); i++){
//			CheckBox oneBox = (CheckBox)lySensorList.getChildAt(i);
//			oneBox.setEnabled(flag);
//		}
//    }
    
	private final SensorEventListener agSensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {

			FileOutputStream sF = sensorDataFileArray.get(event.sensor.getType());
			try{
				StringBuilder recordValue = new StringBuilder();
				for(int i = 0 ; i < event.values.length ; i++){
					recordValue.append(String.format("%f \t", event.values[i]));
				}
				recordValue.append(" \r\n");
				
				totalMessageNum++;
				if(totalMessageNum > 100){
					totalMessageNum = 0;
					txReadingData.setText("");
				}else{
					txReadingData.append(sensor_type_strs[event.sensor.getType()] + ":\n");
					txReadingData.append(recordValue.toString());
				}
				txReadingData.setGravity(Gravity.BOTTOM);
				
				
				byte[] bytes = recordValue.toString().getBytes();
				sF.write(bytes);
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
}
