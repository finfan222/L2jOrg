package org.l2j.gameserver.mobius.gameserver.network.clientpackets;

import org.l2j.gameserver.mobius.gameserver.instancemanager.DuelManager;
import org.l2j.gameserver.mobius.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.mobius.gameserver.network.SystemMessageId;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.SystemMessage;

import java.nio.ByteBuffer;

/**
 * Format:(ch) ddd
 * @author -Wooden-
 */
public final class RequestDuelAnswerStart extends IClientIncomingPacket
{
    private int _partyDuel;
    @SuppressWarnings("unused")
    private int _unk1;
    private int _response;

    @Override
    public void readImpl(ByteBuffer packet)
    {
        _partyDuel = packet.getInt();
        _unk1 = packet.getInt();
        _response = packet.getInt();
    }

    @Override
    public void runImpl()
    {
        final L2PcInstance player = client.getActiveChar();
        if (player == null)
        {
            return;
        }

        final L2PcInstance requestor = player.getActiveRequester();
        if (requestor == null)
        {
            return;
        }

        if (_response == 1)
        {
            SystemMessage msg1 = null;
            SystemMessage msg2 = null;
            if (requestor.isInDuel())
            {
                msg1 = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL);
                msg1.addString(requestor.getName());
                player.sendPacket(msg1);
                return;
            }
            else if (player.isInDuel())
            {
                player.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
                return;
            }

            if (_partyDuel == 1)
            {
                msg1 = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_C1_S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
                msg1.addString(requestor.getName());

                msg2 = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
                msg2.addString(player.getName());
            }
            else
            {
                msg1 = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_C1_S_CHALLENGE_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
                msg1.addString(requestor.getName());

                msg2 = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
                msg2.addString(player.getName());
            }

            player.sendPacket(msg1);
            requestor.sendPacket(msg2);

            DuelManager.getInstance().addDuel(requestor, player, _partyDuel);
        }
        else if (_response == -1)
        {
            final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_DUEL_REQUESTS_AND_CANNOT_RECEIVE_A_DUEL_REQUEST);
            sm.addPcName(player);
            requestor.sendPacket(sm);
        }
        else
        {
            SystemMessage msg = null;
            if (_partyDuel == 1)
            {
                msg = SystemMessage.getSystemMessage(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
            }
            else
            {
                msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
                msg.addPcName(player);
            }
            requestor.sendPacket(msg);
        }

        player.setActiveRequester(null);
        requestor.onTransactionResponse();
    }
}
