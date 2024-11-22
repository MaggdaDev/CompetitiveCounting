grammar TradeOffer;     // The name of the grammar is Hello
startRule : command user ((YOUGET COLON tradable IGET COLON tradable) | ((YOUGET COLON tradable) | (IGET COLON tradable))) EOF;

command  :  '~tradeoffer';

user  :  '<@' NUM '>';

YOUGET  :  'YOU_GET' | 'you_get' | 'You_Get';

IGET  :  'I_GET' | 'I_get' | 'I_Get' | 'i_get';

tradable  :  money | contract | ENDCONTRACTS | tradable '+' tradable;
money  :  'money' COLON NUM;
contract: ('contract' COLON PERC COLON PERCNUM) | ('contract' COLON PERC COLON PERCNUM SEMIC LIMIT COLON NUM);
PERC  :  'perc' | 'percentage' | 'PERC';
LIMIT  :  'LIMIT' | 'limit' | 'Limit';
PERCNUM  :  [1-9] '%' | [1-9] [0-9] '%' | '0' [1-9] '%' | '100%';
ENDCONTRACTS  :  'endcontracts' | 'end_contracts' | 'END_CONTRACTS' | 'ENDCONTRACTS' | 'End_Contracts';

SEMIC  : ';';
COLON  : ':';
NUM  :  [0-9]+;

a  : 'a' | 'b';
r  : 'hello' ID ;         // The rule/production r match keyword `hello` followed by the rule `ID`
ID : [a-z]+ ;             // The rule/production `ID` match all lower-case characters
WS : [ \t\r\n]+ -> skip ; // This WS rule (WhiteSpace) skip spaces, tabs, newlines