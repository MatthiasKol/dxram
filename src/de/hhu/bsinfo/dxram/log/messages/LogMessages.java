
package de.hhu.bsinfo.dxram.log.messages;

/**
 * Type and list of subtypes for all log messages
 * @author Kevin Beineke <kevin.beineke@hhu.de> 30.03.16
 */
public class LogMessages {
	public static final byte TYPE = 4;
	public static final byte SUBTYPE_LOG_MESSAGE = 1;
	public static final byte SUBTYPE_REMOVE_MESSAGE = 2;
	public static final byte SUBTYPE_INIT_REQUEST = 3;
	public static final byte SUBTYPE_INIT_RESPONSE = 4;
	public static final byte SUBTYPE_LOG_COMMAND_REQUEST = 5;
	public static final byte SUBTYPE_LOG_COMMAND_RESPONSE = 6;
}
