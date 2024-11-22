package CompetitiveCounting;

import discord4j.core.object.reaction.ReactionEmoji;

public interface Emojis {
    ReactionEmoji KEKMARK = ReactionEmoji.of(Long.parseLong("805121814296133653"), "kekmark", false),
            ONE = ReactionEmoji.unicode("\u0031\u20E3"),
            TWO = ReactionEmoji.unicode("\u0032\u20E3"),
            THREE = ReactionEmoji.unicode("\u0033\u20E3"),
            BOLT = ReactionEmoji.unicode("\u26A1"),
            TROPHY = ReactionEmoji.unicode("\uD83C\uDFC6"),
            GOLDEN_KEKMARK = ReactionEmoji.of(Long.parseLong("1308931056556314675"), "goldenkekmark", false);
}
