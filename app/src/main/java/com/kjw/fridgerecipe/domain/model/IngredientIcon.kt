package com.kjw.fridgerecipe.domain.model

import com.kjw.fridgerecipe.R

enum class IngredientIcon(val labelResId: Int, val id: String, val category: IngredientCategoryType) {

    // 기타
    DEFAULT(R.string.icon_label_default, "default", IngredientCategoryType.ETC),
    KIMCHI(R.string.icon_label_kimchi, "kimchi", IngredientCategoryType.ETC),
    TOFU(R.string.icon_label_tofu, "tofu", IngredientCategoryType.ETC),

    // 채소
    VEGETABLE(R.string.ingredient_category_vegetable, "vegetable", IngredientCategoryType.VEGETABLE),
    CARROT(R.string.icon_label_carrot, "carrot", IngredientCategoryType.VEGETABLE),
    ONION(R.string.icon_label_onion, "onion", IngredientCategoryType.VEGETABLE),
    POTATO(R.string.icon_label_potato, "potato", IngredientCategoryType.VEGETABLE),
    SWEET_POTATO(R.string.icon_label_sweet_potato, "sweet_potato", IngredientCategoryType.VEGETABLE),
    TOMATO(R.string.icon_label_tomato, "tomato", IngredientCategoryType.VEGETABLE),
    MUSHROOM(R.string.icon_label_mushroom, "mushroom", IngredientCategoryType.VEGETABLE),
    GARLIC(R.string.icon_label_garlic, "garlic", IngredientCategoryType.VEGETABLE),
    GREEN_ONION(R.string.icon_label_green_onion, "green_onion", IngredientCategoryType.VEGETABLE),
    RADISH(R.string.icon_label_radish, "radish", IngredientCategoryType.VEGETABLE),
    CABBAGE(R.string.icon_label_cabbage, "cabbage", IngredientCategoryType.VEGETABLE),
    BEAN_SPROUTS(R.string.icon_label_bean_spr, "bean_sprouts", IngredientCategoryType.VEGETABLE),
    CHILI(R.string.icon_label_chili, "chili", IngredientCategoryType.VEGETABLE),

    // 과일
    FRUIT(R.string.ingredient_category_fruit, "fruit", IngredientCategoryType.FRUIT),
    APPLE(R.string.icon_label_apple, "apple", IngredientCategoryType.FRUIT),
    BANANA(R.string.icon_label_banana, "banana", IngredientCategoryType.FRUIT),
    LEMON(R.string.icon_label_lemon, "lemon", IngredientCategoryType.FRUIT),
    TANGERINE(R.string.icon_label_tangerine, "tangerine", IngredientCategoryType.FRUIT),
    GRAPE(R.string.icon_label_grape, "grape", IngredientCategoryType.FRUIT),
    STRAWBERRY(R.string.icon_label_strawberry, "strawberry", IngredientCategoryType.FRUIT),

    // 육류
    MEAT(R.string.ingredient_category_meat, "meat", IngredientCategoryType.MEAT),
    CHICKEN(R.string.icon_label_chicken, "chicken", IngredientCategoryType.MEAT),
    PORK(R.string.icon_label_pork, "pork", IngredientCategoryType.MEAT),
    BEEF(R.string.icon_label_beef, "beef", IngredientCategoryType.MEAT),
    DUCK(R.string.icon_label_duck, "duck", IngredientCategoryType.MEAT),
    LAMB(R.string.icon_label_lamb, "lamb", IngredientCategoryType.MEAT),

    // 해산물
    SEAFOOD(R.string.ingredient_category_seafood, "seafood", IngredientCategoryType.SEAFOOD),
    FISH(R.string.icon_label_fish, "fish", IngredientCategoryType.SEAFOOD),
    SHRIMP(R.string.icon_label_shrimp, "shrimp", IngredientCategoryType.SEAFOOD),
    SQUID(R.string.icon_label_squid, "squid", IngredientCategoryType.SEAFOOD),
    CLAM(R.string.icon_label_clam, "clam", IngredientCategoryType.SEAFOOD),
    CRAB(R.string.icon_label_crab, "crab", IngredientCategoryType.SEAFOOD),

    // 유제품/계란
    DAIRY(R.string.ingredient_category_dairy, "dairy", IngredientCategoryType.DAIRY),
    EGG(R.string.icon_label_egg, "egg", IngredientCategoryType.DAIRY),
    MILK(R.string.icon_label_milk, "milk", IngredientCategoryType.DAIRY),
    CHEESE(R.string.icon_label_cheese, "cheese", IngredientCategoryType.DAIRY),
    YOGURT(R.string.icon_label_yogurt, "yogurt", IngredientCategoryType.DAIRY),

    // 곡물
    GRAIN(R.string.ingredient_category_grain, "grain", IngredientCategoryType.GRAIN),
    RICE(R.string.icon_label_rice, "rice", IngredientCategoryType.GRAIN),
    BREAD(R.string.icon_label_bread, "bread", IngredientCategoryType.GRAIN),
    NOODLE(R.string.icon_label_noodle, "noodle", IngredientCategoryType.GRAIN),

    // 소스/양념/조미료
    SEASONING(R.string.ingredient_category_seasoning, "seasoning", IngredientCategoryType.SEASONING),
    SALT(R.string.icon_label_salt, "salt", IngredientCategoryType.SEASONING),
    SUGAR(R.string.icon_label_sugar, "sugar", IngredientCategoryType.SEASONING),
    PEPPER(R.string.icon_label_pepper, "pepper", IngredientCategoryType.SEASONING),
    SOY_SAUCE(R.string.icon_label_soy_sauce, "soy_sauce", IngredientCategoryType.SEASONING),
    DOENJANG(R.string.icon_label_doenjang, "doenjang", IngredientCategoryType.SEASONING),
    GOCHUJANG(R.string.icon_label_gochujang, "gochujang", IngredientCategoryType.SEASONING),
    COOKING_OIL(R.string.icon_label_cooking_oil, "cooking_oil", IngredientCategoryType.SEASONING),
    SESAME_OIL(R.string.icon_label_sesame_oil, "sesame_oil", IngredientCategoryType.SEASONING),
    VINEGAR(R.string.icon_label_vinegar, "vinegar", IngredientCategoryType.SEASONING),
    KETCHUP(R.string.icon_label_ketchup, "ketchup", IngredientCategoryType.SEASONING),
    MAYONNAISE(R.string.icon_label_mayonnaise, "mayonnaise", IngredientCategoryType.SEASONING),

    // 음료
    BEVERAGE(R.string.ingredient_category_beverage, "beverage", IngredientCategoryType.BEVERAGE),
    WATER(R.string.icon_label_water, "water", IngredientCategoryType.BEVERAGE),
    SODA(R.string.icon_label_soda, "soda", IngredientCategoryType.BEVERAGE),
    BEER(R.string.icon_label_beer, "beer", IngredientCategoryType.BEVERAGE),
    SOJU(R.string.icon_label_soju, "soju", IngredientCategoryType.BEVERAGE);

    companion object {
        fun fromId(id: String?): IngredientIcon {
            return entries.find { it.id == id } ?: DEFAULT
        }
    }
}