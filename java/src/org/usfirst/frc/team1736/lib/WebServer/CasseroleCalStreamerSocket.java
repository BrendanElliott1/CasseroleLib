package org.usfirst.frc.team1736.lib.WebServer;
import java.io.IOException;
import java.util.TimerTask;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.usfirst.frc.team1736.lib.Calibration.Calibration;
import org.usfirst.frc.team1736.lib.Calibration.CalWrangler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.Timer;

public class CasseroleCalStreamerSocket extends WebSocketAdapter {
	volatile int test_data;
	
	
    @Override
    public void onWebSocketText(String message) {
        if (isConnected()) {
        	if(message.equals("save")){
        		
        	} else {
        		String[] messageParts = message.split(":");
        		//Parse 3-part messages
        		if(messageParts.length == 3){
        			String cmd = messageParts[0];
        			String name = messageParts[1];
        			double val = Double.parseDouble(messageParts[2]);
        			if(cmd.equals("set")){
	        			Calibration cal_to_update = CassesroleWebStates.getCalWrangler().getCalFromName(name);
	        			if(!Double.isFinite(val)){
	        				System.out.println("ERROR: CalStreamer: Invalid value recieved " + Double.toString(val));
	        			} else {
		        			cal_to_update.setOverride(val);
	        			}
        			}
    			//Parse 2-part messages
        		} else if(messageParts.length == 2){
        			String cmd = messageParts[0];
        			String name = messageParts[1];
        			if(cmd.equals("reset")){
	        			Calibration cal_to_update = CassesroleWebStates.getCalWrangler().getCalFromName(name);
	        			cal_to_update.reset();
        			}
        		} else {
        			System.out.println("ERROR: CalStreamer: Client returned garbage message " + message);
        		}
        	}
        	broadcastData();
        }
    }
    
    @Override
    public void onWebSocketConnect(Session sess) {

    	super.onWebSocketConnect(sess);
    	//On client connect, broadcast the current set of calibrations.
    	broadcastData();
    }
    
    @Override
    public void onWebSocketClose(int statusCode, String reason) {

    	super.onWebSocketClose(statusCode, reason);
    }
    
	/**
	 * send socket data out to client
	 */
	public void broadcastData() {
        if (isConnected()) {
        	Calibration[] allCals = CassesroleWebStates.getCalWrangler().registeredCals.toArray(new Calibration[CassesroleWebStates.getCalWrangler().registeredCals.size()]);
        	
            try {
            	JSONObject full_obj = new JSONObject();
            	JSONArray data_array = new JSONArray();
            	
            	//Package all Cal array elements into a JSON array
            	for(Calibration cal : allCals){
            		JSONObject single_obj = new JSONObject();
            		single_obj.put("name", cal.name);
            		single_obj.put("dflt_val", Double.toString(cal.getDefault()));
            		single_obj.put("min_val", Double.toString(cal.min_cal));
            		single_obj.put("max_val", Double.toString(cal.max_cal));
            		single_obj.put("ovrdn", Boolean.toString(cal.overridden));
            		single_obj.put("cur_val", Double.toString(cal.get()));
            		data_array.add(single_obj);
            	}
            	
            	//package array into object
            	full_obj.put("cal_array", data_array);
        		getRemote().sendString(full_obj.toJSONString());
        		
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
	}

}