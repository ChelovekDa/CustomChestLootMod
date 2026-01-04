package ru.hcc.customchestloot;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hcc.customchestloot.commands.CustomLootCommand;
import ru.hcc.customchestloot.util.ChestManager;


public class Main implements ModInitializer {

    public static final String ID = "customchestloot";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    @Override
    public void onInitialize() {
        new CustomLootCommand();
        new ChestManager().startTimer();

    }
}
