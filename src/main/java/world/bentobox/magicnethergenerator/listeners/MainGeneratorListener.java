package world.bentobox.magicnethergenerator.listeners;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.magicnethergenerator.NetherGeneratorAddon;


/**
 * Main Generator Listener. This class contains listener that process Generator options.
 * This class contains listener and all methods that detect if custom generation can be
 * processed.
 */
public class MainGeneratorListener implements Listener {
    /**
     * A bit of randomness
     */
    public final Random random = new Random();
    /**
     * Main addon class.
     */
    private NetherGeneratorAddon addon;






    /**
     * Constructor MainGeneratorListener creates a new MainGeneratorListener instance.
     *
     * @param addon of type NetherGeneratorAddon
     */
    public MainGeneratorListener(NetherGeneratorAddon addon) {
        this.addon = addon;
    }


    // ---------------------------------------------------------------------
    // Section: Private Methods
    // ---------------------------------------------------------------------

    /**
     * This method detects if BlockPlaceEvent can be used by Magic Nether Generator
     * by checking all requirements and calls custom generator if all requirements are met.
     *
     *
     * @param event BlockPlaceEvent which may create the generator.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event){
        Block eventSourceBlock = event.getBlock();
        Material mat1 = Material.getMaterial(addon.getSettings().getMaterial1());
        Material mat2 = Material.getMaterial(addon.getSettings().getMaterial2());

        // If not operating in nether, then return as fast as possible
        if (!this.addon.getManager().canOperateInWorld(eventSourceBlock.getWorld())){
            return;
        }

        // If island members are not online then do not continue
        if (!this.addon.getManager().isMembersOnline(eventSourceBlock.getLocation())) {
            return;
        }

        // If flag is off, return
        if (addon.getIslands().getIslandAt(eventSourceBlock.getLocation())
                .map(island -> !island.isAllowed(addon.getFlag())).orElse(!addon.getFlag().isSetForWorld(eventSourceBlock.getWorld()))) {
            return;
        }

        //If players out of range, return
        if (!this.isInRangeToGenerate(eventSourceBlock)) {
            return;
        }

        //If block is not honey or sponge, end
        Material blockType = eventSourceBlock.getType();
        if (!blockType.equals(mat1) && !blockType.equals(mat2)){
            return;
        }

        //If proper conditions exist, spawn block
        BlockFace face = checkForNeededConditions(eventSourceBlock);
        if (!face.equals(BlockFace.SELF)){
            Block toSpawn = eventSourceBlock.getRelative(face);
            generateBlock(toSpawn);

        }
        else return;


    }

    /**
     * This method detects if BlockBreakEvent can be used by Magic Nether Generator
     * by checking all requirements and calls custom generator if all requirements are met.
     *
     *
     * @param event BlockBreakEvent which may iterate the generator.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Block eventSourceBlock = event.getBlock();
        Player player = event.getPlayer();


        // If not operating in nether, then return as fast as possible
        if (!this.addon.getManager().canOperateInWorld(eventSourceBlock.getWorld())) {
            return;
        }

        // If island members are not online then do not continue
        if (!this.addon.getManager().isMembersOnline(eventSourceBlock.getLocation())) {
            return;
        }

        // If flag is off, return
        if (addon.getIslands().getIslandAt(eventSourceBlock.getLocation())
                .map(island -> !island.isAllowed(addon.getFlag())).orElse(!addon.getFlag().isSetForWorld(eventSourceBlock.getWorld()))) {
            return;
        }

        //If players out of range, return
        if (!this.isInRangeToGenerate(eventSourceBlock)) {
            return;
        }
        Plugin plugin = this.addon.getPlugin();


        if (checkForGenerator(eventSourceBlock)){
            long delay = addon.getSettings().getSpawnDelay();
            event.setCancelled(true);
            eventSourceBlock.breakNaturally(player.getInventory().getItemInMainHand());
            // Give exp
            player.giveExp(((BlockBreakEvent)event).getExpToDrop());
            // Damage tool
            damageTool(player, eventSourceBlock);
            //generateBlock(eventSourceBlock);

            //add delay after block break before block creation
            Bukkit.getScheduler().runTaskLater(plugin, () ->generateBlock(eventSourceBlock), delay);
        }
        else return;

    }

    /**
     * This method detects if BlockPhysicsEvent can be used by Magic Nether Generator
     * by checking all requirements and calls custom generator if all requirements are met.
     *
     *
     * @param event BlockPhysicsEvent which may iterate the generator.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
        Block eventSourceBlock = event.getBlock();

        //If not enabled, return as fast as possible
        if(!this.addon.getSettings().getPhysicsSpawn()){
            return;
        }

        // If not operating in nether, then return
        if (!this.addon.getManager().canOperateInWorld(eventSourceBlock.getWorld())) {
            return;
        }

        // If island members are not online then do not continue
        if (!this.addon.getManager().isMembersOnline(eventSourceBlock.getLocation())) {
            return;
        }

        // If flag is off, return
        if (addon.getIslands().getIslandAt(eventSourceBlock.getLocation())
                .map(island -> !island.isAllowed(addon.getFlag())).orElse(!addon.getFlag().isSetForWorld(eventSourceBlock.getWorld()))) {
            return;
        }

        //If players out of range, return
        if (!this.isInRangeToGenerate(eventSourceBlock)) {
            return;
        }


        Plugin plugin = this.addon.getPlugin();


        if (checkForGenerator(eventSourceBlock)){
            long delay = addon.getSettings().getSpawnDelay();

            //add delay after block falling before block creation
            Bukkit.getScheduler().runTaskLater(plugin, () ->generateBlock(eventSourceBlock), delay);
        }
        else return;

    }



    /**
     * This method detects if BlockPistonRetractEvent can be used by Magic Nether Generator
     * by checking all requirements and calls custom generator if all requirements are met.
     *
     *
     * @param event BlockPistonRetractEvent which may iterate the generator.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        Block eventSourceBlock = event.getBlock();

        //If not enabled, return as fast as possible
        if(!this.addon.getSettings().getPistonSpawn()){
            return;
        }

        // If not operating in nether, then return
        if (!this.addon.getManager().canOperateInWorld(eventSourceBlock.getWorld())) {
            return;
        }

        // If island members are not online then do not continue
        if (!this.addon.getManager().isMembersOnline(eventSourceBlock.getLocation())) {
            return;
        }

        // If flag is off, return
        if (addon.getIslands().getIslandAt(eventSourceBlock.getLocation())
                .map(island -> !island.isAllowed(addon.getFlag())).orElse(!addon.getFlag().isSetForWorld(eventSourceBlock.getWorld()))) {
            return;
        }

        //If players out of range, return
        if (!this.isInRangeToGenerate(eventSourceBlock)) {
            return;
        }


        Plugin plugin = this.addon.getPlugin();


        if (checkForGenerator(eventSourceBlock)){
            long delay = addon.getSettings().getSpawnDelay();

            //add delay after block falling before block creation
            Bukkit.getScheduler().runTaskLater(plugin, () ->generateBlock(eventSourceBlock), delay);
        }
        else return;

    }



    // ---------------------------------------------------------------------
    // Section: Private Methods
    // ---------------------------------------------------------------------

    /**
     * This method plays handles the generation of the new block.
     *
     * @param block block placement where particle and block must be generated.
     */

    private void generateBlock(Block block){
        if (this.addon.getGenerator().isReplacementGenerated(block, true)) {
            // sound when lava transforms to cobble
            this.playEffects(block);
        }
    }


    /**
     * This method plays handles the generation of the new block.
     *
     * @param block block placement where particle and block must be generated.
     */
    private void damageTool(Player player, Block block){
        ItemStack inHand = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = inHand.getItemMeta();

        if (itemMeta instanceof Damageable && !itemMeta.isUnbreakable() && TOOLS.containsKey(inHand.getType())) {
            Damageable meta = (Damageable) itemMeta;
            // Get the item's current durability
            Integer durability = meta.getDamage();
            // Get the damage this will do
            int damage = TOOLS.get(inHand.getType());
            if (durability != null) {
                // Check for DURABILITY
                if (itemMeta.hasEnchant(Enchantment.DURABILITY)) {
                    int level = itemMeta.getEnchantLevel(Enchantment.DURABILITY);
                    if (random.nextInt(level + 1) == 0) {
                        meta.setDamage(durability + damage);
                    }
                } else {
                    meta.setDamage(durability + damage);
                }
                if (meta.getDamage() > inHand.getType().getMaxDurability()) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                    player.getInventory().setItemInMainHand(null);
                } else {
                    inHand.setItemMeta(itemMeta);
                }
            }
        }

    }






    /**
     * This method plays sound effect and adds particles to new block.
     *
     * @param block block placement where particle must be generated.
     */
    private void playEffects(Block block) {
        final double blockX = block.getX();
        final double blockY = block.getY();
        final double blockZ = block.getZ();

        // Run everything in new task
        Bukkit.getScheduler().runTask(this.addon.getPlugin(), () -> {
            // Play sound for spawning block
            block.getWorld().playSound(block.getLocation(),
                    Sound.BLOCK_FIRE_EXTINGUISH,
                    SoundCategory.BLOCKS,
                    0.5F,
                    2.6F + (this.random.nextFloat() * 2 - 1) * 0.8F);

            // This spawns 8 large smoke particles.
            for (int counter = 0; counter < 8; ++counter) {
                block.getWorld().spawnParticle(Particle.SMOKE_LARGE,
                        blockX + Math.random(),
                        blockY + 1 + Math.random(),
                        blockZ + Math.random(),
                        1,
                        0,
                        0,
                        0,
                        0);
            }
        });
    }



    /**
     * This method transforms input block face to next BlockFace by 90 degree in counter clockwise direction.
     * Only on horizontal and vertical pane for NORTH,EAST,SOUTH,WEST,DOWN,UP directions.
     *
     * @param face input BlockFace
     * @return Output BlockFace that is 90 degree from input BlockFace in counter clockwise direction
     */
    private BlockFace getCounterClockwiseDirection(BlockFace face) {
        switch (face) {
            case DOWN:
                return BlockFace.UP;
            case UP:
                return BlockFace.NORTH;
            case NORTH:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.DOWN;
            case SOUTH:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.SOUTH;
            default:
                // Not interested in other directions
                return face;


        }
    }


    /**
     * This method transforms input block face to inverse BlockFace by 180 degree rotation.
     *
     * @param face input BlockFace
     * @return Output BlockFace that is 180 degree from input BlockFace
     */

    private BlockFace getInverseDirection(BlockFace face){
        switch (face) {
            case DOWN:
                return BlockFace.UP;
            case UP:
                return BlockFace.DOWN;
            case NORTH:
                return BlockFace.SOUTH;
            case EAST:
                return BlockFace.WEST;
            case SOUTH:
                return BlockFace.NORTH;
            case WEST:
                return BlockFace.EAST;
            default:
                // Not interested in other directions
                return face;


        }
    }

    /**
     * Check if Spawning conditions are right
     *
     * @param block Block to check for generator conditions around.
     */
    private BlockFace checkForNeededConditions(Block block){
        BlockFace face = BlockFace.NORTH;
        boolean air = false, needed = false;
        Material opposite = getOpposite(block);
        int x = 0;
        do {
            if (block.getRelative(face).getType().equals(Material.AIR) ||
                    block.getRelative(face).getType().equals(Material.CAVE_AIR) ||
                    block.getRelative(face).getType().equals(Material.VOID_AIR)) {
                air = true;
            }
            if (air && block.getRelative(face, 2).getType().equals(opposite)) {
                //check if block two blocks away is correct
                needed = true;
            }
            if (air == true && needed == true) return face;
            face =  getCounterClockwiseDirection(face);
            x++;
            air = false;
            needed = false;
        } while (x < 6);

        return BlockFace.SELF;
    }



    /**
     * Check if generator exists
     *
     * @param block Block to check existing generator around.
     */

    boolean checkForGenerator(Block block){
        Material mat1 = Material.getMaterial(addon.getSettings().getMaterial1());
        Material mat2 = Material.getMaterial(addon.getSettings().getMaterial2());
        BlockFace face = BlockFace.NORTH;
        int x = 0;
        do{
            if (block.getRelative(face).getType().equals(mat1) && block.getRelative(getInverseDirection(face)).getType().equals(mat2)){
                return true;
            }
            if (block.getRelative(face).getType().equals(mat2) && block.getRelative(getInverseDirection(face)).getType().equals(mat1)){
                return true;
            }


            x++;
            if (face == BlockFace.NORTH) face = BlockFace.WEST;
            else if (face == BlockFace.WEST) face = BlockFace.DOWN;

        }while (x<3);

        return false;

    }

    /**
     * Get the opposite block that is needed
     *
     * @param block Input block to find opposite for
     */
    private Material getOpposite (Block block){
        Material mat1 = Material.getMaterial(addon.getSettings().getMaterial1());
        Material mat2 = Material.getMaterial(addon.getSettings().getMaterial2());
        if (block.getType().equals(mat1)) return mat2;
        else if (block.getType().equals(mat2)) return mat1;
        else return null; //if this ever gets triggered, something has gone wrong.
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------



    /**
     * This method returns there is an island member in range to active of "custom" block generation
     *
     * @param block Block that must be checked.
     * @return true if there is a player in the set range
     */
    protected boolean isInRangeToGenerate(Block block) {
        int workingRange = this.addon.getSettings().getWorkingRange() * this.addon.getSettings().getWorkingRange();
        if (workingRange > 0) {
            return addon.getIslands().getIslandAt(block.getLocation()).map(i -> block.getWorld().getNearbyEntities(block.getLocation(), workingRange, workingRange, workingRange)
                    .stream()
                    .filter(Player.class::isInstance)
                    .filter(e -> i.getMemberSet().contains(e.getUniqueId()))
                    .anyMatch(e -> block.getLocation().distanceSquared(e.getLocation()) <= workingRange)
            ).orElse(false);
        }
        return true;

    }

    /**
     * Tools that take damage. See https://minecraft.gamepedia.com/Item_durability#Tool_durability
     */
    private final static Map<Material, Integer> TOOLS;
    static {
        Map<Material, Integer> t = new HashMap<>();
        t.put(Material.DIAMOND_AXE, 1);
        t.put(Material.DIAMOND_SHOVEL, 1);
        t.put(Material.DIAMOND_PICKAXE, 1);
        t.put(Material.IRON_AXE, 1);
        t.put(Material.IRON_SHOVEL, 1);
        t.put(Material.IRON_PICKAXE, 1);
        t.put(Material.WOODEN_AXE, 1);
        t.put(Material.WOODEN_SHOVEL, 1);
        t.put(Material.WOODEN_PICKAXE, 1);
        t.put(Material.GOLDEN_AXE, 1);
        t.put(Material.GOLDEN_SHOVEL, 1);
        t.put(Material.GOLDEN_PICKAXE, 1);
        t.put(Material.STONE_AXE, 1);
        t.put(Material.STONE_SHOVEL, 1);
        t.put(Material.STONE_PICKAXE, 1);
        t.put(Material.SHEARS, 1);
        t.put(Material.DIAMOND_SWORD, 2);
        t.put(Material.GOLDEN_SWORD, 2);
        t.put(Material.STONE_SWORD, 2);
        t.put(Material.IRON_SWORD, 2);
        t.put(Material.WOODEN_SWORD, 2);
        t.put(Material.TRIDENT, 2);
        TOOLS = Collections.unmodifiableMap(t);
    }


}