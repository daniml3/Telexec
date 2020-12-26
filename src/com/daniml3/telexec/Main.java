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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.misc.Signal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        MessageListener listener = new MessageListener();
        StringBuilder stringBuilder = new StringBuilder();
        Telegram telegram = new Telegram();

        File configFile = null;

        JSONArray allowedUsersArray;
        JSONObject configFileContent;

        int i = 0;
        while (i < args.length) {
            if (args[i].equals("--config")) {
                configFile = new File(args[i + 1]);
                i++;
            }
            i++;
        }

        if (configFile == null) {
            Utils.print("Missing config file");
            System.exit(1);
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
            String line = bufferedReader.readLine();

            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }

            bufferedReader.close();

            configFileContent = new JSONObject(stringBuilder.toString());
            allowedUsersArray = configFileContent.getJSONArray("allowed_users");
            telegram.configureBotToken(configFileContent.get("bot_token").toString());

            i = 0;
            while (i < allowedUsersArray.length()) {
                telegram.addAllowedUser(allowedUsersArray.getString(i));
                i++;
            }

        } catch (IOException e) {
            Utils.print("Specified config file doesn't exist!");
            System.exit(1);
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.print("The specified config has an invalid format or wrong named keys");
            System.exit(1);
        }

        Signal.handle(new Signal("INT"), signal -> listener.stopListening(true));

        listener.startListening(telegram);
    }
}
