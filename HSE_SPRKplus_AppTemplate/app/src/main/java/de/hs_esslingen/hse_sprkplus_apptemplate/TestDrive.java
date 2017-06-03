package de.hs_esslingen.hse_sprkplus_apptemplate;

import android.util.Log;

import com.orbotix.ConvenienceRobot;
import com.orbotix.Sphero;
import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.command.RawMotorCommand;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.common.sensor.AccelerometerData;
import com.orbotix.common.sensor.AttitudeSensor;
import com.orbotix.common.sensor.BackEMFData;
import com.orbotix.common.sensor.DeviceSensorsData;
import com.orbotix.common.sensor.GyroData;
import com.orbotix.common.sensor.LocatorData;
import com.orbotix.common.sensor.QuaternionData;
import com.orbotix.common.sensor.QuaternionSensor;
import com.orbotix.macro.cmd.Delay;

import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import de.hs_esslingen.utils.Coordinates;
import de.hs_esslingen.utils.Sphere;

/**
 * Created by Steven on 18.05.2017.
 */

public class TestDrive extends Observable implements ResponseListener, Runnable {
    private long COLLISION_TIMEOUT = 1000;
    private ConvenienceRobot mRobot;
    public static boolean handeledCollision = false;
    public static void setROBOT_SPEED(float ROBOT_SPEED) {
        TestDrive.ROBOT_SPEED = ROBOT_SPEED;
    }
    protected static float ROBOT_SPEED = 0.5f;
    public static void setROBOT_SCAN_SPEED(float ROBOT_SCAN_SPEED) {
        TestDrive.ROBOT_SCAN_SPEED = ROBOT_SCAN_SPEED;
    }
    protected static int handledCollisions = 0;
    private static float ROBOT_SCAN_SPEED = 0.25f;
    /**
     * Helpers to drive back to old positon before collosion
     * Currently unused
     */
    protected float lastHeading = 0f;
    protected List<Coordinates> lastPositionsList = new ArrayList<Coordinates>();
    protected List<Coordinates> sinceCollusions = new ArrayList<>();
    protected List<GyroData> lastGyroData = new ArrayList<GyroData>();
    protected List<AccelerometerData> lastAccelData = new ArrayList<AccelerometerData>();
    protected List<BackEMFData> lastEMFData = new ArrayList<BackEMFData>();
    protected List<QuaternionSensor> lastQuaternions = new ArrayList<QuaternionSensor>();
    protected List<AttitudeSensor> lastAttitudes = new ArrayList<AttitudeSensor>();
    protected int MAX_LIST_SIZE = 20;
    protected int[] color = {204, 255, 51};

    public TestDrive(ConvenienceRobot mRobot,float driveSpeed){
        if(mRobot != null) {
            Log.i("TestDrive", "TestDrive begins");
            this.mRobot = mRobot;
            mRobot.addResponseListener(this);
            Sphere my = new Sphere(5,0,0,0);
        }
    }
    public void startTestDrive() {
        if(mRobot != null) {
            mRobot.enableStabilization(true);
            mRobot.enableCollisions(true);
            mRobot.setLed(204/255f, 255/255f, 51/255f);
            this.run();
            Log.i("start",ROBOT_SPEED+"");
        }
    }

    @Override
    public void handleResponse(DeviceResponse deviceResponse, Robot robot) {
        //Log.i("DeviceResponse", "Response " + deviceResponse.toString());
        //Log.i("DeviceResponse", "Robot " + robot.getName());
    }

    @Override
    public void handleStringResponse(String s, Robot robot) {
        //Log.i("StringResponse", "sResponse " + s);
        //Log.i("StringResponse", "Robot " + robot.getName());
    }

    @Override
    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {

        if( asyncMessage == null )
            return;

        //Check the asyncMessage type to see if it is a DeviceSensor message
        if( asyncMessage instanceof DeviceSensorAsyncMessage ) {
            DeviceSensorAsyncMessage message = (DeviceSensorAsyncMessage) asyncMessage;

            if( message.getAsyncData() == null
                    || message.getAsyncData().isEmpty()
                    || message.getAsyncData().get( 0 ) == null )
                return;

            //Retrieve DeviceSensorsData from the async message
            DeviceSensorsData data = message.getAsyncData().get( 0 );
            this.updateLastAccelData(data.getAccelerometerData());
            //Extract attitude data (yaw, roll, pitch) from the sensor data
            //Log.i("DeviceSensorsData attitude: ",String.valueOf(data.getAttitudeData()));
            this.updateLastAttitude(data.getAttitudeData());
            //Extract quaternion data from the sensor data
            this.updateLastQuaternions(data.getQuaternion());
            //Log.i("DeviceSensorsData quaternion: ",String.valueOf(data.getQuaternion()));

            //Display back EMF data from left and right motors
            this.updateLastEMFData(data.getBackEMFData());
            //Log.i("DeviceSensorsData EMF: ",String.valueOf(data.getBackEMFData().getEMFFiltered()));

            //Extract gyroscope data from the sensor data
            this.updateLastGyroData(data.getGyroData());
            //Log.i("DeviceSensorsData gyroscope: ",String.valueOf(data.getGyroData()));
            this.updateLastPoslist(message);
        }
        if (asyncMessage instanceof CollisionDetectedAsyncData)
        {
            if(!handeledCollision) {
                mRobot.setLed(1f,0.1f,0.1f);
                handleCollision((CollisionDetectedAsyncData)asyncMessage);
            }else{
                mRobot.setLed(1f,0f,0f);
            }
        }
    }
    private void handleCollision(CollisionDetectedAsyncData data){
        handeledCollision = true;
        mRobot.stop();
        Log.i("handleCollision","handledCollisons: "+TestDrive.handledCollisions);
        mRobot.setLed(1f,0f,0f);
        long startt = System.currentTimeMillis();
        mRobot.drive(180f,ROBOT_SCAN_SPEED);
        float newHeading = mRobot.getLastHeading();
        long step = COLLISION_TIMEOUT/8,dt;
        while((dt = System.currentTimeMillis() - startt) <= COLLISION_TIMEOUT*2)
        {
            if((System.currentTimeMillis() - startt) >= step)
            {
                step += COLLISION_TIMEOUT*2/8;
                newHeading = 360f*dt / COLLISION_TIMEOUT*2 ;
                mRobot.drive(newHeading,ROBOT_SCAN_SPEED );
                Log.i("handleCollision","New Heading "+newHeading);
                Log.i("handleCollision","step: "+step+" dt: " +dt);
            }
        }
        Timer resetHandleCollision = new Timer();
        resetHandleCollision.schedule(new TimerTask() {
            @Override
            public void run() {
                TestDrive.handeledCollision = false;
                TestDrive.handledCollisions++;
                Log.i("handlecollison",TestDrive.handledCollisions+"handled Collisions");
            }
        },COLLISION_TIMEOUT);
    }
    private void updateLastPoslist(DeviceSensorAsyncMessage message){
        LocatorData data = message.getAsyncData().get(0).getLocatorData();
        if(!handeledCollision) {
            if(sinceCollusions.size() > 0)
                sinceCollusions = new ArrayList<>();
            if (this.lastPositionsList.size() == MAX_LIST_SIZE) {
                this.lastPositionsList.remove(MAX_LIST_SIZE - 1);
            }
            this.lastPositionsList.add(new Coordinates(data.getPositionX(), data.getPositionY()));
        }else{
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if(System.currentTimeMillis() - message.getTimeStamp().getTime() < COLLISION_TIMEOUT)
            if (this.sinceCollusions.size() == MAX_LIST_SIZE) {
                this.sinceCollusions.remove(MAX_LIST_SIZE - 1);
            }
            sinceCollusions.add(new Coordinates(data.getPositionX(),data.getPositionY()));
        }
    }
    private void updateLastGyroData(GyroData data){
        Log.i("updateLastGyroData gyroscope: ","x: "+data.getRotationRateFiltered().x);
        Log.i("updateLastGyroData gyroscope: ","y: "+data.getRotationRateFiltered().y);
        Log.i("updateLastGyroData gyroscope: ","z: "+data.getRotationRateFiltered().z);
        if(this.lastGyroData.size() == MAX_LIST_SIZE)
            this.lastGyroData.remove(MAX_LIST_SIZE-1);
        this.lastGyroData.add(data);
    }
    private void updateLastAccelData(AccelerometerData data){
        if(this.lastAccelData.size() == MAX_LIST_SIZE)
            this.lastAccelData.remove(MAX_LIST_SIZE-1);
        this.lastAccelData.add(data);
        Log.i("updateLastAccelData acc: ","z: "+data.getFilteredAcceleration().x);
        Log.i("updateLastAccelData acc: ","z: "+data.getFilteredAcceleration().y);
        Log.i("updateLastAccelData acc: ","z: "+data.getFilteredAcceleration().z);
        /*if(data.getFilteredAcceleration().z > 1.2f){
            mRobot.setLed(1f,0f,0f);
        }else if(data.getFilteredAcceleration().z < -1.2f){
            mRobot.setLed(0f,1f,0f);
        }else{
            mRobot.setLed(this.color[0]/255f, this.color[1]/255f,this.color[2]/255f);
        }*/
    }
    private void updateLastEMFData(BackEMFData data){
        if(this.lastEMFData.size() == MAX_LIST_SIZE)
            this.lastEMFData.remove(MAX_LIST_SIZE-1);
        this.lastEMFData.add(data);
    }
    private void updateLastQuaternions(QuaternionSensor sensor){
        if(this.lastQuaternions.size() == MAX_LIST_SIZE)
            this.lastQuaternions.remove(MAX_LIST_SIZE-1);
        this.lastQuaternions.add(sensor);
        Log.i("Quat","x: ");
    }
    private void updateLastAttitude(AttitudeSensor sensor){
        if(this.lastAttitudes.size() == MAX_LIST_SIZE)
            this.lastAttitudes.remove(MAX_LIST_SIZE-1);
        this.lastAttitudes.add(sensor);
    }

    @Override
    public void run() {
        this.mRobot.drive(0f,ROBOT_SPEED);
    }
}
