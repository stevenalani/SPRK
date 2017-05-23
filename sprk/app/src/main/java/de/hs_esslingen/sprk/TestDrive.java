package de.hs_esslingen.sprk;

import android.util.Log;

import com.orbotix.ConvenienceRobot;
import com.orbotix.Sphero;
import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.command.RollCommand;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.common.sensor.AccelerometerData;
import com.orbotix.common.sensor.DeviceSensorsData;
import com.orbotix.common.sensor.GyroData;
import com.orbotix.common.sensor.LocatorData;
import com.orbotix.common.sensor.SensorFlag;
import com.orbotix.subsystem.SensorControl;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven on 18.05.2017.
 */

public class TestDrive implements ResponseListener {
    private class Coordinates{
        public Timestamp updated;
        public Coordinates(float x,float y) {
            this.x = x;
            this.y = y;
            updated = new Timestamp(System.currentTimeMillis());
            //Log.i("TestDrive",updated.toString());
        }
        public float x;
        public float y;
    }


    private ConvenienceRobot mRobot;
    private boolean handeledCollision = false;
    /**
     * Helpers to drive back to old positon before collosion
     * Currently unused
     */
    private float currentHeading,
                currentSpeed,
                lastHeadingBC,
                lastSpeed;
    private List<Coordinates> lastTenPoss = new ArrayList<Coordinates>();
    public TestDrive(ConvenienceRobot mRobot){
        Log.i("TestDrive","TestDrive begins");
        this.mRobot = mRobot;
        long sensorFlag = SensorFlag.ACCELEROMETER_NORMALIZED.longValue() | SensorFlag.GYRO_NORMALIZED.longValue();
        mRobot.enableSensors( sensorFlag, SensorControl.StreamingRate.STREAMING_RATE10 );
        mRobot.enableLocator(true);
        mRobot.addResponseListener(this);
        mRobot.enableCollisions(true);
        mRobot.setLed(0.1f,0.1f,0.1f);
        mRobot.sendCommand(new RollCommand(0, 0.5F, RollCommand.State.GO ));
    }
    @Override
    public void handleResponse(DeviceResponse deviceResponse, Robot robot) {
        //Log.i("TestDrive", "Response " + deviceResponse.toString());
        //Log.i("TestDrive", "Robot " + robot.getName());
    }

    @Override
    public void handleStringResponse(String s, Robot robot) {
        //Log.i("TestDrive", "sResponse " + s);
        //Log.i("TestDrive", "Robot " + robot.getName());
    }

    @Override
    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {
        //Log.i("TestDrive", "Response " + asyncMessage.getType());
        //Log.i("TestDrive", "Robot " + robot.getName());
        if (asyncMessage instanceof DeviceSensorAsyncMessage) {
            DeviceSensorAsyncMessage sensorsData = (DeviceSensorAsyncMessage) asyncMessage;
            ArrayList<DeviceSensorsData> sensorDataArray = sensorsData.getAsyncData();
            DeviceSensorsData dsd = sensorDataArray.get(sensorDataArray.size() - 1);
            try {
                LocatorData locatorData = dsd.getLocatorData();
                //if (lastTenPoss.size() == 10 )
                   // this.lastTenPoss.remove(0);
                this.lastTenPoss.add(new Coordinates(locatorData.getPositionX(),locatorData.getPositionY()));
                Log.i("Locator","Fetched Coordinates:" + lastTenPoss.size() +"x: " +locatorData.getPositionX() + "\t Y: " + locatorData.getPositionY());
            }catch (Exception ex){Log.i("TestDrive", "no location data");}
            try {
                GyroData gyroData = dsd.getGyroData();
                Log.i("gyroData",gyroData.getRotationRateFiltered().y + "");
            }catch (Exception ex){Log.i("gyroData", "no gyro data");}
            try {
                AccelerometerData accelerometerData =  dsd.getAccelerometerData();
            }catch (Exception ex){Log.i("accelerometerData", "no Acc data");}
        }

        if (asyncMessage instanceof CollisionDetectedAsyncData) {
            if(this.mRobot.getRobot() == robot){
                if(!handeledCollision) {
                    Coordinates ac = lastTenPoss.get(lastTenPoss.size() - 1);
                    Coordinates bc = lastTenPoss.get(lastTenPoss.size() - 3);
                    mRobot.setLed(1f,0.1f,0.1f);
                    handleCollision(ac,bc);
                }
            }
        }

    }
    private void handleCollision(Coordinates ac, Coordinates bc){
        mRobot.stop();
        this.handeledCollision = true;
        for(Coordinates tmp : lastTenPoss)
            Log.i("TestDrive","x: " +tmp.x + " Y: " + tmp.y + "update: " + tmp.updated);
        Log.i("TestDrive","bc x: " +bc.x + " bc Y: " + bc.y + "bc update: " + bc.updated);
        Log.i("TestDrive","ac x: " +ac.x + " ac Y: " + ac.y + "ac update: " + ac.updated);
        Log.i("TestDrive","Heading:" + mRobot.getLastHeading());
        mRobot.setLed(0.1f,0f,0.1f);
        mRobot.sendCommand(new RollCommand(180+mRobot.getLastHeading(),0.3f,RollCommand.State.GO));
        mRobot.sendCommand(new RollCommand(180+mRobot.getLastHeading(),0.3f,RollCommand.State.STOP));
        mRobot.sendCommand(new RollCommand(180+mRobot.getLastHeading(),0.1f,RollCommand.State.GO));
        this.handeledCollision = false;
    }
}
