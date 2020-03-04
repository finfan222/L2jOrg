package org.l2j.gameserver.model.actor.instance;

import org.l2j.gameserver.ai.CtrlIntention;
import org.l2j.gameserver.enums.InstanceType;
import org.l2j.gameserver.engine.geo.GeoEngine;
import org.l2j.gameserver.instancemanager.CastleManager;
import org.l2j.gameserver.instancemanager.FortDataManager;
import org.l2j.gameserver.world.World;
import org.l2j.gameserver.model.actor.Attackable;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.actor.templates.NpcTemplate;
import org.l2j.gameserver.model.entity.Castle;
import org.l2j.gameserver.model.entity.Fort;
import org.l2j.gameserver.engine.skill.api.Skill;
import org.l2j.gameserver.network.serverpackets.ActionFailed;
import org.l2j.gameserver.util.GameUtils;
import org.l2j.gameserver.util.MathUtil;

import java.util.Comparator;

public class Defender extends Attackable {
    private Castle _castle = null; // the castle which the instance should defend
    private Fort _fort = null; // the fortress which the instance should defend

    public Defender(NpcTemplate template) {
        super(template);
        setInstanceType(InstanceType.L2DefenderInstance);
    }

    @Override
    public void addDamage(Creature attacker, int damage, Skill skill) {
        super.addDamage(attacker, damage, skill);
        World.getInstance().forEachVisibleObjectInRange(this, Defender.class, 500, defender ->
        {
            defender.addDamageHate(attacker, 0, 10);
        });
    }

    /**
     * Return True if a siege is in progress and the Creature attacker isn't a Defender.
     *
     * @param attacker The Creature that the L2SiegeGuardInstance try to attack
     */
    @Override
    public boolean isAutoAttackable(Creature attacker) {
        // Attackable during siege by all except defenders
        if (!GameUtils.isPlayable(attacker)) {
            return false;
        }

        final Player player = attacker.getActingPlayer();

        // Check if siege is in progress
        if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive())) {
            final int activeSiegeId = (_fort != null) ? _fort.getId() : _castle.getId();

            // Check if player is an enemy of this defender npc
            if ((player != null) && (((player.getSiegeState() == 2) && !player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1)) || (player.getSiegeState() == 0))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasRandomAnimation() {
        return false;
    }

    /**
     * This method forces guard to return to home location previously set
     */
    @Override
    public void returnHome() {
        if (getWalkSpeed() <= 0) {
            return;
        }
        if (getSpawn() == null) {
            return;
        }
        if (!MathUtil.isInsideRadius2D(this, getSpawn(), 40)) {
            setisReturningToSpawnPoint(true);
            clearAggroList();

            if (hasAI()) {
                getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLocation());
            }
        }
    }

    @Override
    public void onSpawn() {
        super.onSpawn();

        _fort = FortDataManager.getInstance().getFort(getX(), getY(), getZ());
        _castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());

        if ((_fort == null) && (_castle == null)) {
            LOGGER.warn("Defender spawned outside of Fortress or Castle zone!" + this);
        }
    }

    /**
     * Custom onAction behaviour. Note that super() is not called because guards need extra check to see if a player should interact or ATTACK them when clicked.
     */
    @Override
    public void onAction(Player player, boolean interact) {
        if (!canTarget(player)) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        // Check if the Player already target the Folk
        if (this != player.getTarget()) {
            // Set the target of the Player player
            player.setTarget(this);
        } else if (interact) {
            if (isAutoAttackable(player) && !isAlikeDead()) {
                if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
                {
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
                }
            }
            if (!isAutoAttackable(player)) {
                if (!canInteract(player)) {
                    // Notify the Player AI with AI_INTENTION_INTERACT
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                }
            }
        }
        // Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public void useMagic(Skill skill) {
        if (!skill.isBad()) {
            Creature target = World.getInstance().findFirstVisibleObject(this, Creature.class, skill.getCastRange(), false, this::isSkillTargetable, Comparator.comparingDouble(Creature::getCurrentHp));
            setTarget(target);
        }
        super.useMagic(skill);
    }

    private boolean isSkillTargetable(Creature creature) {
        if(creature.isDead() || !GeoEngine.getInstance().canSeeTarget(this, creature)) {
            return false;
        }

        if(creature instanceof Defender) {
            return true;
        }

        if(GameUtils.isPlayer(creature)) {
            Player player = (Player) creature;
            return player.getSiegeState() == 2 && ! player.isRegisteredOnThisSiegeField(getScriptValue());
        }

        return false;

    }

    @Override
    public void addDamageHate(Creature attacker, int damage, int aggro) {
        if (attacker == null) {
            return;
        }

        if (!(attacker instanceof Defender)) {
            if ((damage == 0) && (aggro <= 1) && (GameUtils.isPlayable(attacker))) {
                final Player player = attacker.getActingPlayer();
                // Check if siege is in progress
                if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive())) {
                    final int activeSiegeId = (_fort != null) ? _fort.getId() : _castle.getId();
                    if ((player != null) && (((player.getSiegeState() == 2) && player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1)))) {
                        return;
                    }
                }
            }
            super.addDamageHate(attacker, damage, aggro);
        }
    }
}
