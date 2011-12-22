package ros.zeroconf.android.jmdns.master_browser;

import java.lang.Thread;
import java.util.List;

import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import ros.zeroconf.jmdns.Zeroconf;
import ros.zeroconf.android.jmdns.Logger;

// adb logcat System.out:I *:S
// adb logcat zeroconf:I *:S

/**
 * This test does : 
 * 
 * 0) publish a _ros-master._tcp service (DudeMaster)
 * 1) listens for _ros-master._tcp services
 * 2) every second it will display currently found services
 * 3) On destroy, deactivate listeners
 * 4) On destroy, remove all services
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

	/****************************************
	 * Threads, Tasks and Handlers
	 ****************************************/
	private class DiscoveryTask extends AsyncTask<Zeroconf, String, Void> {

		protected Void doInBackground(Zeroconf... zeroconfs) {
			if ( zeroconfs.length == 1 ) {
				Zeroconf zconf = zeroconfs[0];
		        zconf.addListener("_ros-master._tcp","local");
		        publishProgress("*********** Discovering Ros Masters **************");
		        while( true ) {
		    		try {
		    			List<ServiceInfo> service_infos = zconf.listDiscoveredServices();
		    			publishProgress("------------------------------------------");
		    			if ( service_infos.size() > 0 ) {
			    			for ( ServiceInfo service_info : service_infos ) {
				        		publishProgress(zconf.toString(service_info));
			    			}
		    			} else {
			    			publishProgress("...");
		    			}
		        		Thread.sleep(3000L);
				    } catch (InterruptedException e) {
				        e.printStackTrace();
				    }
		        }
			} else {
				publishProgress("Error - DiscoveryTask::doInBackground received #zeroconfs != 1");
			}
			return null;
	    }

	    protected void onProgressUpdate(String... progress) {
	    	TextView tv = (TextView) findViewById(R.id.mytextview);
	    	for (String msg : progress ) {
	    		android.util.Log.i("zeroconf", msg);	
		    	tv.append(msg + "\n");
	    	}
	    	int line_count = tv.getLineCount(); 
	    	int view_height = tv.getHeight();
	    	int pixels_per_line = tv.getLineHeight();
	    	int pixels_difference = line_count*pixels_per_line - view_height;
	    	if ( pixels_difference > 0 ) {
	    		tv.scrollTo(0, pixels_difference);
	    	}
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView tv = (TextView)findViewById(R.id.mytextview);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText("");
        logger = new Logger();
		zeroconf = new Zeroconf(logger);

        new DiscoveryTask().execute(zeroconf);
    }
    
    @Override
    public void onDestroy() {
    	logger.println("*********** Zeroconf Destroy **************");
        zeroconf.removeListener("_ros-master._tcp","local");
		zeroconf.removeAllServices();
		super.onDestroy();
    }
}
