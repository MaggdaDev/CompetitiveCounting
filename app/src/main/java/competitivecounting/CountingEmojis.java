package competitivecounting;

import discord4j.core.object.emoji.Emoji;

public interface CountingEmojis {
    Emoji KEKMARK = Emoji.of(Long.parseLong("805121814296133653"), "kekmark", false),
            ONE = Emoji.unicode("\u0031\u20E3"),
            TWO = Emoji.unicode("\u0032\u20E3"),
            THREE = Emoji.unicode("\u0033\u20E3"),
            BOLT = Emoji.unicode("\u26A1"),
            TROPHY = Emoji.unicode("\uD83C\uDFC6"),
            GOLDEN_KEKMARK = Emoji.of(Long.parseLong("1506024606522282045"), "goldenkekmark", false),
            KEKMARK_BOLT = Emoji.of(Long.parseLong("1506021088528695417"), "kekmark_bolt", false),
            WARNING = Emoji.unicode("\u26A0"),
            GOBLIN = Emoji.unicode("\uD83D\uDC7A"),
            HANDSHAKE = Emoji.unicode("\uD83E\uDD1D"),
            THUMBS_UP = Emoji.unicode("\uD83D\uDC4D"),
            THUMBS_DOWN = Emoji.unicode("\uD83D\uDC4E"),
            VAULT_LOCATOR_ICON = Emoji.unicode("\uD83D\uDCE1"),
            KEY = Emoji.unicode("\uD83D\uDD11"),
            X = Emoji.unicode("\u274C"),
            COIN = Emoji.unicode("\uD83E\uDE99"),
            ARROW_UP = Emoji.unicode("\u2B06"),
            ARROW_DOWN = Emoji.unicode("\u2B07"),
            RED_QUESTION_MARK = Emoji.unicode("\u2753"),
            SPECIAL_TROPHY = Emoji.of(Long.parseLong("1546098104141619271"), "special_trophy", false);

    Emoji[] ALL_NUMBER_EMOJIS = {
            Emoji.unicode("\u0030\u20E3"),
            ONE,
            TWO,
            THREE,
            Emoji.unicode("\u0034\u20E3"),
            Emoji.unicode("\u0035\u20E3"),
            Emoji.unicode("\u0036\u20E3"),
            Emoji.unicode("\u0037\u20E3"),
            Emoji.unicode("\u0038\u20E3"),
            Emoji.unicode("\u0039\u20E3"),
    };
}
