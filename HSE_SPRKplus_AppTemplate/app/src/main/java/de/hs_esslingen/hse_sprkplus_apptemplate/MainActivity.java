package de.hs_esslingen.hse_sprkplus_apptemplate;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

import de.hs_esslingen.opengl.MyGLSurfaceView;

public class MainActivity extends AppCompatActivity implements DiscoveryAgentEventListener,
        RobotChangedStateListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static MyGLSurfaceView mGLView;

    private static ConvenienceRobot mRobot;
    private static TestDrive testDrive;
    private static Jump jump;
    private static DiscoveryAgentLE mDiscoveryAgent;
    private static float driveSpeed = 0.5f;





    public static TestDrive getTestDrive() {
        return testDrive;
    }

    public static Jump getJump() {
        return jump;
    }

    public static DiscoveryAgentLE getmDiscoveryAgent() {
        return mDiscoveryAgent;
    }

    public static float getDriveSpeed() {
        return driveSpeed;
    }

    public static void setTestDrive(TestDrive testDrive) {
        MainActivity.testDrive = testDrive;
    }

    public static void setJump(Jump jump) {
        MainActivity.jump = jump;
    }

    public void setDriveSpeed(float driveSpeed) {
        this.driveSpeed = driveSpeed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mGLView = new MyGLSurfaceView(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }
        private Button testdbtn,jumpbtn, connectbtn,disconnect;
        private SeekBar seekBar;
        private TextView speedLabel,scanSpeedLabel;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    seekBar = (SeekBar) rootView.findViewById(R.id.speedbar);
                    seekBar.setOnSeekBarChangeListener(this);
                    speedLabel = (TextView)rootView.findViewById(R.id.labSpeed);
                    speedLabel.setText(String.valueOf(driveSpeed));
                    scanSpeedLabel = (TextView)rootView.findViewById(R.id.labsSpeed) ;
                    scanSpeedLabel.setText(String.valueOf(driveSpeed/2f));
                    testdbtn = (Button) rootView.findViewById(R.id.Testdrive);
                    testdbtn.setOnClickListener(this);
                    testdbtn.setEnabled(true);
                    jumpbtn = (Button) rootView.findViewById(R.id.Jump);
                    jumpbtn.setOnClickListener(this);
                    jumpbtn.setEnabled(true);
                    connectbtn = (Button)rootView.findViewById(R.id.Connect);
                    connectbtn.setOnClickListener(this);
                    disconnect = (Button)rootView.findViewById(R.id.Disconnect);
                    disconnect.setOnClickListener(this);
                    break;
                case 2:
                    rootView = mGLView = new MyGLSurfaceView(this.getActivity());
                    break;
                case 3:

                    break;
            }
            return rootView;
        }

        @Override
        public void onClick(View v) {
            MainActivity mainActivity = (MainActivity) getActivity();
            switch (v.getId()) {
                case R.id.Jump:
                    jump = new Jump(mRobot,TestDrive.ROBOT_SPEED);
                    break;
                case R.id.Testdrive:
                    testDrive = new TestDrive(mRobot,TestDrive.ROBOT_SPEED);
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
                    mainActivity.startDiscovery();
                    break;
            }
        }
        // SeekBar
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TestDrive.ROBOT_SPEED = seekBar.getProgress()/100f;
            speedLabel.setText(String.valueOf(TestDrive.ROBOT_SPEED));
            scanSpeedLabel.setText(String.valueOf(TestDrive.ROBOT_SPEED/2f));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            testDrive.ROBOT_SPEED = seekBar.getProgress()/100f;
            testDrive.setROBOT_SCAN_SPEED(testDrive.ROBOT_SPEED/2f);

            Log.i("Set Speed",driveSpeed+"");
        }
    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRobot.setLed(125,125,125);
        mRobot.disconnect();
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
                mRobot.enableSensors( sensorFlag, SensorControl.StreamingRate.STREAMING_RATE50 );
            case Connecting:
                Log.i("Sphero", "Robot " + robot.getName() + " Connecting!");
                break;
            case Connected:
                Log.i("Sphero", "Robot " + robot.getName() + " Connected!");
                break;
            // Handle other cases
        }
    }
    public void startDiscovery() {
        mDiscoveryAgent = DiscoveryAgentLE.getInstance();
        mDiscoveryAgent.addDiscoveryListener(this);
        mDiscoveryAgent.addRobotStateListener(this);

        RobotRadioDescriptor robotRadioDescriptor = new RobotRadioDescriptor();
        robotRadioDescriptor.setNamePrefixes(new String[]{"SK-684B"});
        mDiscoveryAgent.setRadioDescriptor(robotRadioDescriptor);

        try {
            mDiscoveryAgent.startDiscovery(this);
        } catch (DiscoveryException e) {
            Log.e("Sphero", "Discovery Error: " + e);
            e.printStackTrace();
        }
    }
}
