package de.uniduesseldorf.dxram.core.lookup.messages;

import de.uniduesseldorf.menet.AbstractRequest;

/**
 * Ask About Successor Request
 * @author Kevin Beineke
 *         06.09.2012
 */
public class AskAboutSuccessorRequest extends AbstractRequest {

	// Constructors
	/**
	 * Creates an instance of AskAboutSuccessorRequest
	 */
	public AskAboutSuccessorRequest() {
		super();
	}

	/**
	 * Creates an instance of AskAboutSuccessorRequest
	 * @param p_destination
	 *            the destination
	 */
	public AskAboutSuccessorRequest(final short p_destination) {
		super(p_destination, LookupMessages.TYPE, LookupMessages.SUBTYPE_ASK_ABOUT_SUCCESSOR_REQUEST);
	}

}