package de.hs_esslingen.sprk;

import android.util.Log;

import com.orbotix.ConvenienceRobot;
import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.async.GyroLimitsExceededAsyncData;
import com.orbotix.command.RawMotorCommand;
import com.orbotix.command.RollCommand;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.common.sensor.AccelerometerData;
import com.orbotix.common.sensor.AttitudeData;
import com.orbotix.common.sensor.AttitudeSensor;
import com.orbotix.common.sensor.DeviceSensorsData;
import com.orbotix.common.sensor.GyroData;
import com.orbotix.common.sensor.LocatorData;
import com.orbotix.common.sensor.SensorFlag;
import com.orbotix.subsystem.SensorControl;

import java.util.ArrayList;
import java.util.List;

import de.hs_esslingen.utils.BlinkTimer;
import de.hs_esslingen.utils.Coordinates;

/**
 * Created by Steven on 18.05.2017.
 */

public class TestDrive implements ResponseListener {
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
    private List<Coordinates> lastpositions = new ArrayList<Coordinates>();
    private List<GyroData> lastgyrodata = new ArrayList<GyroData>();
    public TestDrive(ConvenienceRobot mRobot){
        if(mRobot != null) {
            Log.i("TestDrive", "TestDrive begins");
            this.mRobot = mRobot;
            long sensorFlag = SensorFlag.ACCELEROMETER_NORMALIZED.longValue() | SensorFlag.GYRO_NORMALIZED.longValue();
            mRobot.enableSensors(sensorFlag, SensorControl.StreamingRate.STREAMING_RATE10);
            mRobot.enableLocator(true);
            mRobot.enableCollisions(true);
            mRobot.addResponseListener(this);
            int[] color = {204, 255, 51};
        /*for(int i = 1; i< 20 ; i++) {
            BlinkTimer bliker = new BlinkTimer(i, 50, this.mRobot, color);
        }*/
        }
    }
    public void startTestDrive(){
        if(mRobot != null) {
            mRobot.setLed(0.1f, 0.1f, 0.1f);
            mRobot.drive(0f,0.3f);
        }
    }

    @Override
    public void handleResponse(DeviceResponse deviceResponse, Robot robot) {
        Log.i("DeviceResponse", "Response " + deviceResponse.toString());
        Log.i("DeviceResponse", "Robot " + robot.getName());
    }

    @Override
    public void handleStringResponse(String s, Robot robot) {
        Log.i("StringResponse", "sResponse " + s);
        Log.i("StringResponse", "Robot " + robot.getName());
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
                if (lastpositions.size() == 50 )
                   this.lastpositions.remove(0);
                this.lastpositions.add(new Coordinates(locatorData.getPositionX(),locatorData.getPositionY()));
                Log.i("Locator","Fetched Coordinates:" + lastpositions.size() +"x: " +locatorData.getPositionX() + "\t Y: " + locatorData.getPositionY());
            }catch (Exception ex){Log.i("TestDrive", "no location data");}
            try {
                //gyroData = dsd.getGyroData();

                Log.i("gyroData Rotation","\tYaw "+dsd.getAttitudeData().yaw+"\tpitch "+dsd.getAttitudeData().pitch+"\troll "+dsd.getAttitudeData().roll);
                GyroData gyroData = dsd.getGyroData();
                this.lastgyrodata.add(gyroData);
                Log.i("gyroData Rotation","\tRotatioinY "+gyroData.getRotationRateFiltered().y + "\tRotatioinX: " +gyroData.getRotationRateFiltered().x  + "\tRotatioinZ " +gyroData.getRotationRateFiltered().z);
            }catch (Exception ex){
                //Log.i("gyroData", "no gyro data");
            }
            try {
                AccelerometerData accelerometerData =  dsd.getAccelerometerData();
            }catch (Exception ex){Log.i("accelerometerData", "no Acc data");}
        }

        if (asyncMessage instanceof CollisionDetectedAsyncData) {
            if(this.mRobot.getRobot() == robot){
                if(!handeledCollision) {                    Coordinates ac = lastpositions.get(lastpositions.size() - 1);
                    Coordinates bc = lastpositions.get(lastpositions.size() - 3);
                    mRobot.setLed(1f,0.1f,0.1f);
                    handleCollision(ac,bc);
                }
            }
        }

    }
    private void handleCollision(Coordinates ac, Coordinates bc){
        for(Coordinates tmp : lastpositions)
            Log.i("TestDrive","x: " +tmp.x + " Y: " + tmp.y + "update: " + tmp.updated);
        Log.i("TestDrive","bc x: " +bc.x + " bc Y: " + bc.y + "bc update: " + bc.updated);
        Log.i("TestDrive","ac x: " +ac.x + " ac Y: " + ac.y + "ac update: " + ac.updated);
        Log.i("TestDrive","Heading:" + mRobot.getLastHeading());
        mRobot.setLed(0.1f,0f,0.1f);
        mRobot.rotate(0.9f);

    }
}
