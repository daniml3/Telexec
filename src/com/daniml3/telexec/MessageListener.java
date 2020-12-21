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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;

public class MessageListener {

    // CTRL+C handling values
    final int MAX_STOP_ATTEMPTS = 5;
    int stopAttempts = 0;

    // Tasks and exceptions arrays
    ArrayList<String> runningTasks = new ArrayList<>();
    ArrayList<String> exceptionArrayList= new ArrayList<>();

    // The arguments all the commands contain
    // In this case, only the Telegram argument which will be used for user interaction
    private final Class<?>[] commandArguments = {Telegram.class, MessageListener.class};

    // Telegram class, to be initialized in startListening(Telegram telegram)
    Telegram telegram = null;

    public boolean userInteractionBusy = false;
    private boolean keepListening;
    private boolean silent;

    public void startListening(Telegram telegram) {
        keepListening = true;
        silent = false;
        this.telegram = telegram;
        ArrayList<Class<?>> commandClasses = new ArrayList<>();
        commandClasses.add(com.daniml3.telexec.Commands.class);

        // Detect if the CustomCommands class exists so we can execute commands from there
        try {
            commandClasses.add(Class.forName("com.daniml3.telexec.CustomCommands"));
        } catch (ClassNotFoundException ignore) {}

        while (keepListening) {
            // Get latest updates from the Telegram API
            telegram.getUpdates();

            if (telegram.newMessage && telegram.isCommand && !userInteractionBusy) {
                new Thread (() -> {
                    for (Class<?> commandClass : commandClasses) {
                        try {
                            commandClass
                                    .getMethod(telegram.lastCommand, commandArguments)
                                    .invoke(telegram.lastCommand, telegram, this);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            // Clear the screen for printing the UI
            Utils.clearScreen();

            // Print the UI with the updated information
            Utils.print("=======Telexec======" +
                        "\nCopyright (C) 2020 daniml3 All Rights Reserved" +
                        "\nThis program is licensed under the GNU GPLv3 license" +
                        "\n====================" +
                        "\n=====Debug info=====" +
                        "\nRunning tasks: " + runningTasks.toString() +
                        "\nUser interaction status: " + (userInteractionBusy ? "Busy" : "Free") +
                        "\nAllowed users: " + telegram.getAllowedUsers() +
                        "\nException list: " + exceptionArrayList.toString() +
                        "\nLast API update: " + Instant.now() +
                        "\n====================");
        }

        while (!runningTasks.isEmpty()) {
            Utils.clearScreen();
            Utils.print("====================" +
                        "\nWaiting for all threads to stop" +
                        "\nRunning tasks: " + runningTasks.toString() +
                        "\nIf you want to forcefully stop the Bot, press CTRL+C {0} more time(s)"
                                .replace("{0}", String.valueOf(MAX_STOP_ATTEMPTS - stopAttempts)) +
                        "\n====================");
            Utils.sleep(10);
        }

        if (!silent)
            telegram.sendMessage("Bot stopped");
        Utils.print("Telexec stopped successfully");
        System.exit(0);
    }

    public void stopListening() {
        keepListening = false;
        stopAttempts++;
        if (stopAttempts == MAX_STOP_ATTEMPTS) {
            Utils.print("Killing all processes and exiting");
            System.exit(130);
        }
    }

    public void stopListening(boolean silent) {
        this.silent = silent;
        stopListening();
    }

    public void addNewTask(Runnable runnable, String title) {
        telegram.newMessage = false;
        new Thread(() -> {
            try {
                // Start the task on a separate thread and add it to the running task list
                Thread thread = new Thread(runnable);
                thread.start();
                runningTasks.add(title);
                // Wait for it to finish and remove it from the running task list
                thread.join();
                runningTasks.remove(title);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void addRunningTask(String title) {
        runningTasks.add(title);
    }

    public void removeRunningTask(String title) {
        runningTasks.remove(title);
    }

    public void setUserInteractionBusy(boolean busy) {
        userInteractionBusy = busy;
    }

    // Add a new exception to the exceptions list for 20 seconds (for debugging)
    public void addException (Exception exception) {
        new Thread(() -> {
            StringWriter error = new StringWriter();
            exception.printStackTrace(new PrintWriter(error));
            if (!exceptionArrayList.contains(error.toString())) {
                exceptionArrayList.add(error.toString());
                Utils.sleep(20000);
                exceptionArrayList.remove(error.toString());
            }
        }).start();
    }
}
