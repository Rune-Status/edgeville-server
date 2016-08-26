package edgeville.net.message.impl;

import edgeville.game.character.player.Player;
import edgeville.net.message.InputMessageListener;
import edgeville.net.message.MessageBuilder;

/**
 * The message sent from the client when the player clicks on the 'Click this to
 * continue' link to forward a dialogue.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class AdvanceDialogueMessage implements InputMessageListener {

    @Override
    public void handleMessage(Player player, int opcode, int size, MessageBuilder payload) {
        if (player.isDisabled())
            return;
        player.getDialogueChain().advance();
    }
}
