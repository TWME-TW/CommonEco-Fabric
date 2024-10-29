package cc.thonly.eco.init;

import cc.thonly.eco.command.CommandBalance;
import cc.thonly.eco.command.CommandBalanceTop;
import cc.thonly.eco.command.CommandEco;
import cc.thonly.eco.command.CommandPay;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class InitCommand {
    public static void init() {
        register(CommandBalance::register);
        register(CommandPay::register);
        register(CommandEco::register);
        register(CommandBalanceTop::register);
    }
    public static void register(CommandRegistrationCallback Method) {
        CommandRegistrationCallback.EVENT.register(Method);
    }
}
