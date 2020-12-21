# Telexec

### A  modular Telegram Bot meant for executing tasks

    Telexec: a modular Telegram Bot
    Copyright (C) 2020 daniml3

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

## Table of contents
* Documentation
    * Function of the different classes
    * Commands
    
* Building, configuring and executing it
    * Building
    * Configuring
    * Executing

## Documentation
* ### Function of the different classes:
   * Main: This is the main process, which will load the specified configuration JSON
     ,will configure everything and start the other subprocesses.
   * MessageListener: This will listen to all the new messages, run the specified tasks
     and print the UI which contains debugging info mostly.
   * Telegram: This handles the generic Telegram API calls and provides methods
     for sending messages and for getting the latest API updates.
   * Utils: This provides a few methods for various purposes, like a function to
     clear the screen.
   * Commands: As its name says, this class contains all the commands that can be executed.
    
* ### Commands
    * The commands must follow this specific template for the correct behaviour of the bot:
      ```
        // The command must always contain Telegram and MessageListener as arguments
        public static void myCommandName(Telegram telegram, MessageListener listener) {
        Runnable runnable;
      
        // Do something that doesn't need user interaction
        telegram.sendMessage("Do you want to stop the bot? (y/n)");

        // The code that will be executed on a separate thread
        runnable = () -> {
            boolean stop = false;
            // For user interaction, we need to create an infinite loop that
            // will check for new messages.
            // While the loop is running (which means that the bot should be
            // interacting with the user) we must set the user interaction to busy
            // with listener.setUserInteractionBusy(true);
            while (true) {
                listener.setUserInteractionBusy(true);
                // If there is a new message and it isn't a command, do something with it
                if (telegram.newMessage && !telegram.isCommand) {
                    switch (telegram.lastMessageString.toLowerCase()) {
                        case "case1" -> {
                            telegram.sendMessage("Case 1");
                            stop = true;
                        }
                        case "case2" -> {
                            telegram.sendMessage("Keeping the bot running");
                        }
                        default -> telegram.sendMessage("Default option");
                    }
                }
      
                // Stop the loop
                if (stop)
                    break;
      
                // There must be an sleep function for 10ms so everything works correctly
                Utils.sleep(10);
            }
      
            // When we finish the loop and the user interaction ends,
            // allow other commands to start
            listener.setUserInteractionBusy(false);
        };

        // Register and start the task
        listener.addNewTask(runnable, "Stop");
      ```
      
## Building, configuring and executing it
* ### Building
    * For building you need to have the `ant` package installed, and simply run the command `ant`.\
      This will generate a `jar` file at out/artifacts/Telexec/Telexec.jar
      
* ### Configuring
    * For configuring the Bot, you need to create a `JSON` file wherever you like.
    * The `JSON` should have the following structure:
      ```
      {
          "bot_token": "your_bot_token",
          "allowed_users": ["userId1", "userId2", "userId3"...]
      }
      ```
      
    * The `bot_token` key is the Bot token of the Bot that will send and read the messages.
    * The `allowed_users` array is the list of user ids which are allowed to use the bot.
    
* ### Executing
    * After you built the Bot, for executing it just run:
        ```
        java -jar path/to/jar --config path/to/config.json
        ```
    * For example:
        ```
        java -jar out/artifacts/Telexec_jar/Telexec.jar --config config.json
        ```
