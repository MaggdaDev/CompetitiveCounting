/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package CompetitiveCounting.Parser.TradeOfferParser;

import CompetitiveCounting.CountingBot;
import discord4j.core.object.entity.Message;
import java.util.concurrent.atomic.AtomicBoolean;
import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author david
 */
public class TradeOfferChecker {

    public static boolean isValid(String toCheck, Message message) {
        try {
            System.out.println(toCheck);
            //AtomicBoolean isValid = new AtomicBoolean(true);
            TradeOfferBaseListener listener = new TradeOfferBaseListener();
            TradeOfferLexer lexer = new TradeOfferLexer(CharStreams.fromString(toCheck));

            lexer.removeErrorListeners();
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

            CommonTokenStream stream = new CommonTokenStream((TokenSource) lexer);
            TradeOfferParser parser = new TradeOfferParser(stream);

            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);

            TradeOfferParser.StartRuleContext r = parser.startRule();

            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, r);
            //return isValid.get();
            return true;
        } catch (ParseCancellationException e) {
            if (message != null) {
                CountingBot.write(message, e.getMessage());
            }
            return false;
        }
    }

    public static class ThrowingErrorListener extends BaseErrorListener {

        public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) throws ParseCancellationException {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }
}
