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

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.BlockItemData;
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
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

/**
 * Usage: Hold item in hand, use command {@code (/blockitemcycle)} to randomize
 * first property.
 */
@Plugin(id = TestSpongeBlockItemData.PLUGIN_ID, name = TestSpongeBlockItemData.PLUGIN_NAME, version = TestSpongeBlockItemData.PLUGIN_VERSION)
public class TestSpongeBlockItemData {

    private static final class BlockItemDataCommand implements CommandExecutor {

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (src instanceof ArmorEquipable) {
                Optional<ItemStack> item = ((ArmorEquipable) src).getItemInHand();
                if (item.isPresent()) {
                    Optional<BlockState> manipulatorOpt = item.get().get(BlockItemData.class).get().get(Keys.ITEM_BLOCKSTATE);
                    Optional<BlockState> stateOpt = item.get().get(Keys.ITEM_BLOCKSTATE);
                    Preconditions.checkState(manipulatorOpt.equals(stateOpt));
                    if (!stateOpt.isPresent()) {
                        src.sendMessage(Texts.of("Missing state!"));
                        return CommandResult.empty();
                    }
                    BlockState state = stateOpt.get().copy();
                    src.sendMessage(Texts.of(state.getTraitMap()));
                } else {
                    src.sendMessage(Texts.of("LOL FAIL, no item in hand."));
                }
            } else {
                src.sendMessage(Texts.of("LOL FAIL, no hands!"));
            }
            return CommandResult.empty();
        }

    }

    public static final class BlockItemCycleCommand implements CommandExecutor {

        private static final Random RANDOM = new Random();

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (src instanceof ArmorEquipable) {
                Optional<ItemStack> item = ((ArmorEquipable) src).getItemInHand();
                if (item.isPresent()) {
                    Optional<BlockState> stateOpt = item.get().get(Keys.ITEM_BLOCKSTATE);
                    if (!stateOpt.isPresent()) {
                        src.sendMessage(Texts.of("Missing state!"));
                        return CommandResult.empty();
                    }
                    BlockState state = stateOpt.get().copy();
                    BlockTrait<?> trait = state.getTraits().iterator().next();
                    Optional<?> val = randomFromIterator(trait.getPossibleValues());
                    if (val.isPresent()) {
                        state.withTrait(trait, val.get());
                        if (item.get().offer(Keys.ITEM_BLOCKSTATE, state).getType() == Type.SUCCESS) {
                            src.sendMessage(Texts.of("SUCCESS"));
                            return CommandResult.success();
                        } else {
                            src.sendMessage(Texts.of("LOL FAIL, tried to randomize property on " + item.get()));
                        }
                    } else {
                        src.sendMessage(Texts.of("LOL FAIL, no trait to randomize!"));
                    }
                } else {
                    src.sendMessage(Texts.of("LOL FAIL, no item in hand."));
                }
            } else {
                src.sendMessage(Texts.of("LOL FAIL, no hands!"));
            }
            return CommandResult.empty();
        }

        private Optional<?> randomFromIterator(Collection<?> possibleValues) {
            return Optional.ofNullable(FluentIterable.from(possibleValues).skip(RANDOM.nextInt(possibleValues.size())).first().orNull());
        }

    }

    public static final String PLUGIN_ID = "testblockitemdata", PLUGIN_NAME = "Test BlockItemData", PLUGIN_VERSION = "1.0";
    @Inject private Logger logger;
    @Inject private Game game;

    @Listener
    public void onGamePreInitilization(GamePreInitializationEvent event) {
        this.logger.info("Commencing initialization...");
        CommandSpec cycleCommand = CommandSpec.builder()
                .description(Texts.of("Cycle your items! Colors!"))
                .executor(new BlockItemCycleCommand())
                .build();

        game.getCommandDispatcher().register(this, cycleCommand, "blockitemcycle");
        CommandSpec dumpCommand = CommandSpec.builder()
                .description(Texts.of("Print data."))
                .executor(new BlockItemDataCommand())
                .build();

        game.getCommandDispatcher().register(this, dumpCommand, "blockitemdata");
    }
}
