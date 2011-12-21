package ros.zeroconf.android.jmdns.demos;

import java.lang.Thread;
import java.util.List;

import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import ros.zeroconf.jmdns.Zeroconf;
import ros.zeroconf.android.jmdns.Logger;

// adb logcat System.out:I *:S
// adb logcat zeroconf:I *:S

/**
 * This test does : 
 * 
 * 1) listens for _ros-master._tcp services
 * 2) every second for 10 seconds it will display currently found services
 * 3) deactivate listeners
 * 4) publish a _ros-master._tcp service (DudeMaster)
 * 5) wait 10 seconds
 * 6) remove all services
 * 
 * Best way to use it is to use avahi to publish/browse on the other end, in oen shell:
 * 
 * > avahi-publish -s ConcertMaster _ros-master._tcp 8883
 * 
 * In another window:
 * 
 * > avahi-browse -r _ros-master._tcp
 * 
 */
public class ZeroconfActivity extends Activity {

	/********************
	 * Background Thread
	 *******************/
	private class PublisherTask extends AsyncTask<Zeroconf, String, Void> {

		protected Void doInBackground(Zeroconf... zeroconfs) {
			if ( zeroconfs.length == 1 ) {
				Zeroconf zconf = zeroconfs[0];
				android.util.Log.i("zeroconf", "*********** Zeroconf Publisher Test **************");
				zconf.addService("DudeMaster", "_ros-master._tcp", "local", 8888, "Dude's test master");//	            totalSize += Downloader.downloadFile(urls[i]);
				android.util.Log.i("zeroconf", "**************************************************");
			} else {
				android.util.Log.i("zeroconf", "Error - PublisherTask::doInBackground received #zeroconfs != 1");
			}
			return null;
	    }
	}

	private class DiscoveryTask extends AsyncTask<Zeroconf, String, Void> {

		protected Void doInBackground(Zeroconf... zeroconfs) {
			if ( zeroconfs.length == 1 ) {
				Zeroconf zconf = zeroconfs[0];
				android.util.Log.i("zeroconf", "*********** Zeroconf Discovery Test **************");
		        zconf.addListener("_ros-master._tcp","local");
		        int i = 0;
		        while( i < 10 ) {
		    		try {
		    			android.util.Log.i("zeroconf", "************ Discovered Services ************");
		    			List<ServiceInfo> service_infos = zconf.listDiscoveredServices();
		    			for ( ServiceInfo service_info : service_infos ) {
			        		zconf.display(service_info);
		    			}
		        		Thread.sleep(1000L);
				    } catch (InterruptedException e) {
				        e.printStackTrace();
				    }
		    		++i;
		        }
		        zconf.removeListener("_ros-master._tcp","local");
	//			publishProgress("midway");
			} else {
				android.util.Log.i("zeroconf", "Error - DiscoveryTask::doInBackground received #zeroconfs != 1");
			}
			return null;
	    }

	    protected void onProgressUpdate(String... progress) {
	    	for (String msg : progress ) {
	    		logger.println("Progress update: " + msg);	
	    	}
	        //setProgressPercent(progress[0]);
	    }
	}

	/********************
	 * Variables
	 *******************/
	private Zeroconf zeroconf;
	private Logger logger;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	// gui
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
		tv.setText("Dude is babbling.");
        setContentView(tv);
        logger = new Logger();
        zeroconf = new Zeroconf(logger);

        new PublisherTask().execute(zeroconf);
        new DiscoveryTask().execute(zeroconf);
    }
    
    @Override
    public void onDestroy() {
    	logger.println("*********** Zeroconf Destroy **************");
		zeroconf.removeAllServices();
		super.onDestroy();
    }
}
