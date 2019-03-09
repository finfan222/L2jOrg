package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ExShowAgitInfo;

import java.nio.ByteBuffer;

/**
 * @author KenM
 */
public class RequestAllAgitInfo extends IClientIncomingPacket
{
	@Override
	public void readImpl(ByteBuffer packet)
	{

	}
	
	@Override
	public void runImpl()
	{
		client.sendPacket(ExShowAgitInfo.STATIC_PACKET);
	}
}
