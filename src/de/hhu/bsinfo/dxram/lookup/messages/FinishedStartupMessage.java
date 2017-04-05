/*
 * Copyright (C) 2017 Heinrich-Heine-Universitaet Duesseldorf, Institute of Computer Science, Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hhu.bsinfo.dxram.lookup.messages;

import de.hhu.bsinfo.dxram.DXRAMMessageTypes;
import de.hhu.bsinfo.ethnet.AbstractMessage;

/**
 * Message to inform all nodes about finished startup.
 *
 * @author Kevin Beineke, kevin.beineke@hhu.de, 03.04.2017
 */
public class FinishedStartupMessage extends AbstractMessage {

    // Constructors

    /**
     * Creates an instance of FinishedStartupMessage
     */
    public FinishedStartupMessage() {
        super();
    }

    /**
     * Creates an instance of FinishedStartupMessage
     *
     * @param p_destination
     *     the destination
     */
    public FinishedStartupMessage(final short p_destination) {
        super(p_destination, DXRAMMessageTypes.LOOKUP_MESSAGES_TYPE, LookupMessages.SUBTYPE_FINISHED_STARTUP_MESSAGE);
    }

}
