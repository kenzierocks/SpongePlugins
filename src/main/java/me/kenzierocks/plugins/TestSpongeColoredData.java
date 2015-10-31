/*
 * This file is part of SpongePlugins, licensed under the MIT License (MIT).
 *
 * Copyright (c) kenzierocks (Kenzie Togami) <http://kenzierocks.me>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.plugins;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.awt.Color;
import java.util.Optional;

/**
 * Usage: Hold item in hand, use command {@code (/changecolor <int value>)}.
 */
@Plugin(id = TestSpongeColoredData.PLUGIN_ID, name = TestSpongeColoredData.PLUGIN_NAME, version = TestSpongeColoredData.PLUGIN_VERSION)
public class TestSpongeColoredData {

    public static final class ColorChangeCommand implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            String color = args.<String>getOne("color").get();
            Color c = new Color(Integer.decode(color).intValue());
            if (src instanceof ArmorEquipable) {
                Optional<ItemStack> item = ((ArmorEquipable) src).getItemInHand();
                if (item.isPresent()) {
                    if (item.get().offer(Keys.COLOR, c).getType() == Type.SUCCESS) {
                        src.sendMessage(Texts.of("SUCCESS"));
                        // deserialize for lols
                        Color successColor = item.get().get(Keys.COLOR).get();
                        src.sendMessage(Texts.of("Look at the nice color: " + Integer.toHexString(successColor.getRGB())));
                        return CommandResult.success();
                    } else {
                        src.sendMessage(Texts.of("LOL FAIL, tried to apply color " + c + " to " + item.get()));
                    }
                } else {
                    src.sendMessage(Texts.of("LOL FAIL, no item in hand."));
                }
            } else {
                src.sendMessage(Texts.of("LOL FAIL, no hands!"));
            }
            return CommandResult.empty();
        }
    }

    public static final String PLUGIN_ID = "testcoloreddata", PLUGIN_NAME = "Test ColoredData", PLUGIN_VERSION = "1.0";
    @Inject private Logger logger;
    @Inject private Game game;

    @Listener
    public void onGamePreInitilization(GamePreInitializationEvent event) {
        this.logger.info("Commencing initialization...");
        CommandSpec colorChangeCommand = CommandSpec.builder()
                .description(Texts.of("CHANGE COLORZ!"))
                .arguments(GenericArguments.string(Texts.of("color")))
                .executor(new ColorChangeCommand())
                .build();

        game.getCommandDispatcher().register(this, colorChangeCommand, "changecolor");
    }
}
