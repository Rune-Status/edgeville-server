package edgeville.game.character.combat.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edgeville.game.NodeType;
import edgeville.game.character.Entity;
import edgeville.game.character.Hit;
import edgeville.game.character.HitType;
import edgeville.game.character.PoisonType;
import edgeville.game.character.npc.NpcDefinition;
import edgeville.game.character.player.Player;
import edgeville.game.item.Item;

/**
 * The combat effect applied when a character needs to be poisoned.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class CombatPoisonEffect extends CombatEffect {

    /**
     * The collection of weapons mapped to their respective poison types.
     */
    public static final Map<Integer, PoisonType> TYPES = new HashMap<>();

    /**
     * The amount of times this player has been hit.
     */
    private int amount;

    /**
     * Creates a new {@link CombatPoisonEffect}.
     */
    public CombatPoisonEffect() {
        super(30);
    }

    @Override
    public boolean apply(Entity t) {
        if (t.isPoisoned() || t.getPoisonType() == null)
            return false;
        if (t.getType() == NodeType.PLAYER) {
            Player player = (Player) t;
            if (player.getPoisonImmunity().get() > 0)
                return false;
            player.getMessages().sendMessage("You have been poisoned!");
        }
        t.getPoisonDamage().set(t.getPoisonType().getDamage());
        return true;
    }

    @Override
    public boolean removeOn(Entity t) {
        return !t.isPoisoned();
    }

    @Override
    public void process(Entity t) {
        amount--;
        t.damage(new Hit(t.getPoisonDamage().get(), HitType.POISON));
        if (amount == 0) {
            amount = 4;
            t.getPoisonDamage().decrementAndGet();
        }
    }

    @Override
    public boolean onLogin(Entity t) {
        return t.isPoisoned();
    }

    /**
     * Gets the {@link PoisonType} for {@code item} wrapped in an optional. If a
     * poison type doesn't exist for the item then an empty optional is
     * returned.
     *
     * @param item
     *            the item to get the poison type for.
     * @return the poison type for this item wrapped in an optional, or an empty
     *         optional if no poison type exists.
     */
    public static Optional<PoisonType> getPoisonType(Item item) {
        if (item == null || item.getId() < 1 || item.getAmount() < 1)
            return Optional.empty();
        return Optional.ofNullable(TYPES.get(item.getId()));
    }

    /**
     * Gets the {@link PoisonType} for {@code npc} wrapped in an optional. If a
     * poison type doesn't exist for the NPC then an empty optional is returned.
     *
     * @param npc
     *            the NPC to get the poison type for.
     * @return the poison type for this NPC wrapped in an optional, or an empty
     *         optional if no poison type exists.
     */
    public static Optional<PoisonType> getPoisonType(int npc) {
        NpcDefinition def = NpcDefinition.DEFINITIONS[npc];
        if (def == null || !def.isAttackable() || !def.isPoisonous())
            return Optional.empty();
        if (def.getCombatLevel() < 75)
            return Optional.of(PoisonType.DEFAULT_NPC);
        if (def.getCombatLevel() < 200)
            return Optional.of(PoisonType.STRONG_NPC);
        return Optional.of(PoisonType.SUPER_NPC);
    }
}
