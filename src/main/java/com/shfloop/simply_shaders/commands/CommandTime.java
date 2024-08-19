package com.shfloop.simply_shaders.commands;

import com.shfloop.simply_shaders.Shadows;
import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.commands.Command;

public class CommandTime extends Command {
    public CommandTime() {
    }

    @Override
    public String getShortDescription() {
        return "Control time in dynamic sky";
    }

    @Override
    public void run(Chat chat, String[] args) {
        // want /time set int range 0- cycle time int ticks
        // time length int (ticks)  0 - whatever
        ///time query (show time) seconds and ticks
        // time pause
        //tiem start
        //time add int ticks

        super.run(chat, args);
        if (args.length < 2) {
            //missing args so print help for command
            String HELP_INFO = ("/time set <ticks> - sets time to <ticks> \n " +
                    "/time add <ticks> - adds <ticks> to current time \n " +
                    "/time length <ticks> - sets day/night cycle time [default: 38400]\n" +
                    "/time query - shows current time in ticks \n" +
                    "/time start - starts dynamic sky [default]\n " +
                    "/time stop - stops dynamic sky ");
            chat.sendMessage(this.world, this.player, (Account)null, HELP_INFO);
        } else if (args.length < 3) {

            switch(args[1].toLowerCase()) {
                case "query":
                    chat.sendMessage(this.world, this.player, (Account)null, "Current time: " + Shadows.time_of_day);
                    break;
                case "stop":
                    Shadows.doDaylightCycle = false;
                    break;
                case "start":
                    Shadows.doDaylightCycle = true;
                    break;
                default: {
                    this.commandError("Unknown Command");
                }
            }



        } else {
            String arg2 = args[1];
            int value;
            try {
                 value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                this.commandError("invalid value");
                return;
            }
            switch (arg2.toLowerCase()) {
                case "add":
                    Shadows.updateTime(Shadows.time_of_day + value);
                    break;
                case "length":
                    Shadows.cycleLength = value;
                    Shadows.updateTime(0); //should reset time
                    break;
                case "set":
                   Shadows.updateTime(value);
                    break;
                default: {
                    this.commandError("Unknown command");
                }

            }


        }
    }
}
