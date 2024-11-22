// Generated from TradeOffer.g4 by ANTLR 4.10.1
package CompetitiveCounting.Parser.TradeOfferParser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TradeOfferParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		YOUGET=10, IGET=11, PERC=12, LIMIT=13, PERCNUM=14, ENDCONTRACTS=15, SEMIC=16, 
		COLON=17, NUM=18, ID=19, WS=20;
	public static final int
		RULE_startRule = 0, RULE_command = 1, RULE_user = 2, RULE_tradable = 3, 
		RULE_money = 4, RULE_contract = 5, RULE_a = 6, RULE_r = 7;
	private static String[] makeRuleNames() {
		return new String[] {
			"startRule", "command", "user", "tradable", "money", "contract", "a", 
			"r"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'~tradeoffer'", "'<@'", "'>'", "'+'", "'money'", "'contract'", 
			"'a'", "'b'", "'hello'", null, null, null, null, null, null, "';'", "':'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "YOUGET", 
			"IGET", "PERC", "LIMIT", "PERCNUM", "ENDCONTRACTS", "SEMIC", "COLON", 
			"NUM", "ID", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "TradeOffer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TradeOfferParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class StartRuleContext extends ParserRuleContext {
		public CommandContext command() {
			return getRuleContext(CommandContext.class,0);
		}
		public UserContext user() {
			return getRuleContext(UserContext.class,0);
		}
		public TerminalNode EOF() { return getToken(TradeOfferParser.EOF, 0); }
		public TerminalNode YOUGET() { return getToken(TradeOfferParser.YOUGET, 0); }
		public List<TerminalNode> COLON() { return getTokens(TradeOfferParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(TradeOfferParser.COLON, i);
		}
		public List<TradableContext> tradable() {
			return getRuleContexts(TradableContext.class);
		}
		public TradableContext tradable(int i) {
			return getRuleContext(TradableContext.class,i);
		}
		public TerminalNode IGET() { return getToken(TradeOfferParser.IGET, 0); }
		public StartRuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_startRule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).enterStartRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).exitStartRule(this);
		}
	}

	public final StartRuleContext startRule() throws RecognitionException {
		StartRuleContext _localctx = new StartRuleContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_startRule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			command();
			setState(17);
			user();
			setState(33);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				{
				setState(18);
				match(YOUGET);
				setState(19);
				match(COLON);
				setState(20);
				tradable(0);
				setState(21);
				match(IGET);
				setState(22);
				match(COLON);
				setState(23);
				tradable(0);
				}
				}
				break;
			case 2:
				{
				setState(31);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case YOUGET:
					{
					{
					setState(25);
					match(YOUGET);
					setState(26);
					match(COLON);
					setState(27);
					tradable(0);
					}
					}
					break;
				case IGET:
					{
					{
					setState(28);
					match(IGET);
					setState(29);
					match(COLON);
					setState(30);
					tradable(0);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			}
			setState(35);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommandContext extends ParserRuleContext {
		public CommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).enterCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).exitCommand(this);
		}
	}

	public final CommandContext command() throws RecognitionException {
		CommandContext _localctx = new CommandContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_command);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			match(T__0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UserContext extends ParserRuleContext {
		public TerminalNode NUM() { return getToken(TradeOfferParser.NUM, 0); }
		public UserContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_user; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).enterUser(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).exitUser(this);
		}
	}

	public final UserContext user() throws RecognitionException {
		UserContext _localctx = new UserContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_user);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(39);
			match(T__1);
			setState(40);
			match(NUM);
			setState(41);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TradableContext extends ParserRuleContext {
		public MoneyContext money() {
			return getRuleContext(MoneyContext.class,0);
		}
		public ContractContext contract() {
			return getRuleContext(ContractContext.class,0);
		}
		public TerminalNode ENDCONTRACTS() { return getToken(TradeOfferParser.ENDCONTRACTS, 0); }
		public List<TradableContext> tradable() {
			return getRuleContexts(TradableContext.class);
		}
		public TradableContext tradable(int i) {
			return getRuleContext(TradableContext.class,i);
		}
		public TradableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tradable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).enterTradable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).exitTradable(this);
		}
	}

	public final TradableContext tradable() throws RecognitionException {
		return tradable(0);
	}

	private TradableContext tradable(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TradableContext _localctx = new TradableContext(_ctx, _parentState);
		TradableContext _prevctx = _localctx;
		int _startState = 6;
		enterRecursionRule(_localctx, 6, RULE_tradable, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				{
				setState(44);
				money();
				}
				break;
			case T__5:
				{
				setState(45);
				contract();
				}
				break;
			case ENDCONTRACTS:
				{
				setState(46);
				match(ENDCONTRACTS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(54);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TradableContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_tradable);
					setState(49);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(50);
					match(T__3);
					setState(51);
					tradable(2);
					}
					} 
				}
				setState(56);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class MoneyContext extends ParserRuleContext {
		public TerminalNode COLON() { return getToken(TradeOfferParser.COLON, 0); }
		public TerminalNode NUM() { return getToken(TradeOfferParser.NUM, 0); }
		public MoneyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_money; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).enterMoney(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).exitMoney(this);
		}
	}

	public final MoneyContext money() throws RecognitionException {
		MoneyContext _localctx = new MoneyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_money);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			match(T__4);
			setState(58);
			match(COLON);
			setState(59);
			match(NUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ContractContext extends ParserRuleContext {
		public List<TerminalNode> COLON() { return getTokens(TradeOfferParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(TradeOfferParser.COLON, i);
		}
		public TerminalNode PERC() { return getToken(TradeOfferParser.PERC, 0); }
		public TerminalNode PERCNUM() { return getToken(TradeOfferParser.PERCNUM, 0); }
		public TerminalNode SEMIC() { return getToken(TradeOfferParser.SEMIC, 0); }
		public TerminalNode LIMIT() { return getToken(TradeOfferParser.LIMIT, 0); }
		public TerminalNode NUM() { return getToken(TradeOfferParser.NUM, 0); }
		public ContractContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_contract; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).enterContract(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).exitContract(this);
		}
	}

	public final ContractContext contract() throws RecognitionException {
		ContractContext _localctx = new ContractContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_contract);
		try {
			setState(75);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(61);
				match(T__5);
				setState(62);
				match(COLON);
				setState(63);
				match(PERC);
				setState(64);
				match(COLON);
				setState(65);
				match(PERCNUM);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(66);
				match(T__5);
				setState(67);
				match(COLON);
				setState(68);
				match(PERC);
				setState(69);
				match(COLON);
				setState(70);
				match(PERCNUM);
				setState(71);
				match(SEMIC);
				setState(72);
				match(LIMIT);
				setState(73);
				match(COLON);
				setState(74);
				match(NUM);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AContext extends ParserRuleContext {
		public AContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).enterA(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).exitA(this);
		}
	}

	public final AContext a() throws RecognitionException {
		AContext _localctx = new AContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_a);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			_la = _input.LA(1);
			if ( !(_la==T__6 || _la==T__7) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(TradeOfferParser.ID, 0); }
		public RContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_r; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).enterR(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TradeOfferListener ) ((TradeOfferListener)listener).exitR(this);
		}
	}

	public final RContext r() throws RecognitionException {
		RContext _localctx = new RContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_r);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(T__8);
			setState(80);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 3:
			return tradable_sempred((TradableContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean tradable_sempred(TradableContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0014S\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0003\u0000 \b\u0000\u0003\u0000\"\b\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0003\u00030\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003"+
		"5\b\u0003\n\u0003\f\u00038\t\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005L\b\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0000\u0001"+
		"\u0006\b\u0000\u0002\u0004\u0006\b\n\f\u000e\u0000\u0001\u0001\u0000\u0007"+
		"\bP\u0000\u0010\u0001\u0000\u0000\u0000\u0002%\u0001\u0000\u0000\u0000"+
		"\u0004\'\u0001\u0000\u0000\u0000\u0006/\u0001\u0000\u0000\u0000\b9\u0001"+
		"\u0000\u0000\u0000\nK\u0001\u0000\u0000\u0000\fM\u0001\u0000\u0000\u0000"+
		"\u000eO\u0001\u0000\u0000\u0000\u0010\u0011\u0003\u0002\u0001\u0000\u0011"+
		"!\u0003\u0004\u0002\u0000\u0012\u0013\u0005\n\u0000\u0000\u0013\u0014"+
		"\u0005\u0011\u0000\u0000\u0014\u0015\u0003\u0006\u0003\u0000\u0015\u0016"+
		"\u0005\u000b\u0000\u0000\u0016\u0017\u0005\u0011\u0000\u0000\u0017\u0018"+
		"\u0003\u0006\u0003\u0000\u0018\"\u0001\u0000\u0000\u0000\u0019\u001a\u0005"+
		"\n\u0000\u0000\u001a\u001b\u0005\u0011\u0000\u0000\u001b \u0003\u0006"+
		"\u0003\u0000\u001c\u001d\u0005\u000b\u0000\u0000\u001d\u001e\u0005\u0011"+
		"\u0000\u0000\u001e \u0003\u0006\u0003\u0000\u001f\u0019\u0001\u0000\u0000"+
		"\u0000\u001f\u001c\u0001\u0000\u0000\u0000 \"\u0001\u0000\u0000\u0000"+
		"!\u0012\u0001\u0000\u0000\u0000!\u001f\u0001\u0000\u0000\u0000\"#\u0001"+
		"\u0000\u0000\u0000#$\u0005\u0000\u0000\u0001$\u0001\u0001\u0000\u0000"+
		"\u0000%&\u0005\u0001\u0000\u0000&\u0003\u0001\u0000\u0000\u0000\'(\u0005"+
		"\u0002\u0000\u0000()\u0005\u0012\u0000\u0000)*\u0005\u0003\u0000\u0000"+
		"*\u0005\u0001\u0000\u0000\u0000+,\u0006\u0003\uffff\uffff\u0000,0\u0003"+
		"\b\u0004\u0000-0\u0003\n\u0005\u0000.0\u0005\u000f\u0000\u0000/+\u0001"+
		"\u0000\u0000\u0000/-\u0001\u0000\u0000\u0000/.\u0001\u0000\u0000\u0000"+
		"06\u0001\u0000\u0000\u000012\n\u0001\u0000\u000023\u0005\u0004\u0000\u0000"+
		"35\u0003\u0006\u0003\u000241\u0001\u0000\u0000\u000058\u0001\u0000\u0000"+
		"\u000064\u0001\u0000\u0000\u000067\u0001\u0000\u0000\u00007\u0007\u0001"+
		"\u0000\u0000\u000086\u0001\u0000\u0000\u00009:\u0005\u0005\u0000\u0000"+
		":;\u0005\u0011\u0000\u0000;<\u0005\u0012\u0000\u0000<\t\u0001\u0000\u0000"+
		"\u0000=>\u0005\u0006\u0000\u0000>?\u0005\u0011\u0000\u0000?@\u0005\f\u0000"+
		"\u0000@A\u0005\u0011\u0000\u0000AL\u0005\u000e\u0000\u0000BC\u0005\u0006"+
		"\u0000\u0000CD\u0005\u0011\u0000\u0000DE\u0005\f\u0000\u0000EF\u0005\u0011"+
		"\u0000\u0000FG\u0005\u000e\u0000\u0000GH\u0005\u0010\u0000\u0000HI\u0005"+
		"\r\u0000\u0000IJ\u0005\u0011\u0000\u0000JL\u0005\u0012\u0000\u0000K=\u0001"+
		"\u0000\u0000\u0000KB\u0001\u0000\u0000\u0000L\u000b\u0001\u0000\u0000"+
		"\u0000MN\u0007\u0000\u0000\u0000N\r\u0001\u0000\u0000\u0000OP\u0005\t"+
		"\u0000\u0000PQ\u0005\u0013\u0000\u0000Q\u000f\u0001\u0000\u0000\u0000"+
		"\u0005\u001f!/6K";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}