package world.bentobox.magicnethergenerator.commands;


import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Material;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magicnethergenerator.NetherGeneratorAddon;
import world.bentobox.magicnethergenerator.config.Settings;
import world.bentobox.magicnethergenerator.config.Settings.GeneratorTier;


/**
 * This class allows to run /[toplabel] nethergen levelsall command, that prints all
 * Magic Generator Tier for target world.
 */
public class AllLevelsCommand extends CompositeCommand
{
    /**
     * Formatter for 2 decimal places
     */
    private static final DecimalFormat DF2 = new DecimalFormat("#.##");


    /**
     * Sub-command constructor
     */
    public AllLevelsCommand(Addon addon, CompositeCommand cmd)
    {
        super(addon, cmd, "levelsall");
    }


    /**
     * Setups anything that is needed for this command. <br/><br/> It is recommended you do the following in
     * this method:
     * <ul>
     * <li>Register any of the sub-commands of this command;</li>
     * <li>Define the permission required to use this command using {@link CompositeCommand#setPermission(String)};</li>
     * <li>Define whether this command can only be run by players or not using {@link
     * CompositeCommand#setOnlyPlayer(boolean)};</li>
     * </ul>
     */
    @Override
    public void setup()
    {
        this.setDescription("nethergenerator.commands.alllevels.description");
    }


    /**
     * This method executes /[toplabel] nethergen levelsall command. This command simply finds
     * all Magic Nether Generator Tiers for input world and prints them in chat/console.
     * If magic nether generator is not working in current world, then message will be shown about it.
     */
    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        NetherGeneratorAddon addon = getAddon();
        List<Settings.GeneratorTier> generatorTierList = addon.getManager().getAllGeneratorTiers(this.getWorld());

        if (generatorTierList.isEmpty())
        {
            user.sendMessage("nethergenerator.errors.cannot-find-any-generators");
            return false;
        }
        generatorTierList.forEach(t -> displayTier(user, t));
        return true;
    }


    /**
     * Display the tier stats to the user
     * @param user - user
     * @param generatorTier - tier to show
     */
    static void displayTier(User user, GeneratorTier generatorTier) {
        // Create a sorted list of material and chance
        TreeMap<Double, Material> chances = (TreeMap<Double, Material>) generatorTier.getBlockChanceMap();
        Map<Material, Double> percentages = new EnumMap<>(Material.class);
        double last = 0D;
        for (Entry<Double, Material> en : chances.entrySet()) {
            double percent = (en.getKey() - last) / chances.lastKey() * 100;
            percentages.put(en.getValue(), percent);
            last = en.getKey();
        }

        user.sendMessage("nethergenerator.messages.generator-tier",
                "[name]", generatorTier.getName(),
                "[value]", Integer.toString(generatorTier.getMinLevel()));

        for (Entry<Material, Double> en : percentages.entrySet())
        {
            user.sendMessage("nethergenerator.messages.material-chance",
                    "[name]", Util.prettifyText(en.getKey().toString()),
                    "[value]",  DF2.format(en.getValue()));
        }


    }
}
