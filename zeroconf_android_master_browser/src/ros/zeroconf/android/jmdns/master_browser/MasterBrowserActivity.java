package ros.zeroconf.android.jmdns.master_browser;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import javax.jmdns.ServiceInfo;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import ros.zeroconf.jmdns.Zeroconf;
import ros.zeroconf.jmdns.ZeroconfDiscoveryHandler;
import ros.zeroconf.android.jmdns.Logger;
import ros.zeroconf.android.jmdns.master_browser.R;
import ros.zeroconf.android.jmdns.master_browser.DiscoveryHandler;
import ros.zeroconf.android.jmdns.master_browser.DiscoveredServiceAdapter;
import org.ros.message.zeroconf_comms.DiscoveredService;

// adb logcat System.out:I *:S
// adb logcat zeroconf:I *:S

/**
 * Master browser does discovery for the following services
 * (both xxx._tcp and xxx._udp):
 * 
 * - _ros-master  
 * - _concert-master
 * - _app-manager
 * 
 * Easiest way to test is to use avahi to publish/browse on the other end. In a linux shell:
 * 
 * @code
 * > avahi-publish -s DudeMaster _ros-master._tcp 8882
 * > avahi-publish -s ConcertMaster _concert-master._tcp 8883
 * > avahi-publish -s DudeMasterApps _app-manager._tcp 8884
 * @endcode
 *
 * Then run this program on your android while it is connected to the lan.
 */
public class MasterBrowserActivity extends Activity {

	private void scrollToBottom() {
    	int line_count = tv.getLineCount(); 
    	int view_height = tv.getHeight();
    	int pixels_per_line = tv.getLineHeight();
    	int pixels_difference = line_count*pixels_per_line - view_height;
    	if ( pixels_difference > 0 ) {
    		tv.scrollTo(0, pixels_difference);
    	}
	}
	
	/********************
	 * Discovery Task
	 *******************/
    private class DiscoverySetupTaskOld extends AsyncTask<Zeroconf, String, Void> {

		private ProgressDialog commencing_dialog;
		
        protected Void doInBackground(Zeroconf... zeroconfs) {
            if ( zeroconfs.length == 1 ) {
                Zeroconf zconf = zeroconfs[0];
                android.util.Log.i("zeroconf", "*********** Discovery Commencing **************");	
				try {
		    		Thread.sleep(2000L);
			    } catch (InterruptedException e) {
			        e.printStackTrace();
			    }
                zconf.addListener("_ros-master._tcp","local");
                zconf.addListener("_ros-master._udp","local");
                zconf.addListener("_concert-master._tcp","local");
                zconf.addListener("_concert-master._udp","local");
                zconf.addListener("_app-manager._tcp","local");
                zconf.addListener("_app-manager._udp","local");
            } else {
                publishProgress("Error - DiscoveryTask::doInBackground received #zeroconfs != 1");
            }
            return null;
        }

	    protected void onProgressUpdate(String... progress) {
	        for (String msg : progress ) {
	            android.util.Log.i("zeroconf", msg);
	            tv.append(msg + "\n");
	    	}
	    	scrollToBottom();
		}
	    
	    protected void onPreExecute() {
			commencing_dialog = ProgressDialog.show(MasterBrowserActivity.this,
					"Zeroconf Discovery", "Adding listeners...", true);
	    }
	    protected void onPostExecute(Void result) {
	    	commencing_dialog.dismiss();
	    }
    }
	
	/********************
	 * Variables
	 *******************/
	private Zeroconf zeroconf;
	private Logger logger;
	private ListView lv;
	private ArrayList<DiscoveredService> discovered_services;
    private DiscoveredServiceAdapter discovery_adapter;
	private TextView tv;
	private DiscoveryHandler discovery_handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        discovered_services = new ArrayList<DiscoveredService>();
        setContentView(R.layout.main);
        lv = (ListView)findViewById(R.id.discovered_services_view);
        discovery_adapter = new DiscoveredServiceAdapter(this, discovered_services);
        lv.setAdapter(discovery_adapter);
        tv = (TextView)findViewById(R.id.mytextview);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText("");

        logger = new Logger();
		zeroconf = new Zeroconf(logger);
		discovery_handler = new DiscoveryHandler(tv, discovery_adapter, discovered_services);
		zeroconf.setDefaultDiscoveryCallback(discovery_handler);
		
		new DiscoverySetupTaskOld().execute(zeroconf);
    }
    
    @Override
    public void onDestroy() {
    	logger.println("*********** Zeroconf Destroy **************");
        zeroconf.removeListener("_ros-master._tcp","local");
	    zeroconf.removeListener("_ros-master._udp","local");
	    zeroconf.removeListener("_concert-master._tcp","local");
	    zeroconf.removeListener("_concert-master._udp","local");
	    zeroconf.removeListener("_app-manager._tcp","local");
	    zeroconf.removeListener("_app-manager._udp","local");
		super.onDestroy();
    }
}
