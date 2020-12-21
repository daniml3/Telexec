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

public class Utils {
    public static void print(String text) {
        System.out.println(text);
    }

    public static void sleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException ignore) {

        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
