package ros.zeroconf.android.jmdns.master_browser;

import android.content.Context;
import ros.zeroconf.jmdns.Zeroconf;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/********************
 * Discovery Task
 *******************/
public class DiscoverySetupTask extends AsyncTask<Zeroconf, String, Void> {

	private ProgressDialog commencing_dialog; 
	private final Context context;

	public DiscoverySetupTask(Context context) {
		this.context = context;
	}
	
    protected Void doInBackground(Zeroconf... zeroconfs) {
        if ( zeroconfs.length == 1 ) {
//            Zeroconf zconf = zeroconfs[0];
//			discovery_handler = new DiscoveryHandler();
//            zconf.setDefaultDiscoveryCallback(discovery_handler);
//            android.util.Log.i("zeroconf", "*********** Discovery Commencing **************");	
//			zeroconf.setDefaultDiscoveryCallback(discovery_handler);
//			try {
//	    		Thread.sleep(2000L);
//		    } catch (InterruptedException e) {
//		        e.printStackTrace();
//		    }
//            zconf.addListener("_ros-master._tcp","local");
//            zconf.addListener("_ros-master._udp","local");
//            zconf.addListener("_concert-master._tcp","local");
//            zconf.addListener("_concert-master._udp","local");
//            zconf.addListener("_app-manager._tcp","local");
//            zconf.addListener("_app-manager._udp","local");
        } else {
            publishProgress("Error - DiscoveryTask::doInBackground received #zeroconfs != 1");
        }
        return null;
    }

    protected void onProgressUpdate(String... progress) {
//        for (String msg : progress ) {
//            android.util.Log.i("zeroconf", msg);
//            tv.append(msg + "\n");
//    	}
//    	scrollToBottom();
	}
    
    protected void onPreExecute() {
//		commencing_dialog = ProgressDialog.show(MasterBrowserActivity.this,
//				"Zeroconf Discovery", "Adding listeners...", true);
    }
    protected void onPostExecute(Void result) {
//    	commencing_dialog.dismiss();
    }
}
