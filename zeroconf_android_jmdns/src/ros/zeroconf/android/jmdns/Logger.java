package ros.zeroconf.android.jmdns;

import java.lang.String;
import ros.zeroconf.jmdns.ZeroconfLogger;

public class Logger implements ZeroconfLogger {

	public void println(String msg) {
		android.util.Log.i("zeroconf", msg);
	}
}
