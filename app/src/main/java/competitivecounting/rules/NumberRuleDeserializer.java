package competitivecounting.rules;

import com.google.gson.*;

import java.lang.reflect.Type;

public class NumberRuleDeserializer implements JsonDeserializer<NumberRule> {

    @Override
    public NumberRule deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        String type = obj.get("numberRuleType").getAsString();
        NumberRule ret;
        switch (type) {
            case DividerRule.NUMBER_RULE_TYPE:
                ret = context.deserialize(obj, DividerRule.class);
                break;
            case DigSumRule.NUMBER_RULE_TYPE:
                ret = context.deserialize(obj, DigSumRule.class);
                break;
            case RootRule.NUMBER_RULE_TYPE:
                ret= context.deserialize(obj, RootRule.class);
                break;
            default:
                throw new JsonParseException("Unknown type: " + type);
        };
        return ret;
    }
}