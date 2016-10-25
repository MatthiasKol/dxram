
package de.hhu.bsinfo.dxram.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.google.gson.annotations.Expose;
import de.hhu.bsinfo.dxram.DXRAMComponentOrder;
import de.hhu.bsinfo.dxram.boot.AbstractBootComponent;
import de.hhu.bsinfo.dxram.engine.AbstractDXRAMComponent;
import de.hhu.bsinfo.dxram.engine.DXRAMComponentAccessor;
import de.hhu.bsinfo.dxram.engine.DXRAMContext;
import de.hhu.bsinfo.dxram.event.EventComponent;
import de.hhu.bsinfo.dxram.net.events.ConnectionLostEvent;
import de.hhu.bsinfo.dxram.net.messages.DXRAMMessageTypes;
import de.hhu.bsinfo.dxram.net.messages.DefaultMessage;
import de.hhu.bsinfo.dxram.net.messages.DefaultMessages;
import de.hhu.bsinfo.ethnet.AbstractMessage;
import de.hhu.bsinfo.ethnet.AbstractRequest;
import de.hhu.bsinfo.ethnet.NetworkHandler;
import de.hhu.bsinfo.ethnet.NetworkHandler.MessageReceiver;
import de.hhu.bsinfo.ethnet.RequestMap;
import de.hhu.bsinfo.utils.unit.StorageUnit;
import de.hhu.bsinfo.utils.unit.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Access to the network interface to send messages or requests
 * to other nodes.
 *
 * @author Stefan Nothaas <stefan.nothaas@hhu.de> 26.01.16
 */
public class NetworkComponent extends AbstractDXRAMComponent {

	private static final Logger LOGGER = LogManager.getFormatterLogger(NetworkComponent.class.getSimpleName());

	// configuration values
	@Expose
	private int m_threadCountMsgHandler = 1;
	@Expose
	private int m_requestMapEntryCount = (int) Math.pow(2, 20);
	@Expose
	private StorageUnit m_incomingBufferSize = new StorageUnit(1, StorageUnit.MB);
	@Expose
	private StorageUnit m_outgoingBufferSize = new StorageUnit(1, StorageUnit.MB);
	@Expose
	private int m_numberOfPendingBuffersPerConnection = 100;
	@Expose
	private StorageUnit m_flowControlWindowSize = new StorageUnit(1, StorageUnit.MB);
	@Expose
	private TimeUnit m_requestTimeout = new TimeUnit(333, TimeUnit.MS);

	// dependent components
	private AbstractBootComponent m_boot;
	private EventComponent m_event;

	// Attributes
	private NetworkHandler m_networkHandler;

	/**
	 * Constructor
	 */
	public NetworkComponent() {
		super(DXRAMComponentOrder.Init.NETWORK, DXRAMComponentOrder.Shutdown.NETWORK);
	}

	// --------------------------------------------------------------------------------------

	/**
	 * Activates the connection manager
	 */
	public void activateConnectionManager() {
		m_networkHandler.activateConnectionManager();
	}

	/**
	 * Deactivates the connection manager
	 */
	public void deactivateConnectionManager() {
		m_networkHandler.deactivateConnectionManager();
	}

	/**
	 * Registers a message type
	 *
	 * @param p_type    the unique type
	 * @param p_subtype the unique subtype
	 * @param p_class   the calling class
	 */
	public void registerMessageType(final byte p_type, final byte p_subtype, final Class<?> p_class) {
		m_networkHandler.registerMessageType(p_type, p_subtype, p_class);
	}

	/**
	 * Connect a node.
	 *
	 * @param p_nodeID Node to connect
	 * @return 0 if successful, -1 if not
	 */
	public NetworkErrorCodes connectNode(final short p_nodeID) {
		// #if LOGGER == TRACE
		LOGGER.trace("Connecting node 0x%X", p_nodeID);
		// #endif /* LOGGER == TRACE */

		int res = m_networkHandler.connectNode(p_nodeID);
		NetworkErrorCodes errCode = NetworkErrorCodes.SUCCESS;
		if (res == -1) {
			errCode = NetworkErrorCodes.DESTINATION_UNREACHABLE;

			// #if LOGGER >= ERROR
			LOGGER.error("Connecting node 0x%X failed: %s", p_nodeID, errCode);
			// #endif /* LOGGER >= ERROR */
		}

		return errCode;
	}

	/**
	 * Send a message.
	 *
	 * @param p_message Message to send
	 * @return NetworkErrorCode, refer to enum
	 */
	public NetworkErrorCodes sendMessage(final AbstractMessage p_message) {
		// #if LOGGER == TRACE
		LOGGER.trace("Sending message %s", p_message);
		// #endif /* LOGGER == TRACE */

		int res = m_networkHandler.sendMessage(p_message);
		NetworkErrorCodes errCode = NetworkErrorCodes.UNKNOWN;

		switch (res) {
			case 0:
				errCode = NetworkErrorCodes.SUCCESS;
				break;
			case -1:
				errCode = NetworkErrorCodes.DESTINATION_UNREACHABLE;

				// Connection creation failed -> trigger failure handling
				m_event.fireEvent(new ConnectionLostEvent(getClass().getSimpleName(), p_message.getDestination()));
				break;
			case -2:
				errCode = NetworkErrorCodes.SEND_DATA;
				break;
			default:
				assert false;
				break;
		}

		// #if LOGGER >= ERROR
		if (errCode != NetworkErrorCodes.SUCCESS) {
			LOGGER.error("Sending message %s failed: %s", p_message, errCode);
		}
		// #endif /* LOGGER >= ERROR */

		return errCode;
	}

	/**
	 * Send the Request and wait for fulfillment (wait for response).
	 *
	 * @param p_request The request to send.
	 * @return 0 if successful, -1 if sending the request failed, 1 waiting for the response timed out.
	 */
	public NetworkErrorCodes sendSync(final AbstractRequest p_request) {
		// #if LOGGER == TRACE
		LOGGER.trace("Sending request (sync): %s", p_request);
		// #endif /* LOGGER == TRACE */

		NetworkErrorCodes err = sendMessage(p_request);
		if (err == NetworkErrorCodes.SUCCESS) {
			// #if LOGGER == TRACE
			LOGGER.trace("Waiting for response to request: %s", p_request);
			// #endif /* LOGGER == TRACE */

			if (!p_request.waitForResponses((int) m_requestTimeout.getMs())) {
				// #if LOGGER >= ERROR
				LOGGER.error("Sending sync, waiting for responses %s failed, timeout", p_request);
				// #endif /* LOGGER >= ERROR */

				// #if LOGGER >= DEBUG
				LOGGER.debug(m_networkHandler.getStatus());
				// #endif /* LOGGER >= DEBUG */

				err = NetworkErrorCodes.RESPONSE_TIMEOUT;
			} else {
				// #if LOGGER == TRACE
				LOGGER.trace("Received response: %s", p_request.getResponse());
				// #endif /* LOGGER == TRACE */
			}
		}

		if (err != NetworkErrorCodes.SUCCESS) {
			RequestMap.remove(p_request.getRequestID());
		}

		return err;
	}

	/**
	 * Registers a message receiver
	 *
	 * @param p_message  the message
	 * @param p_receiver the receiver
	 */
	public void register(final Class<? extends AbstractMessage> p_message, final MessageReceiver p_receiver) {
		m_networkHandler.register(p_message, p_receiver);
	}

	/**
	 * Unregisters a message receiver
	 *
	 * @param p_message  the message
	 * @param p_receiver the receiver
	 */
	public void unregister(final Class<? extends AbstractMessage> p_message, final MessageReceiver p_receiver) {
		m_networkHandler.unregister(p_message, p_receiver);
	}

	// --------------------------------------------------------------------------------------

	@Override
	protected void resolveComponentDependencies(final DXRAMComponentAccessor p_componentAccessor) {
		m_boot = p_componentAccessor.getComponent(AbstractBootComponent.class);
		m_event = p_componentAccessor.getComponent(EventComponent.class);
	}

	@Override
	protected boolean initComponent(final DXRAMContext.EngineSettings p_engineEngineSettings) {
		m_networkHandler = new NetworkHandler(m_threadCountMsgHandler, m_requestMapEntryCount);
		m_networkHandler.setEventHandler(m_event);

		// Check if given ip address is bound to one of this node's network interfaces
		boolean found = false;
		InetAddress myAddress = m_boot.getNodeAddress(m_boot.getNodeID()).getAddress();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			outerloop:
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface currentNetworkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> addresses = currentNetworkInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress currentAddress = addresses.nextElement();
					if (myAddress.equals(currentAddress)) {
						// #if LOGGER >= INFO
						LOGGER.info("%s is bound to %s",
								myAddress.getHostAddress(), currentNetworkInterface.getDisplayName());
						// #endif /* LOGGER >= INFO */
						found = true;
						break outerloop;
					}
				}
			}
		} catch (final SocketException e1) {
			// #if LOGGER >= ERROR
			LOGGER.error("Could not get network interfaces for ip confirmation");
			// #endif /* LOGGER >= ERROR */
		} finally {
			if (!found) {
				// #if LOGGER >= ERROR
				LOGGER.error("Could not find network interface with address %s", myAddress.getHostAddress());
				// #endif /* LOGGER >= ERROR */
				return false;
			}
		}

		m_networkHandler.initialize(
				m_boot.getNodeID(),
				new NodeMappings(m_boot),
				(int) m_incomingBufferSize.getBytes(),
				(int) m_outgoingBufferSize.getBytes(),
				m_numberOfPendingBuffersPerConnection,
				(int) m_flowControlWindowSize.getBytes(),
				(int) m_requestTimeout.getMs());

		m_networkHandler.registerMessageType(DXRAMMessageTypes.DEFAULT_MESSAGES_TYPE,
				DefaultMessages.SUBTYPE_DEFAULT_MESSAGE, DefaultMessage.class);

		return true;
	}

	@Override
	protected boolean shutdownComponent() {
		m_networkHandler.close();

		m_networkHandler = null;

		return true;
	}
}
