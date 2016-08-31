package de.hhu.bsinfo.dxgraph.algo.bfs.messages;

import java.nio.ByteBuffer;

import de.hhu.bsinfo.menet.AbstractMessage;

/**
 * Message to determine if BFS has to terminate after the iteration is finished.
 *
 * @author Stefan Nothaas <stefan.nothaas@hhu.de> 19.05.16
 */
public class BFSTerminateMessage extends AbstractMessage {
	private long m_frontierNextVerices;
	private long m_frontierNextEdges;

	/**
	 * Creates an instance of BFSTerminateMessage.
	 * This constructor is used when receiving this message.
	 */
	public BFSTerminateMessage() {
		super();
	}

	/**
	 * Creates an instance of BFSTerminateMessage
	 *
	 * @param p_destination          the destination
	 * @param p_frontierNextVertices Total number of vertices in the next frontier.
	 * @param p_frontierNextEdges    Total number of edges in the next frontier
	 */
	public BFSTerminateMessage(final short p_destination, final long p_frontierNextVertices,
			final long p_frontierNextEdges) {
		super(p_destination, BFSMessages.TYPE, BFSMessages.SUBTYPE_BFS_TERMINATE_MESSAGE);

		m_frontierNextVerices = p_frontierNextVertices;
		m_frontierNextEdges = p_frontierNextEdges;
	}

	/**
	 * Get the number of vertices in the next frontier of the remote peer.
	 *
	 * @return Number of vertices in next frontier.
	 */
	public long getFrontierNextVertices() {
		return m_frontierNextVerices;
	}

	/**
	 * Get the total number of edges of all vertices in the next frontier of the remote peer.
	 *
	 * @return Total number of edges in the next frontier.
	 */
	public long getFrontierNextEdges() {
		return m_frontierNextEdges;
	}

	@Override
	protected final void writePayload(final ByteBuffer p_buffer) {
		p_buffer.putLong(m_frontierNextVerices);
		p_buffer.putLong(m_frontierNextEdges);
	}

	@Override
	protected final void readPayload(final ByteBuffer p_buffer) {
		m_frontierNextVerices = p_buffer.getLong();
		m_frontierNextEdges = p_buffer.getLong();
	}

	@Override
	protected final int getPayloadLength() {
		return 2 * Long.BYTES;
	}
}