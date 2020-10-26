package cl.figonzal.lastquakechile.managers;

import android.content.Context;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

import timber.log.Timber;

/**
 * Class used to parse json string to date, by gson
 */
public class DateGsonDeserializer implements JsonDeserializer<Date> {

    private final Context context;

    public DateGsonDeserializer(Context context) {
        this.context = context;
    }

    @Override
    public Date deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String date = element.getAsString();

        DateManager dateManager = new DateManager();

        try {
            return dateManager.stringToDate(context, date);
        } catch (ParseException e) {
            Timber.e(e, "Parse gson deserializer exception");
        }

        return null;
    }
}
