package edgeville.game.character.timers;

/**
 * @author Simon on 30/8/2016
 */
public class Timer {

	private TimerKey key;
	private int ticks;

	public Timer(TimerKey key, int ticks) {
		this.key = key;
		this.ticks = ticks;
	}

	public int ticks() {
		return ticks;
	}

	public TimerKey key() {
		return key;
	}

	public void tick() {
		if (ticks > 0)
			ticks--;
	}

}
