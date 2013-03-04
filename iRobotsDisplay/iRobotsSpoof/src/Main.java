import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.Button;

public class Main 
{
	private USBSend usb;
    static volatile boolean stop = false;

    public Main()
    {
    	NXTBee xbee = new NXTBee();
        Thread t = new Thread(xbee);
        t.setDaemon(true);
        t.start();
        
        final DataOutputStream out = new DataOutputStream(xbee.getOutputStream());
        final DataInputStream in = new DataInputStream(xbee.getInputStream());
        
        try {
            out.writeUTF("NXT Joined");
            System.out.println("I joined XBee");
            System.out.println("Connection USB...");
            usb = new USBSend();
            System.out.println("...connected USB :) ");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        Thread rt = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while(!stop) {
                        if(in.available() > 0) {
                            String s = in.readUTF();
                            if(s != null) {
                                usb.sendData(s);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        rt.start();
        
        try {
            while(!Button.ESCAPE.isDown()) {
                if(Button.ENTER.isDown()) {
                    out.writeUTF("Button pressed");
                    out.flush();
                }
                while(Button.ENTER.isDown());
            }
            out.writeUTF("NXT left");
            stop = true;
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) 
    {
        new Main();        
    }

}
