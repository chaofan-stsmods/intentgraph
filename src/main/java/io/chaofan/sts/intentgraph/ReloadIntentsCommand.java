package io.chaofan.sts.intentgraph;

import basemod.devcommands.ConsoleCommand;

public class ReloadIntentsCommand extends ConsoleCommand {
    public ReloadIntentsCommand() {
        maxExtraTokens = 1;
        minExtraTokens = 0;
        requiresPlayer = true;
        simpleCheck = true;
    }

    @Override
    protected void execute(String[] args, int depth) {
        IntentGraphMod.instance.loadIntents();
        if (depth < args.length) {
            try {
                IntentGraphMod.instance.overwriteAscension = Integer.parseInt(args[depth]);
            } catch (NumberFormatException ignored) {
                IntentGraphMod.instance.overwriteAscension = -1;
            }
        }
    }
}
