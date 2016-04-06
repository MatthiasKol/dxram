
package de.hhu.bsinfo.dxram.lookup.messages;

import java.nio.ByteBuffer;

import de.hhu.bsinfo.menet.AbstractRequest;

/**
 * Migrate Request
 * @author Kevin Beineke
 *         03.06.2013
 */
public class MigrateRequest extends AbstractRequest {

	// Attributes
	private long m_chunkID;
	private short m_nodeID;
	private boolean m_isBackup;

	// Constructors
	/**
	 * Creates an instance of MigrateRequest
	 */
	public MigrateRequest() {
		super();

		m_chunkID = -1;
		m_nodeID = -1;
		m_isBackup = false;
	}

	/**
	 * Creates an instance of MigrateRequest
	 * @param p_destination
	 *            the destination
	 * @param p_chunkID
	 *            the object that has to be migrated
	 * @param p_nodeID
	 *            the peer where the object has to be migrated
	 * @param p_isBackup
	 *            whether this is a backup message or not
	 */
	public MigrateRequest(final short p_destination, final long p_chunkID, final short p_nodeID, final boolean p_isBackup) {
		super(p_destination, LookupMessages.TYPE, LookupMessages.SUBTYPE_MIGRATE_REQUEST);

		m_chunkID = p_chunkID;
		m_nodeID = p_nodeID;
		m_isBackup = p_isBackup;
	}

	// Getters
	/**
	 * Get the ChunkID
	 * @return the ID
	 */
	public final long getChunkID() {
		return m_chunkID;
	}

	/**
	 * Get the NodeID
	 * @return the NodeID
	 */
	public final short getNodeID() {
		return m_nodeID;
	}

	/**
	 * Returns whether this is a backup message or not
	 * @return whether this is a backup message or not
	 */
	public final boolean isBackup() {
		return m_isBackup;
	}

	// Methods
	@Override
	protected final void writePayload(final ByteBuffer p_buffer) {
		p_buffer.putLong(m_chunkID);
		p_buffer.putShort(m_nodeID);
		if (m_isBackup) {
			p_buffer.put((byte) 1);
		} else {
			p_buffer.put((byte) 0);
		}
	}

	@Override
	protected final void readPayload(final ByteBuffer p_buffer) {
		m_chunkID = p_buffer.getLong();
		m_nodeID = p_buffer.getShort();

		final byte b = p_buffer.get();
		if (b == 1) {
			m_isBackup = true;
		}
	}

	@Override
	protected final int getPayloadLengthForWrite() {
		return Long.BYTES + Short.BYTES + Byte.BYTES;
	}

}
