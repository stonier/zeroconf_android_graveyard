package ros.zeroconf.android.jmdns.master_browser;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
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
	private Handler handler;
	
	/*********************
	 * Discovery Handler
	 ********************/
	public class DiscoveryHandler implements ZeroconfDiscoveryHandler {
		
		public void serviceAdded(DiscoveredService service) {
			final DiscoveredService discovered_service = service;
			handler.post(new Runnable() {
				@Override
				public void run() {
			    	String result = "[+] Service added: " + discovered_service.name + "." + discovered_service.type + "." + discovered_service.domain + ".\n";
					android.util.Log.i("zeroconf", result);	
					tv.append(result);
			    	scrollToBottom();
				}
			});
		}
		public void serviceRemoved(DiscoveredService service) {
			final DiscoveredService discovered_service = service;
			handler.post(new Runnable() {
				@Override
				public void run() {
			    	String result = "[-] Service removed: " + discovered_service.name + "." + discovered_service.type + "." + discovered_service.domain + ".\n";
					android.util.Log.i("zeroconf", result);	
					tv.append(result);
					scrollToBottom();
					discovered_service_names.remove(discovered_service.name);
					adapter.notifyDataSetChanged();
				}
			});
		}
		public void serviceResolved(DiscoveredService service) {
			final DiscoveredService discovered_service = service;
			handler.post(new Runnable() {
				@Override
				public void run() {
			    	String result = "[=] Service resolved: " + discovered_service.name + "." + discovered_service.type + "." + discovered_service.domain + ".\n";
			    	result += "    Port   : " + discovered_service.port + "\n";
			    	for ( String address : discovered_service.ipv4_addresses ) {
			    		result += "    Address: " + address + "\n";
			    	}
			    	for ( String address : discovered_service.ipv6_addresses ) {
			    		result += "    Address: " + address + "\n";
			    	}
					android.util.Log.i("zeroconf", result);	
					tv.append(result);
			    	scrollToBottom();
					discovered_service_names.add(discovered_service.name);
					adapter.notifyDataSetChanged();
				}
			});
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

	/*************************************************************************
	 * Gui Callbacks
	 ************************************************************************/

	public void onItemClick(AdapterView adapter_view, View view, int position, long id) {
		android.util.Log.i("zeroconf", "You clicked the list");
		Toast.makeText(this, "You clicked entry #" + position ,Toast.LENGTH_LONG).show();
	}
	
	/********************
	 * Variables
	 *******************/
	private Zeroconf zeroconf;
	private Logger logger;
	private ListView lv;
	private ArrayAdapter<String> adapter;
	private TextView tv;
	private List<String> discovered_service_names;
	private String services_list[] = {"DudeMaster", "FooMaster"};
	private String new_services_list[] = {"BarMaster", "FooMaster"};
	private DiscoveryHandler discovery_handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        discovered_service_names = new ArrayList<String>();
        setContentView(R.layout.main);
        lv = (ListView)findViewById(R.id.discovered_services_view);
        lv.setOnItemClickListener(this);
//        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , services_list);
        adapter = new ArrayAdapter<String>(this, R.layout.row_layout, R.id.service_name, discovered_service_names);
        lv.setAdapter(adapter);
        tv = (TextView)findViewById(R.id.mytextview);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText("");
		handler = new Handler();

        logger = new Logger();
		zeroconf = new Zeroconf(logger);
		discovery_handler = new DiscoveryHandler();
		android.util.Log.i("zeroconf", "*********** Discovery Commencing **************");	
		zeroconf.setDefaultDiscoveryCallback(discovery_handler);
	    zeroconf.addListener("_ros-master._tcp","local");
	    zeroconf.addListener("_ros-master._udp","local");
	    zeroconf.addListener("_concert-master._tcp","local");
	    zeroconf.addListener("_concert-master._udp","local");
	    zeroconf.addListener("_app-manager._tcp","local");
	    zeroconf.addListener("_app-manager._udp","local");
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
