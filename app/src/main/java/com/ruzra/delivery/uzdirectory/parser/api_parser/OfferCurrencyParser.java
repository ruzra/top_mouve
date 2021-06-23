package com.ruzra.delivery.uzdirectory.parser.api_parser;


import com.ruzra.delivery.uzdirectory.classes.Currency;
import com.ruzra.delivery.uzdirectory.parser.Parser;

import org.json.JSONException;
import org.json.JSONObject;


public class OfferCurrencyParser extends Parser {

    public OfferCurrencyParser(JSONObject json) {
        super(json);
    }

    public Currency getCurrency() {

        Currency mCurrency = new Currency();

        try {


            mCurrency.setId((int) json.getDouble("id"));
            mCurrency.setCode(json.getString("code"));
            mCurrency.setSymbol(json.getString("symbol"));
            mCurrency.setName(json.getString("name"));
            mCurrency.setFormat((int) json.getDouble("format"));
            mCurrency.setRate((int) json.getDouble("rate"));

            return mCurrency;

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }


}
