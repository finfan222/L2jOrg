package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

public class ExRegenMax extends ServerPacket {
    private final int _time;
    private final int _tickInterval;
    private final double _amountPerTick;

    public ExRegenMax(int time, int tickInterval, double amountPerTick) {
        _time = time;
        _tickInterval = tickInterval;
        _amountPerTick = amountPerTick;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.EX_REGEN_MAX);

        writeInt(1);
        writeInt(_time);
        writeInt(_tickInterval);
        writeDouble(_amountPerTick);
    }

}
