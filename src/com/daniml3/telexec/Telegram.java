/*
 *  Telexec: a modular Telegram Bot
 *  Copyright (C) 2020 daniml3
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.daniml3.telexec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// This is the Telegram class
// This class will be able to do most of the API calls with the Telegram API
public class Telegram {

    public boolean newMessage;
    public boolean isCommand;
    public String lastMessageString;
    public String lastCommand;
    public String chatId = "";

    // Initial values to interact with the API
    // The botToken must be updated before any API call
    // The other values will be updated dynamically and automatically in the getUpdates method
    private String botToken = "";
    private ArrayList<String> allowedUsers = new ArrayList<>();
    private int lastUpdateId = 0;

    public JSONObject lastMessage;

    // Basic API call
    private JSONObject telegram(String method, String args) {
        try {
            URL telegramAPIUrl = new URL("https://api.telegram.org/bot" + botToken + "/" + method + args.replace(" ", "%20"));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(telegramAPIUrl.openStream()));
            JSONObject apiResponse;

            StringBuilder response = new StringBuilder();
            String inputLine;
            // Get the API response and save it in a StringBuilder
            while ((inputLine = bufferedReader.readLine()) != null)
                response.append(inputLine);
            bufferedReader.close();

            apiResponse = new JSONObject(response.toString());

            // Return the API response
            return apiResponse;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public void sendMessage(String message) {
        telegram("sendMessage", "?chat_id=" + chatId + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    public void configureBotToken(String token) {
        botToken = token;
    }

    public void getUpdates() {
        JSONArray response;
        String from;

        try {
            // Get the last updates from the Telegram API
            // If the lastUpdateId is 0, which means that is the first API call (the program just started),
            // do not use any offset, and after we get an update id from the response, use it so we get the
            // last available message
            if (lastUpdateId != 0) {
                response = telegram("getUpdates", "?offset=" + lastUpdateId).getJSONArray("result");
            } else {
                response = telegram("getUpdates", "").getJSONArray("result");
            }

            // Get the last JSONObject of the array
            lastMessage = response.getJSONObject(response.length()-1);

            // Try to get the message id using the chat JSONObject, and if doesn't exist, use the from JSONObject (private message)
            try {
                chatId = String.valueOf(lastMessage.getJSONObject("message").getJSONObject("chat").get("id")); // Last message chat id
            } catch (JSONException e) {
                chatId = String.valueOf(lastMessage.getJSONObject("message").getJSONObject("from").get("id")); // Last message chat id
            }

            from = String.valueOf(lastMessage.getJSONObject("message").getJSONObject("from").getInt("id")); // Last message chat id

            // Save all the latest message information
            newMessage = lastMessage.getInt("update_id") > lastUpdateId && lastUpdateId != 0 && allowedUsers.contains(from); // Whether there is a new message
            lastUpdateId = lastMessage.getInt("update_id"); // Last message update id
            lastMessageString = lastMessage.getJSONObject("message").getString("text"); // Last message text
            isCommand = String.valueOf(lastMessageString.charAt(0)).equals("/"); // Whether it is a command

            if (isCommand)
                lastCommand = lastMessageString.split("/")[1];
        } catch (JSONException | java.lang.ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void addAllowedUser(String userId) {
        if (!allowedUsers.contains(userId))
            allowedUsers.add(userId);
    }

    public String getAllowedUsers() {
        return allowedUsers.toString();
    }
}
