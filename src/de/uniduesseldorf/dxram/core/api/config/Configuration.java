package de.uniduesseldorf.dxram.core.api.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uniduesseldorf.dxram.utils.Tools;

/**
 * Represents a configuration for DXRAM
 * @author Florian Klein
 *         03.09.2013
 */
public final class Configuration {

	// Attributes
	private Map<String, String> m_entries;
	private boolean m_immutable;

	// Constructors
	/**
	 * Creates an instance of AbstractConfiguration
	 */
	Configuration() {
		this(false);
	}

	/**
	 * Creates an instance of AbstractConfiguration
	 * @param p_immutable
	 *            defines if the configuration is immutable
	 */
	Configuration(final boolean p_immutable) {
		m_entries = new HashMap<String, String>(ConfigurationConstants.CONFIGURATION_ENTRY_COUNT);
		m_immutable = p_immutable;
	}

	// Getters
	/**
	 * Defines if the configuration is immutable
	 * @return true if the configuration is immutable, false otherwise
	 */
	public synchronized boolean isImmutable() {
		return m_immutable;
	}

	// Methods
	/**
	 * Makes the configuration immutable
	 */
	public synchronized void makeImmutable() {
		m_immutable = true;
	}

	/**
	 * Gets the corresponding configuration value
	 * @param p_entry
	 *            the configuration entry
	 * @return the corresponding configuration value
	 */
	public synchronized String getValue(final ConfigurationEntry<?> p_entry) {
		return getValue(p_entry.getKey());
	}

	/**
	 * Gets the corresponding configuration value
	 * @param p_key
	 *            the key of the configuration value
	 * @return the corresponding configuration value
	 */
	public synchronized String getValue(final String p_key) {
		return m_entries.get(p_key);
	}

	/**
	 * Sets the corresponding configuration value
	 * @param p_entry
	 *            the configuration entry
	 * @param p_value
	 *            the configuration value
	 */
	public synchronized void setValue(final ConfigurationEntry<?> p_entry, final String p_value) {
		setValue(p_entry.getKey(), p_value);
	}

	/**
	 * Sets the corresponding configuration value
	 * @param p_key
	 *            the key of the configuration value
	 * @param p_value
	 *            the configuration value
	 */
	public synchronized void setValue(final String p_key, final String p_value) {
		if (!m_immutable) {
			m_entries.put(p_key, p_value);
		}
	}

	// Methods
	@Override
	public String toString() {
		StringBuffer ret;

		ret = new StringBuffer();

		ret.append("Configuration:\n");
		for (Entry<String, String> entry : m_entries.entrySet()) {
			ret.append(entry.getKey());
			ret.append(":\t");
			ret.append(entry.getValue());
			ret.append("\n");
		}
		ret.append("----------------------------------------");

		return ret.toString();
	}

	// Classes
	/**
	 * Describes a configuration entry
	 * @author Florian Klein
	 *         03.09.2013
	 * @param ValueType
	 *            the value class
	 */
	public static final class ConfigurationEntry<ValueType> {

		// Attributes
		private String m_key;
		private Class<ValueType> m_valueClass;
		private ValueType m_defaultValue;

		// Constructors
		/**
		 * Creates an instance of ConfigurationEntry
		 * @param p_key
		 *            the key of the configuration entry
		 * @param p_valueClass
		 *            the value class of the configuration entry
		 * @param p_defaultValue
		 *            the default value of the configuration entry
		 */
		private ConfigurationEntry(final String p_key, final Class<ValueType> p_valueClass,
				final ValueType p_defaultValue) {
			m_key = p_key;
			m_valueClass = p_valueClass;
			m_defaultValue = p_defaultValue;
		}

		// Getters
		/**
		 * Gets the key of the configuration entry
		 * @return the key
		 */
		public String getKey() {
			return m_key;
		}

		/**
		 * Gets the value class of the configuration entry
		 * @return the defaultValue
		 */
		public Class<ValueType> getValueClass() {
			return m_valueClass;
		}

		/**
		 * Gets the default value of the configuration entry
		 * @return the defaultValue
		 */
		public ValueType getDefaultValue() {
			return m_defaultValue;
		}

		// Methods
		@Override
		public String toString() {
			return "ConfigurationEntry [m_key=" + m_key + ", m_valueClass=" + m_valueClass + ", m_defaultValue="
					+ m_defaultValue + "]";
		}

	}

	/**
	 * Stores all available ConfigurationEntries
	 * @author Florian Klein
	 *         03.09.2013
	 */
	public static final class ConfigurationConstants {

		// Constants
		// Recover from log
		public static final ConfigurationEntry<Boolean> RECOVER = new ConfigurationEntry<Boolean>("recover",
				Boolean.class, false);

		// Max size of a Chunk (default 16 MB)
		public static final ConfigurationEntry<Integer> CHUNK_MAXSIZE = new ConfigurationEntry<Integer>(
				"chunk_maxsize", Integer.class, 16777215);

		// Class for the ChunkInterface
		public static final ConfigurationEntry<String> INTERFACE_CHUNK = new ConfigurationEntry<String>(
				"interface.chunk", String.class, "de.uniduesseldorf.dxram.core.chunk.ChunkHandler");
		// Class for the LookupInterface
		public static final ConfigurationEntry<String> INTERFACE_LOOKUP = new ConfigurationEntry<String>(
				"interface.lookup", String.class, "de.uniduesseldorf.dxram.core.lookup.CachedTreeLookup");
		// Class for the ZooKeeperInterface
		public static final ConfigurationEntry<String> INTERFACE_ZOOKEEPER = new ConfigurationEntry<String>(
				"interface.zookeeper", String.class, "de.uniduesseldorf.dxram.core.zookeeper.ZooKeeperHandler");
		// Class for the NetworkInterface
		public static final ConfigurationEntry<String> INTERFACE_NETWORK = new ConfigurationEntry<String>(
				"interface.network", String.class, "de.uniduesseldorf.dxram.core.net.NetworkHandler");
		// Class for the MemoryInterface
		public static final ConfigurationEntry<String> INTERFACE_RAM = new ConfigurationEntry<String>(
				"interface.ram", String.class, "de.uniduesseldorf.dxram.core.chunk.storage.UnsafeRAMHandler");
		// Class for the LogInterface
		public static final ConfigurationEntry<String> INTERFACE_LOG = new ConfigurationEntry<String>(
				"interface.log", String.class, "de.uniduesseldorf.dxram.core.log.LogHandler");
		// Class for the RecoveryInterface
		public static final ConfigurationEntry<String> INTERFACE_RECOVERY = new ConfigurationEntry<String>(
				"interface.recovery", String.class, "de.uniduesseldorf.dxram.core.recovery.RecoveryHandler");
		// Class for the LockInterface
		public static final ConfigurationEntry<String> INTERFACE_LOCK = new ConfigurationEntry<String>(
				"interface.lock", String.class, "de.uniduesseldorf.dxram.core.lock.LockHandler");

		// Local IP address
		public static final ConfigurationEntry<String> NETWORK_IP = new ConfigurationEntry<String>("network.ip",
				String.class, Tools.getLocalIP());
		// Global Port for DXRAM
		public static final ConfigurationEntry<Integer> NETWORK_PORT = new ConfigurationEntry<Integer>(
				"network.port", Integer.class, Tools.getFreePort(22222));
		// Max connection count at the same time
		public static final ConfigurationEntry<Integer> NETWORK_CONNECTIONS = new ConfigurationEntry<Integer>(
				"network.connections", Integer.class, 100);
		// Size of the incoming message buffer (default 64 KB)
		public static final ConfigurationEntry<Integer> NETWORK_BUFFERSIZE = new ConfigurationEntry<Integer>(
				"network.buffersize", Integer.class, 35536);
		// Class for creating new network connections (default creator uses Java NIO)
		public static final ConfigurationEntry<String> NETWORK_CREATOR = new ConfigurationEntry<String>(
				"network.creator", String.class, "de.uniduesseldorf.dxram.core.net.NIOConnectionCreator");
		//
		public static final ConfigurationEntry<Integer> NETWORK_NIO_THREADCOUNT = new ConfigurationEntry<Integer>(
				"network.nio.threadcount", Integer.class, 1);
		//
		public static final ConfigurationEntry<Integer> NETWORK_MESSAGEHANDLER_THREADCOUNT =
				new ConfigurationEntry<Integer>("network.messagehandler.threadcount", Integer.class, 4);
		//
		public static final ConfigurationEntry<Integer> NETWORK_TASKHANDLER_THREADCOUNT =
				new ConfigurationEntry<Integer>("network.taskhandler.threadcount", Integer.class, 4);
		//
		public static final ConfigurationEntry<Integer> NETWORK_MAX_CACHESIZE = new ConfigurationEntry<Integer>(
				"network.maxCachesize", Integer.class, 10485760);

		// Size of the RAM
		public static final ConfigurationEntry<Long> RAM_SIZE = new ConfigurationEntry<Long>("ram.size", Long.class,
				1073741824L);
		// Class for the Memory-Management of the RAM
		public static final ConfigurationEntry<String> RAM_MANAGEMENT = new ConfigurationEntry<String>(
				"ram.management", String.class,
				"de.uniduesseldorf.dxram.core.chunk.storage.SimpleListStorageManagement");

		// Size of the logfile
		public static final ConfigurationEntry<Long> LOG_SIZE = new ConfigurationEntry<Long>("log.size", Long.class,
				107374182400L);
		// Size of the segments (default 64 MB)
		public static final ConfigurationEntry<Integer> LOG_SEGMENTSIZE = new ConfigurationEntry<Integer>(
				"log.segmentsize", Integer.class, 67108864);
		// Size of the blocks in a segment (default 1 MB)
		public static final ConfigurationEntry<Integer> LOG_BLOCKSIZE = new ConfigurationEntry<Integer>(
				"log.blocksize", Integer.class, 1048576);
		// Name of the logfile
		public static final ConfigurationEntry<String> LOG_FILENAME = new ConfigurationEntry<String>("log.filename",
				String.class, "./log/chunk.log");
		// Class for the Memory-Management of the log
		public static final ConfigurationEntry<String> LOG_MANAGEMENT = new ConfigurationEntry<String>(
				"log.management", String.class, "de.uniduesseldorf.dxram.core.data.memory.MemoryManagementHandler");

		// Sleep interval
		public static final ConfigurationEntry<Integer> LOOKUP_SLEEP = new ConfigurationEntry<Integer>(
				"lookup.sleep", Integer.class, 1);
		// Cache size
		public static final ConfigurationEntry<Integer> LOOKUP_CACHESIZE = new ConfigurationEntry<Integer>(
				"lookup.cacheSize", Integer.class, 1000);
		// Cache ttl
		public static final ConfigurationEntry<Long> LOOKUP_CACHETTL = new ConfigurationEntry<Long>(
				"lookup.cacheTTL", Long.class, (long)1000);
		// Length of backup ranges
		public static final ConfigurationEntry<Integer> LOOKUP_NS_CACHE_SIZE = new ConfigurationEntry<Integer>(
				"lookup.ns.cacheSize", Integer.class, 1000000);
		// Length of backup ranges
		public static final ConfigurationEntry<Integer> LOOKUP_INIT_RANGE = new ConfigurationEntry<Integer>(
				"lookup.initRange", Integer.class, 2500000);
		// Nameservice type
		public static final ConfigurationEntry<String> NAMESERVICE_TYPE = new ConfigurationEntry<String>(
				"nameservice.type", String.class, "NAME");
		// Nameservice key length
		public static final ConfigurationEntry<Integer> NAMESERVICE_KEY_LENGTH = new ConfigurationEntry<Integer>(
				"nameservice.keyLength", Integer.class, 32);

		// Path in ZooKeeper
		public static final ConfigurationEntry<String> ZOOKEEPER_PATH = new ConfigurationEntry<String>(
				"zookeeper.path", String.class, "/dxram");
		// Connection String for ZooKeeper
		public static final ConfigurationEntry<String> ZOOKEEPER_CONNECTION_STRING = new ConfigurationEntry<String>(
				"zookeeper.connectionString", String.class, "127.0.0.1:2181");
		// Session Timeout for ZooKeeper
		public static final ConfigurationEntry<Integer> ZOOKEEPER_TIMEOUT = new ConfigurationEntry<Integer>(
				"zookeeper.timeout", Integer.class, 5000);
		// Bitfield size
		public static final ConfigurationEntry<Integer> ZOOKEEPER_BITFIELDSIZE = new ConfigurationEntry<Integer>(
				"zookeeper.bitfieldSize", Integer.class, 256 * 1024);

		public static final int CONFIGURATION_ENTRY_COUNT = 26;
		private static final List<ConfigurationEntry<?>> CONFIGURATION_ENTRIES;
		static {
			CONFIGURATION_ENTRIES = new ArrayList<>(CONFIGURATION_ENTRY_COUNT);
			CONFIGURATION_ENTRIES.add(RECOVER);
			CONFIGURATION_ENTRIES.add(CHUNK_MAXSIZE);
			CONFIGURATION_ENTRIES.add(INTERFACE_CHUNK);
			CONFIGURATION_ENTRIES.add(INTERFACE_LOOKUP);
			CONFIGURATION_ENTRIES.add(INTERFACE_ZOOKEEPER);
			CONFIGURATION_ENTRIES.add(INTERFACE_NETWORK);
			CONFIGURATION_ENTRIES.add(INTERFACE_RAM);
			CONFIGURATION_ENTRIES.add(INTERFACE_LOG);
			CONFIGURATION_ENTRIES.add(INTERFACE_RECOVERY);
			CONFIGURATION_ENTRIES.add(INTERFACE_LOCK);
			CONFIGURATION_ENTRIES.add(NETWORK_IP);
			CONFIGURATION_ENTRIES.add(NETWORK_PORT);
			CONFIGURATION_ENTRIES.add(NETWORK_CONNECTIONS);
			CONFIGURATION_ENTRIES.add(NETWORK_BUFFERSIZE);
			CONFIGURATION_ENTRIES.add(NETWORK_CREATOR);
			CONFIGURATION_ENTRIES.add(RAM_SIZE);
			CONFIGURATION_ENTRIES.add(RAM_MANAGEMENT);
			CONFIGURATION_ENTRIES.add(LOG_SIZE);
			CONFIGURATION_ENTRIES.add(LOG_SEGMENTSIZE);
			CONFIGURATION_ENTRIES.add(LOG_BLOCKSIZE);
			CONFIGURATION_ENTRIES.add(LOG_FILENAME);
			CONFIGURATION_ENTRIES.add(LOG_MANAGEMENT);
			CONFIGURATION_ENTRIES.add(LOOKUP_SLEEP);
			CONFIGURATION_ENTRIES.add(ZOOKEEPER_PATH);
			CONFIGURATION_ENTRIES.add(ZOOKEEPER_CONNECTION_STRING);
			CONFIGURATION_ENTRIES.add(ZOOKEEPER_TIMEOUT);

			Collections.sort(CONFIGURATION_ENTRIES, new ConfigurationEntryComparator());
		}

		// Constructors
		/**
		 * Creates an instance of ConfigurationConstants
		 */
		private ConfigurationConstants() {}

		// Getters
		/**
		 * Gets a sorted List of all ConfigurationEntries
		 * @return a sorted List of all ConfigurationEntries
		 */
		public static Collection<ConfigurationEntry<?>> getConfigurationEntries() {
			return CONFIGURATION_ENTRIES;
		}

		// Methods
		/**
		 * Gets the corresponding ConfigurationEntry for the given key
		 * @param p_key
		 *            the key of the ConfigurationEntry
		 * @return the corresponding ConfigurationEntry
		 */
		public static ConfigurationEntry<?> getConfigurationEntry(final String p_key) {
			ConfigurationEntry<?> ret = null;

			for (ConfigurationEntry<?> entry : CONFIGURATION_ENTRIES) {
				if (entry.getKey().equals(p_key)) {
					ret = entry;

					break;
				}
			}

			return ret;
		}

		// Classes
		/**
		 * Comparator for the ConfigurationEntries
		 * @author Florian Klein
		 *         03.09.2013
		 */
		private static final class ConfigurationEntryComparator implements Comparator<ConfigurationEntry<?>> {

			// Constructors
			/**
			 * Creates an instance of ConfigurationEntryComparator
			 */
			private ConfigurationEntryComparator() {}

			// Methods
			@Override
			public int compare(final ConfigurationEntry<?> p_entry1, final ConfigurationEntry<?> p_entry2) {
				return p_entry1.getKey().compareTo(p_entry2.getKey());
			}

		}

	}

}
