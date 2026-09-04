package competitivecounting.vaults;

import competitivecounting.Counter;
import competitivecounting.CountingBot;
import competitivecounting.CountingEmojis;
import competitivecounting.dialogue.Dialogue;
import discord4j.core.object.entity.Message;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static competitivecounting.vaults.Vault.RIDDLE_KEY_TIMEOUT_SECONDS;

public class RiddleDialogue extends Dialogue {
    private AtomicReference<String> riddleSolverRef = new AtomicReference<>(null);

    public RiddleDialogue addWaitForCorrectSolutionAndSetWinningUserRef(BiFunction<Message, Integer, Boolean> solutionChecker) {
        addWaitForUserAnswer((msg) -> {
            String content = msg.getContent().trim().toLowerCase();
            if (!content.startsWith("~")) {
                return false;
            }
            String answerStr = content.substring(1);
            int answer;
            try {
                answer = Integer.parseInt(answerStr);
            } catch (NumberFormatException e) {
                return false;
            }
            boolean ret = solutionChecker.apply(msg, answer);
            if (ret) {
                if (msg.getAuthor().isPresent()) {
                    riddleSolverRef.set(msg.getAuthor().get().getId().asString());
                }
            }
            return ret;
        });
        return this;
    }

    public RiddleDialogue addWaitForCorrectSolutionAndSetWinningUserRef(int solution) {
        return addWaitForCorrectSolutionAndSetWinningUserRef((msg, answer) -> answer == solution);
    }

    public RiddleDialogue addWaitForKeyReaction(String solutionExplanation) {
        addEmojiReaction(CountingEmojis.KEY);
        if (solutionExplanation != null && !solutionExplanation.isEmpty()) {
            addNpcLineButKeepOldMessage(solutionExplanation, 0);
        }
        addWaitForEmojiReaction(CountingEmojis.KEY, false,
                m -> {
                }, riddleSolverRef, RIDDLE_KEY_TIMEOUT_SECONDS, m -> {
                    m.removeReactions(CountingEmojis.KEY).subscribe();
                    CountingBot.write(m, getCounterFromIdRef(m, riddleSolverRef).getName() +
                            ", your vault key timed out! The vault will remain locked forever.");
                    riddleSolverRef.set(null);
                    return true;
                });
        return this;
    }

    public RiddleDialogue addWaitForKeyReaction() {
        return addWaitForKeyReaction(null);
    }

    protected static Counter getCounterFromIdRef(Message msg, AtomicReference<String> ref) {
        if (ref.get() == null) {
            return null;
        }
        return CountingBot.getCounter(msg.getGuildId().get().asString(), ref.get());
    }

    public AtomicReference<String> getRiddleSolverRef() {
        return riddleSolverRef;
    }

    public void setRiddleSolverId(String riddleSolverId) {
        this.riddleSolverRef.set(riddleSolverId);
    }

    public String getRiddleSolverId() {
        return riddleSolverRef.get();
    }

    public Counter getWinningCounter(Message m) {
        if (getRiddleSolverId() == null) {
            return null;
        }
        return getCounterFromIdRef(m, riddleSolverRef);
    }
}
