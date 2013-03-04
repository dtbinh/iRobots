import lejos.nxt.*;
import java.io.*;

import lejos.nxt.comm.*;

/**
 * Test of Java streams over USB.
 * Run the PC example, USBSend, to send data.
 * 
 * @author Lawrie Griffiths
 *
 */
public class USBSend 
{
	private USBConnection conn;
	private DataOutputStream dOut;
	private DataInputStream dIn;
	
	public USBSend()
	{
		conn = null;
		conn = USB.waitForConnection();
		dOut = conn.openDataOutputStream();
		dIn = conn.openDataInputStream();
	}
	
	public void sendData(String s) throws IOException
	{
		System.out.println("Sending");
		dOut.writeUTF(s);
		dOut.flush();
		System.out.println("Sent: " + s);
	}
	
	public USBConnection getState()
	{
		return this.conn;
	}
}
