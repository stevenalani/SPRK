package de.hs_esslingen.sprk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.orbotix.ConvenienceRobot;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotRadioDescriptor;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DiscoveryAgentEventListener,View.OnClickListener,
    RobotChangedStateListener{
        DiscoveryAgentLE mDiscoveryAgent;
        ConvenienceRobot mRobot;
        Button starter;
        TestDrive testDrive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        starter = (Button) findViewById(R.id.button);
        starter.setOnClickListener(this);
        starter.setEnabled(true);
        startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRobot.disconnect();
    }

    @Override
    public void handleRobotsAvailable(List<Robot> robots) {
        Log.i("Sphero", "Found " + robots.size() + " robots");
        for (Robot robot : robots) {
            Log.i("Sphero", "  " + robot.getName());
            if(mRobot == null) {
                if(!mDiscoveryAgent.getConnectedRobots().contains(robot))
                    mDiscoveryAgent.connect(robot);
            }
        }
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateListener.RobotChangedStateNotificationType robotChangedStateNotificationType) {
        switch (robotChangedStateNotificationType) {
            case Online:
                Log.i("Sphero", "Robot " + robot.getName() + " Online!");
                mRobot = new ConvenienceRobot(robot);
                starter.setEnabled(true);
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
            case R.id.button:
                testDrive = new TestDrive(mRobot);
                break;
            case R.id.test:

                break;
        }
        testDrive = new TestDrive(mRobot);
    }
}