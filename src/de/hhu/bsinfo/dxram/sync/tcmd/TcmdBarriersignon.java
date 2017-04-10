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

package de.hhu.bsinfo.dxram.sync.tcmd;

import de.hhu.bsinfo.dxram.data.ChunkID;
import de.hhu.bsinfo.dxram.lookup.overlay.storage.BarrierID;
import de.hhu.bsinfo.dxram.lookup.overlay.storage.BarrierStatus;
import de.hhu.bsinfo.dxram.sync.SynchronizationService;
import de.hhu.bsinfo.dxram.term.AbstractTerminalCommand;
import de.hhu.bsinfo.dxram.term.TerminalCommandContext;
import de.hhu.bsinfo.ethnet.NodeID;

/**
 * Sign on to an allocated barrier for synchronization
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 03.04.2017
 */
public class TcmdBarriersignon extends AbstractTerminalCommand {
    public TcmdBarriersignon() {
        super("barriersignon");
    }

    @Override
    public String getHelp() {
        return "Sign on to an allocated barrier for synchronization (for testing/debugging)\n" + "Usage: barriersignon <bid> [data]\n" +
            "  bid: Id of the barrier to sign on to\n" + "  data: Custom data to pass along with the sign on call (optional)";
    }

    @Override
    public void exec(final String[] p_args, final TerminalCommandContext p_ctx) {
        int bid = TerminalCommandContext.getArgBarrierId(p_args, 0, BarrierID.INVALID_ID);
        long data = TerminalCommandContext.getArgLong(p_args, 1, 0);

        if (bid == BarrierID.INVALID_ID) {
            TerminalCommandContext.printlnErr("No bid specified");
            return;
        }

        SynchronizationService sync = p_ctx.getService(SynchronizationService.class);
        BarrierStatus result = sync.barrierSignOn(bid, data);

        if (result == null) {
            TerminalCommandContext.printflnErr("Signing on to barrier 0x%X failed", bid);
            return;
        }

        String str = "";
        for (int i = 0; i < result.getSignedOnNodeIDs().length; i++) {
            str += '\n' + NodeID.toHexString(result.getSignedOnNodeIDs()[i]) + ": " + ChunkID.toHexString(result.getCustomData()[i]);
        }

        TerminalCommandContext.printfln("Synchronized to barrier 0x%X custom data: %s", bid, str);
    }
}
