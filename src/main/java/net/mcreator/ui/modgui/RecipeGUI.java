/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2023, Pylo, opensource contributors
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

package net.mcreator.ui.modgui;

import net.mcreator.element.parts.MItemBlock;
import net.mcreator.element.types.Recipe;
import net.mcreator.minecraft.ElementUtil;
import net.mcreator.minecraft.RegistryNameFixer;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.MCreatorApplication;
import net.mcreator.ui.component.JEmptyBox;
import net.mcreator.ui.component.util.AdaptiveGridLayout;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.minecraft.recipemakers.*;
import net.mcreator.ui.validation.AggregatedValidationResult;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.validation.validators.RegistryNameValidator;
import net.mcreator.workspace.elements.ModElement;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecipeGUI extends ModElementGUI<Recipe> {

	private CraftingRecipeMaker rm;
	private SmeltingRecipeMaker fm;
	private BlastFurnaceRecipeMaker bm;
	private SmokerRecipeMaker sm;
	private StoneCutterRecipeMaker scm;
	private CampfireCookingRecipeMaker ccm;
	private SmithingRecipeMaker smcm;
	private BrewingRecipeMaker brm;

	private final JCheckBox recipeShapeless = L10N.checkbox("elementgui.common.enable");

	private final JSpinner xpReward = new JSpinner(new SpinnerNumberModel(1.0, 0, 256, 1));
	private final JSpinner cookingTime = new JSpinner(new SpinnerNumberModel(200, 0, 1000000, 1));

	private final JComboBox<String> namespace = new JComboBox<>(new String[] { "mod", "minecraft" });

	private final VComboBox<String> name = new VComboBox<>();

	private final VTextField group = new VTextField();

	private final JComboBox<String> recipeType = new JComboBox<>(
			new String[] { "Crafting", "Smelting", "Brewing", "Blasting", "Smoking", "Stone cutting",
					"Campfire cooking", "Smithing" });

	private final JComboBox<String> cookingBookCategory = new JComboBox<>(
			new String[] { "MISC", "FOOD", "BLOCKS" });

	private final JComboBox<String> craftingBookCategory = new JComboBox<>(
			new String[] { "MISC", "BUILDING", "REDSTONE", "EQUIPMENT" });

	private final CardLayout recipesPanelLayout = new CardLayout();
	private final JPanel recipesPanel = new JPanel(recipesPanelLayout);

	private JComponent namePanel;
	private JComponent namespacePanel;
	private JComponent xpRewardPanel;
	private JComponent cookingTimePanel;
	private JComponent groupPanel;
	private JComponent shapelessPanel;
	private JComponent cookingBookCategoryPanel;
	private JComponent craftingBookCategoryPanel;

	public RecipeGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);
		this.initGUI();
		super.finalizeGUI();
	}

	@Override protected void initGUI() {
		rm = new CraftingRecipeMaker(mcreator, ElementUtil::loadBlocksAndItemsAndTags, ElementUtil::loadBlocksAndItems);
		fm = new SmeltingRecipeMaker(mcreator, ElementUtil::loadBlocksAndItemsAndTags, ElementUtil::loadBlocksAndItems);
		bm = new BlastFurnaceRecipeMaker(mcreator, ElementUtil::loadBlocksAndItemsAndTags,
				ElementUtil::loadBlocksAndItems);
		sm = new SmokerRecipeMaker(mcreator, ElementUtil::loadBlocksAndItemsAndTags, ElementUtil::loadBlocksAndItems);
		scm = new StoneCutterRecipeMaker(mcreator, ElementUtil::loadBlocksAndItemsAndTags,
				ElementUtil::loadBlocksAndItems);
		ccm = new CampfireCookingRecipeMaker(mcreator, ElementUtil::loadBlocksAndItemsAndTags,
				ElementUtil::loadBlocksAndItems);
		smcm = new SmithingRecipeMaker(mcreator, ElementUtil::loadBlocksAndItemsAndTags,
				ElementUtil::loadBlocksAndItems);
		brm = new BrewingRecipeMaker(mcreator, ElementUtil::loadBlocksAndItemsAndTagsAndPotions,
				ElementUtil::loadBlocksAndItemsAndTags, ElementUtil::loadBlocksAndItemsAndPotions);

		rm.setOpaque(false);
		fm.setOpaque(false);
		bm.setOpaque(false);
		sm.setOpaque(false);
		scm.setOpaque(false);
		ccm.setOpaque(false);
		smcm.setOpaque(false);
		brm.setOpaque(false);

		name.setValidator(new RegistryNameValidator(name, "Loot table").setValidChars(Arrays.asList('_', '/')));
		name.enableRealtimeValidation();

		name.addItem("crafting_table");
		name.addItem("diamond_block");

		name.setEditable(true);
		name.setOpaque(false);

		ComponentUtils.deriveFont(group, 16);

		if (isEditingMode()) {
			name.setEnabled(false);
			namespace.setEnabled(false);
		} else {
			name.getEditor().setItem(RegistryNameFixer.fromCamelCase(modElement.getName()));
		}

		JPanel pane5 = new JPanel(new BorderLayout(10, 10));


		recipeShapeless.setOpaque(false);
		recipeShapeless.addActionListener(event -> rm.setShapeless(recipeShapeless.isSelected()));

		recipesPanel.setOpaque(false);

		recipesPanel.add(PanelUtils.totalCenterInPanel(rm), "crafting");
		recipesPanel.add(PanelUtils.totalCenterInPanel(fm), "smelting");
		recipesPanel.add(PanelUtils.totalCenterInPanel(bm), "blasting");
		recipesPanel.add(PanelUtils.totalCenterInPanel(sm), "smoking");
		recipesPanel.add(PanelUtils.totalCenterInPanel(scm), "stone cutting");
		recipesPanel.add(PanelUtils.totalCenterInPanel(ccm), "campfire cooking");
		recipesPanel.add(PanelUtils.totalCenterInPanel(smcm), "smithing");
		recipesPanel.add(PanelUtils.totalCenterInPanel(brm), "brewing");

		JComponent recwrap = PanelUtils.maxMargin(recipesPanel, 10, true, true, true, true);
		recwrap.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder((Color) UIManager.get("MCreatorLAF.BRIGHT_COLOR"), 1),
				L10N.t("elementgui.recipe.definition"), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, getFont(), Color.white));

		JPanel northPanel = new JPanel(new AdaptiveGridLayout(-1, 1, 10, 2));
		northPanel.setOpaque(false);

		northPanel.add(PanelUtils.gridElements(1, 2,
				HelpUtils.wrapWithHelpButton(this.withEntry("recipe/type"), L10N.label("elementgui.recipe.type")),
				recipeType));

		northPanel.add(namePanel = PanelUtils.gridElements(1, 2,
				HelpUtils.wrapWithHelpButton(this.withEntry("recipe/registry_name"),
						L10N.label("elementgui.recipe.registry_name")), name));

		northPanel.add(namespacePanel = PanelUtils.gridElements(1, 2, HelpUtils.wrapWithHelpButton(this.withEntry("recipe/namespace"),
				L10N.label("elementgui.recipe.name_space")), namespace));

		northPanel.add(groupPanel = PanelUtils.gridElements(1, 2, HelpUtils.wrapWithHelpButton(this.withEntry("recipe/group_name"),
				L10N.label("elementgui.recipe.group")), group));

		northPanel.add(craftingBookCategoryPanel = PanelUtils.gridElements(1, 2, HelpUtils.wrapWithHelpButton(this.withEntry("recipe/crafting_book_category"),
				L10N.label("elementgui.recipe.crafting_book_category")), craftingBookCategory));

		northPanel.add(cookingBookCategoryPanel = PanelUtils.gridElements(1, 2, HelpUtils.wrapWithHelpButton(this.withEntry("recipe/cooking_book_category"),
				L10N.label("elementgui.recipe.cooking_book_category")), cookingBookCategory));

		northPanel.add(shapelessPanel = PanelUtils.gridElements(1, 2, HelpUtils.wrapWithHelpButton(this.withEntry("recipe/shapeless"),
				L10N.label("elementgui.recipe.is_shapeless")), recipeShapeless));

		northPanel.add(xpRewardPanel = PanelUtils.gridElements(1, 2, HelpUtils.wrapWithHelpButton(this.withEntry("recipe/xp_reward"),
				L10N.label("elementgui.recipe.xp_reward")), xpReward));

		northPanel.add(cookingTimePanel = PanelUtils.gridElements(1, 2, HelpUtils.wrapWithHelpButton(this.withEntry("recipe/cooking_time"),
				L10N.label("elementgui.recipe.cooking_time")), cookingTime));

		pane5.setOpaque(false);
		pane5.add(PanelUtils.totalCenterInPanel(PanelUtils.westAndEastElement(PanelUtils.pullElementUp(northPanel), PanelUtils.pullElementUp(recwrap), 15, 15)));

		recipeType.addActionListener(e -> updateUIFields());

		group.enableRealtimeValidation();
		group.setValidator(new RegistryNameValidator(group, "Recipe group").setAllowEmpty(true).setMaxLength(128));

		updateUIFields();

		addPage(pane5);
	}

	private void updateUIFields() {
		String recipeTypeValue = (String) recipeType.getSelectedItem();
		if (recipeTypeValue != null) {
			boolean isCookingRecipe = List.of("Smelting", "Blasting", "Smoking", "Campfire cooking").contains(recipeTypeValue);
			xpRewardPanel.setVisible(isCookingRecipe);
			cookingTimePanel.setVisible(isCookingRecipe);
			cookingBookCategoryPanel.setVisible(isCookingRecipe);

			boolean isRecipeJSON = List.of("Crafting", "Smelting", "Blasting", "Smoking", "Stone cutting", "Campfire cooking", "Smithing").contains(recipeTypeValue);
			groupPanel.setVisible(isRecipeJSON);
			namespacePanel.setVisible(isRecipeJSON);
			namePanel.setVisible(isRecipeJSON);

			boolean isRecipeCrafting = recipeTypeValue.equals("Crafting");
			shapelessPanel.setVisible(isRecipeCrafting);
			craftingBookCategoryPanel.setVisible(isRecipeCrafting);

			if (!isEditingMode() && isCookingRecipe) {
				if (recipeTypeValue.equals("Smelting")) {
					cookingTime.setValue(200);
				} else if (recipeTypeValue.equals("Campfire cooking")) {
					cookingTime.setValue(600);
				} else {
					cookingTime.setValue(100);
				}
			}

			recipesPanelLayout.show(recipesPanel, recipeTypeValue.toLowerCase(Locale.ENGLISH));
		}
	}

	@Override protected AggregatedValidationResult validatePage(int page) {
		if ("Crafting".equals(recipeType.getSelectedItem())) {
			if (!rm.cb10.containsItem()) {
				return new AggregatedValidationResult.FAIL(L10N.t("elementgui.recipe.error_crafting_no_result"));
			} else if (!(rm.cb1.containsItem() || rm.cb2.containsItem() || rm.cb3.containsItem()
					|| rm.cb4.containsItem() || rm.cb5.containsItem() || rm.cb6.containsItem() || rm.cb7.containsItem()
					|| rm.cb8.containsItem() || rm.cb9.containsItem())) {
				return new AggregatedValidationResult.FAIL(L10N.t("elementgui.recipe.error_crafting_no_ingredient"));
			}
		} else if ("Smelting".equals(recipeType.getSelectedItem())) {
			if (!fm.cb1.containsItem() || !fm.cb2.containsItem()) {
				return new AggregatedValidationResult.FAIL(
						L10N.t("elementgui.recipe.error_smelting_no_ingredient_and_result"));
			}
		} else if ("Blasting".equals(recipeType.getSelectedItem())) {
			if (!bm.cb1.containsItem() || !bm.cb2.containsItem()) {
				return new AggregatedValidationResult.FAIL(
						L10N.t("elementgui.recipe.error_blasting_no_ingredient_and_result"));
			}
		} else if ("Smoking".equals(recipeType.getSelectedItem())) {
			if (!sm.cb1.containsItem() || !sm.cb2.containsItem()) {
				return new AggregatedValidationResult.FAIL(
						L10N.t("elementgui.recipe.error_smoking_no_ingredient_and_result"));
			}
		} else if ("Stone cutting".equals(recipeType.getSelectedItem())) {
			if (!scm.cb1.containsItem() || !scm.cb2.containsItem()) {
				return new AggregatedValidationResult.FAIL(
						L10N.t("elementgui.recipe.error_stone_cutting_no_ingredient_and_result"));
			}
		} else if ("Campfire cooking".equals(recipeType.getSelectedItem())) {
			if (!ccm.cb1.containsItem() || !ccm.cb2.containsItem()) {
				return new AggregatedValidationResult.FAIL(
						L10N.t("elementgui.recipe.error_campfire_no_ingredient_and_result"));
			}
		} else if ("Smithing".equals(recipeType.getSelectedItem())) {
			if (!smcm.cb1.containsItem() || !smcm.cb2.containsItem() || !smcm.cb3.containsItem()) {
				return new AggregatedValidationResult.FAIL(
						L10N.t("elementgui.recipe.error_smithing_no_ingredient_addition_and_result"));
			}
		} else if ("Brewing".equals(recipeType.getSelectedItem())) {
			if (!brm.cb1.containsItem() || !brm.cb2.containsItem() || !brm.cb3.containsItem()) {
				return new AggregatedValidationResult.FAIL(
						L10N.t("elementgui.recipe.error_brewing_no_input_ingredient_and_result"));
			}
		}

		return new AggregatedValidationResult(name, group);
	}

	@Override public void openInEditingMode(Recipe recipe) {
		recipeType.setSelectedItem(recipe.recipeType);

		namespace.setSelectedItem(recipe.namespace);
		name.getEditor().setItem(recipe.name);

		group.setText(recipe.group);

		cookingBookCategory.setSelectedItem(recipe.cookingBookCategory);
		craftingBookCategory.setSelectedItem(recipe.craftingBookCategory);

		if ("Crafting".equals(recipe.recipeType)) {
			recipeShapeless.setSelected(recipe.recipeShapeless);
			rm.cb1.setBlock(recipe.recipeSlots[0]);
			rm.cb2.setBlock(recipe.recipeSlots[3]);
			rm.cb3.setBlock(recipe.recipeSlots[6]);
			rm.cb4.setBlock(recipe.recipeSlots[1]);
			rm.cb5.setBlock(recipe.recipeSlots[4]);
			rm.cb6.setBlock(recipe.recipeSlots[7]);
			rm.cb7.setBlock(recipe.recipeSlots[2]);
			rm.cb8.setBlock(recipe.recipeSlots[5]);
			rm.cb9.setBlock(recipe.recipeSlots[8]);
			rm.cb10.setBlock(recipe.recipeReturnStack);
			rm.sp.setValue(recipe.recipeRetstackSize);
			rm.setShapeless(recipeShapeless.isSelected());
		} else if ("Smelting".equals(recipe.recipeType)) {
			fm.cb1.setBlock(recipe.smeltingInputStack);
			fm.cb2.setBlock(recipe.smeltingReturnStack);
			xpReward.setValue(recipe.xpReward);
			cookingTime.setValue(recipe.cookingTime);
		} else if ("Blasting".equals(recipe.recipeType)) {
			bm.cb1.setBlock(recipe.blastingInputStack);
			bm.cb2.setBlock(recipe.blastingReturnStack);
			xpReward.setValue(recipe.xpReward);
			cookingTime.setValue(recipe.cookingTime);
		} else if ("Smoking".equals(recipe.recipeType)) {
			sm.cb1.setBlock(recipe.smokingInputStack);
			sm.cb2.setBlock(recipe.smokingReturnStack);
			xpReward.setValue(recipe.xpReward);
			cookingTime.setValue(recipe.cookingTime);
		} else if ("Stone cutting".equals(recipe.recipeType)) {
			scm.cb1.setBlock(recipe.stoneCuttingInputStack);
			scm.cb2.setBlock(recipe.stoneCuttingReturnStack);
			scm.sp.setValue(recipe.recipeRetstackSize);
		} else if ("Campfire cooking".equals(recipe.recipeType)) {
			ccm.cb1.setBlock(recipe.campfireCookingInputStack);
			ccm.cb2.setBlock(recipe.campfireCookingReturnStack);
			xpReward.setValue(recipe.xpReward);
			cookingTime.setValue(recipe.cookingTime);
		} else if ("Smithing".equals(recipe.recipeType)) {
			smcm.cb1.setBlock(recipe.smithingInputStack);
			smcm.cb2.setBlock(recipe.smithingInputAdditionStack);
			smcm.cb3.setBlock(recipe.smithingReturnStack);
		} else if ("Brewing".equals(recipe.recipeType)) {
			brm.cb1.setBlock(recipe.brewingInputStack);
			brm.cb2.setBlock(recipe.brewingIngredientStack);
			brm.cb3.setBlock(recipe.brewingReturnStack);
		}
	}

	@Override public Recipe getElementFromGUI() {
		Recipe recipe = new Recipe(modElement);
		recipe.recipeType = (String) recipeType.getSelectedItem();

		if ("Crafting".equals(recipe.recipeType)) {
			MItemBlock[] recipeSlots = new MItemBlock[9];
			recipeSlots[0] = rm.cb1.getBlock();
			recipeSlots[3] = rm.cb2.getBlock();
			recipeSlots[6] = rm.cb3.getBlock();
			recipeSlots[1] = rm.cb4.getBlock();
			recipeSlots[4] = rm.cb5.getBlock();
			recipeSlots[7] = rm.cb6.getBlock();
			recipeSlots[2] = rm.cb7.getBlock();
			recipeSlots[5] = rm.cb8.getBlock();
			recipeSlots[8] = rm.cb9.getBlock();
			recipe.recipeRetstackSize = (int) rm.sp.getValue();
			recipe.recipeShapeless = recipeShapeless.isSelected();
			recipe.recipeReturnStack = rm.cb10.getBlock();
			recipe.recipeSlots = recipeSlots;
		} else if ("Smelting".equals(recipe.recipeType)) {
			recipe.smeltingInputStack = fm.getBlock();
			recipe.smeltingReturnStack = fm.getBlock2();
			recipe.xpReward = (double) xpReward.getValue();
			recipe.cookingTime = (int) cookingTime.getValue();
		} else if ("Blasting".equals(recipe.recipeType)) {
			recipe.blastingInputStack = bm.getBlock();
			recipe.blastingReturnStack = bm.getBlock2();
			recipe.xpReward = (double) xpReward.getValue();
			recipe.cookingTime = (int) cookingTime.getValue();
		} else if ("Smoking".equals(recipe.recipeType)) {
			recipe.smokingInputStack = sm.getBlock();
			recipe.smokingReturnStack = sm.getBlock2();
			recipe.xpReward = (double) xpReward.getValue();
			recipe.cookingTime = (int) cookingTime.getValue();
		} else if ("Stone cutting".equals(recipe.recipeType)) {
			recipe.recipeRetstackSize = (int) scm.sp.getValue();
			recipe.stoneCuttingInputStack = scm.getBlock();
			recipe.stoneCuttingReturnStack = scm.getBlock2();
		} else if ("Campfire cooking".equals(recipe.recipeType)) {
			recipe.campfireCookingInputStack = ccm.getBlock();
			recipe.campfireCookingReturnStack = ccm.getBlock2();
			recipe.xpReward = (double) xpReward.getValue();
			recipe.cookingTime = (int) cookingTime.getValue();
		} else if ("Smithing".equals(recipe.recipeType)) {
			recipe.smithingInputStack = smcm.cb1.getBlock();
			recipe.smithingInputAdditionStack = smcm.cb2.getBlock();
			recipe.smithingReturnStack = smcm.cb3.getBlock();
		} else if ("Brewing".equals(recipe.recipeType)) {
			recipe.brewingInputStack = brm.cb1.getBlock();
			recipe.brewingIngredientStack = brm.cb2.getBlock();
			recipe.brewingReturnStack = brm.cb3.getBlock();
		}

		recipe.namespace = (String) namespace.getSelectedItem();
		recipe.name = name.getEditor().getItem().toString();

		recipe.group = group.getText();

		recipe.cookingBookCategory = (String) cookingBookCategory.getSelectedItem();
		recipe.craftingBookCategory = (String) craftingBookCategory.getSelectedItem();

		return recipe;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return new URI(MCreatorApplication.SERVER_DOMAIN + "/wiki/how-make-recipe");
	}

}
