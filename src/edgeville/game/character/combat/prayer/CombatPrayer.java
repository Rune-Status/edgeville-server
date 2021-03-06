package edgeville.game.character.combat.prayer;

import java.util.Arrays;

import com.google.common.collect.ImmutableList;

import edgeville.game.World;
import edgeville.game.character.Flag;
import edgeville.game.character.player.Player;
import edgeville.game.character.player.skill.Skills;
import edgeville.utility.TextUtils;

/**
 * The enumerated type whose elements represent the prayers that can be
 * activated and deactivated. This currently only has support for prayers
 * present in the {@code 317} protocol.
 *
 * @author lare96 <http://github.com/lare96>
 */
public enum CombatPrayer {
    THICK_SKIN(20, -1, 1, 83, 3, 9),
    BURST_OF_STRENGTH(20, -1, 4, 84, 4, 10),
    CLARITY_OF_THOUGHT(20, -1, 7, 85, 5, 11),
    ROCK_SKIN(10, -1, 10, 86, 9, 0),
    SUPERHUMAN_STRENGTH(10, -1, 13, 87, 1, 10),
    IMPROVED_REFLEXES(10, -1, 16, 88, 2, 11),
    RAPID_RESTORE(29, -1, 19, 89),
    RAPID_HEAL(29, -1, 22, 90),
    PROTECT_ITEM(29, -1, 25, 91),
    STEEL_SKIN(5, -1, 28, 92, 0, 3),
    ULTIMATE_STRENGTH(5, -1, 31, 93, 1, 4),
    INCREDIBLE_REFLEXES(5, -1, 34, 94, 2, 5),
    PROTECT_FROM_MAGIC(5, 2, 37, 95, 13, 14, 15, 16, 17),
    PROTECT_FROM_MISSILES(5, 1, 40, 96, 12, 14, 15, 16, 17),
    PROTECT_FROM_MELEE(5, 0, 43, 97, 12, 13, 15, 16, 17),
    RETRIBUTION(17, 3, 46, 98, 12, 13, 14, 16, 17),
    REDEMPTION(6, 5, 49, 99, 12, 13, 14, 15, 17),
    SMITE(7, 4, 52, 100, 12, 13, 14, 15, 16),
	
	CHIVALRY(7, 4, 52, 100, 12, 13, 14, 15, 16),//TODO:correct numbers, drain is correct though
	PIETY(7, 4, 52, 100, 12, 13, 14, 15, 16);//TODO:correct numbers, drain is correct though
	//int drainRate, int headIcon, int level, int config, int... deactivate) {

    /**
     * The cached array that will contain mappings of all the elements to their
     * identifiers.
     */
    public static final ImmutableList<CombatPrayer> VALUES = ImmutableList.copyOf(values());

    /**
     * The amount of ticks it takes for prayer to be drained.
     */
    private final int drainRate;

    /**
     * The head icon present when this prayer is activated.
     */
    private final int headIcon;

    /**
     * The level required to use this prayer.
     */
    private final int level;

    /**
     * The config to make the prayer button light up when activated.
     */
    private final int config;

    /**
     * The combat prayers that will be automatically deactivated when this one
     * is activated.
     */
    private final int[] deactivate;

    /**
     * Creates a new {@link CombatPrayer}.
     *
     * @param id
     *            the identification for this prayer.
     * @param drainRate
     *            the amount of ticks it takes for prayer to be drained.
     * @param headIcon
     *            the head icon present when this prayer is activated.
     * @param level
     *            the level required to use this prayer.
     * @param config
     *            the config to make the prayer button light up when activated.
     * @param deactivate
     *            the combat prayers that will be automatically deactivated.
     */
    private CombatPrayer(int drainRate, int headIcon, int level, int config, int... deactivate) {
        this.drainRate = drainRate;
        this.headIcon = headIcon;
        this.level = level;
        this.config = config;
        this.deactivate = deactivate;
    }

    @Override
    public String toString() {
        return TextUtils.capitalize(name().toLowerCase().replaceAll("_", " "));
    }

    /**
     * Executed dynamically when this combat prayer is activated for
     * {@code player}.
     *
     * @param player
     *            the player that activated this prayer.
     * @return {@code true} if this prayer can activated, {@code false}
     *         otherwise.
     */
    public boolean onActivation(Player player) {
        return true;
    }

    /**
     * Executed dynamically when this combat prayer is deactivated for
     * {@code player}.
     *
     * @param player
     *            the player that deactivated this prayer.
     * @return {@code true} if this prayer can deactivated, {@code false}
     *         otherwise.
     */
    public boolean onDeactivation(Player player) {
        return true;
    }

    /**
     * Activates this combat prayer for {@code player}. If
     * {@code deactivateIfActivated} is flagged {@code true} then if this prayer
     * is already activated it will be deactivated instead.
     *
     * @param player
     *            the player to activate this prayer for.
     * @param deactivateIfActivated
     *            if this prayer should be deactivated, if it is already
     *            activated.
     */
    public final void activate(Player player, boolean deactivateIfActivated) {
        if (CombatPrayer.isActivated(player, this)) {
            if (deactivateIfActivated)
                deactivate(player);
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (player.getSkills()[Skills.PRAYER].getRealLevel() < level) {
            sb.append("You need a @blu@Prayer " + "level of " + level + " @bla@to use @blu@" + this + "@bla@.");
        } else if (player.getSkills()[Skills.PRAYER].getLevel() < 1) {
            sb.append("You need to recharge your prayer at an altar!");
        }
        if (sb.length() > 0) {
            player.getMessages().sendByteState(config, 0);
            player.getMessages().sendMessage(sb.toString());
            return;
        }
        if (!onActivation(player))
            return;
        if (player.getPrayerDrain() == null || !player.getPrayerDrain().isRunning()) {
            player.setPrayerDrain(new CombatPrayerTask(player));
            World.submit(player.getPrayerDrain());
        }
        Arrays.stream(deactivate).forEach(it -> VALUES.get(it).deactivate(player));
        player.getPrayerActive().add(this);
        player.getMessages().sendByteState(config, 1);
        if (headIcon != -1) {
            player.setHeadIcon(headIcon);
            player.getFlags().set(Flag.APPEARANCE);
        }
    }

    /**
     * Activates this combat prayer for {@code player}. If this prayer is
     * already activated it then this method does nothing when invoked.
     *
     * @param player
     *            the player to activate this prayer for.
     */
    public final void activate(Player player) {
        activate(player, false);
    }

    /**
     * Attempts to deactivate this prayer for {@code player}. If this prayer is
     * already deactivated then invoking this method does nothing.
     *
     * @param player
     *            the player to deactivate this prayer for.
     */
    public final void deactivate(Player player) {
        if (!CombatPrayer.isActivated(player, this))
            return;
        if (!onDeactivation(player))
            return;
        player.getPrayerActive().remove(this);
        player.getMessages().sendByteState(config, 0);
        if (headIcon != -1) {
            player.setHeadIcon(-1);
            player.getFlags().set(Flag.APPEARANCE);
        }
    }

    /**
     * Deactivates activated combat prayers for {@code player}. Combat prayers
     * that are already deactivated will be ignored.
     *
     * @param player
     *            the player to deactivate prayers for.
     */
    public static void deactivateAll(Player player) {
        VALUES.forEach(it -> it.deactivate(player));
    }

    /**
     * Determines if the {@code prayer} is activated for the {@code player}.
     *
     * @param player
     *            the player's prayers to check.
     * @param prayer
     *            the prayer to check is active.
     * @return {@code true} if the prayer is activated for the player,
     *         {@code false} otherwise.
     */
    public static boolean isActivated(Player player, CombatPrayer prayer) {
        return player.getPrayerActive().contains(prayer);
    }

    /**
     * Gets the amount of ticks it takes for prayer to be drained.
     *
     * @return the amount of ticks.
     */
    public final int getDrainRate() {
        return drainRate;
    }

    /**
     * Gets the head icon present when this prayer is activated.
     *
     * @return the head icon.
     */
    public final int getHeadIcon() {
        return headIcon;
    }

    /**
     * Gets the level required to use this prayer.
     *
     * @return the level required.
     */
    public final int getLevel() {
        return level;
    }

    /**
     * Gets the config to make the prayer button light up when activated.
     *
     * @return the config for the prayer button.
     */
    public final int getConfig() {
        return config;
    }

    /**
     * Gets the combat prayers that will be automatically deactivated when this
     * one is activated.
     * 
     * @return the deactivated combat prayers.
     */
    public int[] getDeactivate() {
        return deactivate;
    }
}
