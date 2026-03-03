package com.kjw.fridgerecipe.presentation.mapper

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType

val CookingToolType.labelResId: Int
    get() =
        when (this) {
            CookingToolType.AIR_FRYER -> R.string.cooking_tool_airfryer
            CookingToolType.MICROWAVE -> R.string.cooking_tool_microwave
            CookingToolType.POT -> R.string.cooking_tool_pot
            CookingToolType.PAN -> R.string.cooking_tool_pan
        }

val IngredientCategoryType.labelResId: Int
    get() =
        when (this) {
            IngredientCategoryType.ETC -> R.string.ingredient_category_etc
            IngredientCategoryType.VEGETABLE -> R.string.ingredient_category_vegetable
            IngredientCategoryType.FRUIT -> R.string.ingredient_category_fruit
            IngredientCategoryType.MEAT -> R.string.ingredient_category_meat
            IngredientCategoryType.SEAFOOD -> R.string.ingredient_category_seafood
            IngredientCategoryType.DAIRY -> R.string.ingredient_category_dairy
            IngredientCategoryType.GRAIN -> R.string.ingredient_category_grain
            IngredientCategoryType.BEVERAGE -> R.string.ingredient_category_beverage
            IngredientCategoryType.SEASONING -> R.string.ingredient_category_seasoning
        }

val IngredientIcon.labelResId: Int
    get() =
        when (this) {
            IngredientIcon.DEFAULT -> R.string.icon_label_default
            IngredientIcon.KIMCHI -> R.string.icon_label_kimchi
            IngredientIcon.TOFU -> R.string.icon_label_tofu
            IngredientIcon.VEGETABLE -> R.string.ingredient_category_vegetable
            IngredientIcon.CARROT -> R.string.icon_label_carrot
            IngredientIcon.ONION -> R.string.icon_label_onion
            IngredientIcon.POTATO -> R.string.icon_label_potato
            IngredientIcon.SWEET_POTATO -> R.string.icon_label_sweet_potato
            IngredientIcon.TOMATO -> R.string.icon_label_tomato
            IngredientIcon.MUSHROOM -> R.string.icon_label_mushroom
            IngredientIcon.GARLIC -> R.string.icon_label_garlic
            IngredientIcon.GREEN_ONION -> R.string.icon_label_green_onion
            IngredientIcon.RADISH -> R.string.icon_label_radish
            IngredientIcon.CABBAGE -> R.string.icon_label_cabbage
            IngredientIcon.BEAN_SPROUTS -> R.string.icon_label_bean_spr
            IngredientIcon.CHILI -> R.string.icon_label_chili
            IngredientIcon.FRUIT -> R.string.ingredient_category_fruit
            IngredientIcon.APPLE -> R.string.icon_label_apple
            IngredientIcon.BANANA -> R.string.icon_label_banana
            IngredientIcon.LEMON -> R.string.icon_label_lemon
            IngredientIcon.TANGERINE -> R.string.icon_label_tangerine
            IngredientIcon.GRAPE -> R.string.icon_label_grape
            IngredientIcon.STRAWBERRY -> R.string.icon_label_strawberry
            IngredientIcon.MEAT -> R.string.ingredient_category_meat
            IngredientIcon.CHICKEN -> R.string.icon_label_chicken
            IngredientIcon.PORK -> R.string.icon_label_pork
            IngredientIcon.BEEF -> R.string.icon_label_beef
            IngredientIcon.DUCK -> R.string.icon_label_duck
            IngredientIcon.LAMB -> R.string.icon_label_lamb
            IngredientIcon.SEAFOOD -> R.string.ingredient_category_seafood
            IngredientIcon.FISH -> R.string.icon_label_fish
            IngredientIcon.SHRIMP -> R.string.icon_label_shrimp
            IngredientIcon.SQUID -> R.string.icon_label_squid
            IngredientIcon.CLAM -> R.string.icon_label_clam
            IngredientIcon.CRAB -> R.string.icon_label_crab
            IngredientIcon.DAIRY -> R.string.ingredient_category_dairy
            IngredientIcon.EGG -> R.string.icon_label_egg
            IngredientIcon.MILK -> R.string.icon_label_milk
            IngredientIcon.CHEESE -> R.string.icon_label_cheese
            IngredientIcon.YOGURT -> R.string.icon_label_yogurt
            IngredientIcon.GRAIN -> R.string.ingredient_category_grain
            IngredientIcon.RICE -> R.string.icon_label_rice
            IngredientIcon.BREAD -> R.string.icon_label_bread
            IngredientIcon.NOODLE -> R.string.icon_label_noodle
            IngredientIcon.SEASONING -> R.string.ingredient_category_seasoning
            IngredientIcon.SALT -> R.string.icon_label_salt
            IngredientIcon.SUGAR -> R.string.icon_label_sugar
            IngredientIcon.PEPPER -> R.string.icon_label_pepper
            IngredientIcon.SOY_SAUCE -> R.string.icon_label_soy_sauce
            IngredientIcon.DOENJANG -> R.string.icon_label_doenjang
            IngredientIcon.GOCHUJANG -> R.string.icon_label_gochujang
            IngredientIcon.COOKING_OIL -> R.string.icon_label_cooking_oil
            IngredientIcon.SESAME_OIL -> R.string.icon_label_sesame_oil
            IngredientIcon.VINEGAR -> R.string.icon_label_vinegar
            IngredientIcon.KETCHUP -> R.string.icon_label_ketchup
            IngredientIcon.MAYONNAISE -> R.string.icon_label_mayonnaise
            IngredientIcon.BEVERAGE -> R.string.ingredient_category_beverage
            IngredientIcon.WATER -> R.string.icon_label_water
            IngredientIcon.SODA -> R.string.icon_label_soda
            IngredientIcon.BEER -> R.string.icon_label_beer
            IngredientIcon.SOJU -> R.string.icon_label_soju
        }

val LevelType.labelResId: Int
    get() =
        when (this) {
            LevelType.BEGINNER -> R.string.level_beginner
            LevelType.INTERMEDIATE -> R.string.level_intermediate
            LevelType.ADVANCED -> R.string.level_advanced
            LevelType.ETC -> R.string.level_etc
        }

val RecipeCategoryType.labelResId: Int
    get() =
        when (this) {
            RecipeCategoryType.KOREAN -> R.string.category_korean
            RecipeCategoryType.JAPANESE -> R.string.category_japanese
            RecipeCategoryType.CHINESE -> R.string.category_chinese
            RecipeCategoryType.WESTERN -> R.string.category_western
        }

val StorageType.labelResId: Int
    get() =
        when (this) {
            StorageType.REFRIGERATED -> R.string.storage_refrigerated
            StorageType.FROZEN -> R.string.storage_frozen
            StorageType.ROOM_TEMPERATURE -> R.string.storage_room_temperature
        }

val UnitType.labelResId: Int
    get() =
        when (this) {
            UnitType.COUNT -> R.string.unit_count
            UnitType.GRAM -> R.string.unit_gram
            UnitType.MILLILITER -> R.string.unit_milliliter
            UnitType.ETC -> R.string.unit_etc
        }
