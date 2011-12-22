package ros.zeroconf.android.jmdns.master_browser;

import javax.jmdns.ServiceInfo;

import ros.zeroconf.jmdns.ZeroconfListener;

public class Listener implements ZeroconfListener {
	public void serviceAdded(ServiceInfo service) {
		android.util.Log.i("zeroconf", "serviceAdded");	
	}
	public void serviceRemoved(ServiceInfo service) {
		android.util.Log.i("zeroconf", "serviceRemoved");	
	}
	public void serviceResolved(ServiceInfo service) {
		android.util.Log.i("zeroconf", "serviceResolved");	
	}
}
