package competitivecounting.items.equippables;

import com.google.gson.*;
import competitivecounting.items.Item;

import java.lang.reflect.Type;

public class EquippablesDeserializer implements JsonDeserializer<Equippable> {

    @Override
    public Equippable deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        String type = Item.removeExpliciteEmojis(obj.get("name").getAsString());
        Equippable ret;
        ret = context.deserialize(obj, Item.getItemByName(type).getClass());
        return ret;
    }
}