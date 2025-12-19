package com.kjw.fridgerecipe.presentation.util

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.IngredientIcon

fun getIconResId(icon: IngredientIcon): Int {
    return when (icon) {
        // 기타
        IngredientIcon.DEFAULT -> R.drawable.default_image
        IngredientIcon.KIMCHI -> R.drawable.kimchi
        IngredientIcon.TOFU -> R.drawable.tofu
        // 채소
        IngredientIcon.VEGETABLE -> R.drawable.vegetable
        IngredientIcon.CARROT -> R.drawable.carrot
        IngredientIcon.ONION -> R.drawable.onion
        IngredientIcon.POTATO -> R.drawable.potato
        IngredientIcon.SWEET_POTATO -> R.drawable.sweet_potato
        IngredientIcon.TOMATO -> R.drawable.tomato
        IngredientIcon.MUSHROOM -> R.drawable.mushroom
        IngredientIcon.GARLIC -> R.drawable.garlic
        IngredientIcon.GREEN_ONION -> R.drawable.green_onion
        IngredientIcon.RADISH -> R.drawable.radish
        IngredientIcon.CABBAGE -> R.drawable.cabbage
        IngredientIcon.BEAN_SPROUTS -> R.drawable.bean_sprouts
        IngredientIcon.CHILI -> R.drawable.chili
        // 과일
        IngredientIcon.FRUIT -> R.drawable.fruit
        IngredientIcon.APPLE -> R.drawable.apple
        IngredientIcon.BANANA -> R.drawable.banana
        IngredientIcon.LEMON -> R.drawable.lemon
        IngredientIcon.TANGERINE -> R.drawable.tangerine
        IngredientIcon.GRAPE -> R.drawable.grape
        IngredientIcon.STRAWBERRY -> R.drawable.strawberry
        // 육류
        IngredientIcon.MEAT -> R.drawable.meat
        IngredientIcon.CHICKEN -> R.drawable.chicken
        IngredientIcon.PORK -> R.drawable.pork
        IngredientIcon.BEEF -> R.drawable.beef
        IngredientIcon.DUCK -> R.drawable.duck
        IngredientIcon.LAMB -> R.drawable.lamb
        // 해산물
        IngredientIcon.SEAFOOD -> R.drawable.seafood
        IngredientIcon.FISH -> R.drawable.fish
        IngredientIcon.SHRIMP -> R.drawable.shrimp
        IngredientIcon.SQUID -> R.drawable.squid
        IngredientIcon.CLAM -> R.drawable.clam
        IngredientIcon.CRAB -> R.drawable.crab
        // 유제품/계란
        IngredientIcon.DAIRY -> R.drawable.dairy
        IngredientIcon.EGG -> R.drawable.egg
        IngredientIcon.MILK -> R.drawable.milk
        IngredientIcon.CHEESE -> R.drawable.cheese
        IngredientIcon.YOGURT -> R.drawable.yogurt
        // 곡물
        IngredientIcon.GRAIN -> R.drawable.grain
        IngredientIcon.RICE -> R.drawable.rice
        IngredientIcon.BREAD -> R.drawable.bread
        IngredientIcon.NOODLE -> R.drawable.noodle
        // 소스/양념/조미료
        IngredientIcon.SEASONING -> R.drawable.seasoning
        IngredientIcon.SALT -> R.drawable.salt
        IngredientIcon.SUGAR -> R.drawable.sugar
        IngredientIcon.PEPPER -> R.drawable.pepper
        IngredientIcon.SOY_SAUCE -> R.drawable.soy_sauce
        IngredientIcon.DOENJANG -> R.drawable.doenjang
        IngredientIcon.GOCHUJANG -> R.drawable.gochujang
        IngredientIcon.COOKING_OIL -> R.drawable.cooking_oil
        IngredientIcon.SESAME_OIL -> R.drawable.sesame_oil
        IngredientIcon.VINEGAR -> R.drawable.vinegar
        IngredientIcon.KETCHUP -> R.drawable.ketchup
        IngredientIcon.MAYONNAISE -> R.drawable.mayonnaise
        // 음료
        IngredientIcon.BEVERAGE -> R.drawable.beverage
        IngredientIcon.WATER -> R.drawable.water
        IngredientIcon.SODA -> R.drawable.soda
        IngredientIcon.BEER -> R.drawable.beer
        IngredientIcon.SOJU -> R.drawable.soju
    }
}