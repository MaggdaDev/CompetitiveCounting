package competitivecounting;

import discord4j.core.object.reaction.ReactionEmoji;

public interface CountingEmojis {
    ReactionEmoji KEKMARK = ReactionEmoji.of(Long.parseLong("805121814296133653"), "kekmark", false),
            ONE = ReactionEmoji.unicode("\u0031\u20E3"),
            TWO = ReactionEmoji.unicode("\u0032\u20E3"),
            THREE = ReactionEmoji.unicode("\u0033\u20E3"),
            BOLT = ReactionEmoji.unicode("\u26A1"),
            TROPHY = ReactionEmoji.unicode("\uD83C\uDFC6"),
            GOLDEN_KEKMARK = ReactionEmoji.of(Long.parseLong("1506024606522282045"), "goldenkekmark", false),
            KEKMARK_BOLT = ReactionEmoji.of(Long.parseLong("1506021088528695417"), "kekmark_bolt", false),
            WARNING = ReactionEmoji.unicode("\u26A0"),
            GOBLIN = ReactionEmoji.unicode("\uD83D\uDC7A"),
            HANDSHAKE = ReactionEmoji.unicode("\uD83E\uDD1D"),
            THUMBS_UP = ReactionEmoji.unicode("\uD83D\uDC4D"),
            THUMBS_DOWN = ReactionEmoji.unicode("\uD83D\uDC4E"),
            VAULT_LOCATOR_ICON = ReactionEmoji.unicode("\uD83D\uDCE1"),
            KEY = ReactionEmoji.unicode("\uD83D\uDD11"),
            X = ReactionEmoji.unicode("\u274C"),
            COIN = ReactionEmoji.unicode("\uD83E\uDE99"),
            ARROW_UP = ReactionEmoji.unicode("\u2B06"),
            ARROW_DOWN = ReactionEmoji.unicode("\u2B07"),
            SPECIAL_TROPHY = ReactionEmoji.of(Long.parseLong("1546098104141619271"), "special_trophy", false);

    ReactionEmoji[] ALL_NUMBER_EMOJIS = {
            ReactionEmoji.unicode("\u0030\u20E3"),
            ONE,
            TWO,
            THREE,
            ReactionEmoji.unicode("\u0034\u20E3"),
            ReactionEmoji.unicode("\u0035\u20E3"),
            ReactionEmoji.unicode("\u0036\u20E3"),
            ReactionEmoji.unicode("\u0037\u20E3"),
            ReactionEmoji.unicode("\u0038\u20E3"),
            ReactionEmoji.unicode("\u0039\u20E3"),
    };
}
