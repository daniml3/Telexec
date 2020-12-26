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

public class Commands {
    public static void start(Telegram telegram, MessageListener listener, String chatId) {
        status(telegram, listener, chatId);
    }

    public static void status(Telegram telegram, MessageListener listener, String chatId) {
        telegram.sendMessage("Telexec is successfully running!\n\nJava version: {0}"
                .replace("{0}",System.getProperty("java.version")), chatId);
    }

    public static void stop(Telegram telegram, MessageListener listener, String chatId) {
        Runnable runnable;
        telegram.sendMessage("Do you want to stop the bot? (y/n)", chatId);

        runnable = () -> {
            boolean stop = false;

            while (true) {
                listener.setUserInteractionBusy(true);
                if (telegram.newMessage && !telegram.isCommand) {
                    switch (telegram.lastMessageString.toLowerCase()) {
                        case "y", "yes" -> {
                            telegram.sendMessage("The bot will stop when all the threads finishes", chatId);
                            listener.stopListening();
                            stop = true;
                        }
                        case "n", "no" -> {
                            telegram.sendMessage("Keeping the bot running", chatId);
                            stop = true;
                        }
                        default -> telegram.sendMessage("That is not a valid option", chatId);
                    }
                }
                if (stop)
                    break;
                Utils.sleep(10);
            }
            listener.setUserInteractionBusy(false);
        };

        listener.addNewTask(runnable, "Stop");
    }
}
