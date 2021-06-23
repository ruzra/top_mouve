package com.ruzra.delivery.uzdirectory.parser.api_parser;


import android.util.Log;

import com.ruzra.delivery.uzdirectory.appconfig.AppConfig;
import com.ruzra.delivery.uzdirectory.classes.OpeningTime;
import com.ruzra.delivery.uzdirectory.parser.Parser;

import org.json.JSONException;
import org.json.JSONObject;


public class OpeningTimeParser extends Parser {

    public OpeningTimeParser(JSONObject json) {
        super(json);
    }

    public OpeningTime getOpeningTimes() {


        if (AppConfig.APP_DEBUG) {
            Log.i("JSONOpeningTimeArray", json.toString());
        }


        try {

            JSONObject json_ot = json.getJSONObject(0 + "");

            if (AppConfig.APP_DEBUG) {
                Log.i("OpeningTimeUD", json_ot + "");
            }
            OpeningTime mOpeningTime = new OpeningTime();
            mOpeningTime.setId(json_ot.getInt("id"));
            mOpeningTime.setStore_id(json_ot.getInt("store_id"));
            mOpeningTime.setDay(json_ot.getString("day"));
            mOpeningTime.setClosing(json_ot.getString("closing"));
            mOpeningTime.setOpening(json_ot.getString("opening"));
            mOpeningTime.setTimezone(json_ot.getString("timezone"));
            mOpeningTime.setEnabled(json_ot.getInt("enabled"));

            if (AppConfig.APP_DEBUG) {
                Log.i("ParserOpeningTime", mOpeningTime.getId() + "  " + mOpeningTime.getDay());
            }
            return mOpeningTime;


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }


}
