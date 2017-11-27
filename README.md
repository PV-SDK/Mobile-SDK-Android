# Mobile-SDK-Android
The API for UAV by PowerVision

## Product introductions:
PV-SDK is a software component aimed at obtaining the extended development kit of the PowerVision aircraft(PowerEye and other products will open one after another) （PV-SDK supports Android and iOS）. PV-SDK will only open the transmission and control functions of the universal mounting interface at the present stage(own control, attitude, PTZ Control,camera setting control,remote control link and other things will be completed progressively). By using the API functions provided in the SDK, collecting, sending and controlling the data of the aircraft with general mount via mobile planes, developers can integrate the user's product into the power vision plane.

## SDK contents 

Users can access API functions of PowerEye universal mounting in API.（iOS&Android）  
Sample code and tutorial(Users will have preliminary understandings through sample codes in demo)    
Developer's Guide and API document.

## Required
    PowerEye
    Environment(JDK7+  AndroidStudio 3.0+)

## Calling Steps
 
  Initialize sdk  
 mPowerSDK = PowerSDK.getInstance();  
  Connect device    
 mPowerSDK.startConnectSDK(ConnectIpAndPortFactory.getEggConnectIpAndPortFactory());    
  Set up connection monitor    
 mPowerSDK.addConnectListener(simpleConnectListener);   
  The callback for connection (For a simple example, refer to the specific sdk example) 
``` java 
ConnectListener.SimpleConnectListener simpleConnectListener = new ConnectListener.SimpleConnectListener() {
    @Override
    public void onChainConnected() {
        super.onChainConnected();
        Log.e(TAG, "onChainConnected");
    }

    @Override
    public void onDroneConnected() {
        super.onDroneConnected();
        Log.e(TAG, "onDroneConnected");
    }

    @Override
    public void onDeviceDisconnected() {
        super.onDeviceDisconnected();
        Log.e(TAG, "onDeviceDisconnected");
    }

    @Override
    public void onChainDisconnected() {
        super.onChainDisconnected();
        Log.e(TAG, "onChainDisconnected");
    }
  };
  ```
Demo and the SDK document instructions for more details about api.(include apk and doc.)  

**The following picture is PowerEye**
![](https://github.com/kjergit/dailypractice/blob/master/powerEye.jpg?raw=true)

For more information about products,please visit our [website](http://www.powervision.me/en/)
