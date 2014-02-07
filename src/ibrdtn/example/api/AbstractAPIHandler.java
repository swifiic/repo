package ibrdtn.example.api;

import ibrdtn.api.APIException;
import ibrdtn.api.ExtendedClient;
import ibrdtn.api.object.Block;
import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.BundleID;
import ibrdtn.api.sab.Custody;
import ibrdtn.api.sab.StatusReport;
import ibrdtn.example.data.Envelope;
import in.swifiic.hub.lib.SwifiicHandler;
import in.swifiic.hub.lib.SwifiicHandler.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstracts functionality common to all classes implementing a CallbackHandler,
 * such as loading Bundles into the Bundle register
 * or marking Bundles as delivered.
 *
 * @author Julian Timpner <timpner@ibr.cs.tu-bs.de>
 */
public class AbstractAPIHandler implements ibrdtn.api.sab.CallbackHandler {

    private static final Logger logger = Logger.getLogger(AbstractAPIHandler.class.getName());
    protected PipedInputStream is;
    protected PipedOutputStream os;
    protected ExtendedClient client;
    protected ExecutorService executor;
    protected SwifiicHandler hndlr;
    protected Bundle bundle = null;
    protected Thread t;
    protected Envelope envelope;
    protected byte[] bytes;

    public AbstractAPIHandler(ExtendedClient exClient, ExecutorService executor, SwifiicHandler hndlr) {
		this.client = exClient;
		this.executor = executor;
		this.hndlr = hndlr;
	}

	/**
     * Marks the Bundle currently in the register as delivered.
     */
    protected void markDelivered() {
        final BundleID finalBundleID = new BundleID(bundle);
        final ExtendedClient finalClient = this.client;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Example: add message to database
                // Message msg = new Message(received.source, received.destination, playfile);
                // msg.setCreated(received.timestamp);
                // msg.setReceived(new Date());
                // _database.put(Folder.INBOX, msg);

                try {
                    // Mark bundle as delivered...                    
                    finalClient.markDelivered(finalBundleID);
                    logger.log(Level.FINE, "Delivered: {0}", finalBundleID);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unable to mark bundle as delivered.", e);
                }
            }
        });
    }

    /**
     * Loads the given bundle from the queue into the register and initiates the file transfer.
     */
    protected void loadAndGet(BundleID bundleId) {
        final BundleID finalBundleId = bundleId;
        final ExtendedClient exClient = this.client;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    exClient.loadBundle(finalBundleId);
                    exClient.getBundle();
                    logger.log(Level.INFO, "New bundle loaded");
                } catch (APIException e) {
                    logger.log(Level.WARNING, "Failed to load next bundle");
                }
            }
        });
    }

    /**
     * Loads the next bundle from the queue into the register and initiates transfer of the Bundle's meta data.
     */
    protected void loadAndGetInfo(BundleID bundleId) {
        final BundleID finalBundleId = bundleId;
        final ExtendedClient exClient = this.client;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    exClient.loadBundle(finalBundleId);
                    exClient.getBundleInfo();
                    logger.log(Level.INFO, "New bundle loaded, getting meta data");
                } catch (APIException e) {
                    logger.log(Level.WARNING, "Failed to load next bundle");
                }
            }
        });
    }

    /**
     * Initiates transfer of the Bundle's payload. Requires loading the Bundle into the register first.
     */
    protected void getPayload() {
        final ExtendedClient finalClient = this.client;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.log(Level.INFO, "Requesting payload");
                    finalClient.getPayload();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unable to mark bundle as delivered.", e);
                }
            }
        });
    }

    /**
     * Concurrently reads from a PipedInputStream lest the streams internal buffer runs full and creates a deadlock
     * situation.
     */
    class PipedStreamReader implements Runnable {

        @Override
        public void run() {
	        ByteArrayOutputStream buffer;
	        try {
	            buffer = new ByteArrayOutputStream();
	
	            int nRead;
	            byte[] data = new byte[16384];
	
	            while ((nRead = is.read(data, 0, data.length)) != -1) {
	                buffer.write(data, 0, nRead);
	            }	
	            buffer.flush();
	
	            bytes = buffer.toByteArray();
	
	            StringBuilder sb = new StringBuilder();
	
	            for (byte b : bytes) {
	                sb.append(String.format("%02X ", b));
	            }
	
	            logger.log(Level.INFO, "Payload received: \n\t{0} [{1}]",
	                    new Object[]{sb.toString(), new String(bytes)});
	        } catch (IOException ex) {
	            logger.log(Level.SEVERE, "Unable to decode payload");
	        }
        }
    }

    @Override
    public void notify(BundleID id) {
        loadAndGetInfo(id);
    }

    @Override
    public void notify(StatusReport r) {
        logger.log(Level.INFO, r.toString());
    }

    @Override
    public void notify(Custody c) {
        logger.log(Level.INFO, c.toString());
    }

    @Override
    public void startBundle(Bundle bundle) {
        logger.log(Level.FINE, "Receiving: {0}", bundle);
        this.bundle = bundle;
    }

    @Override
    public void endBundle() {
        logger.log(Level.INFO, "Bundle info received");

        /*
         * Decide if payload is interesting, based on bundle meta data, e.g., payload length of the 
         * individual block. If applicable, partial payloads can be requested, as well. 
         * 
         * If payload is not to be loaded, markDelivered();
         */
        boolean isPayloadInteresting = true;

        if (isPayloadInteresting) {
            getPayload();
        } else {
            markDelivered();
        }
    }

    @Override
    public void startBlock(Block block) {
        //logger.log(Level.FINE, "Receiving: {0}", block.toString());
        bundle.appendBlock(block);
    }

    @Override
    public void endBlock() {
        //logger.log(Level.FINE, "Ending block");
    }

    @Override
    public OutputStream startPayload() {
        //logger.log(Level.INFO, "Receiving payload");
        /*
         * For a detailed description of how different streams affect efficiency, consult:
         * 
         * code.google.com/p/io-tools
         */
        is = new PipedInputStream();
        try {
            os = new PipedOutputStream(is);

            // Concurrently read data from stream lest the buffer runs full and creates a deadlock situation
            t = new Thread(new PipedStreamReader());
            t.start();

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Opening pipes failed", ex);
        }
        return os;
    }

    @Override
    public void endPayload() {
        if (os != null) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Failed to close streams", ex);
                }
            }
        }

        /*
         * With the SelectiveHandler, when the payload has been received, the bundle is complete and can be deleted
         * 
         * Bundle needs to be marked delivered before processing starts concurrently, as the transfer of new Bundles 
         * might interfere otherwise.
         */
        markDelivered();

        logger.log(Level.INFO, "Handling bundle received from {0}", bundle.getSource());
        
        String data = new String(bytes);
        Context ctx = new Context();
        ctx.srcUrl = bundle.getSource().toString();
        hndlr.handlePayload(data, ctx);
        //TODO: Need a processor to process the messages...
        //executor.execute(new Processor(envelope, client, executor));
    }

    @Override
    public void progress(long pos, long total) {
        logger.log(Level.INFO, "Payload: {0} of {1} bytes", new Object[]{pos, total});
    }
}
