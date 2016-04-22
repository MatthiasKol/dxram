
package de.hhu.bsinfo.dxcompute.ms.messages;

import java.nio.ByteBuffer;

import de.hhu.bsinfo.menet.AbstractMessage;

/**
 * Notify remote listeners that execution of a submitted task has finished.
 * @author Stefan Nothaas <stefan.nothaas@hhu.de> 22.04.16
 */
public class TaskExecutionFinishedMessage extends AbstractMessage {
	private long m_taskPayloadId;
	private int[] m_executionReturnCodes;

	/**
	 * Creates an instance of TaskRemoteCallbackMessage.
	 * This constructor is used when receiving this message.
	 */
	public TaskExecutionFinishedMessage() {
		super();
	}

	/**
	 * Creates an instance of TaskRemoteCallbackMessage.
	 * This constructor is used when sending this message.
	 * @param p_destination
	 *            the destination node id.
	 * @param p_taskPayloadId
	 *            Payload id of the task that finished
	 * @param p_executionReturnCodes
	 *            Return codes of all slaves that executed the task (Indexable by slave id).
	 */
	public TaskExecutionFinishedMessage(final short p_destination, final long p_taskPayloadId,
			final int[] p_executionReturnCodes) {
		super(p_destination, MasterSlaveMessages.TYPE, MasterSlaveMessages.SUBTYPE_TASK_EXECUTION_FINISHED_MESSAGE);

		m_taskPayloadId = p_taskPayloadId;
		m_executionReturnCodes = p_executionReturnCodes;
	}

	/**
	 * Get the payload if of the task that finished execution.
	 * @return Payload id of the finished task.
	 */
	public long getTaskPayloadId() {
		return m_taskPayloadId;
	}

	/**
	 * Get the return codes of all slaves that executed the task (Indexable by slave id).
	 * @return Return codes.
	 */
	public int[] getExecutionReturnCodes() {
		return m_executionReturnCodes;
	}

	@Override
	protected final void writePayload(final ByteBuffer p_buffer) {
		p_buffer.putLong(m_taskPayloadId);
		p_buffer.putInt(m_executionReturnCodes.length);
		for (int i = 0; i < m_executionReturnCodes.length; i++) {
			p_buffer.putInt(m_executionReturnCodes[i]);
		}
	}

	@Override
	protected final void readPayload(final ByteBuffer p_buffer) {
		m_taskPayloadId = p_buffer.getLong();
		int size = p_buffer.getInt();
		m_executionReturnCodes = new int[size];
		for (int i = 0; i < size; i++) {
			m_executionReturnCodes[i] = p_buffer.getInt();
		}
	}

	@Override
	protected final int getPayloadLength() {
		return Long.BYTES + Integer.BYTES + Integer.BYTES * m_executionReturnCodes.length;
	}
}