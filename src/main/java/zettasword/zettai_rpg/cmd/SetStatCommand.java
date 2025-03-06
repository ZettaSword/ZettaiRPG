package zettasword.zettai_rpg.cmd;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import zettasword.zettai_rpg.cap.RPGData;
import zettasword.zettai_rpg.variables.Stats;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SetStatCommand extends CommandBase {
    @Override
    public String getName() {
        return "setStat";
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "/setStat [PlayerName] [vit/str/agi/int/mining] [points]";
    }

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender commandSender, String[] args) throws CommandException {
        if (args.length != 3) {
            throw new WrongUsageException(this.getUsage(commandSender));
        }
        EntityPlayer player;
        try {
            player = minecraftServer.getPlayerList().getPlayerByUsername(args[0]);
            RPGData data = RPGData.get(Objects.requireNonNull(player));
            switch (args[1]) {
                case "vit": data.setVariable(Stats.VIT, parseInt(args[2])); data.sync(); break;
                case "str": data.setVariable(Stats.STR, parseInt(args[2])); data.sync(); break;
                case "agi": data.setVariable(Stats.AGI, parseInt(args[2])); data.sync(); break;
                case "int": data.setVariable(Stats.INT, parseInt(args[2])); data.sync(); break;
                case "mining": data.setVariable(Stats.MINING_SPEED, parseInt(args[2])); data.sync(); break;
            }
        }catch (Exception ignore){
            throw new WrongUsageException(this.getUsage(commandSender));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender commandSender, String[] args, @Nullable BlockPos pos) {
        List<String> tabs = Lists.newArrayList();
        if (args.length == 1){
            tabs.addAll(Arrays.asList(server.getPlayerList().getOnlinePlayerNames()));
        } else if (args.length == 2) {
            tabs.addAll(Arrays.asList("vit", "str", "agi", "int", "mining"));
        } else if (args.length == 3) {
            tabs.add("0");
        }
        return tabs;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayer && server.getPlayerList().canSendCommands(((EntityPlayer) sender).getGameProfile());
    }
}
