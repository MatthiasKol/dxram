package de.uniduesseldorf.dxram.test;

import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniduesseldorf.dxram.core.api.Core;
import de.uniduesseldorf.dxram.core.api.NodeID;
import de.uniduesseldorf.dxram.core.api.config.ConfigurationHandler;
import de.uniduesseldorf.dxram.core.api.config.NodesConfigurationHandler;
import de.uniduesseldorf.dxram.core.chunk.Chunk;
import de.uniduesseldorf.dxram.core.chunk.storage.CIDTable;
import de.uniduesseldorf.dxram.core.chunk.storage.RawMemory;
import de.uniduesseldorf.dxram.core.exceptions.DXRAMException;
import de.uniduesseldorf.dxram.core.exceptions.MemoryException;
import de.uniduesseldorf.dxram.utils.Tools;

/*
 * Start-up:
 * 1) Start at least one superpeer: With parameter "superpeer", must also be a superpeer in nodes.dxram
 * 2) Start server: With parameters "server x" whereas x is the number of messages that should be stored on server
 * 3) Set serverID to NodeID of server
 * 4) Start clients: No parameters
 * Notes:
 * a) The server's NodeID only changes if the number of first initialized superpeers differs. For one superpeer:
 * serverID = -15999
 * b) Server and clients can be peers and superpeers, but should be peers only for a reasonable allocation of roles in
 * DXRAM
 */
/**
 * Test case for the distributed evaluation for 2015 conference
 * @author Florian Klein
 *         27.12.2014
 */
public final class ThroughputTest {

	// Constructors
	/**
	 * Creates an instance of ThroughputTest
	 */
	private ThroughputTest() {}

	// Methods
	/**
	 * Program entry point
	 * @param p_arguments
	 *            The program arguments
	 */
	public static void main(final String[] p_arguments) {
		if (p_arguments.length == 1 && p_arguments[0].equals("superpeer")) {
			new Superpeer().start();
		} else if (p_arguments.length == 3 && p_arguments[0].equals("server")) {
			new Server(Integer.parseInt(p_arguments[1]), Integer.parseInt(p_arguments[2])).start();
		} else if (p_arguments.length == 3 && p_arguments[0].equals("client")) {
			new Client(Integer.parseInt(p_arguments[1]), Integer.parseInt(p_arguments[2])).start();
		} else {
			System.out.println("Missing or wrong parameters!");
		}
	}

	// Classes
	/**
	 * Represents a superpeer
	 * @author Florian Klein
	 *         27.12.2014
	 */
	private static class Superpeer {

		// Constructors
		/**
		 * Creates an instance of Superpeer
		 */
		public Superpeer() {}

		// Methods
		/**
		 * Starts the superpeer
		 */
		public void start() {
			System.out.println("Initialize superpeer");

			// Initialize DXRAM
			try {
				Core.initialize(ConfigurationHandler.getConfigurationFromFile("config/dxram.config"),
						NodesConfigurationHandler.getConfigurationFromFile("config/nodes.dxram"));

				System.out.println("Superpeer initialized");
			} catch (final DXRAMException e) {
				e.printStackTrace();
			}

			System.out.println("Superpeer started");

			while (true) {}
		}

	}

	/**
	 * Represents the server
	 * @author Florian Klein
	 *         03.07.2014
	 */
	private static class Server {

		// Attributes
		private int m_chunkCount;
		private int m_threadCount;

		// Constructors
		/**
		 * Creates an instance of Server
		 * @param p_chunkCount
		 *            the amount of Chunks to create
		 */
		public Server(final int p_chunkCount, final int p_threadCount) {
			m_chunkCount = p_chunkCount;
			m_threadCount = p_threadCount;
		}

		// Methods
		/**
		 * Starts the server
		 */
		public void start() {
			TestResult result;

			try {
				init();

				result = evaluateCreate(m_chunkCount, m_threadCount);
				printTestResult("Create", m_chunkCount, result);

				result = evaluatePut(m_chunkCount, m_threadCount);
				printTestResult("Put", m_chunkCount, result);

				result = evaluateGet(m_chunkCount, m_threadCount);
				printTestResult("Get", m_chunkCount, result);

				result = evaluateMultiGet(m_chunkCount, m_threadCount);
				printTestResult("MultiGet", m_chunkCount, result);

				result = evaluateLock(m_chunkCount, m_threadCount);
				printTestResult("Lock", m_chunkCount, result);

				result = evaluateCreateNS(m_chunkCount, m_threadCount);
				printTestResult("CreateNS", m_chunkCount, result);

				result = evaluateGetNS(m_chunkCount, m_threadCount);
				printTestResult("GetNS", m_chunkCount, result);

				result = evaluateGetNS(m_chunkCount, m_threadCount);
				printTestResult("GetNS2", m_chunkCount, result);

				deinit();

				while (true) {}
			} catch (final Throwable e) {
				e.printStackTrace();
			}
		}

		private void init() throws DXRAMException {
			Core.initialize(ConfigurationHandler.getConfigurationFromFile("config/dxram.config"),
					NodesConfigurationHandler.getConfigurationFromFile("config/nodes.dxram"));
		}

		private void deinit() throws DXRAMException {
			Chunk chunk;
			ByteBuffer data;

			System.out.println("\nCreate Index Chunk");
			chunk = Core.createNewChunk(2, "Idx");
			System.out.println("ChunkID: " + Long.toHexString(chunk.getChunkID()));
			data = chunk.getData();
			data.putShort(NodeID.getLocalNodeID());
			Core.put(chunk);

			CIDTable.printDebugInfos();
			RawMemory.printDebugInfos();
		}

		private TestResult evaluateCreate(final int p_chunkCount, final int p_threadCount)
				throws InterruptedException, ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						for (int i = 1;i <= COUNT;i++) {
							try {
								Core.createNewChunk(40);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluatePut(final int p_chunkCount, final int p_threadCount) throws InterruptedException,
				ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			final long nodeID = ((long)NodeID.getLocalNodeID()) << 48;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						Chunk chunk;

						for (int i = 1;i <= COUNT;i++) {
							try {
								chunk = new Chunk(i + nodeID, new byte[40]);
								Core.put(chunk);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluateGet(final int p_chunkCount, final int p_threadCount) throws InterruptedException,
				ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			final long nodeID = ((long)NodeID.getLocalNodeID()) << 48;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						for (int i = 1;i <= COUNT;i++) {
							try {
								Core.get(i + nodeID);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluateMultiGet(final int p_chunkCount, final int p_threadCount)
				throws InterruptedException, ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			final long nodeID = ((long)NodeID.getLocalNodeID()) << 48;
			final long[] chunkIDs = new long[COUNT];
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			for (int i = 1;i <= COUNT;i++) {
				chunkIDs[i - 1] = i + nodeID;
			}

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						try {
							Core.get(chunkIDs);
						} catch (final DXRAMException e) {
							e.printStackTrace();
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluateLock(final int p_chunkCount, final int p_threadCount)
				throws InterruptedException, ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			final long nodeID = ((long)NodeID.getLocalNodeID()) << 48;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						for (int i = 1;i <= COUNT;i++) {
							try {
								Core.lock(i + nodeID);
								Core.unlock(i + nodeID);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluateCreateNS(final int p_chunkCount, final int p_threadCount)
				throws InterruptedException, ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						for (int i = 1;i <= COUNT;i++) {
							try {
								Core.createNewChunk(40, "" + i);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluateGetNS(final int p_chunkCount, final int p_threadCount)
				throws InterruptedException, ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						for (int i = 1;i <= COUNT;i++) {
							try {
								Core.get("" + i);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private void printTestResult(final String p_test, final int p_chunkCount, final TestResult p_result) {
			StringBuffer output;
			NumberFormat format;

			format = NumberFormat.getNumberInstance();
			output = new StringBuffer();

			output.append("\n" + p_test + ":");
			output.append("\nOverall Time:\t" + Tools.readableNanoTime(p_result.getTime()));
			output.append("\nTime / Op:\t" + format.format(p_result.getTime() / (p_chunkCount * 1.0))
					+ " nanoseconds");
			output.append("\nOps / Second:\t" + format.format((p_chunkCount * 1000000000.0) / p_result.getTime()));

			System.out.println(output);
		}

	}

	/**
	 * Represents the server
	 * @author Florian Klein
	 *         03.07.2014
	 */
	private static class Client {

		// Attributes
		private int m_chunkCount;
		private int m_threadCount;

		private long m_nodeID;

		// Constructors
		/**
		 * Creates an instance of Server
		 * @param p_chunkCount
		 *            the amount of Chunks to create
		 */
		public Client(final int p_chunkCount, final int p_threadCount) {
			m_chunkCount = p_chunkCount;
			m_threadCount = p_threadCount;
		}

		// Methods
		/**
		 * Starts the server
		 */
		public void start() {
			TestResult result;

			try {
				init();

				result = evaluateGet(m_chunkCount, m_threadCount);
				printTestResult("Gets", m_chunkCount, result);

				result = evaluateMultiGet(m_chunkCount, m_threadCount);
				printTestResult("MultiGet", m_chunkCount, result);

				result = evaluateLock(m_chunkCount, m_threadCount);
				printTestResult("Lock", m_chunkCount, result);

				result = evaluateGetNS(m_chunkCount, m_threadCount);
				printTestResult("GetNS", m_chunkCount, result);

				result = evaluateGetNS(m_chunkCount, m_threadCount);
				printTestResult("GetNS2", m_chunkCount, result);

				deinit();
			} catch (final Throwable e) {
				e.printStackTrace();
			}
		}

		private void init() throws DXRAMException {
			Chunk chunk;
			ByteBuffer data;

			Core.initialize(ConfigurationHandler.getConfigurationFromFile("config/dxram.config"),
					NodesConfigurationHandler.getConfigurationFromFile("config/nodes.dxram"));

			System.out.println("Get Index Chunk");
			chunk = Core.get("Idx");
			System.out.println("ChunkID: " + Long.toHexString(chunk.getChunkID()));
			data = chunk.getData();
			m_nodeID = ((long)data.getShort()) << 48;
		}

		private void deinit() throws MemoryException {}

		private TestResult evaluateGet(final int p_chunkCount, final int p_threadCount) throws InterruptedException,
				ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						for (int i = 1;i <= COUNT;i++) {
							try {
								Core.get(i + m_nodeID);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluateMultiGet(final int p_chunkCount, final int p_threadCount)
				throws InterruptedException, ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			final long[] chunkIDs;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			chunkIDs = new long[COUNT];
			for (int i = 1;i <= COUNT;i++) {
				chunkIDs[i - 1] = i + m_nodeID;
			}

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						try {
							Core.get(chunkIDs);
						} catch (final Throwable e) {
							e.printStackTrace();
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluateLock(final int p_chunkCount, final int p_threadCount)
				throws InterruptedException, ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						for (int i = 1;i <= COUNT;i++) {
							try {
								Core.lock(i + m_nodeID);
								Core.unlock(i + m_nodeID);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private TestResult evaluateGetNS(final int p_chunkCount, final int p_threadCount)
				throws InterruptedException, ExecutionException {
			final int COUNT = p_chunkCount / p_threadCount;
			ExecutorService executorService;
			Future<?>[] futures;
			long time;

			executorService = Executors.newFixedThreadPool(p_threadCount);

			futures = new Future[p_threadCount];
			time = System.nanoTime();
			for (int i = 0;i < p_threadCount;i++) {
				futures[i] = executorService.submit(new Runnable() {

					@Override
					public void run() {
						for (int i = 1;i <= COUNT;i++) {
							try {
								Core.get("" + i);
							} catch (final DXRAMException e) {
								e.printStackTrace();
							}
						}
					}

				});
			}

			for (final Future<?> future : futures) {
				future.get();
			}
			time = System.nanoTime() - time;
			executorService.shutdown();

			return new TestResult(time);
		}

		private void printTestResult(final String p_test, final int p_chunkCount, final TestResult p_result) {
			StringBuffer output;
			NumberFormat format;

			format = NumberFormat.getNumberInstance();
			output = new StringBuffer();

			output.append("\n" + p_test + ":");
			output.append("\nOverall Time:\t" + Tools.readableNanoTime(p_result.getTime()));
			output.append("\nTime / Op:\t" + format.format(p_result.getTime() / (p_chunkCount * 1.0))
					+ " nanoseconds");
			output.append("\nOps / Second:\t" + format.format((p_chunkCount * 1000000000.0) / p_result.getTime()));

			System.out.println(output);
		}

	}

	private static final class TestResult {

		// Attributes
		private long time;

		/**
		 * Creates an instance of MemoryManagementTest
		 * @param p_time
		 *            the test time
		 */
		public TestResult(long p_time) {
			time = p_time;
		}

		/**
		 * Returns the test time
		 * @return the test time
		 */
		public long getTime() {
			return time;
		}

	}

}
