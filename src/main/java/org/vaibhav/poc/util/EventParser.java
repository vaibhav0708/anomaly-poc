package org.vaibhav.poc.util;

import org.json.JSONObject;

public class EventParser {

    public static String extractPartnerId(String json) {
        JSONObject obj = new JSONObject(json);

        // direct key, because your event is flat
        if (obj.has("partnerId")) {
            return obj.getString("partnerId");
        }

        // fallback if future schema nests partner
        if (obj.has("partner")) {
            JSONObject partnerObj = obj.getJSONObject("partner");
            if (partnerObj.has("partnerId")) {
                return partnerObj.getString("partnerId");
            }
        }

        throw new RuntimeException("partnerId not found in event JSON: " + json);
    }

}
