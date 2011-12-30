package ros.zeroconf.android.jmdns.master_browser;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import javax.jmdns.ServiceInfo;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
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
import ros.zeroconf.android.jmdns.master_browser.DiscoveredServiceAdapter;
import org.ros.message.zeroconf_comms.DiscoveredService;

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
public class MasterBrowserActivity extends Activity implements OnItemClickListener {

	/*************************************************************************
	 * Threads, Tasks and Handlers
	 ************************************************************************/
	
	/*********************
	 * Discovery Handler
	 ********************/
	public class DiscoveryHandler implements ZeroconfDiscoveryHandler {

		/*********************
		 * Tasks
		 ********************/
		private class ServiceAddedTask extends AsyncTask<DiscoveredService, String, Void> {
			
		    protected Void doInBackground(DiscoveredService... services) {
		        if ( services.length == 1 ) {
		            DiscoveredService service = services[0];
					String result = "[+] Service added: " + service.name + "." + service.type + "." + service.domain + ".";
					publishProgress(result);
		        } else {
		            publishProgress("Error - ServiceAddedTask::doInBackground received #services != 1");
		        }
		        return null;
		    }

		    protected void onProgressUpdate(String... progress) {
		    	uiLog(progress);
			}
		}

		private class ServiceResolvedTask extends AsyncTask<DiscoveredService, String, DiscoveredService> {
			
		    protected DiscoveredService doInBackground(DiscoveredService... services) {
		        if ( services.length == 1 ) {
		            DiscoveredService discovered_service = services[0];
			    	String result = "[=] Service resolved: " + discovered_service.name + "." + discovered_service.type + "." + discovered_service.domain + ".\n";
			    	result += "    Port: " + discovered_service.port;
			    	for ( String address : discovered_service.ipv4_addresses ) {
			    		result += "\n    Address: " + address;
			    	}
			    	for ( String address : discovered_service.ipv6_addresses ) {
			    		result += "\n    Address: " + address;
			    	}
			    	publishProgress(result);
					int index = 0;
					for ( DiscoveredService s : discovered_services ) {
						if ( s.name.equals(discovered_service.name) ) {
							break;
						} else {
							++index;
						}
					}
					if ( index == discovered_services.size() ) {
						return discovered_service;
					} else {
						android.util.Log.i("zeroconf", "Tried to add an existing service (fix this)");
					}
		        } else {
		            publishProgress("Error - ServiceAddedTask::doInBackground received #services != 1");
		        }
		        return null;
		    }

		    protected void onProgressUpdate(String... progress) {
		    	uiLog(progress);
			}
		    
		    protected void onPostExecute(DiscoveredService discovered_service) {
		    	// add to the content and notify the list view if its a new service
		    	if ( discovered_service != null ) {
					discovered_services.add(discovered_service);
					adapter.notifyDataSetChanged();
		    	}
		    }
		}
		
		private class ServiceRemovedTask extends AsyncTask<DiscoveredService, String, DiscoveredService> {
			
		    protected DiscoveredService doInBackground(DiscoveredService... services) {
		        if ( services.length == 1 ) {
		            DiscoveredService discovered_service = services[0];
		            String result = "[-] Service removed: " + discovered_service.name + "." + discovered_service.type + "." + discovered_service.domain + ".\n";			    	result += "    Port: " + discovered_service.port;
			    	publishProgress(result);
			    	return discovered_service;
		        } else {
		            publishProgress("Error - ServiceAddedTask::doInBackground received #services != 1");
		        }
		        return null;
		    }

		    protected void onProgressUpdate(String... progress) {
		    	uiLog(progress);
			}
		    
		    protected void onPostExecute(DiscoveredService discovered_service) {
		    	// remove service from storage and notify list view
		    	if ( discovered_service != null ) {
					int index = 0;
					for ( DiscoveredService s : discovered_services ) {
						if ( s.name.equals(discovered_service.name) ) {
							break;
						} else {
							++index;
						}
					}
					if ( index != discovered_services.size() ) {
						discovered_services.remove(index);
						discovery_adapter.notifyDataSetChanged();
					} else {
						android.util.Log.i("zeroconf", "Tried to remove a non-existant service");
					}
		    	}
		    }
		}

		/*********************
		 * Variables
		 ********************/
		private ArrayList<DiscoveredService> discovered_services;
	    private DiscoveredServiceAdapter adapter;
		private TextView text_view;

		/*********************
		 * Constructors
		 ********************/
		public DiscoveryHandler(TextView tv, DiscoveredServiceAdapter discovery_adapter, ArrayList<DiscoveredService> discovered_services) {
			this.text_view = tv;
			this.adapter = discovery_adapter;
			this.discovered_services = discovered_services;
		}

		/*********************
		 * Callbacks
		 ********************/
		public void serviceAdded(DiscoveredService service) {
			new ServiceAddedTask().execute(service);
		}
		
		public void serviceRemoved(DiscoveredService service) {
			new ServiceRemovedTask().execute(service);
		}
		
		public void serviceResolved(DiscoveredService service) {
			new ServiceResolvedTask().execute(service);
		}

		/*********************
		 * Utility
		 ********************/
		private void uiLog(String... messages) {
	        for (String msg : messages ) {
	            android.util.Log.i("zeroconf", msg);
	            text_view.append(msg + "\n");
	    	}
	    	int line_count = text_view.getLineCount(); 
	    	int view_height = text_view.getHeight();
	    	int pixels_per_line = text_view.getLineHeight();
	    	int pixels_difference = line_count*pixels_per_line - view_height;
	    	if ( pixels_difference > 0 ) {
	    		text_view.scrollTo(0, pixels_difference);
	    	}
		}
	}

	private void scrollToBottom() {
    	int line_count = tv.getLineCount(); 
    	int view_height = tv.getHeight();
    	int pixels_per_line = tv.getLineHeight();
    	int pixels_difference = line_count*pixels_per_line - view_height;
    	if ( pixels_difference > 0 ) {
    		tv.scrollTo(0, pixels_difference);
    	}
	}

	/*******************
	 * Gui Callbacks
	 *******************/

	public void onItemClick(AdapterView adapter_view, View view, int position, long id) {
		android.util.Log.i("zeroconf", "You clicked the list");
		Toast.makeText(this, "You clicked entry #" + position ,Toast.LENGTH_LONG).show();
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
        lv.setOnItemClickListener(this);
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
