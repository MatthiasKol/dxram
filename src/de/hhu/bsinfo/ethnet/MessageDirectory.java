
package de.hhu.bsinfo.ethnet;

import java.lang.reflect.Constructor;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles mapping from type and subtype to message class.
 * Every message class has to be registered here before it
 * can be used.
 * Message type 0 is dedicated to package intern used message
 * classes.
 * Currently used subtypes:
 * 1 - FlowcontrolMessage @see de.uniduesseldorf.dxram.core.net.AbstractConnection.FlowcontrolMessage
 *
 * @author Marc Ewert 21.10.14
 */
final class MessageDirectory {

	// Attributes
	private Constructor<?>[][] m_constructors = new Constructor[0][0];

	private ReentrantLock m_lock = new ReentrantLock(false);

	/**
	 * MessageDirectory is not designated to be instantiable
	 */
	protected MessageDirectory() {
	}

	/**
	 * Registers a Message Type for receiving
	 *
	 * @param p_type    the type of the Message
	 * @param p_subtype the subtype of the Message
	 * @param p_class   Message class
	 * @return True if successful, false if the specified type and subtype are already in use.
	 */
	protected boolean register(final byte p_type, final byte p_subtype, final Class<?> p_class) {
		Constructor<?>[][] constructors = m_constructors;
		Constructor<?> constructor;

		m_lock.lock();
		try {
			constructor = p_class.getDeclaredConstructor();
		} catch (final NoSuchMethodException e) {
			m_lock.unlock();
			throw new IllegalArgumentException("Class " + p_class.getCanonicalName() + " has no default constructor",
					e);
		}

		if (contains(p_type, p_subtype)) {
			// everything's fine if the same message type for the same constructor
			// is registered multiple times
			if (constructors[p_type][p_subtype].equals(constructor)) {
				m_lock.unlock();
				return true;
			}

			m_lock.unlock();
			return false;
		}

		// enlarge array
		if (constructors.length <= p_type) {
			final Constructor<?>[][] newArray = new Constructor[(byte) (p_type + 1)][];
			System.arraycopy(constructors, 0, newArray, 0, constructors.length);
			constructors = newArray;
			m_constructors = constructors;
		}

		// create new sub array when it is not existing until now
		if (constructors[p_type] == null) {
			constructors[p_type] = new Constructor<?>[p_subtype + 1];
		}

		// enlarge subtype array
		if (constructors[p_type].length <= p_subtype) {
			final Constructor<?>[] newArray = new Constructor[p_subtype + 1];
			System.arraycopy(constructors[p_type], 0, newArray, 0, constructors[p_type].length);
			constructors[p_type] = newArray;
		}

		constructors[p_type][p_subtype] = constructor;
		m_lock.unlock();
		return true;
	}

	/**
	 * Lookup, if a specific message type is already registered
	 *
	 * @param p_type    the type of the Message
	 * @param p_subtype the subtype of the Message
	 * @return true if registered
	 */
	private boolean contains(final byte p_type, final byte p_subtype) {
		boolean result;
		final Constructor<?>[][] constructors = m_constructors;

		if (constructors.length <= p_type) {
			result = false;
		} else if (constructors[p_type] == null || constructors[p_type].length <= p_subtype) {
			result = false;
		} else {
			result = constructors[p_type][p_subtype] != null;
		}

		return result;
	}

	/**
	 * Returns the constructor for a message class by its type and subtype
	 *
	 * @param p_type    the type of the Message
	 * @param p_subtype the subtype of the Message
	 * @return message class constructor
	 */
	private Constructor<?> getConstructor(final byte p_type, final byte p_subtype) {
		Constructor<?> result = null;

		if (contains(p_type, p_subtype)) {
			result = m_constructors[p_type][p_subtype];
		}

		return result;
	}

	/**
	 * Creates a Message instance for the type and subtype
	 *
	 * @param p_type    the type of the Message
	 * @param p_subtype the subtype of the Message
	 * @return a new Message instance
	 */
	protected AbstractMessage getInstance(final byte p_type, final byte p_subtype) {
		AbstractMessage ret;
		Constructor<?> constructor;

		constructor = getConstructor(p_type, p_subtype);

		if (constructor == null) {
			throw new NetworkRuntimeException("Could not create message instance: Message type (" + p_type + ":"
					+ p_subtype + ") not registered");
		}

		try {
			ret = (AbstractMessage) constructor.newInstance();
		} catch (final Exception e) {
			throw new NetworkRuntimeException("Could not create message instance", e);
		}

		return ret;
	}
}