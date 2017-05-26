package de.hs_esslingen.sprk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.orbotix.ConvenienceRobot;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.sensor.SensorFlag;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotRadioDescriptor;
import com.orbotix.subsystem.SensorControl;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity implements
        DiscoveryAgentEventListener,
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        RobotChangedStateListener{
        DiscoveryAgentLE mDiscoveryAgent;
        ConvenienceRobot mRobot;
        Button testdbtn,jumpbtn;
        SeekBar seekBar;
        TextView speedLabel;
        TestDrive testDrive;
        Jump jump;
        float driveSpeed = 0.5f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        seekBar = (SeekBar) findViewById(R.id.speedbar);
        seekBar.setOnSeekBarChangeListener(this);
        speedLabel = (TextView)findViewById(R.id.labSpeed);
        speedLabel.setText(String.valueOf(driveSpeed));
        testdbtn = (Button) findViewById(R.id.Testdrive);
        testdbtn.setOnClickListener(this);
        testdbtn.setEnabled(false);
        jumpbtn = (Button) findViewById(R.id.Jump);
        jumpbtn.setOnClickListener(this);
        jumpbtn.setEnabled(true);

    }

    @Override
    public void handleRobotsAvailable(List<Robot> robots) {
        Log.i("Sphero", "Found " + robots.size() + " robots");
        for (Robot robot : robots) {
            Log.i("Sphero", "  " + robot.getName());
            if(mRobot == null) {
                if(!mDiscoveryAgent.getConnectedRobots().contains(robot)) {
                    mDiscoveryAgent.connect(robot);
                    mDiscoveryAgent.stopDiscovery();
                }
            }
        }
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateListener.RobotChangedStateNotificationType robotChangedStateNotificationType) {
        switch (robotChangedStateNotificationType) {
            case Online:
                Log.i("Sphero", "Robot " + robot.getName() + " Online!");
                mRobot = new ConvenienceRobot(robot);
                long sensorFlag = SensorFlag.QUATERNION.longValue()
                        | SensorFlag.ACCELEROMETER_NORMALIZED.longValue()
                        | SensorFlag.GYRO_NORMALIZED.longValue()
                        | SensorFlag.MOTOR_BACKEMF_NORMALIZED.longValue()
                        | SensorFlag.ATTITUDE.longValue()
                        | SensorFlag.LOCATOR.longValue();

                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot( robot );
                //Enable sensors based on the flag defined above, and stream their data ten times a second to the mobile device
                mRobot.enableSensors( sensorFlag, SensorControl.StreamingRate.STREAMING_RATE10 );
                testdbtn.setEnabled(true);
            case Connecting:
                Log.i("Sphero", "Robot " + robot.getName() + " Connecting!");
                break;
            case Connected:
                Log.i("Sphero", "Robot " + robot.getName() + " Connected!");
                break;
            // Handle other cases
        }
    }

    private void startDiscovery() {
        mDiscoveryAgent = DiscoveryAgentLE.getInstance();
        mDiscoveryAgent.addDiscoveryListener(this);
        mDiscoveryAgent.addRobotStateListener(this);

        RobotRadioDescriptor robotRadioDescriptor = new RobotRadioDescriptor();
        robotRadioDescriptor.setNamePrefixes(new String[]{"SK-"});
        mDiscoveryAgent.setRadioDescriptor(robotRadioDescriptor);

        try {
            mDiscoveryAgent.startDiscovery(this);
        } catch (DiscoveryException e) {
            Log.e("Sphero", "Discovery Error: " + e);
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Jump:
                jump = new Jump(mRobot,driveSpeed);
                break;
            case R.id.Testdrive:
                testDrive = new TestDrive(mRobot,driveSpeed);
                testDrive.startTestDrive();
              break;
            case R.id.Disconnect:
                if (mRobot != null) {
                    mRobot.stop();
                    mRobot.disconnect();
                    mRobot = null;
                }
                break;
            case R.id.Connect:
                startDiscovery();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRobot.setLed(125,125,125);
        mRobot.disconnect();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        driveSpeed = seekBar.getProgress()/100f;
        speedLabel.setText(String.valueOf(driveSpeed));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        driveSpeed = seekBar.getProgress()/100f;
        testDrive.setROBOT_SPEED(driveSpeed);

        Log.i("Set Speed",driveSpeed+"");
    }
}
