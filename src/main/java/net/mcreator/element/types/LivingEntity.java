/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
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

package net.mcreator.element.types;

import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.blockly.java.BlocklyToJava;
import net.mcreator.element.BaseType;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.ModElementType;
import net.mcreator.element.parts.BiomeEntry;
import net.mcreator.element.parts.MItemBlock;
import net.mcreator.element.parts.Sound;
import net.mcreator.element.parts.TabEntry;
import net.mcreator.element.parts.procedure.LogicProcedure;
import net.mcreator.element.parts.procedure.Procedure;
import net.mcreator.element.types.interfaces.ICommonType;
import net.mcreator.element.types.interfaces.IEntityWithModel;
import net.mcreator.element.types.interfaces.IMCItemProvider;
import net.mcreator.element.types.interfaces.ITabContainedElement;
import net.mcreator.generator.blockly.BlocklyBlockCodeGenerator;
import net.mcreator.generator.blockly.ProceduralBlockCodeGenerator;
import net.mcreator.generator.template.IAdditionalTemplateDataProvider;
import net.mcreator.minecraft.MCItem;
import net.mcreator.minecraft.MinecraftImageGenerator;
import net.mcreator.ui.blockly.BlocklyEditorType;
import net.mcreator.ui.modgui.LivingEntityGUI;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.workspace.resources.Model;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

@SuppressWarnings("unused") public class LivingEntity extends GeneratableElement
		implements IEntityWithModel, ITabContainedElement, ICommonType, IMCItemProvider {

	public String mobName;
	public String mobLabel;

	public String mobModelName;
	public String mobModelTexture;
	public String mobModelGlowTexture;
	public Procedure transparentModelCondition;
	public Procedure isShakingCondition;
	public LogicProcedure solidBoundingBox;

	public double modelWidth, modelHeight, modelShadowSize;
	public double mountedYOffset;

	public boolean hasSpawnEgg;
	public Color spawnEggBaseColor;
	public Color spawnEggDotColor;
	public TabEntry creativeTab;

	public boolean isBoss;
	public String bossBarColor;
	public String bossBarType;

	public MItemBlock equipmentMainHand;
	public MItemBlock equipmentOffHand;
	public MItemBlock equipmentHelmet;
	public MItemBlock equipmentBody;
	public MItemBlock equipmentLeggings;
	public MItemBlock equipmentBoots;

	public String mobBehaviourType;
	public String mobCreatureType;
	public int attackStrength;
	public double attackKnockback;
	public double knockbackResistance;
	public double movementSpeed;
	public double armorBaseValue;
	public int trackingRange;
	public int followRange;
	public int health;
	public int xpAmount;
	public boolean waterMob;
	public boolean flyingMob;

	public String guiBoundTo;
	public int inventorySize;
	public int inventoryStackSize;

	public boolean disableCollisions;

	public boolean ridable;
	public boolean canControlForward;
	public boolean canControlStrafe;

	public boolean immuneToFire;
	public boolean immuneToArrows;
	public boolean immuneToFallDamage;
	public boolean immuneToCactus;
	public boolean immuneToDrowning;
	public boolean immuneToLightning;
	public boolean immuneToPotions;
	public boolean immuneToPlayer;
	public boolean immuneToExplosion;
	public boolean immuneToTrident;
	public boolean immuneToAnvil;
	public boolean immuneToWither;
	public boolean immuneToDragonBreath;

	public MItemBlock mobDrop;

	public Sound livingSound;
	public Sound hurtSound;
	public Sound deathSound;
	public Sound stepSound;

	public Procedure onStruckByLightning;
	public Procedure whenMobFalls;
	public Procedure whenMobDies;
	public Procedure whenMobIsHurt;
	public Procedure onRightClickedOn;
	public Procedure whenThisMobKillsAnother;
	public Procedure onMobTickUpdate;
	public Procedure onPlayerCollidesWith;
	public Procedure onInitialSpawn;

	public boolean hasAI;
	public String aiBase;
	public String aixml;

	public boolean breedable;
	public boolean tameable;
	public List<MItemBlock> breedTriggerItems;

	public boolean ranged;
	public MItemBlock rangedAttackItem;
	public String rangedItemType;
	public int rangedAttackInterval;
	public double rangedAttackRadius;

	public boolean spawnThisMob;
	public boolean doesDespawnWhenIdle;
	public Procedure spawningCondition;
	public int spawningProbability;
	public String mobSpawningType;
	public int minNumberOfMobsPerGroup;
	public int maxNumberOfMobsPerGroup;
	public List<BiomeEntry> restrictionBiomes;
	public boolean spawnInDungeons;

	private LivingEntity() {
		this(null);
	}

	public LivingEntity(ModElement element) {
		super(element);

		this.modelShadowSize = 0.5;
		this.mobCreatureType = "UNDEFINED";
		this.trackingRange = 64;
		this.rangedItemType = "Default item";
		this.rangedAttackInterval = 20;
		this.rangedAttackRadius = 10;

		this.followRange = 16;

		this.inventorySize = 9;
		this.inventoryStackSize = 64;
	}

	@Override public Model getEntityModel() {
		Model.Type modelType = Model.Type.BUILTIN;
		if (Arrays.stream(LivingEntityGUI.builtinmobmodels).map(Model::getReadableName).noneMatch(mobModelName::equals)
				&& !mobModelName.equals("Zombie")) // legacy check as zombie was supported in the past
			modelType = Model.Type.JAVA;
		return Model.getModelByParams(getModElement().getWorkspace(), mobModelName, modelType);
	}

	@Override public Collection<BaseType> getBaseTypesProvided() {
		if (hasSpawnEgg)
			return List.of(BaseType.ITEM, BaseType.ENTITY);
		else
			return List.of(BaseType.ENTITY);
	}

	@Override public TabEntry getCreativeTab() {
		return creativeTab;
	}

	@Override public BufferedImage generateModElementPicture() {
		return MinecraftImageGenerator.Preview.generateMobPreviewPicture(getModElement().getWorkspace(),
				mobModelTexture, spawnEggBaseColor, spawnEggDotColor, hasSpawnEgg);
	}

	public boolean hasDrop() {
		return !mobDrop.isEmpty();
	}

	public boolean hasCustomProjectile() {
		return ranged && "Default item".equals(rangedItemType) && !rangedAttackItem.isEmpty();
	}

	@Override public @Nullable IAdditionalTemplateDataProvider getAdditionalTemplateData() {
		return additionalData -> {
			BlocklyBlockCodeGenerator blocklyBlockCodeGenerator = new BlocklyBlockCodeGenerator(
					BlocklyLoader.INSTANCE.getBlockLoader(BlocklyEditorType.AI_TASK).getDefinedBlocks(),
					this.getModElement().getGenerator()
							.getTemplateGeneratorFromName(BlocklyEditorType.AI_TASK.registryName()),
					additionalData).setTemplateExtension(
					this.getModElement().getGeneratorConfiguration().getGeneratorFlavor().getBaseLanguage().name()
							.toLowerCase(Locale.ENGLISH));
			BlocklyToJava blocklyToJava = new BlocklyToJava(this.getModElement().getWorkspace(), this.getModElement(),
					BlocklyEditorType.AI_TASK, this.aixml, this.getModElement().getGenerator()
					.getTemplateGeneratorFromName(BlocklyEditorType.AI_TASK.registryName()),
					new ProceduralBlockCodeGenerator(blocklyBlockCodeGenerator));

			List<?> unmodifiableAIBases = (List<?>) getModElement().getWorkspace().getGenerator()
					.getGeneratorConfiguration().getDefinitionsProvider()
					.getModElementDefinition(ModElementType.LIVINGENTITY).get("unmodifiable_ai_bases");
			additionalData.put("aicode", unmodifiableAIBases != null && !unmodifiableAIBases.contains(aiBase) ?
					blocklyToJava.getGeneratedCode() :
					"");
		};
	}

	@Override public List<MCItem> providedMCItems() {
		if (hasSpawnEgg)
			return List.of(new MCItem.Custom(this.getModElement(), "spawn_egg", "item", "Spawn egg"));
		return Collections.emptyList();
	}

	@Override public List<MCItem> getCreativeTabItems() {
		return providedMCItems();
	}

	@Override public ImageIcon getIconForMCItem(Workspace workspace, String suffix) {
		if ("spawn_egg".equals(suffix)) {
			return MinecraftImageGenerator.generateSpawnEggIcon(spawnEggBaseColor, spawnEggDotColor);
		}

		return null;
	}
}
