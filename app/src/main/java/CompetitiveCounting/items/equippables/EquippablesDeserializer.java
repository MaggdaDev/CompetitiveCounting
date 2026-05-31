package CompetitiveCounting.items.equippables;

import com.google.gson.*;

import java.lang.reflect.Type;

public class EquippablesDeserializer implements JsonDeserializer<Equippable> {

    @Override
    public Equippable deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        String type = obj.get("name").getAsString();
        Equippable ret;
        switch (type) {
            case VaultLocator.NAME:
                ret = context.deserialize(obj, VaultLocator.class);
                break;
            default:
                throw new JsonParseException("Unknown type: " + type);
        };
        return ret;
    }
}