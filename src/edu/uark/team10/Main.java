package edu.uark.team10;


import java.util.concurrent.atomic.AtomicInteger;

import com.jfastnet.Client;
import com.jfastnet.Config;
import com.jfastnet.Server;
import com.jfastnet.messages.GenericMessage;

public class Main {
    private static final AtomicInteger received = new AtomicInteger(0);

    // Entry point
    public static void main(String[] args)
    {
        // This will be the reference to our user interface
        final Application application = new Application();
        application.setVisible(true);
        
        // 
        // Server server = new Server(new Config().setBindPort(15150));
        // Client client = new Client(new Config().setPort(15150));

        // server.start();
        // client.start();
        // client.blockingWaitUntilConnected();

        // server.send(new PrintMessage("Hello Client!"));
        // client.send(new PrintMessage("Hello Server!"));

    }

	// public static class PrintMessage extends GenericMessage {

	// 	/** no-arg constructor required for serialization. */
	// 	private PrintMessage() {}

	// 	PrintMessage(Object object) { super(object); }

	// 	@Override
	// 	public void process(Object context) {
	// 		System.out.println(object);
	// 		received.incrementAndGet();
	// 	}
	// }
    
}
