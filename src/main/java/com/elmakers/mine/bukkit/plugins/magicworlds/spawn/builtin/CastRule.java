package com.elmakers.mine.bukkit.plugins.magicworlds.spawn.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.plugins.magicworlds.MagicWorldsController;
import com.elmakers.mine.bukkit.plugins.magicworlds.spawn.SpawnRule;

public class CastRule extends SpawnRule {
	protected List<CastSpell>		spells;
    private class CastSpell
    {
    	protected final String			name;
        protected final String[] 		parameters;
        
        public CastSpell(String name, String[] parameters) {
        	this.name = name;
        	this.parameters = parameters;
        }
    }
    
    @Override
    public boolean load(String key, ConfigurationSection parameters, MagicWorldsController controller)
    {
    	if (!super.load(key, parameters, controller)) return false;
		Collection<String> spells = parameters.getStringList("spells");
		if (spells == null || spells.size() == 0) return false;
		this.spells = new ArrayList<CastSpell>();
		for (String spellName : spells) {
			String[] spellParameters = new String[0];
			if (spellName.contains(" ")) {
				String[] pieces = spellName.split(" ");
				spellName = pieces[0];
				spellParameters = new String[pieces.length - 1];
				for (int i = 1; i < pieces.length; i++) {
					spellParameters[i - 1] = pieces[i];
				}
			}
			
			this.spells.add(new CastSpell(spellName, spellParameters));
			
			controller.getLogger().info(" Casting: " + spellName + " on " + targetEntityType.name() + " at y > " + minY  
					+ " with a " + (percentChance * 100) + "% chance");
		}
		
		return true;
    }
    
    @Override
    public LivingEntity onProcess(Plugin plugin, LivingEntity entity) {
    	if (controller == null) {
    		return null;
    	}
    	
    	String[] standardParameters = {
        	"pworld", entity.getLocation().getWorld().getName(), 
    		"px", Integer.toString(entity.getLocation().getBlockX()), 
    		"py", Integer.toString(entity.getLocation().getBlockY()), 
    		"pz", Integer.toString(entity.getLocation().getBlockZ()), 
    		"target", "self"
    	};
    	
    	for (CastSpell spell : spells) {
	    	String[] fullParameters = new String[spell.parameters.length + standardParameters.length];
	    	for (int index = 0; index < standardParameters.length; index++) {
	    		fullParameters[index] = standardParameters[index];
			
	    	}
	    	for (int index = 0; index < spell.parameters.length; index++) {
	    		fullParameters[index  + standardParameters.length] = spell.parameters[index];
	    	}
	    	
	    	MagicAPI magic = controller.getMagic();
	    	if (magic != null) {
	    		magic.cast(spell.name, fullParameters);    	
	        	controller.getLogger().info(" Spawn rule casting: " + spell.name + " at " + entity.getLocation().toVector());
	    	}
    	}
    	
    	// This will always end rule processing, so make sure to put them last.
    	return entity;
    }
}