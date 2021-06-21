package temp.cv.com.tempreader.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 * Created by Administrator on 2020-09-01.
 */

public class USBDeviceReceive extends BroadcastReceiver {
    private String TAG = "USBDeviceReceive";
    private USBReceiverInterface cbk;
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action != null) {

            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    final UsbDevice detDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    final String detMsg="Device DEtached";
                    Log.d(TAG,detMsg+" "+detDevice);
                    if(cbk != null)
                    {
                        cbk.USBDetached(detDevice);
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                    final UsbDevice attDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    final String attMsg="Device atached";
                    Log.d(TAG,attMsg+" "+attDevice);
                    if(cbk != null)
                    {
                        cbk.USBAttateched(attDevice);
                    }
                    break;
                default:
                    // Nothing to do
                    break;
            } // END SWITCH
        }
    }

    public interface USBReceiverInterface {
        public void USBAttateched(final UsbDevice newDevice);

        public void USBDetached(final UsbDevice removedDevice);
    }

    public void setUsbDeviceListener(USBReceiverInterface brInteraction) {
        this.cbk = brInteraction;
    }
}
