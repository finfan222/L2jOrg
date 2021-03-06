/*
 * Copyright © 2019-2020 L2JOrg
 *
 * This file is part of the L2JOrg project.
 *
 * L2JOrg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2JOrg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.item.Henna;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerPacketId;

/**
 * @author Zoey76
 */
public class HennaRemoveList extends ServerPacket {
    private final Player _player;

    public HennaRemoveList(Player player) {
        _player = player;
    }

    @Override
    public void writeImpl(GameClient client) {
        writeId(ServerPacketId.HENNA_UNEQUIP_LIST);

        writeLong(_player.getAdena());
        writeInt(0x03); // seems to be max size
        writeInt(3 - _player.getHennaEmptySlots());

        for (Henna henna : _player.getHennaList()) {
            if (henna != null) {
                writeInt(henna.getDyeId());
                writeInt(henna.getDyeItemId());
                writeLong(henna.getCancelCount());
                writeLong(henna.getCancelFee());
                writeInt(0x00);
            }
        }
    }

}
