package CompetitiveCounting.dialogue;

import discord4j.core.object.entity.Message;

public class SubDialogue extends DialogueElement {
    private final Dialogue dialogue;

    public SubDialogue(Dialogue dialogue) {
        this.dialogue = dialogue;
    }

    @Override
    public void run(Message message) {
        dialogue.playBlocking(message);
    }

     @Override
    public void dispose() {
        super.dispose();
        dialogue.stop();
     }
}
