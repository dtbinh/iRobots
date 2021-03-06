package info;

import info.Social.State;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class BluetoothConnection extends Thread {
	private BTConnection con;
	private DataOutputStream out;
	private DataInputStream in;
	private Social social;
	
	private volatile boolean connected;
	
	public BluetoothConnection(Social s)
	{
		this.social = s;
		connected = false;
		
		start();
	}
	
	@Override
	public void run() {
		con = Bluetooth.waitForConnection();
		
		System.out.println("Connected");
		
		connected = true;
		Main.run = true;
		out = con.openDataOutputStream();
		in = con.openDataInputStream();
		
		try {
			sendStatusUpdate(social.getState());
			
			while(Main.run)
			{

				switch(in.readInt())
				{
				case 1:
					social.alwaysSetState(State.shy);
					break;
				case 2:
					social.alwaysSetState(State.curious);
					break;
				case 3:
					social.alwaysSetState(State.aggressive);
					break;
				case 4:
					social.setChangeState(false);
					break;
				case 5:
					social.setChangeState(true);
					break;
				case 6:
					Main.pause = !Main.pause;
					if(Main.pause)
					{
						out.writeInt(4);
						out.flush();
					}
					else
					{
						sendStatusUpdate(social.getState());
					}
					break;
				case 7:
					System.exit(0); 
				}
			} 
		} catch (IOException e) {}
	}
	
	/*
	 * 1 = shy
	 * 2 = curious
	 * 3 = aggressive
	 * 0 = disconnected
	 * -1 = error followed by message
	 */
	public void sendStatusUpdate(State s) throws IOException
	{
		if(!connected)
		{
			System.out.println("Not connected");
			return;
		}
		
		switch(s)
		{
		case shy:
			out.writeInt(1);
			break;
		case curious:
			out.writeInt(2);
			break;
		case aggressive:
			out.writeInt(3);
			break;
		default:
			out.writeInt(0);
		}
		
		out.flush();
	}
	
	public void sendError(String m) throws IOException
	{
		if(connected)
		{
			out.writeInt(-1);
			out.writeChars(m);
		}
	}
}
