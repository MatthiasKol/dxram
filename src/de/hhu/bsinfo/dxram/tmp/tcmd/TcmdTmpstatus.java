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

package de.hhu.bsinfo.dxram.tmp.tcmd;

import de.hhu.bsinfo.dxram.lookup.overlay.storage.SuperpeerStorage;
import de.hhu.bsinfo.dxram.term.AbstractTerminalCommand;
import de.hhu.bsinfo.dxram.term.TerminalCommandContext;
import de.hhu.bsinfo.dxram.tmp.TemporaryStorageService;

/**
 * Get the status of the temporary (superpeer) storage
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 03.04.2017
 */
public class TcmdTmpstatus extends AbstractTerminalCommand {
    public TcmdTmpstatus() {
        super("tmpstatus");
    }

    @Override
    public String getHelp() {
        return "Get the status of the temporary (superpeer) storage\n" + "Usage: tmpstatus";
    }

    @Override
    public void exec(final String[] p_args, final TerminalCommandContext p_ctx) {
        TemporaryStorageService tmpstore = p_ctx.getService(TemporaryStorageService.class);

        SuperpeerStorage.Status status = tmpstore.getStatus();

        if (status != null) {
            TerminalCommandContext.printfln("Total size occupied (bytes): %d\n%s", status.calculateTotalDataUsageBytes(), status);
        } else {
            TerminalCommandContext.printlnErr("Getting status of temporary storage failed");
        }
    }
}
